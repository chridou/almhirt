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