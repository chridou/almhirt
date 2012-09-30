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
package almhirt

import java.util.UUID

/** An object that when disposed cancels the assigned registration */
trait RegistrationHolder extends Disposable

/** An registration with its ticket that when disposed cancels the assigned registration 
 * 
 * @tparam T The type of the ticket
 */
trait Registration[T] extends RegistrationHolder {
  /** The registration's ticket */
  def ticket: T
}