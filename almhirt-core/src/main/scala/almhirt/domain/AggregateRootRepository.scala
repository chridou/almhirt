package almhirt.domain

import java.util.{ UUID => JUUID }
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almfuture.all._

object AggregateRootRepository {

}

trait AggregateRootRepository { actor: akka.actor.Actor =>
  type Event <: DomainEvent
  type AR <: AggregateRoot[AR, Event]

  protected def receiveRepositoryMsg: Receive
}

trait AggregateRepositoryTemplate extends AggregateRootRepository { actor: Actor =>
  import scala.reflect.ClassTag
  implicit def arTag: ClassTag[AR]
  implicit def eventTag: ClassTag[Event]
}

trait AggregateRootRepositoryWithCacheTemplate { actor: Actor with AggregateRepositoryTemplate =>
  import almhirt.common._
  import almhirt.almvalidation.kit._
  import almhirt.core.Almhirt
  import DomainMessages._
  import AggregateRootCell._

  type ArCellCache
  type GetResult

  protected def theAlmhirt: Almhirt
  implicit protected def executionContext = theAlmhirt.futuresExecutor

  protected def cellAskMaxDuration: scala.concurrent.duration.FiniteDuration

  protected def initialCellLookup: ArCellCache

  protected def getCell(aggregateRootId: JUUID, from: ArCellCache): AlmFuture[GetResult]

  protected def onceWithGetResult[T](result: GetResult, f: (ActorRef) => AlmFuture[T]): AlmFuture[T]

  protected def currentState(cellCache: ArCellCache): Receive = {
    case GetAggregateRoot(arId) =>
      handleGet(arId, sender, cellCache)
    case UpdateAggregateRoot(newState, eventsToNewState) =>
      handleUpdate(newState, eventsToNewState, sender, cellCache)
  }

  protected override def receiveRepositoryMsg(): Receive = currentState(initialCellLookup)

  protected def handleGet(arId: JUUID, sender: ActorRef, cellCache: ArCellCache) {
    getCell(arId, cellCache).onComplete(
      fail => {
        sender ! AggregateRootFetchFailed(arId, fail)
      },
      getResult => onceWithGetResult(getResult, cell => askCellForAr(arId, cell, sender, cellCache)))
  }

  protected def handleUpdate(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent], sender: ActorRef, cellCache: ArCellCache) {
    if (!newState.castTo[AR].isSuccess)
      sender ! IncompatibleAggregateRoot(newState, arTag.runtimeClass.getName())
    else if (!eventsToNewState.forall(_.castTo[Event].isSuccess))
      sender ! IncompatibleDomainEvent(eventTag.runtimeClass.getName())
    else
      getCell(newState.id, cellCache).onComplete(
        fail => {
          sender ! AggregateRootUpdateFailed(fail)
        },
        getResult => onceWithGetResult(getResult, cell => askCellForUpdate(newState, eventsToNewState, cell, sender, cellCache)))
  }

  private def askCellForAr(arId: JUUID, cell: ActorRef, sender: ActorRef, cellCache: ArCellCache) = {
    (cell ? GetManagedAggregateRoot)(cellAskMaxDuration).successfulAlmFuture[Any].fold(
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
        case m: AggregateRootFetchFailed =>
          sender ! m
        case AggregateRootWasDeleted(arId) =>
          sender ! AggregateRootNotFound(arId)
      })
  }

  private def askCellForUpdate(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent], cell: ActorRef, sender: ActorRef, cellCache: ArCellCache) = {
    (cell ? UpdateAggregateRoot(newState, eventsToNewState))(cellAskMaxDuration).successfulAlmFuture[Any].fold(
      fail =>
        sender ! AggregateRootUpdateFailed(fail),
      {
        case m: AggregateRootUpdated =>
          sender ! m
        case AggregateRootNotFound(arId) =>
          sender ! AggregateRootUpdateFailed(AggregateRootNotFoundProblem(arId))
        case AggregateRootFetchFailed(arId, problem) =>
          sender ! AggregateRootUpdateFailed(problem)
        case AggregateRootWasDeleted(arId) =>
          sender ! AggregateRootUpdateFailed(AggregateRootNotFoundProblem(arId))
        case AggregateRootPartiallyUpdated(lastKnownState, uncommitted, problem) =>
          sender ! AggregateRootUpdateFailed(problem)
        case m: AggregateRootUpdateFailed =>
          sender ! m
      })
  }
}

trait AggregateRootRepositoryWithCellSourceActor extends AggregateRootRepositoryWithCacheTemplate { actor: Actor with AggregateRepositoryTemplate =>
  import caching.AggregateRootCellSource._

  type ArCellCache = ActorRef
  type GetResult = CellHandle

  protected def cacheAskMaxDuration: scala.concurrent.duration.FiniteDuration

  override final protected def getCell(aggregateRootId: JUUID, from: ArCellCache): AlmFuture[GetResult] =
    (from ? GetCell(aggregateRootId, arTag.runtimeClass))(cacheAskMaxDuration).successfulAlmFuture[AggregateRootCellSourceResult].map { _.cellHandle }

  override final protected def onceWithGetResult[T](result: GetResult, f: (ActorRef) => AlmFuture[T]): AlmFuture[T] =
    result.onceWithCell(f)
}

