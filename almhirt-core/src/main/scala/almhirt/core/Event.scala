package almhirt.core

import org.joda.time.DateTime
import almhirt.common._

trait EventHeader {
  /** The events unique identifier */
  def id: java.util.UUID
  /** The events timestamp of creation */
  def timestamp: DateTime  
}

final case class BasicEventHeader(id: java.util.UUID, timestamp: DateTime) extends EventHeader

object EventHeader {
  def apply(anId: java.util.UUID, aTimestamp: DateTime): EventHeader = BasicEventHeader(anId, aTimestamp)
}

trait Event {
  def header: EventHeader
}

final case class ProblemEvent(header: EventHeader, problem: Problem) extends Event

object ProblemEvent {
  def apply(problem: Problem)(implicit ccuad: CanCreateUuidsAndDateTimes): ProblemEvent =
    ProblemEvent(EventHeader(ccuad.getUuid, ccuad.getDateTime), problem)
}