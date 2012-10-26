/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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

/**
 * id: A unique identifier
 * grouping: Defines whether this message belongs to a group of messages
 * metaData: Any meta data that can be serialized into a String. Key-value pairs
 * timestamp: Timestamp of creation
 * topic: An optional topic for messaging scenarios
 */
final case class MessageHeader(id: UUID, grouping: Option[MessageGrouping], metaData: Map[String, String],timestamp: DateTime, topic: Option[String])

/** A message with a payload */
final case class Message[+TPayload <: AnyRef](header: MessageHeader, payload: TPayload) {
  override val hashCode = header.id.hashCode
  override def equals(other: Any) = {
  	other match {
  	  case null => false
  	  case m: Message[_] => m.header.id == this.header.id
  	  case _ => false
  	}
  }
}

/** A factory for messages */
object Message {
  def apply[T <: AnyRef](grouping: Option[MessageGrouping], metaData: Map[String,String], payload: T)(implicit id: UUID): Message[T] =
  	new Message[T](MessageHeader(id, grouping, metaData, DateTime.now, None), payload)
  
  def apply[T <: AnyRef](payload: T)(implicit id: UUID): Message[T] =
  	apply(None, Map.empty, payload)(id)

  def createWithUuid[T <: AnyRef](payload: T): Message[T] =
  	apply(None, Map.empty, payload)(UUID.randomUUID)
	
}