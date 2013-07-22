package almhirt.common

import java.util.{UUID => JUUID}
import org.joda.time.LocalDateTime

case class MessageHeader(id: JUUID, timestamp: LocalDateTime, metadata: Map[String, String])

object MessageHeader {
  def apply(metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): MessageHeader =
    MessageHeader(ccuad.getUuid, ccuad.getUtcTimestamp, metadata)
  def apply()(implicit ccuad: CanCreateUuidsAndDateTimes): MessageHeader =
    apply(Map.empty)
}

case class Message(header: MessageHeader, payload: AnyRef)

object Message {
  def apply(id: JUUID, timestamp: LocalDateTime, payload: AnyRef, metadata: Map[String, String]): Message =
    Message(MessageHeader(id, timestamp, metadata), payload)
  def apply(payload: AnyRef, metadata: Map[String, String])(implicit ccuad: CanCreateUuidsAndDateTimes): Message =
    Message(MessageHeader(metadata), payload)
  def apply(payload: AnyRef)(implicit ccuad: CanCreateUuidsAndDateTimes): Message =
    Message(MessageHeader(), payload)
}