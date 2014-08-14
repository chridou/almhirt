package almhirt.aggregates

import almhirt.common._

trait BuildsAggregateRoot[T <: AggregateRoot, E <: AggregateEvent] {
  def applyEventAntemortem(state: Antemortem[T], event: E): Postnatalis[T]

  final def applyEvent(agg: T, event: E): Postnatalis[T] =
    applyEventAntemortem(Alive(agg), event)

  final def fromEvent(event: E): Postnatalis[T] =
    applyEventAntemortem(NeverExisted, event)

  final def applyEvents(agg: T, events: Iterable[E]): Postnatalis[T] =
    events.foldLeft(Alive(agg): Postnatalis[T]) {
      case (state, next) ⇒
        state match {
          case Alive(st) ⇒
            applyEvent(st, next)
          case Dead(id, v) ⇒
            throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
        }
    }

  final def rebuildFromTimeline(events: Iterable[E]): AggregateRootState[T] =
    if (events.isEmpty) {
      NeverExisted
    } else {
      applyEventsPostnatalis(fromEvent(events.head), events.tail)
    }

  final def applyEventPostnatalis(agg: Postnatalis[T], event: E): Postnatalis[T] =
    agg match {
      case Alive(a) ⇒ applyEvent(a, event)
      case Dead(id, v) ⇒ throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
    }

  final def applyEventsPostnatalis(agg: Postnatalis[T], events: Iterable[E]): Postnatalis[T] =
    if (events.isEmpty) {
      agg
    } else {
      agg match {
        case Alive(a) ⇒ applyEvents(a, events)
        case Dead(id, v) ⇒ throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
      }
    }

  final def applyEventLifecycleAgnostic(agg: AggregateRootState[T], event: E): Postnatalis[T] = {
    agg match {
      case Alive(a) ⇒ applyEvent(a, event)
      case NeverExisted ⇒ fromEvent(event)
      case Dead(id, v) ⇒ throw new Exception(s"Aggregate root with id $id and version $v is dead. No more events can be applied.")
    }
  }

}

