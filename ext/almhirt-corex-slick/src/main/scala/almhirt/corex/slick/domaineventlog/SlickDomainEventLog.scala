package almhirt.corex.slick.domaineventlog

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.domaineventlog.DomainEventLog
import almhirt.messaging.MessagePublisher
import scala.concurrent.ExecutionContext
import almhirt.problem.Problem

trait SlickDomainEventLog extends DomainEventLog { actor: Actor with ActorLogging =>
  import DomainEventLog._

  type TRow <: DomainEventLogRow

  def messagePublisher: MessagePublisher

  def storeComponent: DomainEventLogStoreComponent[TRow]

  def domainEventToRow(domainEvent: DomainEvent, channel: String): AlmValidation[TRow]
  def rowToDomainEvent(row: TRow): AlmValidation[DomainEvent]

  private def domainEventsToRows(domainEvents: Seq[DomainEvent], channel: String): AlmValidation[Seq[TRow]] =
    domainEvents.foldLeft(Seq.empty[TRow].successAlm) { (accV, cur) =>
      accV match {
        case scalaz.Success(acc) =>
          domainEventToRow(cur, channel).map(acc :+ _)
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

  final protected def currentState(serializationChannel: String): Receive = {
    case CommitDomainEvents(events) =>
      val pinnedSender = sender
      (for {
        rows <- domainEventsToRows(events, serializationChannel)
        storeResult <- storeComponent.insertManyEventRows(rows)
      } yield storeResult).fold(
        problem => {
          pinnedSender ! CommittedDomainEvents(Seq.empty, Some((events, problem)))
        },
        succ => pinnedSender ! CommittedDomainEvents(events, None))

    case GetAllDomainEvents =>
      val pinnedSender = sender
      (for {
          rows <- storeComponent.getAllEventRows
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      ).fold(
        problem => {
          pinnedSender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => pinnedSender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEvent(eventId) =>
      val pinnedSender = sender
      (for {
          row <- storeComponent.getEventRowById(eventId)
          event <- rowToDomainEvent(row)
        } yield event
      ).fold(
        problem => {
          problem match {
            case Problem(_, NotFoundProblem, _) => pinnedSender ! QueriedDomainEvent(eventId, None)
            case p => pinnedSender ! DomainEventQueryFailed(eventId, p)
          }
        },
        event => pinnedSender ! QueriedDomainEvent(eventId, Some(event)))

    case GetAllDomainEventsFor(aggId) =>
      val pinnedSender = sender
      (for {
          rows <- storeComponent.getAllEventRowsFor(aggId)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      ).fold(
        problem => {
          pinnedSender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => pinnedSender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsFrom(aggId, fromVersion) =>
      val pinnedSender = sender
      (for {
          rows <- storeComponent.getAllEventRowsForFrom(aggId, fromVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      ).fold(
        problem => {
          pinnedSender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => pinnedSender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsTo(aggId, toVersion) =>
      val pinnedSender = sender
      (for {
          rows <- storeComponent.getAllEventRowsForTo(aggId, toVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      ).fold(
        problem => {
          pinnedSender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => pinnedSender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsUntil(aggId, untilVersion) =>
      val pinnedSender = sender
      (for {
          rows <- storeComponent.getAllEventRowsForUntil(aggId, untilVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      ).fold(
        problem => {
          pinnedSender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => pinnedSender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsFromTo(aggId, fromVersion, toVersion) =>
      val pinnedSender = sender
      (for {
          rows <- storeComponent.getAllEventRowsForFromTo(aggId, fromVersion, toVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      ).fold(
        problem => {
          pinnedSender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => pinnedSender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsFromUntil(aggId, fromVersion, untilVersion) =>
      val pinnedSender = sender
      (for {
          rows <- storeComponent.getAllEventRowsForFromUntil(aggId, fromVersion, untilVersion)
          domainEvents <- rowsToDomainEvents(rows)
        } yield domainEvents
      ).fold(
        problem => {
          pinnedSender ! DomainEventsChunkFailure(0, problem)
        },
        domainEvents => pinnedSender ! DomainEventsChunk(0, true, domainEvents))
    case almhirt.serialization.UseSerializationChannel(newChannel) =>
      context.become(currentState(newChannel))
  }
}