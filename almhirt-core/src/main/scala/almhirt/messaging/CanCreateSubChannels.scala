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

import scala.concurrent.duration.FiniteDuration
import almhirt.common.AlmFuture

/** Creates subchannels with a payload type smaller than the current payload type. Types that do not fit will not be published on the subchannel.
 */
trait CanCreateSubChannels[T <: AnyRef] {
  /** Creates a subchannel filtered by type a classifier to further filter the messages
   * 
   * @tparam TPayload The type of the to which the subchannel is restricted
   * @param classifier A predicate to further filter the messages of type TPayload 
   */
  def createSubChannel[TPayload <: T](name: String, classifier: Message[TPayload] => Boolean)(implicit atMost: FiniteDuration, m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]]
  /** Creates a subchannel filtered by type
   * 
   * @tparam TPayload The type of the to which the subchannel is restricted
   */
  def createSubChannel[TPayload <: T](name: String)(implicit atMost: FiniteDuration, m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] = createSubChannel(name, _ => true)
}