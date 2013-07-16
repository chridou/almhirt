package almhirt.domain

import java.util.{ UUID => JUUID }
import akka.actor._

object AggregateRootRepository {

}

trait AggregateRootRepository { actor: akka.actor.Actor =>
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  protected def receiveRepositoryMsg: Receive

}

trait AggregateRootRepositoryWithLookupTemplate extends AggregateRootRepository { actor: akka.actor.Actor =>
  import scala.reflect.ClassTag
  import akka.pattern._
  import akka.util.Timeout
  import almhirt.common._
  import almhirt.almvalidation.kit._
  import almhirt.almfuture.all._
  import almhirt.core.Almhirt
  import DomainMessages._
  import AggregateRootCell._

  type ActorCellCache

  implicit def arTag: ClassTag[AR]
  implicit def eventTag: ClassTag[Event]

  protected def theAlmhirt: Almhirt
  implicit protected def executionContext = theAlmhirt.futuresExecutor

  protected def cellAskMaxDuration: scala.concurrent.duration.FiniteDuration
  protected def cacheAskMaxDuration: scala.concurrent.duration.FiniteDuration

  protected def createCell(aggregateRootId: JUUID): ActorRef
  protected def initialCellLookup: ActorCellCache

  protected def getCell(aggregateRootId: JUUID, from: ActorCellCache): AlmFuture[Option[ActorRef]]
  protected def addCell(aggregateRootId: JUUID, cell: ActorRef, to: ActorCellCache)
  protected def removeCell(aggregateRootId: JUUID, from: ActorCellCache)

  protected def currentState(cellCache: ActorCellCache): Receive = {
    case GetAggregateRoot(arId) =>
      handleGet(arId, sender, cellCache)
    case UpdateAggregateRoot(newState, eventsToNewState) =>
      handleUpdate(newState, eventsToNewState, sender, cellCache)
  }

  protected override def receiveRepositoryMsg(): Receive = currentState(initialCellLookup)

  private def handleGet(arId: JUUID, sender: ActorRef, cellCache: ActorCellCache) {
    getCell(arId, cellCache).onComplete(
      fail => {
        sender ! AggregateRootFetchFailed(arId, fail)
      },
      {
        case Some(cell) =>
          askCellForAr(arId, cell, sender, cellCache)
        case None =>
          val newCell = createCell(arId)
          addCell(arId, newCell, cellCache)
          askCellForAr(arId, newCell, sender, cellCache)
      })
  }

  private def handleUpdate(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent], sender: ActorRef, cellCache: ActorCellCache) {
    if (!newState.castTo[AR].isSuccess)
      sender ! IncompatibleAggregateRoot(newState, arTag.runtimeClass.getName())
    else if (!eventsToNewState.forall(_.castTo[Event].isSuccess))
      sender ! IncompatibleDomainEvent(eventTag.runtimeClass.getName())
    else
      getCell(newState.id, cellCache).onComplete(
        fail => {
          sender ! AggregateRootUpdateFailed(fail)
        },
        {
          case Some(cell) =>
            askCellForUpdate(newState, eventsToNewState, cell, sender, cellCache)
          case None =>
            val newCell = createCell(newState.id)
            addCell(newState.id, newCell, cellCache)
            askCellForUpdate(newState, eventsToNewState, newCell, sender, cellCache)
        })
  }

  private def askCellForAr(arId: JUUID, cell: ActorRef, sender: ActorRef, cellCache: ActorCellCache) {
    (cell ? GetManagedAggregateRoot)(cellAskMaxDuration).successfulAlmFuture[Any].onComplete(
      fail =>
        sender ! AggregateRootFetchFailed(arId, fail),
      {
        case RequestedAggregateRoot(ar) =>
          ar.castTo[AR].fold(
            fail => AggregateRootFetchFailed(arId, fail),
            ar => {
              sender ! RequestedAggregateRoot(ar)
            })
        case m: AggregateRootNotFound =>
          sender ! m
          removeFromCacheAndKill(arId, cell, cellCache)
        case m: AggregateRootFetchFailed =>
          sender ! m
          removeFromCacheAndKill(arId, cell, cellCache)
        case AggregateRootWasDeleted(arId) =>
          sender ! AggregateRootNotFound(arId)
          removeFromCacheAndKill(arId, cell, cellCache)
      })
  }

  private def askCellForUpdate(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent], cell: ActorRef, sender: ActorRef, cellCache: ActorCellCache) {
    (cell ? UpdateAggregateRoot(newState, eventsToNewState))(cellAskMaxDuration).successfulAlmFuture[Any].onComplete(
      fail =>
        sender ! AggregateRootUpdateFailed(fail),
      {
        case m: AggregateRootUpdated =>
          sender ! m
        case AggregateRootNotFound(arId) =>
          sender ! AggregateRootUpdateFailed(AggregateRootNotFoundProblem(arId))
          removeFromCacheAndKill(arId, cell, cellCache)
        case AggregateRootFetchFailed(arId, problem) =>
          sender ! AggregateRootUpdateFailed(problem)
          removeFromCacheAndKill(arId, cell, cellCache)
        case AggregateRootWasDeleted(arId) =>
          sender ! AggregateRootUpdateFailed(AggregateRootNotFoundProblem(arId))
          removeFromCacheAndKill(arId, cell, cellCache)
        case AggregateRootPartiallyUpdated(lastKnownState, uncommitted, problem) =>
          sender ! AggregateRootUpdateFailed(problem)
          removeFromCacheAndKill(lastKnownState.id, cell, cellCache)
        case m: AggregateRootUpdateFailed =>
          sender ! m
      })
  }

  private def removeFromCacheAndKill(arId: JUUID, cell: ActorRef, cellCache: ActorCellCache) {
    cell ! PoisonPill
    removeCell(arId, cellCache)
  }
}

