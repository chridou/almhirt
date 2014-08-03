package almhirt.aggregates

import almhirt.common._

sealed trait AggregateRootState[+T <: AggregateRoot]
sealed trait NotAliveState[+T <: AggregateRoot] extends AggregateRootState[T]
sealed trait TouchedTheWorld[+T <: AggregateRoot] extends AggregateRootState[T]
case object NeverExisted extends NotAliveState[Nothing]
final case class Alive[T <: AggregateRoot](ar: T) extends TouchedTheWorld[T]
final case class Dead(id: AggregateRootId, version: AggregateRootVersion) extends NotAliveState[Nothing] with TouchedTheWorld[Nothing]

trait BuildsAggregateRoot[T <: AggregateRoot, E <: AggregateEvent] {
  def applyEvent(agg: T, event: E): TouchedTheWorld[T]
  
  def fromEvent(event: E): T
  
  def applyEvents(agg: T, events: Iterable[E]): AggregateRootState[T] =
    events.foldLeft(Alive(agg): TouchedTheWorld[T]){case (state, next) =>
      state match {
        case Alive(st) => 
          applyEvent(st, next)
        case Dead(id, v) =>
          throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
      }
    }
    
  def rebuildFromHistory(events: Iterable[E]): AggregateRootState[T] =
    if(events.isEmpty) {
      NeverExisted
    } else {
      applyEvents(fromEvent(events.head), events.tail)
    }
}

trait BuildsAggregetaRootFromEventHandlers[T <: AggregateRoot, E <: AggregateEvent] extends BuildsAggregateRoot[T, E] {
  override def applyEvent(agg: T, event: E) = 
    if(event.aggId != agg.id) {
      throw new Exception(s"Aggregate root ids do not match: ${agg.id} does not equal ${event.aggId}.")
    } else if(event.aggVersion != agg.version) {
      throw new Exception(s"Aggregate root versions do not match: ${agg.version} does not equal ${event.aggVersion}.")
    } else {
      mutateHandler(agg, event)
    }

  override def fromEvent(event: E): T =
    if(event.aggVersion != AggregateRootVersion(0L)) {
      throw new Exception(s"Event version has to be 0 but is ${event.aggVersion}.")
    } else {
      createHandler(event)
    }
    
  
  /** The implementation must increase the version!
   */
  def createHandler: PartialFunction[E, T]
  
  /** The implementation must increase the version!
   */
  def mutateHandler: (T, E) => TouchedTheWorld[T]
}
