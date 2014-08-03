package almhirt.aggregates

import almhirt.common._

sealed trait AggregateRootState[+T <: AggregateRoot]
sealed trait NotAliveState[+T <: AggregateRoot] extends AggregateRootState[T]
sealed trait TouchedTheWorld[+T <: AggregateRoot] extends AggregateRootState[T]
final case class Alive[T <: AggregateRoot](ar: T) extends TouchedTheWorld[T]
case object NeverExisted extends NotAliveState[Nothing]
final case class Dead(id: AggregateRootId, version: AggregateRootVersion) extends NotAliveState[Nothing] with TouchedTheWorld[Nothing]

trait BuildsAggregateRoot[T <: AggregateRoot, E <: AggregateEvent] {
  def apply(agg: T, event: E): TouchedTheWorld[T]
  
  def apply(event: E): T
  
  def applyEvents(agg: T, events: Iterable[E]): AggregateRootState[T] =
    events.foldLeft(Alive(agg): TouchedTheWorld[T]){case (state, next) =>
      state match {
        case Alive(st) => 
          apply(st, next)
        case Dead(id, v) =>
          throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
      }
    }
    
  def rebuildFromHistory(events: Iterable[E]): AggregateRootState[T] =
    if(events.isEmpty) {
      NeverExisted
    } else {
      applyEvents(apply(events.head), events.tail)
    }
}
