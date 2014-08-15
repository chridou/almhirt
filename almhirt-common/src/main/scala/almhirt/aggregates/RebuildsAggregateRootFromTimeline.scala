package almhirt.aggregates

import almhirt.common._

trait RebuildsAggregateRootFromTimeline[T <: AggregateRoot, E <: AggregateEvent] { self: AggregateRootEventHandler[T, E] =>
  final def rebuildFromTimeline(timeline: Iterable[E]): AggregateRootState[T] =
    if (timeline.isEmpty) {
      NeverExisted
    } else {
      applyEventsPostnatalis(fromEvent(timeline.head), timeline.tail)
    }
}
