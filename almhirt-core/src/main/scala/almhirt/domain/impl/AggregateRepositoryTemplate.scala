package almhirt.domain.impl

import java.util.{ UUID => JUUID }
import scala.reflect.ClassTag
import scalaz.syntax.validation._
import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.core.Almhirt
import almhirt.almfuture.all._
import almhirt.domain._

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

  type GetResult

  protected def theAlmhirt: Almhirt
  implicit protected def executionContext = theAlmhirt.futuresExecutor

  protected def cellAskMaxDuration: scala.concurrent.duration.FiniteDuration

  protected def cellCache: ActorRef

  protected def getCell(aggregateRootId: JUUID): AlmFuture[GetResult]

  protected def onceWithGetResult[T](result: GetResult, f: (ActorRef) => AlmFuture[T]): AlmFuture[T]

  protected def receiveRepositoryMsg: Receive = {
    case GetAggregateRoot(arId) =>
      handleGet(arId, sender)
    case UpdateAggregateRoot(newState, eventsToNewState) =>
      handleUpdate(newState, eventsToNewState, sender)
  }

  protected def handleGet(arId: JUUID, requester: ActorRef) {
    getCell(arId).onComplete(
      fail => {
        fail match {
          case OperationTimedOutProblem(p) =>
            requester ! AggregateRootFetchFailed(arId, OperationTimedOutProblem(s"""Getting the cell for "$arId" for "get" timed out.""", cause = Some(fail)))
          case _ =>
            requester ! AggregateRootFetchFailed(arId, UnspecifiedProblem(s"""Getting the cell for "$arId" for "get" failed.""", cause = Some(fail)))
        }
      },
      getResult => onceWithGetResult(getResult, cell => askCellForAr(arId, cell, requester)))
  }

  protected def handleUpdate(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent], requester: ActorRef) {
    if (!newState.castTo[AR].isSuccess)
      requester ! IncompatibleAggregateRoot(newState, arTag.runtimeClass.getName())
    else if (!eventsToNewState.forall(_.castTo[Event].isSuccess))
      requester ! IncompatibleDomainEvent(eventTag.runtimeClass.getName())
    else
      getCell(newState.id).onComplete(
        fail => {
          fail match {
            case OperationTimedOutProblem(p) =>
              requester ! AggregateRootUpdateFailed(newState.id, OperationTimedOutProblem(s"""Getting the cell for "${newState.id}" for "update" timed out.""", cause = Some(fail)))
            case _ =>
              requester ! AggregateRootUpdateFailed(newState.id, UnspecifiedProblem(s"""Getting the cell for "${newState.id}" for "update" failed.""", cause = Some(fail)))
          }
        },
        getResult => onceWithGetResult(getResult, cell => askCellForUpdate(newState, eventsToNewState, cell, requester)))
  }

  private def askCellForAr(arId: JUUID, cell: ActorRef, requester: ActorRef) = {
    (cell ? GetManagedAggregateRoot)(cellAskMaxDuration).successfulAlmFuture[Any].fold(
      fail =>
        fail match {
          case OperationTimedOutProblem(p) =>
            requester ! AggregateRootFetchFailed(arId, OperationTimedOutProblem(s"""Getting the AR from cell for "$arId" timed out(Timeout: ${cellAskMaxDuration.defaultUnitString}).""", cause = Some(fail)))
          case _ =>
            requester ! AggregateRootFetchFailed(arId, UnspecifiedProblem(s"""Getting the AR from cell for "$arId" failed.""", cause = Some(fail)))
        },
      {
        case RequestedAggregateRoot(ar) =>
          ar.castTo[AR].fold(
            fail => AggregateRootFetchFailed(arId, fail),
            ar => {
              requester ! RequestedAggregateRoot(ar)
            })
        case m: AggregateRootNotFound =>
          requester ! m
        case m: AggregateRootFetchFailed =>
          requester ! m
      })
  }

  private def askCellForUpdate(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent], cell: ActorRef, requester: ActorRef) = {
    (cell ? UpdateAggregateRoot(newState, eventsToNewState))(cellAskMaxDuration).successfulAlmFuture[Any].fold(
      fail =>
        fail match {
          case OperationTimedOutProblem(p) =>
            requester ! AggregateRootUpdateFailed(newState.id, OperationTimedOutProblem(s"""Updating the AR in cell for "${newState.id}" timed out(Timeout: ${cellAskMaxDuration.defaultUnitString}).""", cause = Some(fail)))
          case _ =>
            requester ! AggregateRootUpdateFailed(newState.id, UnspecifiedProblem(s"""Updating the AR in cell for "${newState.id}" failed.""", cause = Some(fail)))
        },
      {
        case m: AggregateRootUpdated =>
          requester ! m
        case AggregateRootNotFound(arId) =>
          requester ! AggregateRootUpdateFailed(newState.id, AggregateRootNotFoundProblem(arId))
        case AggregateRootFetchFailed(arId, problem) =>
          requester ! AggregateRootUpdateFailed(newState.id, problem)
        case m: AggregateRootUpdateFailed =>
          requester ! m
      })
  }
}

trait AggregateRootRepositoryWithCellSourceActor extends AggregateRootRepositoryWithCacheTemplate { actor: Actor with AggregateRepositoryTemplate =>
  import almhirt.components.AggregateRootCellSource._

  type GetResult = CellHandle

  protected def cacheAskMaxDuration: scala.concurrent.duration.FiniteDuration

  override final protected def getCell(aggregateRootId: JUUID): AlmFuture[GetResult] =
    (cellCache ? GetCell(aggregateRootId, arTag.runtimeClass.asInstanceOf[Class[AggregateRoot[_, _]]]))(cacheAskMaxDuration)
      .successfulAlmFuture[AggregateRootCellSourceResult]
      .mapTimeout(p => OperationTimedOutProblem(s"""Asking the cell cache for cell($aggregateRootId) timed out(Timeout = ${cacheAskMaxDuration.defaultUnitString}).""", cause = Some(p))
        )
      .map { _.cellHandle }

  override final protected def onceWithGetResult[T](result: GetResult, f: (ActorRef) => AlmFuture[T]): AlmFuture[T] =
    result.onceWithCell(f)

}

