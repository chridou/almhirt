package almhirt.domain

import java.util.UUID
import org.joda.time.DateTime

trait DomainEvent {
  def aggRootId: UUID 
  def aggRootVersion: Long
  def timestamp: DateTime
}

trait CreatingNewAggregateRootEvent {
  val aggRootVersion = 1L
}

