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

/** Generates a [[java.util.UUID]] */
trait UuidGenerator {
  /** Generate a new [[java.util.UUID]] 
   * @return A new [[java.util.UUID]]
   */
  def generate: UUID
}

/** Generates a new [[java.util.UUID]] by calling [[java.util.UUID]].randomUUID */
class JavaUtilUuidGenerator extends UuidGenerator {
  def generate = UUID.randomUUID
}

/** Generates always the same [[java.util.UUID]]
 * Use for testing
 */
class FixedUuidGenerator(uuid: UUID) extends UuidGenerator{
  /** The predefined [[java.util.UUID]] 
   * @return The predefined [[java.util.UUID]]
   */
  def generate = uuid
}