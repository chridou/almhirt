package almhirt.aggregates

import almhirt.common._

/** Mix in this trait to rebuild aggregate roots from its lifetime.
 *  It uses the [[AggregateRootEventHandler]] to rebuild the aggregate root.
 */
trait RebuildsAggregateRootFromTimeline[T <: AggregateRoot, E <: AggregateEvent] { self: AggregateRootEventHandler[T, E] =>
  final def rebuildFromTimeline(timeline: Iterable[E]): AggregateRootLifecycle[T] =
    if (timeline.isEmpty) {
      Vacat
    } else {
      applyEventsPostnatalis(fromEvent(timeline.head), timeline.tail)
    }
}
