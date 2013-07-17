package almhirt.domain.caching.impl

import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.domain.caching._

trait AggregateRootCellSourceImpl extends AggregateRootCellSource with SupervisioningActorCellSource { actor: Actor with ActorLogging =>
  import AggregateRootCellSource._

  private var currentHandleId = 0L

  private def nextChacheState(currentState: CacheState): Receive = {
    case GetCell(arId, arType) =>
      val (handle, nextState) = bookCellFor(arId, arType, currentState)
      sender ! AggregateRootCellSourceResult(arId, handle)
      context.become(nextChacheState(cleanUp(nextState)))
    case Unbook(handleId) =>
      log.debug(s"""Handle with id "$handleId" has been unbooked.""")
      val nextState = currentState.unbook(handleId)
      context.become(nextChacheState(cleanUp(nextState)))
    case DoesNotExistNotification(arId) =>
      log.debug(s"""Cell for aggregate root "${arId.toString()}" reported that the aggregate root does not exist.""")
      val nextState = currentState.markForRemoval(arId)
      context.become(nextChacheState(cleanUp(nextState)))
    case Remove(arId) =>
      log.debug(s"""Cell for aggregate root "${arId.toString()}" was marked for removal.""")
      val nextState = currentState.markForRemoval(arId)
      context.become(nextChacheState(cleanUp(nextState)))
  }

  override def receiveAggregateRootCellSourceMessage: Receive = nextChacheState(CacheState.empty)

  private def cleanUp(currentState: CacheState): CacheState = {
    val (removed, nextState) = currentState.removeCandidatesForRemoval
    removed.foreach(cell => this.context.stop(cell))
    if (!removed.isEmpty)
      log.debug(s"""Removed ${removed.size} cells.""")
    nextState
  }

  private def cellIsBooked(arId: JUUID, currentState: CacheState): Boolean =
    currentState.handleIdsByArId.get(arId).map(handleIds => !handleIds.isEmpty).getOrElse(false)

  private def cellForArExists(arId: JUUID, currentState: CacheState): Boolean =
    currentState.cellByArId.contains(arId)

  private def bookCellFor(arId: JUUID, arType: Class[_], currentState: CacheState): (CellHandle, CacheState) =
    if (cellForArExists(arId, currentState)) {
      bookExistingCell(arId, currentState)
    } else {
      bookNewCell(arId, arType, currentState)
    }

  private def bookExistingCell(arId: JUUID, currentState: CacheState): (CellHandle, CacheState) = {
    val theCell = currentState.getCell(arId)
    currentHandleId = currentHandleId + 1L
    val handle = new CellHandle {
      val cell = theCell
      def release() = self ! Unbook(currentHandleId)
    }
    log.debug(s"""Booked existing cell(${theCell.path}) for aggregate root "${arId}". The handle id is $currentHandleId.""")
    (handle, currentState.book(arId, currentHandleId))
  }

  private def bookNewCell(arId: JUUID, arType: Class[_], currentState: CacheState): (CellHandle, CacheState) = {
    val newCell = createCell(arId, arType)
    val stateWithNewCell = currentState.addCell(arId, newCell)
    currentHandleId = currentHandleId + 1L
    val handle = new CellHandle {
      val cell = newCell
      def release() = self ! Unbook(currentHandleId)
    }
    log.debug(s"""Booked new cell(${newCell.path}) for aggregate root "${arId}". The handle id is $currentHandleId.""")
    (handle, currentState.book(arId, currentHandleId))
  }

  private case class Remove(arId: JUUID)
  private case class Unbook(handleId: Long)

  private case class CacheState(
    cellByArId: Map[JUUID, ActorRef],
    handleIdsByArId: Map[JUUID, Set[Long]],
    arIdByHandleId: Map[Long, JUUID],
    markedForRemoval: Set[JUUID]) {

    def unbook(handleId: Long): CacheState = {
      val arId = arIdByHandleId(handleId)
      val newHandleIdsByArId = {
        val newHandlesForArId = handleIdsByArId(arId) - handleId
        if (newHandlesForArId.isEmpty)
          handleIdsByArId - arId
        else
          handleIdsByArId + (arId -> newHandlesForArId)
      }
      CacheState(cellByArId, newHandleIdsByArId, arIdByHandleId - handleId, markedForRemoval)
    }

    def book(arId: JUUID, handleId: Long): CacheState = {
      val newArIdByHandleId = arIdByHandleId + (handleId -> arId)
      val newHandleIdsByArId =
        if (handleIdsByArId.contains(arId)) {
          handleIdsByArId + (arId -> (handleIdsByArId(arId) + handleId))
        } else {
          handleIdsByArId + (arId -> Set(handleId))

        }
      CacheState(cellByArId, newHandleIdsByArId, newArIdByHandleId, markedForRemoval)
    }

    def addCell(arId: JUUID, cell: ActorRef): CacheState =
      copy(cellByArId = this.cellByArId + (arId -> cell))

    def getCell(arId: JUUID): ActorRef =
      cellByArId(arId)

    def markForRemoval(arId: JUUID): CacheState =
      copy(markedForRemoval = this.markedForRemoval + arId)

    def unbookedCellIds = handleIdsByArId.keySet

    def getCandidatesForRemoval: Set[JUUID] =
      if (markedForRemoval.isEmpty)
        Set.empty
      else {
        markedForRemoval diff unbookedCellIds
      }

    def removeCandidatesForRemoval(): (Iterable[ActorRef], CacheState) = {
      val cellIdsToRemove = getCandidatesForRemoval
      val cellsToRemove = cellIdsToRemove.map(cellByArId)
      val newCellsByArId = cellByArId.filterKeys(cellId => !cellIdsToRemove.contains(cellId))
      (cellsToRemove, this.copy(cellByArId = newCellsByArId))
    }
  }

  private object CacheState {
    def empty: CacheState = CacheState(Map.empty, Map.empty, Map.empty, Set.empty)
  }
}