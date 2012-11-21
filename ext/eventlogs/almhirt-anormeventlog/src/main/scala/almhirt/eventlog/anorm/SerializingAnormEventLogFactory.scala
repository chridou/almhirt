package almhirt.eventlog.anorm

import java.util.Properties
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.environment._
import almhirt.eventlog._
import almhirt.environment.configuration._
import almhirt.eventlog.impl.DomainEventLogActorHull
import scala.io.Source

class SerializingAnormEventLogFactory extends DomainEventLogFactory {
  private def createSchema(settings: AnormSettings, pathToSchema: String): AlmValidation[AnormSettings] = {
    val source = Source.fromURL(getClass.getResource(pathToSchema))
    val ddlSql = source.mkString.replaceAll("%tblname%", settings.logTableName)
    DbUtil.inTransactionWithConnection(() => DbUtil.getConnection(settings.connection, settings.props)) { conn =>
      val statement = conn.createStatement()
      statement.executeUpdate(ddlSql)
      settings.success
    }
  }

  private def createEventLog(settings: AnormSettings, actorName: String, dropOnClose: Boolean, ctx: AlmhirtContext): DomainEventLog = {
    val props =
      SystemHelper.addDispatcherToProps(ctx.config)(ConfigPaths.eventlog, Props(new SerializingAnormEventLogActor(settings)(ctx)))
    val actor = ctx.system.actorSystem.actorOf(props, actorName)
    if (dropOnClose) {
      def dropTable() =
        DbUtil.inTransactionWithConnection(() => DbUtil.getConnection(settings.connection, settings.props)) { conn =>
          val statement = conn.createStatement()
          statement.executeUpdate("DROP TABLE %s".format(settings.logTableName))
          Unit.success
        }
      new DomainEventLogActorHull(actor, dropTable)(ctx)
    } else {
      new DomainEventLogActorHull(actor)(ctx)
    }
  }

  def createDomainEventLog(ctx: AlmhirtContext): AlmValidation[DomainEventLog] = {
    val createSchemaFun: AnormSettings => AlmValidation[AnormSettings] =
      ConfigHelper
        .getBoolean(ctx.config)(ConfigPaths.eventlog + ".create_schema")
        .fold(
          f => x => x.success,
          s => {
            if (s) {
              val path = ConfigHelper.tryGetString(ctx.config)(ConfigPaths.eventlog + ".ddlpath").getOrElse("/conf/ddl.sql")
              x => createSchema(x, path)
            } else
              x => x.success

          })

    val dropOnClose = ConfigHelper.isBooleanSet(ctx.config)(ConfigPaths.eventlog + ".drop_on_close")
    val tableName = {
      val baseName = ConfigHelper.tryGetString(ctx.config)(ConfigPaths.eventlog + ".eventlogtable").getOrElse("eventlog")
      if (ConfigHelper.isBooleanSet(ctx.config)(ConfigPaths.eventlog + ".randomize_tablename"))
        "%s_%s".format(baseName, util.Random.nextInt.toString.replaceAll("-", "N"))
      else
        baseName
    }

    val actorName = ConfigHelper.tryGetString(ctx.config)(ConfigPaths.eventlog + ".actorname").getOrElse("domaineventlog")
    ConfigHelper.getString(ctx.config)(ConfigPaths.eventlog + ".connection").bind(connection =>
      ConfigHelper.getString(ctx.config)(ConfigPaths.eventlog + ".driver").bind(drivername =>
        almhirt.almvalidation.funs.inTryCatch({ Class.forName(drivername); () }).bind { _ =>
          val settings = AnormSettings(connection, new Properties(), tableName)
          createSchemaFun(settings).map(settings => createEventLog(settings, actorName, dropOnClose, ctx))
        }))
  }
}