package almhirt.core

import org.joda.time.DateTime

trait CanCreateUuid{ def getUuid: java.util.UUID }

trait CanCreateDateTime{ def getDateTime: DateTime }

trait CanCreateUuidsAndDateTimes extends CanCreateUuid with CanCreateDateTime