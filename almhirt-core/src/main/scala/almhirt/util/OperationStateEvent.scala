package almhirt.util

import almhirt.core.{Event, EventHeader }
import org.joda.time.DateTime
import almhirt.common.CanCreateUuidsAndDateTimes

final case class OperationStateEvent(header: EventHeader, operationState: OperationState) extends Event

object OperationStateEvent{
  def apply(operationState: OperationState, sender: Option[String])(implicit ccuad: CanCreateUuidsAndDateTimes): OperationStateEvent =
    OperationStateEvent(EventHeader(ccuad.getUuid, ccuad.getDateTime, sender), operationState)
}