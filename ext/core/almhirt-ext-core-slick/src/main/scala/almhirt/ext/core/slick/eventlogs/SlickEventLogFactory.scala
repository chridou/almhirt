package almhirt.ext.core.slick.eventlogs

import language.reflectiveCalls
import scalaz.syntax.validation._
import akka.actor._
import scala.slick.session.{ Database, Session }
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.eventlog.EventLogFactory
import almhirt.core._
import almhirt.eventlog.util.SyncEventStorage
import almhirt.core.HasConfig
import almhirt.environment.configuration.{ ConfigHelper, SystemHelper }
import almhirt.serialization.BlobStorageWithUuidBlobId
import almhirt.serializing.EventToStringSerializer
import almhirt.eventlog.util.BlockingEventLogActor
import com.typesafe.config.Config

class SlickEventLogFactory extends EventLogFactory {
  def createEventLog(theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    theAlmhirt.log.info("Starting to create a SLICK event log")
    for {
      config <- theAlmhirt.getService[HasConfig].map(_.config)
      eventLogConfig <- ConfigHelper.eventLog.getConfig(config)
      actor <- ConfigHelper.getString(eventLogConfig)("storage_mode").flatMap(mode =>
        mode.toLowerCase() match {
          case "text" => createTextEventLog(theAlmhirt, eventLogConfig)
          case "binary" => createBinaryEventLog(theAlmhirt, eventLogConfig)
          case x => UnspecifiedProblem(s"""$x is not a valid storage mode""").failure
        })
    } yield actor
  }

  private def createTextEventLog(theAlmhirt: Almhirt, eventLogConfig: Config): AlmValidation[ActorRef] = {
    for {
      channel <- ConfigHelper.getString(eventLogConfig)("channel")
      connection <- ConfigHelper.getString(eventLogConfig)("connection")
      eventlogtable <- ConfigHelper.getString(eventLogConfig)("eventlog_table")
      blobtable <- ConfigHelper.getString(eventLogConfig)("blob_table")
      createSchema <- ConfigHelper.getBoolean(eventLogConfig)("create_schema")
      dropOnClose <- ConfigHelper.getBoolean(eventLogConfig)("drop_on_close")
      eventLogDataAccess <- ConfigHelper.getString(eventLogConfig)("profile").flatMap(profileName => {
        val props = ConfigHelper.getPropertiesMapFrom(eventLogConfig)("properties")
        val createDataBase: String => Database = (driver => Database.forURL(connection, props))
        Profiles.createTextEventLogAccess(profileName, eventlogtable, blobtable, createDataBase)(theAlmhirt)
      })
      _ <- computeSafely {
        if (dropOnClose)
          theAlmhirt.actorSystem.registerOnTermination({
            theAlmhirt.log.info("Dropping schema for event log")
            eventLogDataAccess.drop
          })
        if (createSchema) {
          theAlmhirt.log.info("Creating schema for event log")
          eventLogDataAccess.create
        } else
          ().success
      }
      serializerFactoryName <- ConfigHelper.getString(eventLogConfig)("serializer_factory")
      serializerFactory <- inTryCatch {
        Class.forName(serializerFactoryName)
          .newInstance()
          .asInstanceOf[{ def createSerializer(storeBlobsHere: Option[(BlobStorageWithUuidBlobId, Int)], anAlmhirt: Almhirt): AlmValidation[EventToStringSerializer] }]

      }
      serializer <- computeSafely {
        val blobSettings =
          if (ConfigHelper.isBooleanSet(eventLogConfig)("with_blobs_stored_separately"))
            Some((eventLogDataAccess, ConfigHelper.getIntOrDefault(32000)(eventLogConfig)("min_blob_size_for_separation")))
          else
            None
        serializerFactory.createSerializer(blobSettings, theAlmhirt)
      }
      actor <- inTryCatch {
        val name = ConfigHelper.domainEventLog.getActorName(eventLogConfig)
        theAlmhirt.log.info(s"EventLog is text based SlickEventLog with name '$name'.")
        val dispatcherName =
          ConfigHelper.getDispatcherNameFromComponentConfig(eventLogConfig).fold(
            fail => {
              theAlmhirt.log.warning("No dispatchername found for EventLog. Using default Dispatcher")
              None
            },
            succ => {
              theAlmhirt.log.info(s"DomainEventLog is using dispatcher '$succ'")
              Some(succ)
            })
        val syncStorage = new SyncTextSlickEventStorage(eventLogDataAccess, serializer.serializeToChannel(channel))
        val props = SystemHelper.addDispatcherByNameToProps(dispatcherName)(Props(new BlockingEventLogActor(syncStorage, theAlmhirt)))
        theAlmhirt.actorSystem.actorOf(props, name)
      }
    } yield actor
  }

  private def createBinaryEventLog(theAlmhirt: Almhirt, eventLogConfig: Config): AlmValidation[ActorRef] = {
    ???
  }

}