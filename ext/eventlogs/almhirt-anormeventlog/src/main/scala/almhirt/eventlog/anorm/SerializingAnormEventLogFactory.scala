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
    val tableName = ConfigHelper.tryGetString(ctx.config)(ConfigPaths.eventlog + ".eventlogtable").getOrElse("eventlog")
    val actorName = ConfigHelper.tryGetString(ctx.config)(ConfigPaths.eventlog + ".actorname").getOrElse("domaineventlog")
    ConfigHelper.getString(ctx.config)(ConfigPaths.eventlog + ".connection").bind(connection =>
      ConfigHelper.getString(ctx.config)(ConfigPaths.eventlog + ".driver").bind(drivername =>
        almhirt.almvalidation.funs.inTryCatch({ Class.forName(drivername); () }).map { _ =>
          val settings = AnormSettings(connection, new Properties(), tableName)
          val props =
            SystemHelper.addDispatcherToProps(ctx.config)(ConfigPaths.eventlog, Props(new SerializingAnormEventLogActor(settings)(ctx)))
          val actor = ctx.system.actorSystem.actorOf(props, actorName)
          new DomainEventLogActorHull(actor)(ctx)
        }))
  }
}