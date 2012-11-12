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

/** Broadcasts [[almhirt.messaging.Message]] to its subscribers in a fire and forget manner.
 */
trait CanBroadcastMessages {
  /** Broadcast a message optionally published under a topic
   * 
   * @param message The [[almhirt.messaging.Message]] to broadcast
   * @param topic The optional topic under which the [[almhirt.messaging.Message]] will be published
   */
  def broadcast(message: Message[AnyRef], topic: Option[String]): Unit
  /** Broadcast a [[almhirt.messaging.Message]] not published under any topic
   * 
   * @param message The [[almhirt.messaging.Message]] to broadcast
   */
  def broadcast(message: Message[AnyRef]): Unit = broadcast(message, None)
  /** Broadcast a [[almhirt.messaging.Message]] under a topic
   * 
   * @param message The [[almhirt.messaging.Message]] to broadcast
   * @param topic The  topic under which the [[almhirt.messaging.Message]] will be published
   */
  def broadcast(message: Message[AnyRef], topic: String): Unit = broadcast(message, Some(topic))
}