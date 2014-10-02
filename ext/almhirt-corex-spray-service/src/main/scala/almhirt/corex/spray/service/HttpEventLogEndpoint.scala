package almhirt.corex.spray.service

import scala.language.postfixOps
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.context.AlmhirtContext
import almhirt.httpx.spray.marshalling._
import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.akkax._
import almhirt.context.HasAlmhirtContext
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.marshalling.Marshaller
import play.api.libs.iteratee._

object HttpEventLogEndpoint {
  final case class HttpEventLogEndpointParams(
    eventLog: ActorRef,
    maxQueryDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    eventMarshaller: Marshaller[Event],
    eventsMarshaller: Marshaller[Seq[Event]],
    problemMarshaller: Marshaller[Problem])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(ActorRef, Marshaller[Event], Marshaller[Seq[Event]], Marshaller[Problem]) => HttpEventLogEndpointParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section <- ctx.config.v[Config]("almhirt.http.endpoints.event-log-endpoint")
      maxQueryDuration <- section.v[FiniteDuration]("max-query-duration")
      selector <- section.v[ExtendedExecutionContextSelector]("execution-context-selector")
    } yield {
      (eventLog: ActorRef, eventMarshaller: Marshaller[Event], eventsMarshaller: Marshaller[Seq[Event]], problemMarshaller: Marshaller[Problem]) =>
        HttpEventLogEndpointParams(eventLog, maxQueryDuration, selector, eventMarshaller, eventsMarshaller, problemMarshaller)
    }
  }
}

trait HttpEventLogEndpoint extends Directives { me: Actor with AlmHttpEndpoint with HasAlmhirtContext =>
  import almhirt.eventlog.EventLog

  def httpEventLogEndpointParams: HttpEventLogEndpoint.HttpEventLogEndpointParams

  implicit private lazy val execCtx = httpEventLogEndpointParams.exectionContextSelector.select(me.almhirtContext, me.context)
  implicit private val eventMarshaller = httpEventLogEndpointParams.eventMarshaller
  implicit private val eventsMarshaller = httpEventLogEndpointParams.eventsMarshaller
  implicit private val problemMarshaller = httpEventLogEndpointParams.problemMarshaller 

  val eventlogQueryTerminator = pathPrefix("event-log") {
    path(Segment) { id =>
      get {
        implicit ctx =>
          val fut = (httpEventLogEndpointParams.eventLog ? EventLog.FindEvent(EventId(id)))(httpEventLogEndpointParams.maxQueryDuration)
            .mapCastTo[EventLog.FindEventResponse].collectV {
              case EventLog.FoundEvent(id, Some(event)) =>
                event.success
              case EventLog.FoundEvent(id, None) =>
                NotFoundProblem(s"""No event found with id "${id.value}. It might appear later.""").failure
              case EventLog.FindEventFailed(id, problem) =>
                problem.failure
            }
          fut.completeRequestOk
      }
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
                rsp <- (httpEventLogEndpointParams.eventLog ? eventLogMessage)(httpEventLogEndpointParams.maxQueryDuration).mapCastTo[EventLog.FetchEventsResponse]
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