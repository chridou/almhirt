package almhirt.ext.core.slick.eventlogs

import language.reflectiveCalls
import scalaz.syntax.validation._
import akka.actor._
import scala.slick.session.{ Database, Session }
import almhirt.common._
import almhirt.syntax.args._
import almhirt.almvalidation.kit._
import almhirt.eventlog.DomainEventLogFactory
import almhirt.core.Almhirt
import almhirt.eventlog.SyncDomainEventStorage
import almhirt.core.HasConfig
import almhirt.environment.configuration.{ ConfigHelper, SystemHelper }
import almhirt.serialization._
import almhirt.ext.core.slick.shared.Profiles
import almhirt.serializing.DomainEventToStringSerializer
import almhirt.eventlog.impl.BlockingDomainEventLogActor
import com.typesafe.config.Config

class SlickDomainEventLogFactory extends DomainEventLogFactory {
  def createDomainEventLog(args: Map[String, Any]): AlmValidation[ActorRef] = {
    args.value[Almhirt]("almhirt").flatMap(theAlmhirt => {
      theAlmhirt.log.info("Starting to create a SLICK domain event log")
      for {
        config <- theAlmhirt.getService[HasConfig].map(_.config)
        eventLogConfig <- ConfigHelper.domainEventLog.getConfig(config)
        actor <- ConfigHelper.getString(eventLogConfig)("storage_mode").flatMap(mode =>
          mode.toLowerCase() match {
            case "text" => createTextDomainEventLog(theAlmhirt, eventLogConfig)
            case "binary" => createBinaryDomainEventLog(theAlmhirt, eventLogConfig)
            case x => UnspecifiedProblem(s"""$x is not a valid storage mode""").failure
          })
      } yield actor
    })
  }

  private def createTextDomainEventLog(theAlmhirt: Almhirt, eventLogConfig: Config): AlmValidation[ActorRef] = {
    for {
      channel <- ConfigHelper.getString(eventLogConfig)("channel")
      connection <- ConfigHelper.getString(eventLogConfig)("connection")
      eventlogtable <- ConfigHelper.getString(eventLogConfig)("eventlog_table")
      createSchema <- ConfigHelper.getBoolean(eventLogConfig)("create_schema")
      dropOnClose <- ConfigHelper.getBoolean(eventLogConfig)("drop_on_close")
      eventLogDataAccess <- ConfigHelper.getString(eventLogConfig)("profile").flatMap(profileName => {
        val props = ConfigHelper.getPropertiesMapFrom(eventLogConfig)("properties")
        val createDataBase: String => Database = (driver => Database.forURL(connection, props))
        Profiles.createTextDomainEventLogAccess(profileName, eventlogtable, createDataBase)(theAlmhirt)
      })
      _ <- computeSafely {
        if (dropOnClose)
          theAlmhirt.actorSystem.registerOnTermination({
            theAlmhirt.log.info("Dropping schema for domain event log")
            eventLogDataAccess.drop
          })
        if (createSchema) {
          theAlmhirt.log.info("Creating schema for domain event log")
          eventLogDataAccess.create
        } else
          ().success
      }
      serializerFactoryName <- ConfigHelper.getString(eventLogConfig)("serializer_factory")
      serializerFactory <- inTryCatch {
        Class.forName(serializerFactoryName)
          .newInstance()
          .asInstanceOf[{ def createSerializer(anAlmhirt: Almhirt): AlmValidation[DomainEventToStringSerializer] }]

      }
      serializer <- computeSafely { serializerFactory.createSerializer(theAlmhirt) }
      actor <- inTryCatch {
        val name = ConfigHelper.domainEventLog.getActorName(eventLogConfig)
        theAlmhirt.log.info(s"DomainEventLog is text based SlickDomainEventLog with name '$name'.")
        val dispatcherName =
          ConfigHelper.getDispatcherNameFromComponentConfig(eventLogConfig).fold(
            fail => {
              theAlmhirt.log.warning("No dispatchername found for DomainEventLog. Using default Dispatcher")
              None
            },
            succ => {
              theAlmhirt.log.info(s"DomainEventLog is using dispatcher '$succ'")
              Some(succ)
            })
        val syncStorage = new SyncTextSlickDomainEventStorage(eventLogDataAccess, serializer.serializingToChannel(channel))
        val props = SystemHelper.addDispatcherByNameToProps(dispatcherName)(Props(new BlockingDomainEventLogActor(syncStorage, theAlmhirt)))
        theAlmhirt.actorSystem.actorOf(props, name)
      }
    } yield actor
  }

  private def createBinaryDomainEventLog(theAlmhirt: Almhirt, eventLogConfig: Config): AlmValidation[ActorRef] = {
    ???
  }

}