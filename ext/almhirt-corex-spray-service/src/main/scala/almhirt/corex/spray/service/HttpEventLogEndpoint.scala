package almhirt.corex.spray.service

import scala.language.postfixOps
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.httpx.spray.marshalling._
import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.akkax._
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.marshalling.Marshaller
import play.api.libs.iteratee._

object HttpEventLogQueryEndpoint {
  protected trait HttpEventLogQueryEndpointParams {
    def eventLog: ActorRef
    def maxQueryDuration: scala.concurrent.duration.FiniteDuration
    def exectionContextSelector: ExtendedExecutionContextSelector
    implicit def eventMarshaller: Marshaller[Event]
    implicit def eventsMarshaller: Marshaller[Seq[Event]]
  }
}

trait HttpEventLogQueryEndpoint extends Directives { me: Actor with AlmHttpEndpoint with HasProblemMarshaller with HasExecutionContexts =>
  import almhirt.eventlog.EventLog

  def httpEventLogQueryEndpointParams: HttpEventLogQueryEndpoint.HttpEventLogQueryEndpointParams

  implicit private lazy val execCtx = httpEventLogQueryEndpointParams.exectionContextSelector.select(me, me.context)
  implicit private val eventMarshaller = httpEventLogQueryEndpointParams.eventMarshaller
  implicit private val eventsMarshaller = httpEventLogQueryEndpointParams.eventsMarshaller

  val eventlogQueryTerminator = pathPrefix("event-log") {
    path(Segment) { id =>
      implicit ctx =>
        val fut = (httpEventLogQueryEndpointParams.eventLog ? EventLog.FindEvent(EventId(id)))(httpEventLogQueryEndpointParams.maxQueryDuration)
          .mapCastTo[EventLog.FindEventResponse].collectV {
            case EventLog.FoundEvent(id, Some(event)) =>
              event.success
            case EventLog.FoundEvent(id, None) =>
              NotFoundProblem(s"""No event found with id "${id.value}. It might appear later.""").failure
            case EventLog.FindEventFailed(id, problem) =>
              problem.failure
          }
        fut.completeRequestOk
    } ~
      get {
        parameters('from ?, 'to?, 'skip ?, 'take ?) { (from, to, skip, take) =>
          implicit ctx =>
            val fut =
              for {
                (eventLogMessage) <- AlmFuture.completed {
                  for {
                    from <- from.map(_.toLocalDateTimeAlm).validationOut
                    to <- to.map(_.toLocalDateTimeAlm).validationOut
                    skip <- skip.map(_.toIntAlm).validationOut
                    take <- take.map(_.toIntAlm).validationOut
                  } yield {
                    val msg: EventLog.EventLogMessage = (from, to) match {
                      case (Some(from), Some(to)) =>
                        EventLog.FetchEventsFromTo(from, to, skip, take)
                      case (Some(from), None) =>
                        EventLog.FetchEventsFrom(from, skip, take)
                      case (None, Some(to)) =>
                        EventLog.FetchEventsTo(to, skip, take)
                      case (None, None) =>
                        EventLog.FetchAllEvents(skip, take)
                    }
                  }
                }
                rsp <- (httpEventLogQueryEndpointParams.eventLog ? eventLogMessage)(httpEventLogQueryEndpointParams.maxQueryDuration).mapCastTo[EventLog.FetchEventsResponse]
                events <- rsp match {
                  case EventLog.FetchedEvents(enumerator) =>
                    enumerator.run(Iteratee.fold[Event, Vector[Event]](Vector.empty) { case (acc, elem) => acc :+ elem }).toAlmFuture
                  case EventLog.FetchEventsFailed(problem) =>
                    AlmFuture.failed(problem)
                }
              } yield events
            fut.completeRequestOk
        }
      }
  }
}