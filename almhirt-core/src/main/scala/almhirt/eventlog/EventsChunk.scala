package almhirt.eventlog

import almhirt.common._

case class EventsChunk(
  /**
   * Starts with Zero
   */
  index: Int,
  isLast: Boolean,
  events: AlmValidation[Iterable[Event]])