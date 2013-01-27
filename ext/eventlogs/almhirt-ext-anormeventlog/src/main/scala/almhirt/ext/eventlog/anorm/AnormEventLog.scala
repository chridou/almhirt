package almhirt.ext.eventlog.anorm

import almhirt.core.Almhirt
import almhirt.common.AlmValidation
import almhirt.eventlog.DomainEventLog
import almhirt.eventlog.impl.DomainEventLogActorHull

object AnormEventLog {
  def serializingJson(implicit theAlmhirt: Almhirt): AlmValidation[DomainEventLog] = {
    theAlmhirt.getConfig.flatMap(config =>
      new SerializingAnormJsonEventLogFactory().createDomainEventLog(theAlmhirt).map(DomainEventLogActorHull(_, config)))
  }
}