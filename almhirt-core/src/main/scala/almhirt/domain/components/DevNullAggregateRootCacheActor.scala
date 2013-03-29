package almhirt.domain.components

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration._

class DevNullAggregateRootCacheActor extends Actor {
  override def receive: Receive = {
    case ev: AggregateRootCacheReq =>
      ev match {
        case GetCachedAggregateRootQry(id) => sender ! AggregateRootFromCacheRsp(None, id)
        case CacheAggregateRootCmd(ar) => ()
        case RemoveAggregateRootFromCacheCmd(id) => ()
        case ContainsCachedAggregateRootQry(id) => sender ! ContainsCachedAggregateRootRsp(false, id)
      }
  }
}

class DevNullAggregateRootCacheFactory extends AggregateRootCacheFactory {
  def createAggregateRootCache(theAlmhirt: Almhirt): AlmValidation[ActorRef] = 
    theAlmhirt.getService[HasConfig].flatMap(c => ConfigHelper.getSubConfig(c.config)(ConfigPaths.aggregateRootCache)).fold(
      fail => {
        theAlmhirt.log.warning(s"No configuration(${ConfigPaths.aggregateRootCache}) found. Using default Dispatcher")
        theAlmhirt.actorSystem.actorOf(Props(new DevNullAggregateRootCacheActor()), "AggregateRootCache")
      },
      configSection => {
        val actorname = ConfigHelper.snapshotStorage.getActorName(configSection)
        val dispatcherName =
          ConfigHelper.getDispatcherNameFromComponentConfig(configSection).fold(
            fail => {
              theAlmhirt.log.warning("No dispatchername found for AggregateRootCache. Using default Dispatcher")
              None
            },
            succ => {
              theAlmhirt.log.info(s"AggregateRootCache is using dispatcher '$succ'")
              Some(succ)
            })
        val props = SystemHelper.addDispatcherByNameToProps(dispatcherName)(Props(new DevNullAggregateRootCacheActor()))
        theAlmhirt.actorSystem.actorOf(props, actorname)
      }).success
}