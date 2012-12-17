package almhirt.core

import org.joda.time.DateTime

trait CanCreateUuidsAndDateTimes {
  def getDateTime: DateTime
  def getUuid: java.util.UUID
}