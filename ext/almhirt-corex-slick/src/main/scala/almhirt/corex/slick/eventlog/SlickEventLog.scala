package almhirt.corex.slick.eventlog

import scala.concurrent.ExecutionContext
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.eventlog.EventLog
import almhirt.messaging.MessagePublisher
import almhirt.problem.{ Problem, Major }

trait SlickEventLog extends EventLog with Actor with ActorLogging {
  type TRow <: EventLogRow

  implicit def canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes
  def messagePublisher: MessagePublisher

  def storeComponent: EventLogStoreComponent[TRow]

  def eventToRow(event: Event, channel: String): AlmValidation[TRow]
  def rowToEvent(row: TRow): AlmValidation[Event]

  private def rowsToEvents(rows: Seq[TRow]): AlmValidation[Seq[Event]] =
    rows.foldLeft(Seq.empty[Event].successAlm) { (accV, cur) =>
      accV match {
        case scalaz.Success(acc) =>
          rowToEvent(cur).map(acc :+ _)
        case scalaz.Failure(problem) =>
          problem.failure
      }
    }

  import almhirt.eventlog.EventLog._

  private def fetchEvents(query: () => AlmValidation[Seq[TRow]], sender: ActorRef): Unit = {
    (for {
      rows <- query()
      events <- rowsToEvents(rows)
    } yield events).fold(
      problem => throw new EscalatedProblemException(problem),
      events => sender ! EventsChunk(0, true, events))
  }

  import almhirt.corex.slick.TypeConversion._

  final protected def currentState(serializationChannel: String): Receive = {
    case LogEvent(event) =>
      (for {
        row <- eventToRow(event, serializationChannel)
        storeResult <- storeComponent.insertEventRow(row)
      } yield storeResult).fold(
        problem =>
          {
            messagePublisher.publish(FailureEvent(s"""Could not store event of type "${event.getClass().getName()}" with event id "${event.eventId.toString()}"""", problem, Major))
            throw new EscalatedProblemException(problem)
          },
        succ => ())

    case GetEvent(eventId) =>
      (for {
        row <- storeComponent.getEventRowById(eventId)
        event <- rowToEvent(row)
      } yield event).fold(
        problem => {
          problem match {
            case Problem(_, NotFoundProblem, _) => sender ! QueriedEvent(eventId, None)
            case p => throw new EscalatedProblemException(problem)
          }
        },
        event => sender ! QueriedEvent(eventId, Some(event)))

    case GetAllEvents =>
      fetchEvents(() => storeComponent.getAllEventRows, sender)

    case GetEventsFrom(from) =>
      fetchEvents(() => storeComponent.getAllEventRowsFrom(from), sender)

    case GetEventsAfter(after) =>
      fetchEvents(() => storeComponent.getAllEventRowsAfter(after), sender)

    case GetEventsTo(to) =>
      fetchEvents(() => storeComponent.getAllEventRowsTo(to), sender)

    case GetEventsUntil(until) =>
      fetchEvents(() => storeComponent.getAllEventRowsUntil(until), sender)

    case GetEventsFromTo(from, to) =>
      fetchEvents(() => storeComponent.getAllEventRowsFromTo(from, to), sender)

    case GetEventsFromUntil(from, until) =>
      fetchEvents(() => storeComponent.getAllEventRowsFromUntil(from, until), sender)

    case GetEventsAfterTo(after, to) =>
      fetchEvents(() => storeComponent.getAllEventRowsAfterTo(after, to), sender)

    case GetEventsAfterUntil(after, until) =>
      fetchEvents(() => storeComponent.getAllEventRowsAfterUntil(after, until), sender)

  }

}