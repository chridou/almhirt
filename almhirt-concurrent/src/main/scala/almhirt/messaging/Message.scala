package almhirt.messaging

import java.util.UUID
import org.joda.time.DateTime

/** Multiple [[almhirt.messaging.Message]]s that share [[almhirt.messaging.MessageGrouping]]s with the same groupId belong to one group
 * 
 * Equality is defined by the id, since no two messages may ever have the same id.
 * 
 * @constructor Creates a new instance with a  group identifier, the position of this member in the group and a flag, whether this is the groups last message
 */ 
final case class MessageGrouping(groupId: UUID, seq: Int, isLast: Boolean)

/** A message with a payload */
class Message[+TPayload <: AnyRef](
  /** A unique identifier */
  val id: UUID,
  /** Defines whether this message belongs to a group of messages */
  val grouping: Option[MessageGrouping],
  /** Any meta data that can be serialized into a String. Key-value pairs */
  val metaData: Map[String, String],
  /** Timestamp of creation */
  val timestamp: DateTime,
  /** An optional topic for messaging scenarios */
  val topic: Option[String],
  /** The payload */
  val payload: TPayload) {
  override val hashCode = id.hashCode
  override def equals(other: Any) = {
  	other match {
  	  case null => false
  	  case m: Message[_] => m.id == this.id
  	  case _ => false
  	}
  }
}

/** A factory for messages */
object Message {
  def apply[T <: AnyRef](grouping: Option[MessageGrouping], metaData: Map[String,String], payload: T)(implicit id: UUID): Message[T] =
  	new Message[T](id, grouping, metaData, DateTime.now, None, payload)
  
  def apply[T <: AnyRef](payload: T)(implicit id: UUID): Message[T] =
  	apply(None, Map.empty, payload)(id)

  def createWithUuid[T <: AnyRef](payload: T): Message[T] =
  	apply(None, Map.empty, payload)(UUID.randomUUID)
	
}