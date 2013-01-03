package almhirt.ext.eventlog.anorm

import java.util.Properties
import scalaz.syntax.validation._
import scalaz.std._
import akka.actor._
import almhirt.common._
import almhirt.environment._
import almhirt.eventlog._
import almhirt.environment.configuration._
import scala.io.Source
import riftwarp.RiftWarp
import com.typesafe.config._

class SerializingAnormJsonEventLogFactory extends DomainEventLogFactory {
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

  private def createEventLog(settings: AnormSettings, actorName: String, dropOnClose: Boolean, riftWarp: RiftWarp, theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    ConfigHelper.eventLog.getConfig(theAlmhirt.system.config).map { eventLogConfig =>
      val props =
        SystemHelper.addDispatcherToProps(eventLogConfig)(Props(new SerializingAnormJsonEventLogActor(settings)(riftWarp, theAlmhirt)))
      val actor = theAlmhirt.system.actorSystem.actorOf(props, actorName)
      if (dropOnClose) {
        def dropTable() = {
          DbUtil.inTransactionWithConnection(() => DbUtil.getConnection(settings.connection, settings.props)) { conn =>
            val statement = conn.createStatement()
            statement.executeUpdate("DROP TABLE %s".format(settings.logTableName))
            Unit.success
          }
          ()
        }
        theAlmhirt.system.actorSystem.registerOnTermination(dropOnClose)
      }
      actor
    }
  }

  def createDomainEventLog(theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    ConfigHelper.eventLog.getConfig(theAlmhirt.system.config).flatMap { eventLogConfig =>
      val createSchemaFun: (AnormSettings, DbTemplate) => AlmValidation[AnormSettings] =
        ConfigHelper
          .getBoolean(eventLogConfig)("create_schema")
          .fold(
            f => (settings, template) => settings.success,
            s => {
              if (s) {
                (settings, template) =>
                  option.cata(template.ddlScriptName)(createSchema(settings, _), UnspecifiedProblem("Schema path not specified. Cannot create a schema.").failure)
              } else
                (settings, template) => settings.success

            })

      val dropOnClose = ConfigHelper.isBooleanSet(eventLogConfig)("drop_on_close")

      val tableName = {
        val baseName = ConfigHelper.tryGetString(eventLogConfig)("eventlogtable").getOrElse("eventlog")
        if (ConfigHelper.isBooleanSet(eventLogConfig)("randomize_tablename"))
          "%s_%s".format(baseName, _root_.scala.util.Random.nextInt.toString.replaceAll("-", "N"))
        else
          baseName
      }

      val dbTemplate = {
        DbTemplate.tryGetTemplate(eventLogConfig) match {
          case Some(template) =>
            template.success
          case None =>
            ConfigHelper.getString(eventLogConfig)("driver").map { driverName =>
              val ddlPath = ConfigHelper.tryGetString(eventLogConfig)("ddlpath")
              DbTemplate(driverName, ddlPath)
            }
        }
      }

      import collection.JavaConversions._

      val actorName = ConfigHelper.eventLog.getActorName(eventLogConfig)
      ConfigHelper.getString(eventLogConfig)("connection").flatMap(connection =>
        theAlmhirt.getService[RiftWarp].flatMap(riftWarp =>
          dbTemplate.flatMap(dbTemplate =>
            almhirt.almvalidation.funs.inTryCatch({ Class.forName(dbTemplate.driverName); () }).flatMap { _ =>
              val props =
                ConfigHelper.tryGetSubConfig(eventLogConfig)("properties") match {
                  case Some(config) =>
                    config.entrySet()
                      .map(x => (x.getKey(), x.getValue().unwrapped().toString()))
                      .foldLeft(new Properties)((acc, x) => { acc.setProperty(x._1, x._2); acc })
                  case None => new Properties()
                }
              val settings =
                AnormSettings(connection, props, tableName)
              createSchemaFun(settings, dbTemplate).flatMap(settings => createEventLog(settings, actorName, dropOnClose, riftWarp, theAlmhirt))
            })))
    }
  }
}