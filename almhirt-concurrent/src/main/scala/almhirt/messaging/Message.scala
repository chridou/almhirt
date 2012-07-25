package almhirt.messaging

import java.util.UUID
import org.joda.time.DateTime

final case class MesssgeGrouping(groupId: UUID, seq: Int, isLast: Boolean)

class Message[+TPayload <: AnyRef] (
  val id: UUID,
  val grouping: Option[MesssgeGrouping],
  val metaData: Map[String, String],
  val timestamp: DateTime,
  val topic: Option[String],
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

object Message {
  def apply[T <: AnyRef](grouping: Option[MesssgeGrouping], metaData: Map[String,String], payload: T)(implicit id: UUID): Message[T] =
  	new Message[T](id, grouping, metaData, DateTime.now, None, payload)
  
  def apply[T <: AnyRef](payload: T)(implicit id: UUID): Message[T] =
  	apply(None, Map.empty, payload)(id)

 def createWithUuid[T <: AnyRef](payload: T): Message[T] =
  	apply(None, Map.empty, payload)(UUID.randomUUID)
	
}