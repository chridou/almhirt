package almhirt.aggregates

import almhirt.common._

trait RebuildsAggregateRootFromTimeline[T <: AggregateRoot, E <: AggregateEvent] { self: AggregateRootEventHandler[T, E] =>
  final def rebuildFromTimeline(timeline: Iterable[E]): AggregateRootLifecycle[T] =
    if (timeline.isEmpty) {
      Vacat
    } else {
      applyEventsPostnatalis(fromEvent(timeline.head), timeline.tail)
    }
}
