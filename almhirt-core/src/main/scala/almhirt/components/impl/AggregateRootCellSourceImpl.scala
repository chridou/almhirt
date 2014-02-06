package almhirt.components.impl

import java.util.{ UUID => JUUID }
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.configuration._
import almhirt.core.Almhirt
import almhirt.domain.AggregateRootCellStateSink
import com.typesafe.config.Config

class AggregateRootCellSourceImpl(cellPropsFactories: Class[_] => Option[(JUUID, AggregateRootCellStateSink) => Props], theAlmhirt: Almhirt)
  extends AggregateRootCellSourceTemplate with Actor with ActorLogging {

  val configSection = theAlmhirt.config.v[Config]("almhirt.aggregate-root-cell-source").resultOrEscalate

  override implicit val executionContext = theAlmhirt.futuresExecutor

  override val cacheControlHeartBeatInterval =
    if (configSection.v[Boolean]("enable-cache-control").resultOrEscalate) {
      val interval = configSection.v[FiniteDuration]("cache-control-heart-beat-interval").resultOrEscalate
      log.info(s"Cache control enabled. Heartbeat: ${interval.defaultUnitString}")
      Some(interval)
    } else {
      log.info("Cache control disabled")
      log.info("""enable-cache-control: false""")
      None
    }

  override val maxCachedAggregateRootAge =
    cacheControlHeartBeatInterval.flatMap { _ =>
      if (configSection.v[Boolean]("remove-cached-aggregate-roots").resultOrEscalate) {
        val maxAge = configSection.v[FiniteDuration]("max-cached-aggregate-root-age").resultOrEscalate
        log.info(s"""max-cached-aggregate-root-age: ${maxAge.defaultUnitString}""")
        Some(maxAge)
      } else {
        log.info("""remove-cached-aggregate-roots: false""")
        None
      }
    }

  override val maxDoesNotExistAge =
    cacheControlHeartBeatInterval.flatMap { _ =>
      if (configSection.v[Boolean]("remove-does-not-exist-cells").resultOrEscalate) {
        val maxAge = configSection.v[FiniteDuration]("max-does-not-exist-age").resultOrEscalate
        log.info(s"""max-does-not-exist-age: ${maxAge.defaultUnitString}""")
        Some(maxAge)
      } else {
        log.info("""remove-does-not-exist-cells: false""")
        None
      }
    }

  override val maxUninitializedAge =
    cacheControlHeartBeatInterval.flatMap { _ =>
      if (configSection.v[Boolean]("remove-uninitialized-cells").resultOrEscalate) {
        val maxAge = configSection.v[FiniteDuration]("max-uninitialized-age").resultOrEscalate
        log.info(s"""max-uninitialized-age: ${maxAge.defaultUnitString}""")
        Some(maxAge)
      } else {
        log.info("""remove-uninitialized-cells: false""")
        None
      }
    }

  
  override val logCleanUpEvents = (configSection.v[Boolean]("log-cleanup-events").resultOrEscalate)
  override val logCleanUpStatistics = (configSection.v[Boolean]("log-cleanup-statistics").resultOrEscalate)
  
  override protected def createProps(aggregateRootId: JUUID, forArType: Class[_], aggregateRootCellStateSink: AggregateRootCellStateSink): Props =
    cellPropsFactories(forArType) match {
      case Some(factory) => factory(aggregateRootId, aggregateRootCellStateSink)
      case None => throw new Exception(s"""No factory to create props for aggregate root of type "${forArType.getName()} found."""")
    }

  override def receive: Receive = receiveAggregateRootCellSourceMessage

  override def preStart() {
    cacheControlHeartBeatInterval.foreach(dur => context.system.scheduler.scheduleOnce(dur)(requestCleanUp()))
  }
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    super.preRestart(reason, message)
    log.info(s"""Aggregate root cell source is about to restart: "${reason.getMessage()}"""")
  }

  override def postStop() {
    val numPendingRequest = pendingRequests.map(_._2).flatten.size
    log.info("Aggregate root cell source stopped.")
    log.info(s"""${stats.toNiceString}\n\n$numPendingRequest request(s) on unconfirmed cell kills left.""")
  }

}