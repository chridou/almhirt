package almhirt.common

import java.util.{UUID => JUUID}
import org.joda.time.DateTime

case class MessageHeader(id: JUUID, timestamp: DateTime, metadata: Map[String, String])

object MessageHeader {
  def apply(metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): MessageHeader =
    MessageHeader(ccuad.getUuid, ccuad.getDateTime, metaData)
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): MessageHeader =
    apply(Map.empty)
}

case class Message(header: MessageHeader, payload: AnyRef)

object Message {
  def apply(id: JUUID, timestamp: DateTime, payload: AnyRef, metaData: Map[String, String]): Message =
    Message(MessageHeader(id, timestamp, metaData), payload)
  def apply(payload: AnyRef, metaData: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): Message =
    Message(MessageHeader(metaData), payload)
  def apply(payload: AnyRef)(implicit ccuad: CanCreateUuidsAndDateTimes): Message =
    Message(MessageHeader(), payload)
}