package almhirt.ext.core.slick.snapshots

import language.reflectiveCalls
import scala.slick.session._
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.core._
import almhirt.domain.components._
import almhirt.environment.configuration._
import almhirt.serializing._
import almhirt.ext.core.slick.shared.Profiles
import com.typesafe.config.Config

class SlickSnapshotStorageFactory extends SnapshotStorageFactory {
  import almhirt.almvalidation.kit._
  override def createActorRefComponent(args: Map[String, Any]): AlmValidation[ActorRef] =
    (args.lift >! "almhirt").flatMap(_.castTo[Almhirt].flatMap(theAlmhirt => createSnapshotStorage(theAlmhirt)))

  override def createSnapshotStorage(theAlmhirt: Almhirt): AlmValidation[ActorRef] = {
    theAlmhirt.log.info("Starting to create a SLICK snapshot storage log")
    for {
      config <- theAlmhirt.getService[HasConfig].map(_.config)
      configSection <- ConfigHelper.snapshotStorage.getConfig(config)
      actor <- ConfigHelper.getString(configSection)("storage_mode").flatMap(mode =>
        mode.toLowerCase() match {
          case "text" => createTextDomainEventLog(theAlmhirt, configSection)
          case "binary" => createBinaryDomainEventLog(theAlmhirt, configSection)
          case x => UnspecifiedProblem(s"""$x is not a valid storage mode""").failure
        })
    } yield actor
  }

  private def createTextDomainEventLog(theAlmhirt: Almhirt, configSection: Config): AlmValidation[ActorRef] = {
    for {
      channel <- ConfigHelper.getString(configSection)("channel")
      connection <- ConfigHelper.getString(configSection)("connection")
      snapshotstable <- ConfigHelper.getString(configSection)("snapshots_table")
      createSchema <- ConfigHelper.getBoolean(configSection)("create_schema")
      dropOnClose <- ConfigHelper.getBoolean(configSection)("drop_on_close")
      snapshotsDataAccess <- ConfigHelper.getString(configSection)("profile").flatMap(profileName => {
        val props = ConfigHelper.getPropertiesMapFrom(configSection)("properties")
        val createDataBase: String => Database = (driver => Database.forURL(connection, props))
        Profiles.createTextSnapshotsAccess(profileName, snapshotstable, createDataBase)(theAlmhirt)
      })
      _ <- computeSafely {
        if (dropOnClose)
          theAlmhirt.log.info("Dropping schema for snapshots")
        theAlmhirt.actorSystem.registerOnTermination { snapshotsDataAccess.drop }
        if (createSchema) {
          theAlmhirt.log.info("Creating schema for snapshots")
          snapshotsDataAccess.create
        } else
          ().success
      }
      serializerFactoryName <- ConfigHelper.getString(configSection)("serializer_factory")
      serializerFactory <- inTryCatch {
        Class.forName(serializerFactoryName)
          .newInstance()
          .asInstanceOf[{ def createSerializer(anAlmhirt: Almhirt): AlmValidation[AggregateRootToStringSerializer] }]

      }
      serializer <- computeSafely { serializerFactory.createSerializer(theAlmhirt) }
      actor <- inTryCatch {
        val name = ConfigHelper.snapshotStorage.getActorName(configSection)
        theAlmhirt.log.info(s"DomainEventLog is text based SlickDomainEventLog with name '$name'.")
        val dispatcherName =
          ConfigHelper.getDispatcherNameFromComponentConfig(configSection).fold(
            fail => {
              theAlmhirt.log.warning("No dispatchername found for SnapshotsStorage. Using default Dispatcher")
              None
            },
            succ => {
              theAlmhirt.log.info(s"SnapshotsStorage is using dispatcher '$succ'")
              Some(succ)
            })
        val syncStorage = new SlickSyncTextSnapshotStorage(snapshotsDataAccess, serializer.serializeToChannel(channel))
        val props = SystemHelper.addDispatcherByNameToProps(dispatcherName)(Props(new SyncSnapshotStorageActor(syncStorage, theAlmhirt)))
        theAlmhirt.actorSystem.actorOf(props, name)
      }
    } yield actor
  }

  private def createBinaryDomainEventLog(theAlmhirt: Almhirt, eventLogConfig: Config): AlmValidation[ActorRef] = {
    ???
  }

}