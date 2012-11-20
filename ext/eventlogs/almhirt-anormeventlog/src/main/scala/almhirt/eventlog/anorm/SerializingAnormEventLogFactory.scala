package almhirt.eventlog.anorm

import java.util.Properties
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.environment._
import almhirt.eventlog._
import almhirt.environment.configuration._
import almhirt.eventlog.impl.DomainEventLogActorHull

class SerializingAnormEventLogFactory extends DomainEventLogFactory {
  def createDomainEventLog(ctx: AlmhirtContext): AlmValidation[DomainEventLog] = {
    val settings = AnormSettings("", new Properties() , "")
    val props =
      SystemHelper.addDispatcherToProps(ctx.config)(ConfigPaths.eventlog, Props(new SerializingAnormEventLogActor(settings)(ctx)))
    val actor = ctx.system.actorSystem.actorOf(props, "domainEventLog")
    new DomainEventLogActorHull(actor)(ctx).success
  }
}