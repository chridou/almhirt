package almhirt.domain

import java.util.{ UUID => JUUID }
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.almfuture.all._
import scala.reflect.ClassTag

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

  protected def handleGet(arId: JUUID, sender: ActorRef) {
    getCell(arId).onComplete(
      fail => {
        fail match {
          case OperationTimedOutProblem(p) =>
            sender ! AggregateRootFetchFailed(arId, OperationTimedOutProblem(s"""Getting the cell for "$arId" for "get" timed out.""", cause = Some(fail)))
          case _ =>
            sender ! AggregateRootFetchFailed(arId, UnspecifiedProblem(s"""Getting the cell for "$arId" for "get" failed.""", cause = Some(fail)))
        }
      },
      getResult => onceWithGetResult(getResult, cell => askCellForAr(arId, cell, sender)))
  }

  protected def handleUpdate(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent], sender: ActorRef) {
    if (!newState.castTo[AR].isSuccess)
      sender ! IncompatibleAggregateRoot(newState, arTag.runtimeClass.getName())
    else if (!eventsToNewState.forall(_.castTo[Event].isSuccess))
      sender ! IncompatibleDomainEvent(eventTag.runtimeClass.getName())
    else
      getCell(newState.id).onComplete(
        fail => {
          fail match {
            case OperationTimedOutProblem(p) =>
              sender ! AggregateRootUpdateFailed(OperationTimedOutProblem(s"""Getting the cell for "${newState.id}" for "update" timed out.""", cause = Some(fail)))
            case _ =>
              sender ! AggregateRootUpdateFailed(UnspecifiedProblem(s"""Getting the cell for "${newState.id}" for "update" failed.""", cause = Some(fail)))
          }
        },
        getResult => onceWithGetResult(getResult, cell => askCellForUpdate(newState, eventsToNewState, cell, sender)))
  }

  private def askCellForAr(arId: JUUID, cell: ActorRef, sender: ActorRef) = {
    (cell ? GetManagedAggregateRoot)(cellAskMaxDuration).successfulAlmFuture[Any].fold(
      fail =>
        fail match {
          case OperationTimedOutProblem(p) =>
            sender ! AggregateRootFetchFailed(arId, OperationTimedOutProblem(s"""Getting the AR from cell for "$arId" timed out.""", cause = Some(fail)))
          case _ =>
            sender ! AggregateRootFetchFailed(arId, UnspecifiedProblem(s"""Getting the AR from cell for "$arId" failed.""", cause = Some(fail)))
        },
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

  private def askCellForUpdate(newState: IsAggregateRoot, eventsToNewState: IndexedSeq[DomainEvent], cell: ActorRef, sender: ActorRef) = {
    (cell ? UpdateAggregateRoot(newState, eventsToNewState))(cellAskMaxDuration).successfulAlmFuture[Any].fold(
      fail =>
        fail match {
          case OperationTimedOutProblem(p) =>
            sender ! AggregateRootUpdateFailed(OperationTimedOutProblem(s"""Updating the AR in cell for "${newState.id}" timed out.""", cause = Some(fail)))
          case _ =>
            sender ! AggregateRootUpdateFailed(UnspecifiedProblem(s"""Updating the AR in cell for "${newState.id}" failed.""", cause = Some(fail)))
        },
      {
        case m: AggregateRootUpdated =>
          sender ! m
        case AggregateRootNotFound(arId) =>
          sender ! AggregateRootUpdateFailed(AggregateRootNotFoundProblem(arId))
        case AggregateRootFetchFailed(arId, problem) =>
          sender ! AggregateRootUpdateFailed(problem)
        case AggregateRootWasDeleted(arId) =>
          sender ! AggregateRootUpdateFailed(AggregateRootNotFoundProblem(arId))
        case m: AggregateRootUpdateFailed =>
          sender ! m
      })
  }
}

trait AggregateRootRepositoryWithCellSourceActor extends AggregateRootRepositoryWithCacheTemplate { actor: Actor with AggregateRepositoryTemplate =>
  import almhirt.components.AggregateRootCellSource._

  type GetResult = CellHandle

  protected def cacheAskMaxDuration: scala.concurrent.duration.FiniteDuration

  override final protected def getCell(aggregateRootId: JUUID): AlmFuture[GetResult] =
    (cellCache ? GetCell(aggregateRootId, arTag.runtimeClass.asInstanceOf[Class[AggregateRoot[_, _]]]))(cacheAskMaxDuration).successfulAlmFuture[AggregateRootCellSourceResult].map { _.cellHandle }

  override final protected def onceWithGetResult[T](result: GetResult, f: (ActorRef) => AlmFuture[T]): AlmFuture[T] =
    result.onceWithCell(f)

}

object AggregateRootRepository {
  import almhirt.configuration._
  import com.typesafe.config._
  def props[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](cellCache: ActorRef, configSection: Config, theAlmhirt: Almhirt)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]): AlmValidation[Props] =
    for {
      cellAskMaxDuration <- configSection.v[FiniteDuration]("cell-ask-max-duration")
      cacheAskMaxDuration <- configSection.v[FiniteDuration]("cell-cache-ask-max-duration")
    } yield Props(
      new almhirt.domain.impl.AggregateRootRepositoryImpl[TAR, TEvent](theAlmhirt, cellCache, cellAskMaxDuration, cacheAskMaxDuration))

  def props[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](cellCache: ActorRef, configPath: String, theAlmhirt: Almhirt)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]): AlmValidation[Props] =
  	theAlmhirt.config.v[Config](configPath).flatMap(configSection =>
  	  props[TAR, TEvent](cellCache,configSection, theAlmhirt))

  def props[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent](cellCache: ActorRef, theAlmhirt: Almhirt)(implicit tagAr: ClassTag[TAR], tagE: ClassTag[TEvent]): AlmValidation[Props] =
  	  props[TAR, TEvent](cellCache,"almhirt.repositories", theAlmhirt)
  	  
}