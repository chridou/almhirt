package almhirt.aggregates

import almhirt.common._

/**
 * Handles the [[AggregateEvents]]s for a specific type of [[AggregateRoot]].
 * The [[AggregateRootEventHandler]] doesn't need to check for correct versions since
 * events notify about something that has already happened and therefore an event can always be applied unless 
 * the events are unordered, have gaps or are received more than once.
 * As a result, it is up to the user not to mess with the timeline of an aggregate root.
 * In case of an exception, you have a serious (consistency, programming) problem.
 * Remember that each event must increase the version of the affected aggregate root by 1.
 * 
 * If you don't trust your components, you can mix in [[VersionCheckingAggregateRootCommandHandler]] to have
 * the versions and ids checked.
 */
trait AggregateRootEventHandler[T <: AggregateRoot, E <: AggregateEvent] {
  /** Implement this method to make the handler complete. */
  def applyEventAntemortem(state: Antemortem[T], event: E): Postnatalis[T]

  final def applyEvent(agg: T, event: E): Postnatalis[T] =
    applyEventAntemortem(Vivus(agg), event)

  final def fromEvent(event: E): Postnatalis[T] =
    applyEventAntemortem(Vacat, event)

  final def applyEvents(agg: T, events: Iterable[E]): Postnatalis[T] =
    events.foldLeft(Vivus(agg): Postnatalis[T]) {
      case (state, next) ⇒
        state match {
          case Vivus(st) ⇒
            applyEvent(st, next)
          case Mortuus(id, v) ⇒
            throw new Exception(s"Aggregate root with id ${id.value} and version ${v.value} is dead. No more events can be applied.")
        }
    }
  final def applyEventPostnatalis(agg: Postnatalis[T], event: E): Postnatalis[T] =
    agg match {
      case Vivus(a) ⇒
        applyEvent(a, event)
      case Mortuus(id, v) ⇒
        throw new Exception(s"Aggregate root with id ${id.value} and version ${v.value} is dead. No more events can be applied.")
    }

  final def applyEventsPostnatalis(agg: Postnatalis[T], events: Iterable[E]): Postnatalis[T] =
    if (events.isEmpty) {
      agg
    } else {
      agg match {
        case Vivus(a) ⇒
          applyEvents(a, events)
        case Mortuus(id, v) ⇒
          throw new Exception(s"Aggregate root with id ${id.value} and version ${v.value} is dead. No more events can be applied.")
      }
    }

  final def applyEventLifecycleAgnostic(agg: AggregateRootLifecycle[T], event: E): Postnatalis[T] = {
    agg match {
      case Vacat ⇒
        fromEvent(event)
      case Vivus(a) ⇒
        applyEvent(a, event)
      case Mortuus(id, v) ⇒
        throw new Exception(s"Aggregate root with id ${id.value} and version ${v.value} is dead. No more events can be applied.")
    }
  }

}

