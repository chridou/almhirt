package almhirt

import java.util.UUID

trait UuidGenerator {
  def generate: UUID
}

class JavaUtilUuidGenerator extends UuidGenerator {
  def generate = UUID.randomUUID
}