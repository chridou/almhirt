package almhirt.corex.slick.domaineventlog

import scalaz.syntax.validation._
import scala.concurrent.duration._
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

  def writeWarnThreshold: FiniteDuration
  def readWarnThreshold: FiniteDuration

  var writeStatistics = DomainEventLogWriteStatistics()
  var readStatistics = DomainEventLogReadStatistics()
  var serializationStatistics = DomainEventLogSerializationStatistics()
  var deserializationStatistics = DomainEventLogSerializationStatistics()

  private def domainEventsToRows(domainEvents: Seq[DomainEvent], channel: String): AlmValidation[Seq[TRow]] = {
    val start = System.currentTimeMillis()
    val res = domainEvents.foldLeft(Seq.empty[TRow].successAlm) { (accV, cur) =>
      accV match {
        case scalaz.Success(acc) =>
          domainEventToRow(cur, channel).map(acc :+ _)
        case scalaz.Failure(problem) =>
          problem.failure
      }
    }
    val time = System.currentTimeMillis() - start
    serializationStatistics = serializationStatistics add time
    res
  }

  private def rowsToDomainEvents(rows: Seq[TRow]): AlmValidation[Seq[DomainEvent]] = {
    val start = System.currentTimeMillis()
    val res = rows.foldLeft(Seq.empty[DomainEvent].successAlm) { (accV, cur) =>
      accV match {
        case scalaz.Success(acc) =>
          rowToDomainEvent(cur).map(acc :+ _)
        case scalaz.Failure(problem) =>
          problem.failure
      }
    }
    val time = System.currentTimeMillis() - start
    deserializationStatistics = deserializationStatistics add time
    res
  }

  final protected def currentState(serializationChannel: String): Receive = {
    case CommitDomainEvents(events) =>
      (for {
        rows <- domainEventsToRows(events, serializationChannel)
        storeResult <- {
          val start = System.currentTimeMillis()
          val res = storeComponent.insertManyEventRows(rows)
          val time = System.currentTimeMillis() - start
          if (!events.isEmpty)
            writeStatistics = writeStatistics add time
          else
            writeStatistics addNoOp ()
          if (time > writeWarnThresholdMs)
            log.warning(s"""Writing ${events.size} events took longer than $writeWarnThresholdMs[ms]($time[ms]).""")
          res
        }
      } yield storeResult).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        succ => {
          sender ! CommittedDomainEvents(events)
          events.foreach(publishCommittedEvent)
        })

    case GetAllDomainEvents =>
      (for {
        rows <- storeComponent.getAllEventRows
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! FetchedDomainEventsBatch(domainEvents))

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
        rows <- {
          val deadline = readWarnThreshold.fromNow
          val res = storeComponent.getAllEventRowsFor(aggId)
          readStatistics = readStatistics add time
          if (deadline.isOverdue)
            log.warning(s"""Reading events(GetAllDomainEventsFor(aggId=$aggId)) took longer than $readWarnThresholdMs[ms]($time[ms]).""")
          res
        }
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! FetchedDomainEventsBatch(domainEvents))

    case GetDomainEventsFrom(aggId, fromVersion) =>
      (for {
        rows <- {
          val start = System.currentTimeMillis()
          val res = storeComponent.getAllEventRowsForFrom(aggId, fromVersion)
          val time = System.currentTimeMillis() - start
          readStatistics = readStatistics add time
          if (time > readWarnThresholdMs)
            log.warning(s"""Reading events(GetDomainEventsFrom(aggId=$aggId)) took longer than $readWarnThresholdMs[ms]($time[ms]).""")
          res
        }
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! FetchedDomainEventsBatch(domainEvents))

    case GetDomainEventsTo(aggId, toVersion) =>
      (for {
        rows <- {
          val start = System.currentTimeMillis()
          val res = storeComponent.getAllEventRowsForTo(aggId, toVersion)
          val time = System.currentTimeMillis() - start
          readStatistics = readStatistics add time
          if (time > readWarnThresholdMs)
            log.warning(s"""Reading events(GetDomainEventsTo(aggId=$aggId)) took longer than $readWarnThresholdMs[ms]($time[ms]).""")
          res
        }
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! FetchedDomainEventsBatch(domainEvents))

    case GetDomainEventsUntil(aggId, untilVersion) =>
      (for {
        rows <- {
          val start = System.currentTimeMillis()
          val res = storeComponent.getAllEventRowsForUntil(aggId, untilVersion)
          val time = System.currentTimeMillis() - start
          readStatistics = readStatistics add time
          if (time > readWarnThresholdMs)
            log.warning(s"""Reading events(GetDomainEventsUntil(aggId=$aggId)) took longer than $readWarnThresholdMs[ms]($time[ms]).""")
          res
        }
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! FetchedDomainEventsBatch(domainEvents))

    case GetDomainEventsFromTo(aggId, fromVersion, toVersion) =>
      (for {
        rows <- {
          val start = System.currentTimeMillis()
          val res = storeComponent.getAllEventRowsForFromTo(aggId, fromVersion, toVersion)
          val time = System.currentTimeMillis() - start
          readStatistics = readStatistics add time
          if (time > readWarnThresholdMs)
            log.warning(s"""Reading events(GetDomainEventsFromTo(aggId=$aggId)) took longer than $readWarnThresholdMs[ms]($time[ms]).""")
          res
        }
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! FetchedDomainEventsBatch(domainEvents))

    case GetDomainEventsFromUntil(aggId, fromVersion, untilVersion) =>
      (for {
        rows <- {
          val start = System.currentTimeMillis()
          val res = storeComponent.getAllEventRowsForFromUntil(aggId, fromVersion, untilVersion)
          val time = System.currentTimeMillis() - start
          readStatistics = readStatistics add time
          if (time > readWarnThresholdMs)
            log.warning(s"""Reading events(GetDomainEventsFromUntil(aggId=$aggId)) took longer than $readWarnThresholdMs[ms]($time[ms]).""")
          res
        }
        domainEvents <- rowsToDomainEvents(rows)
      } yield domainEvents).fold(
        problem => {
          throw new EscalatedProblemException(problem)
        },
        domainEvents => sender ! FetchedDomainEventsBatch(domainEvents))
    case almhirt.serialization.UseSerializationChannel(newChannel) =>
      context.become(currentState(newChannel))
  }
}