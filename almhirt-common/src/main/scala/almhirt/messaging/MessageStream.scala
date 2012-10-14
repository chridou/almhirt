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
import almhirt._

/** Publishes messages to its subscribers. 
 * This is the weakest contract a channel must fulfill:
 * A Channel does
 * * guarantee that all message received by handlers have been published by someone
 * * not guarantee that messages arrive in the same order as they were published  
 * * not guarantee that all messages will be published
 * * not guarantee that all handlers will be called on the same thread
 * * not guarantee that handlers won't be called concurrently
 */
trait MessageStream extends SubscribableForMessages with almhirt.MightBeRegisteredSomewhere with almhirt.Disposable {
  /** The message stream might be limited to some topic
   */
  def topicPattern: Option[String]
  
  /** Ceases activity of this message stream and unregisteres it if it was registered somewhere
   */
  def dispose() =
    registration.foreach(_.dispose())
}

