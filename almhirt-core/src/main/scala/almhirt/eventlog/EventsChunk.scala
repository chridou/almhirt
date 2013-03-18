package almhirt.eventlog

import almhirt.common.AlmValidation
import almhirt.core.Event

case class EventsChunk(
  /**
   * Starts with Zero
   */
  index: Int,
  isLast: Boolean,
  events: AlmValidation[Iterable[Event]])