package almhirt.ext.eventlog.anorm

import almhirt.environment.Almhirt
import almhirt.common.AlmValidation
import almhirt.eventlog.DomainEventLog
import almhirt.eventlog.impl.DomainEventLogActorHull

object AnarmEventLog {
  def serializingJson(implicit theAlmhirt: Almhirt): AlmValidation[DomainEventLog] = {
    new SerializingAnormJsonEventLogFactory().createDomainEventLog(theAlmhirt).map(DomainEventLogActorHull(_))
  }
}