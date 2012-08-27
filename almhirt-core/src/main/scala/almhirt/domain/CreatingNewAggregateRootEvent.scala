package almhirt.domain

/** Marks an event that creates a new aggregate root */
trait CreatingNewAggregateRootEvent {
  val aggRootVersion = 1L
}