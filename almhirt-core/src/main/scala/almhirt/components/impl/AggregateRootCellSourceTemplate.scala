package almhirt.components.impl

import java.util.{ UUID => JUUID }
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.almvalidation.kit._
import almhirt.core.types._
import almhirt.core.Almhirt
import almhirt.components._
import com.typesafe.config.Config

trait AggregateRootCellSourceTemplate extends AggregateRootCellSource with SupervisioningActorCellSource { actor: Actor with ActorLogging =>
  import AggregateRootCellSource._

  def cacheControlHeartBeatInterval: Option[FiniteDuration]

  def maxCachedAggregateRootAge: Option[FiniteDuration]
  def maxDoesNotExistAge: Option[FiniteDuration]
  def maxUninitializedAge: Option[FiniteDuration]

  implicit def executionContext: ExecutionContext

  private val cacheState = new MutableAggregateRootCellCache(
    createCell,
    cell => {
      this.context.watch(cell)
      this.context.stop(cell)
    },
    handleId => self ! Unbook(handleId))

  private var lastStatsAfterCleanUp = cacheState.stats
  
  val pendingRequests = scala.collection.mutable.HashMap.empty[JUUID, scala.collection.mutable.Buffer[(Class[_ <: AggregateRoot[_, _]], ActorRef)]]

  override def receiveAggregateRootCellSourceMessage: Receive = {
    case GetCell(arId, arType) =>
      val (bookingResult, _) = cacheState.bookCell(arId, arType, log.warning)
      bookingResult match {
        case MutableAggregateRootCellCache.CellBooked(handle) =>
          sender ! AggregateRootCellSourceResult(arId, handle)
        case MutableAggregateRootCellCache.AwaitingDeathCertificate =>
          enqueueRequest(arId, arType, sender)
      }
    case Unbook(handleId) =>
      cacheState.unbookCell(handleId, log.warning)
    case CellStateNotification(managedArId, reportedState) =>
      cacheState.updateCellState(managedArId, reportedState)
    case GetStats =>
      sender ! AggregateRootCellSourceStats(cacheState.stats)
    case Terminated(cell) =>
      this.context.unwatch(cell)
      cacheState.arIdForUnconfirmedKill(cell) match {
        case Some(arId) =>
          cacheState.confirmDeath(cell, log.warning)
          handlePendingRequestsOnDeathCertificate(arId)
        case None =>
          val msg = s"""There was a confirmed kill for "${cell.path.toString()}" but it is not in the list of unconfirmed kills."""
          log.error(msg)
          throw new CriticalAggregateRootCellSourceException(msg, null)
      }
    case CleanUp =>
      val oldStats = lastStatsAfterCleanUp
      val statsBeforeCleanUp = cacheState.stats
      val (_, timings) =
        try {
          cacheState.cleanUp(maxDoesNotExistAge, maxCachedAggregateRootAge, maxUninitializedAge)
        } catch {
          case scala.util.control.NonFatal(exn) =>
            throw new CriticalAggregateRootCellSourceException(s"The cell cache failed to perform a clean up: ${exn.getMessage()}", exn)
        }
      lastStatsAfterCleanUp = cacheState.stats
      val numPendingRequest = pendingRequests.map(_._2).flatten.size
      cacheControlHeartBeatInterval.foreach(dur => context.system.scheduler.scheduleOnce(dur)(requestCleanUp()))
      val comparisonString = AggregateRootCellCacheStats.tripletComparisonString(oldStats, statsBeforeCleanUp, lastStatsAfterCleanUp, "last clean up", "before clean up", "after clean up")
      log.info(s"""Performed clean up.\n$comparisonString\n${timings.toNiceString()}\n\n$numPendingRequest request(s) on unconfirmed cell kills left.""")
  }

  private def enqueueRequest(arId: JUUID, arType: Class[_ <: AggregateRoot[_, _]], waitingCaller: ActorRef) {
    pendingRequests.get(arId) match {
      case Some(requests) =>
        requests.append((arType, waitingCaller))
      case None =>
        pendingRequests.put(arId, scala.collection.mutable.Buffer((arType, waitingCaller)))
    }
  }

  private def handlePendingRequestsOnDeathCertificate(arId: JUUID) {
    pendingRequests.get(arId) match {
      case Some(waiting) =>
        val stillWaiting = scala.collection.mutable.Buffer[(JUUID, Class[_ <: AggregateRoot[_, _]], ActorRef)]()
        waiting.foreach {
          case (arType, caller) =>
            val (bookingResult, _) = cacheState.bookCell(arId, arType, log.warning)
            bookingResult match {
              case MutableAggregateRootCellCache.CellBooked(handle) =>
                caller ! AggregateRootCellSourceResult(arId, handle)
              case MutableAggregateRootCellCache.AwaitingDeathCertificate =>
                stillWaiting.append((arId, arType, caller))
            }
        }
        pendingRequests.remove(arId)
        stillWaiting.foreach {
          case (arId, arType, waitingCaller) =>
            enqueueRequest(arId, arType, waitingCaller)
        }
      case None =>
        ()
    }
  }

  private case class Unbook(handleId: Long)

  private object CleanUp

  protected def requestCleanUp() {
    self ! CleanUp
  }

  protected def stats = cacheState.stats
}
