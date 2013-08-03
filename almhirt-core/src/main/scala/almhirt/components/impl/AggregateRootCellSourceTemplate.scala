package almhirt.components.impl

import java.util.{ UUID => JUUID }
import akka.actor._
import almhirt.components._

trait AggregateRootCellSourceTemplate extends AggregateRootCellSource with SupervisioningActorCellSource { actor: Actor with ActorLogging =>
  import AggregateRootCellSource._

  private var currentHandleId = 0L

  private def nextChacheState(currentState: CacheState): Receive = {
    case GetCell(arId, arType) =>
      val (handle, nextState) = bookCellFor(arId, arType, currentState)
      sender ! AggregateRootCellSourceResult(arId, handle)
      context.become(nextChacheState(cleanUp(nextState)))
    case Unbook(handleId) =>
      val nextState = currentState.unbook(handleId)
      context.become(nextChacheState(cleanUp(nextState)))
    case DoesNotExistNotification(arId) =>
      val nextState = currentState.markForRemoval(arId)
      context.become(nextChacheState(cleanUp(nextState)))
    case Remove(arId) =>
      val nextState = currentState.markForRemoval(arId)
      context.become(nextChacheState(cleanUp(nextState)))
    case GetStats =>
      sender ! AggregateRootCellSourceStats(
        currentState.cellByArId.size,
        currentState.handleIdsByArId.size,
        currentState.arIdByHandleId.size)
  }

  override def receiveAggregateRootCellSourceMessage: Receive = nextChacheState(CacheState.empty)

  private def cleanUp(currentState: CacheState): CacheState = {
    val (removed, nextState) = currentState.removeCandidatesForRemoval
    removed.foreach(cell => this.context.stop(cell))
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
    val pinnedHandleId = currentHandleId
    val handle = new CellHandle {
      val cell = theCell
      def release() = self ! Unbook(pinnedHandleId)
    }
    (handle, currentState.book(arId, pinnedHandleId))
  }

  private def bookNewCell(arId: JUUID, arType: Class[_], currentState: CacheState): (CellHandle, CacheState) = {
    val newCell = createCell(arId, arType)
    val stateWithNewCell = currentState.addCell(arId, newCell)
    currentHandleId = currentHandleId + 1L
    val pinnedHandleId = currentHandleId
    val handle = new CellHandle {
      val cell = newCell
      def release() = self ! Unbook(pinnedHandleId)
    }
    val newState = currentState.book(arId, pinnedHandleId).addCell(arId, newCell)
    (handle, newState)
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

    def bookedCellIds = handleIdsByArId.keySet

    def getCandidatesForRemoval: Set[JUUID] =
      if (markedForRemoval.isEmpty)
        Set.empty
      else {
        markedForRemoval diff bookedCellIds
      }

    def removeCandidatesForRemoval(): (Iterable[ActorRef], CacheState) = {
      val cellIdsToRemove = getCandidatesForRemoval
      val cellsToRemove = cellIdsToRemove.map(cellByArId)
      val newCellsByArId = cellByArId.filterKeys(cellId => !cellIdsToRemove.contains(cellId))
      (cellsToRemove, this.copy(cellByArId = newCellsByArId, markedForRemoval = Set.empty))
    }
  }

  private object CacheState {
    def empty: CacheState = CacheState(Map.empty, Map.empty, Map.empty, Set.empty)
  }
}