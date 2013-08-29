package almhirt.components.impl

import java.util.{ UUID => JUUID }
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.almvalidation.kit._
import almhirt.configuration._
import almhirt.core.Almhirt
import com.typesafe.config.Config

class AggregateRootCellSourceImpl(cellPropsFactories: Class[_] => Option[(JUUID, () => Unit) => Props], theAlmhirt: Almhirt)
  extends AggregateRootCellSourceTemplate with Actor with ActorLogging {

  val configSection = theAlmhirt.config.v[Config]("almhirt.aggregate-root-cell-source").resultOrEscalate
  
  override val cacheControlHeartBeatInterval =
    if (configSection.v[Boolean]("enable-cache-control").resultOrEscalate) {
      val interval = configSection.v[FiniteDuration]("cache-control-heart-beat-interval").resultOrEscalate
      log.info(s"Cache control enabled. Heartbeat: ${interval.toString()}")
      Some(interval)
    } else {
      log.info("Cache control disabled")
      None
    }

  override val maxCellCacheAge =
    cacheControlHeartBeatInterval.flatMap { _ =>
      if (configSection.v[Boolean]("remove-old-cells").resultOrEscalate) {
        val maxAge = configSection.v[FiniteDuration]("max-cell-cache-age").resultOrEscalate
        log.info(s"Max cell age: ${maxAge.toString}")
        Some(maxAge)
      } else
        None
    }

  override val maxDoesNotExistAge =
    cacheControlHeartBeatInterval.flatMap { _ =>
      if (configSection.v[Boolean]("remove-does-not-exist-cells").resultOrEscalate) {
        val maxAge = configSection.v[FiniteDuration]("max-does-not-exist-age").resultOrEscalate
        log.info(s"""Max "does-not-exist"-age: ${maxAge.toString()}""")
        Some(maxAge)
      } else
        None
    }
  
  
  
  override protected def createProps(aggregateRootId: JUUID, forArType: Class[_], notifiyOnDoesNotExist: () => Unit): Props =
    cellPropsFactories(forArType) match {
      case Some(factory) => factory(aggregateRootId, notifiyOnDoesNotExist)
      case None => throw new Exception(s"""No factory to create props for aggregate root of type "${forArType.getName()} found."""")
    }
  
  override def receive: Receive = receiveAggregateRootCellSourceMessage
}