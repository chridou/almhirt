package almhirt.domain

import java.util.UUID
import org.joda.time.DateTime

/** These events can create or mutate an aggregate root in time */
trait DomainEvent {
  /** The affected aggregate root */
  def aggRootId: UUID 
  /** The version of the aggregate root __before__ applying the event */
  def aggRootVersion: Long
}

