package almhirt.corex.slick.domaineventlog

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.domaineventlog._
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
  
  def writeWarnThresholdMs: Long

  var writeStatistics = DomainEventLogWriteStatistics()

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
      (for {
        rows <- domainEventsToRows(events, serializationChannel)
        storeResult <- {
          val start = System.currentTimeMillis()
          val res = storeComponent.insertManyEventRows(rows)
          val time = System.currentTimeMillis()-start
          writeStatistics = writeStatistics.add(time)
          if(time > writeWarnThresholdMs)
            log.warning(s"""Writing ${events.size} events took longer the $writeWarnThresholdMs[ms].""")
          res
        }
      } yield storeResult).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        succ => sender ! CommittedDomainEvents(events))

    case GetAllDomainEvents =>
      (for {
        rows <- storeComponent.getAllEventRows
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEvent(eventId) =>
      (for {
        row <- storeComponent.getEventRowById(eventId)
        event <- rowToDomainEvent(row)
      } yield event).fold(
        problem => {
          problem match {
            case Problem(_, NotFoundProblem, _) => sender ! QueriedDomainEvent(eventId, None)
            case p =>
              throw new EscalatedProblemException(problem)
          }
        },
        event => sender ! QueriedDomainEvent(eventId, Some(event)))

    case GetAllDomainEventsFor(aggId) =>
      (for {
        rows <- storeComponent.getAllEventRowsFor(aggId)
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsFrom(aggId, fromVersion) =>
      (for {
        rows <- storeComponent.getAllEventRowsForFrom(aggId, fromVersion)
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsTo(aggId, toVersion) =>
      (for {
        rows <- storeComponent.getAllEventRowsForTo(aggId, toVersion)
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsUntil(aggId, untilVersion) =>
      (for {
        rows <- storeComponent.getAllEventRowsForUntil(aggId, untilVersion)
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsFromTo(aggId, fromVersion, toVersion) =>
      (for {
        rows <- storeComponent.getAllEventRowsForFromTo(aggId, fromVersion, toVersion)
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))

    case GetDomainEventsFromUntil(aggId, fromVersion, untilVersion) =>
      (for {
        rows <- storeComponent.getAllEventRowsForFromUntil(aggId, fromVersion, untilVersion)
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! DomainEventsChunk(0, true, domainEvents))
    case almhirt.serialization.UseSerializationChannel(newChannel) =>
      context.become(currentState(newChannel))
  }
}