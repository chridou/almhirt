package almhirt.domain

import java.util.UUID
import org.joda.time.DateTime

trait EntityEvent {
  def entityId: UUID 
  def entityVersion: Long
  def timestamp: DateTime
}

trait CreatingNewEntityEvent {
  val entityVersion = 1L
}

