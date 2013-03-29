package almhirt.domain.components

import java.util.{ UUID => JUUID }
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.domain.IsAggregateRoot
import scala.collection.mutable._
import almhirt.environment.configuration._

class GrowOnlyAggregateRootCacheActor extends Actor {
  val cache: Map[JUUID, IsAggregateRoot] = Map.empty
  override def receive: Receive = {
    case ev: AggregateRootCacheReq =>
      ev match {
        case GetCachedAggregateRootQry(id) => sender ! AggregateRootFromCacheRsp(cache.get(id), id)
        case CacheAggregateRootCmd(ar) => cache.put(ar.id, ar)
        case RemoveAggregateRootFromCacheCmd(id) => cache.remove(id)
        case ContainsCachedAggregateRootQry(id) => sender ! ContainsCachedAggregateRootRsp(cache.contains(id), id)
      }
  }
}

class GrowOnlyAggregateRootCacheFactory extends AggregateRootCacheFactory {
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
        val props = SystemHelper.addDispatcherByNameToProps(dispatcherName)(Props(new GrowOnlyAggregateRootCacheActor()))
        theAlmhirt.actorSystem.actorOf(props, actorname)
      }).success
}