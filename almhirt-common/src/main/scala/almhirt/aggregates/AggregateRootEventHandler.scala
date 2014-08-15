package almhirt.aggregates

import almhirt.common._

/**
 * Handles the [[AggregateEvents]]s for a specific type of [[AggregateRoot]].
 * The [[AggregateRootEventHandler]] doesn't need to check for correct versions since
 * events notify about something that has already happened and therefore an event can always be applied.
 * As a result, it is up to the user not to mess with the timeline of an aggregate root.
 * In case of an exception, you have a serious (consistency, programming) problem.
 * Remember that each event must increase the version of the affected aggregate root by 1.
 */
trait AggregateRootEventHandler[T <: AggregateRoot, E <: AggregateEvent] {
  /** Implement this method to make the handler complete. */
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

