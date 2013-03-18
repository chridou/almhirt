package almhirt.eventlog

import almhirt.common.AlmValidation
import almhirt.domain.DomainEvent

case class DomainEventsChunk(
  /**
   * Starts with Zero
   */
  index: Int,
  isLast: Boolean,
  events: AlmValidation[Iterable[DomainEvent]])