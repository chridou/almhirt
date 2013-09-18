package almhirt.components.impl

import java.util.{ UUID => JUUID }
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.almvalidation.kit._
import almhirt.components._
import com.typesafe.config.Config
import almhirt.core.Almhirt
import scala.concurrent.ExecutionContext

trait AggregateRootCellSourceTemplate extends AggregateRootCellSource with SupervisioningActorCellSource { actor: Actor with ActorLogging =>
  import AggregateRootCellSource._

  def cacheControlHeartBeatInterval: Option[FiniteDuration]

  def maxCellCacheAge: Option[FiniteDuration]

  def maxDoesNotExistAge: Option[FiniteDuration]
  
  implicit def executionContext: ExecutionContext

  private val cacheState = new MutableAggregateRootCellCache(
      createCell,
      this.context.stop,
      handleId => self ! Unbook(handleId))
  
  override def receiveAggregateRootCellSourceMessage: Receive = {
    case GetCell(arId, arType) =>
      val (handle, _) = cacheState.bookCell(arId, arType, log.warning)
      sender ! AggregateRootCellSourceResult(arId, handle)
    case Unbook(handleId) =>
      cacheState.unbookCell(handleId, log.warning)
    case CellStateNotification(managedArId, reportedState) =>
      cacheState.updateCellState(managedArId, reportedState)
    case GetStats =>
      sender ! AggregateRootCellSourceStats(cacheState.stats)
    case CleanUp =>
      val oldStats = cacheState.stats
      val (_, time) = cacheState.cleanUp(maxDoesNotExistAge, maxCellCacheAge)
      val newStats = cacheState.stats
      cacheControlHeartBeatInterval.foreach(dur => context.system.scheduler.scheduleOnce(dur)(requestCleanUp()))
      log.info(s"""Performed clean up in $time.\n${newStats.toNiceDiffString(oldStats)}""")
  }
  private case class Unbook(handleId: Long)

  private object CleanUp
  
  protected def requestCleanUp() {
    self ! CleanUp
  }
  
  protected def stats = cacheState.stats
}
