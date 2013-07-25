package almhirt.ext.core.slick.domaineventlog

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.domaineventlog.DomainEventLog
import almhirt.messaging.MessagePublisher
import scala.concurrent.ExecutionContext

trait SlickDomainEventLog extends DomainEventLog { actor: Actor with ActorLogging =>
  import DomainEventLog._

  type TRow <: DomainEventLogRow

  def messagePublisher: MessagePublisher
  
  def storeComponent: DomainEventLogStoreComponent[TRow]

  implicit def syncIoExecutionContext: ExecutionContext

  def domainEventToRow(domainEvent: DomainEvent): AlmValidation[TRow]
  def rowToDomainEvent(row: TRow): AlmValidation[DomainEvent]

  private def domainEventsToRows(domainEvents: Seq[DomainEvent]): AlmValidation[Seq[TRow]] =
    domainEvents.foldLeft(Seq.empty[TRow].successAlm) { (accV, cur) =>
      accV match {
        case scalaz.Success(acc) =>
          domainEventToRow(cur).map(acc :+ _)
        case scalaz.Failure(problem) =>
          problem.failure
      }
    }

  private def rowsToDomainEvents(rows: Seq[TRow]): AlmValidation[Seq[DomainEvent]] =
    rows.foldLeft(Seq.empty[DomainEvent].successAlm) { (accV, cur) =>
      accV match {
        case scalaz.Success(acc) =>
          rowToDomainEvent(cur).map(acc :+ _)
        case scalaz.Failure(problem) =>
          problem.failure
      }
    }

  final protected def receiveDomainEventLogMsg: Receive = {
    case CommitDomainEvents(events) =>
      AlmFuture {
        for {
          rows <- domainEventsToRows(events)
          storeResult <- storeComponent.insertManyEventRows(rows)
        } yield storeResult
      }.onComplete(
        problem => {
          sender ! CommittedDomainEvents(Seq.empty, Some((events, problem)))
        },
        succ => sender ! CommittedDomainEvents(events, None))
    case GetAllDomainEvents =>
      AlmFuture {
        for {
          rows <- storeComponent.getAllEventRows
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      }.onComplete(
        problem => {
          sender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))
    case GetDomainEvent(eventId) =>
      AlmFuture {
        for {
          row <- storeComponent.getEventRowById(eventId)
          event <- rowToDomainEvent(row)
        } yield event
      }.onComplete(
        problem => {
          problem match {
            case p @ NotFoundProblem => sender ! QueriedDomainEvent(eventId, None)
            case p => sender ! DomainEventQueryFailed(eventId, p)
          }
        },
        event => sender ! QueriedDomainEvent(eventId, Some(event)))
    case GetAllDomainEventsFor(aggId) =>
      AlmFuture {
        for {
          rows <- storeComponent.getAllEventRowsFor(aggId)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      }.onComplete(
        problem => {
          sender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))
    case GetDomainEventsFrom(aggId, fromVersion) =>
      AlmFuture {
        for {
          rows <- storeComponent.getAllEventRowsForFrom(aggId, fromVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      }.onComplete(
        problem => {
          sender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))
    case GetDomainEventsTo(aggId, toVersion) =>
      AlmFuture {
        for {
          rows <- storeComponent.getAllEventRowsForTo(aggId, toVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      }.onComplete(
        problem => {
          sender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))
    case GetDomainEventsUntil(aggId, untilVersion) =>
      AlmFuture {
        for {
          rows <- storeComponent.getAllEventRowsForUntil(aggId, untilVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      }.onComplete(
        problem => {
          sender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))
    case GetDomainEventsFromTo(aggId, fromVersion, toVersion) =>
      AlmFuture {
        for {
          rows <- storeComponent.getAllEventRowsForFromTo(aggId, fromVersion, toVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      }.onComplete(
        problem => {
          sender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))
    case GetDomainEventsFromUntil(aggId, fromVersion, untilVersion) =>
      AlmFuture {
        for {
          rows <- storeComponent.getAllEventRowsForFromUntil(aggId, fromVersion, untilVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      }.onComplete(
        problem => {
          sender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))
  }
}