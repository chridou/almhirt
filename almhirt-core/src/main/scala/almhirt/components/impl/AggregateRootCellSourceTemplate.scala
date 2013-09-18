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
      log.info(s"""Performed clean up in $time.\nOld state: ${oldStats.niceString}\nNew state: ${newStats.niceString}""")
  }
  private case class Unbook(handleId: Long)

  private object CleanUp
  
  protected def requestCleanUp() {
    self ! CleanUp
  }
}

//trait AggregateRootCellSourceTemplate extends AggregateRootCellSource with SupervisioningActorCellSource { actor: Actor with ActorLogging =>
//  import AggregateRootCellSource._
//
//  private var currentHandleId = 0L
//
//  def cacheControlHeartBeatInterval: Option[FiniteDuration]
//
//  def maxCellCacheAge: Option[FiniteDuration]
//
//  def maxDoesNotExistAge: Option[FiniteDuration]
//  
//  implicit def executionContext: ExecutionContext
//
//  private def nextChacheState(currentState: CacheState): Receive = {
//    case GetCell(arId, arType) =>
//      val (handle, nextState) = bookCellFor(arId, arType, currentState)
//      sender ! AggregateRootCellSourceResult(arId, handle)
//      context.become(nextChacheState(nextState))
//    case Unbook(handleId) =>
//      val nextState = currentState.unbook(handleId)
//      context.become(nextChacheState(nextState))
//    case CellStateNotification(managedArId, reportedState) =>
////      val nextState = currentState.markForRemoval(arId)
////      context.become(nextChacheState(nextState))
//    case Remove(arId) =>
//      val nextState = currentState.markForRemoval(arId)
//      context.become(nextChacheState(nextState))
//    case GetStats =>
//      sender ! AggregateRootCellSourceStats(
//        currentState.cellByArId.size,
//        currentState.handleIdsByArId.size,
//        currentState.arIdByHandleId.size)
//    case CleanUp =>
//      val newState = cleanUp(currentState)
//      context.become(nextChacheState(newState))
//      cacheControlHeartBeatInterval.foreach(dur => context.system.scheduler.scheduleOnce(dur)(requestCleanUp()))
//      val oldStateStats = AggregateRootCellSourceStats(
//        currentState.cellByArId.size,
//        currentState.handleIdsByArId.size,
//        currentState.arIdByHandleId.size)
//      val newStateStats = AggregateRootCellSourceStats(
//        newState.cellByArId.size,
//        newState.handleIdsByArId.size,
//        newState.arIdByHandleId.size)
//      log.info(s"""Performed clean up\nOld state: ${oldStateStats.toString()}\nNew state: ${newStateStats.toString()}""")
//  }
//
//  override def receiveAggregateRootCellSourceMessage: Receive = nextChacheState(CacheState.empty)
//
//  private def cleanUp(currentState: CacheState): CacheState = {
//    val (removed, nextState) = currentState.removeCandidatesForRemoval
//    removed.foreach(cell => this.context.stop(cell))
//    nextState
//  }
//
//  private def IsCellBooked(arId: JUUID, currentState: CacheState): Boolean =
//    currentState.handleIdsByArId.get(arId).map(handleIds => !handleIds.isEmpty).getOrElse(false)
//
//  private def existsCellForAr(arId: JUUID, currentState: CacheState): Boolean =
//    currentState.cellByArId.contains(arId)
//
//  private def bookCellFor(arId: JUUID, arType: Class[_], currentState: CacheState): (CellHandle, CacheState) =
//    if (existsCellForAr(arId, currentState)) {
//      bookExistingCell(arId, currentState)
//    } else {
//      bookNewCell(arId, arType, currentState)
//    }
//
//  private def bookExistingCell(arId: JUUID, currentState: CacheState): (CellHandle, CacheState) = {
//    val theCell = currentState.getCell(arId)
//    currentHandleId = currentHandleId + 1L
//    val pinnedHandleId = currentHandleId
//    val handle = new CellHandle {
//      val cell = theCell
//      def release() = self ! Unbook(pinnedHandleId)
//    }
//    (handle, currentState.book(arId, pinnedHandleId))
//  }
//
//  private def bookNewCell(arId: JUUID, arType: Class[_], currentState: CacheState): (CellHandle, CacheState) = {
//    val newCell = createCell(arId, arType)
//    val stateWithNewCell = currentState.addCell(arId, newCell)
//    currentHandleId = currentHandleId + 1L
//    val pinnedHandleId = currentHandleId
//    val handle = new CellHandle {
//      val cell = newCell
//      def release() = self ! Unbook(pinnedHandleId)
//    }
//    val newState = currentState.book(arId, pinnedHandleId).addCell(arId, newCell)
//    (handle, newState)
//  }
//
//  private case class Remove(arId: JUUID)
//  private case class Unbook(handleId: Long)
//
//  private case class CacheState(
//    cellByArId: Map[JUUID, ActorRef],
//    handleIdsByArId: Map[JUUID, Set[Long]],
//    arIdByHandleId: Map[Long, JUUID],
//    markedForRemoval: Set[JUUID]) {
//
//    def unbook(handleId: Long): CacheState = {
//      arIdByHandleId.get(handleId) match {
//        case Some(arId) =>
//          val newHandleIdsByArId = {
//            val newHandlesForArId = handleIdsByArId(arId) - handleId
//            if (newHandlesForArId.isEmpty)
//              handleIdsByArId - arId
//            else
//              handleIdsByArId + (arId -> newHandlesForArId)
//          }
//          CacheState(cellByArId, newHandleIdsByArId, arIdByHandleId - handleId, markedForRemoval)
//        case None =>
//          log.warning(s"""Tried to unbook cell for handle with id $handleId but there was no handle registered with that id. The cell source might have been restarted.""")
//          this
//      }
//    }
//
//    def book(arId: JUUID, handleId: Long): CacheState = {
//      val newArIdByHandleId = arIdByHandleId + (handleId -> arId)
//      val newHandleIdsByArId =
//        if (handleIdsByArId.contains(arId)) {
//          handleIdsByArId + (arId -> (handleIdsByArId(arId) + handleId))
//        } else {
//          handleIdsByArId + (arId -> Set(handleId))
//
//        }
//      CacheState(cellByArId, newHandleIdsByArId, newArIdByHandleId, markedForRemoval)
//    }
//
//    def addCell(arId: JUUID, cell: ActorRef): CacheState =
//      copy(cellByArId = this.cellByArId + (arId -> cell))
//
//    def getCell(arId: JUUID): ActorRef =
//      cellByArId(arId)
//
//    def markForRemoval(arId: JUUID): CacheState =
//      copy(markedForRemoval = this.markedForRemoval + arId)
//
//    def bookedCellIds = handleIdsByArId.keySet
//
//    def getCandidatesForRemoval: Set[JUUID] =
//      if (markedForRemoval.isEmpty)
//        Set.empty
//      else {
//        markedForRemoval diff bookedCellIds
//      }
//
//    def removeCandidatesForRemoval(): (Iterable[ActorRef], CacheState) = {
//      val cellIdsToRemove = getCandidatesForRemoval
//      val cellsToRemove = cellIdsToRemove.map(cellByArId)
//      val newCellsByArId = cellByArId.filterKeys(cellId => !cellIdsToRemove.contains(cellId))
//      (cellsToRemove, this.copy(cellByArId = newCellsByArId, markedForRemoval = Set.empty))
//    }
//  }
//
//  private object CleanUp
//  
//  protected def requestCleanUp() {
//    self ! CleanUp
//  }
//  
//  private object CacheState {
//    def empty: CacheState = CacheState(Map.empty, Map.empty, Map.empty, Set.empty)
//  }
//}