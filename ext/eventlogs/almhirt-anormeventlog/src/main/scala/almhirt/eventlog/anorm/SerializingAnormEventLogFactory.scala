package almhirt.eventlog.anorm

import java.util.Properties
import scalaz.syntax.validation._
import scalaz.std._
import akka.actor._
import almhirt.common._
import almhirt.environment._
import almhirt.eventlog._
import almhirt.environment.configuration._
import almhirt.eventlog.impl.DomainEventLogActorHull
import scala.io.Source
import com.typesafe.config._

class SerializingAnormEventLogFactory extends DomainEventLogFactory {
  private def createSchema(settings: AnormSettings, pathToSchema: String): AlmValidation[AnormSettings] = {
    val resource = getClass.getResource(pathToSchema)
    if (resource != null) {
      val source = Source.fromURL(resource)
      val ddlSql = source.mkString.replaceAll("%tblname%", settings.logTableName)
      DbUtil.inTransactionWithConnection(() => DbUtil.getConnection(settings.connection, settings.props)) { conn =>
        val statement = conn.createStatement()
        statement.execute(ddlSql)
        settings.success
      }
    } else {
      UnspecifiedProblem("Could not find the schema ddl file in resources at '%s'".format(pathToSchema)).failure
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
    val createSchemaFun: (AnormSettings, DbTemplate) => AlmValidation[AnormSettings] =
      ConfigHelper
        .getBoolean(ctx.config)(ConfigPaths.eventlog + ".create_schema")
        .fold(
          f => (settings, template) => settings.success,
          s => {
            if (s) {
              (settings, template) =>
                option.cata(template.ddlScriptName)(createSchema(settings, _), UnspecifiedProblem("Schema path not specified. Cannot create a schema.").failure)
            } else
              (settings, template) => settings.success

          })

    val dropOnClose = ConfigHelper.isBooleanSet(ctx.config)(ConfigPaths.eventlog + ".drop_on_close")

    val tableName = {
      val baseName = ConfigHelper.tryGetString(ctx.config)(ConfigPaths.eventlog + ".eventlogtable").getOrElse("eventlog")
      if (ConfigHelper.isBooleanSet(ctx.config)(ConfigPaths.eventlog + ".randomize_tablename"))
        "%s_%s".format(baseName, _root_.scala.util.Random.nextInt.toString.replaceAll("-", "N"))
      else
        baseName
    }

    val dbTemplate = {
      DbTemplate.tryGetTemplate(ctx.config) match {
        case Some(template) =>
          template.success
        case None =>
          ConfigHelper.getString(ctx.config)(ConfigPaths.eventlog + ".driver").map { driverName =>
            val ddlPath = ConfigHelper.tryGetString(ctx.config)(ConfigPaths.eventlog + ".ddlpath")
            DbTemplate(driverName, ddlPath)
          }
      }
    }

    import collection.JavaConversions._

    val actorName = ConfigHelper.tryGetString(ctx.config)(ConfigPaths.eventlog + ".actorname").getOrElse("domaineventlog")
    ConfigHelper.getString(ctx.config)(ConfigPaths.eventlog + ".connection").bind(connection =>
      dbTemplate.bind(dbTemplate =>
        almhirt.almvalidation.funs.inTryCatch({ Class.forName(dbTemplate.driverName); () }).bind { _ =>
          val props =
            ConfigHelper.tryGetSubConfig(ctx.config)(ConfigPaths.eventlog + ".properties") match {
              case Some(config) =>
                config.entrySet()
                  .map(x => (x.getKey(), x.getValue().unwrapped().toString()))
                  .foldLeft(new Properties)((acc, x) => { acc.setProperty(x._1, x._2); acc })
              case None => new Properties()
            }
          val settings =
            AnormSettings(connection, props, tableName)
          createSchemaFun(settings, dbTemplate).map(settings => createEventLog(settings, actorName, dropOnClose, ctx))
        }))
  }
}