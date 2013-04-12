package almhirt.domain.components

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration._

class DevNullSnapshotStorage extends Actor {
  override def receive: Receive = {
    case ev: SnapshotStorageReq =>
      ev match {
        case GetSnapshotQry(id) => sender ! SnapshotRsp(None, id)
        case PutSnapshotCmd(ar) => ()
        case ContainsSnapshotQry(id) => sender ! ContainsSnapshotRsp(false, id)
        case GetVersionForSnapshotQry(id) => sender ! VersionForSnapshotRsp(None, id)
      }
  }
}

class DevNullSnapshotStorageFactory extends SnapshotStorageFactory {
  import almhirt.almvalidation.kit._
  def createActorRefComponent(args: Map[String, Any]): AlmValidation[ActorRef] =
    (args.lift >! "almhirt").flatMap(_.castTo[Almhirt].flatMap(theAlmhirt => createSnapshotStorage(theAlmhirt)))
    
  override def createSnapshotStorage(theAlmhirt: Almhirt): AlmValidation[ActorRef] = 
    theAlmhirt.getService[HasConfig].flatMap(c => ConfigHelper.getSubConfig(c.config)(ConfigPaths.snapshotStorage)).fold(
      fail => {
        theAlmhirt.log.warning(s"No configuration(${ConfigPaths.snapshotStorage}) found. Using default Dispatcher")
        theAlmhirt.actorSystem.actorOf(Props(new DevNullSnapshotStorage()), "Snapshots")
      },
      configSection => {
        val actorname = ConfigHelper.snapshotStorage.getActorName(configSection)
        val dispatcherName =
          ConfigHelper.getDispatcherNameFromComponentConfig(configSection).fold(
            fail => {
              theAlmhirt.log.warning("No dispatchername found for SnapshotStorage. Using default Dispatcher")
              None
            },
            succ => {
              theAlmhirt.log.info(s"SnapshotStorage is using dispatcher '$succ'")
              Some(succ)
            })
        val props = SystemHelper.addDispatcherByNameToProps(dispatcherName)(Props(new DevNullSnapshotStorage()))
        theAlmhirt.actorSystem.actorOf(props, actorname)
      }).success
}