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
    pathPrefix(Segment) { eventId =>
      pathEnd {
        parameter('uuid ?) { uuid =>
          get {
            implicit ctx =>
              val fut =
                for {
                  validatedEventId <- AlmFuture.completed {
                    (uuid.map(_.toLowerCase()) match {
                      case Some("true") =>
                        almhirt.converters.MiscConverters.uuidStrToBase64Str(eventId)
                      case Some("false") =>
                        eventId.success
                      case Some(x) =>
                        BadDataProblem(s""""?uuid=$x" is not allowed for ?uuid. Only "true" or "false".""").failure
                      case None =>
                        eventId.success
                    }).flatMap(ValidatedEventId(_))
                  }
                  res <- (httpEventLogEndpointParams.eventLog ? EventLog.FindEvent(validatedEventId))(httpEventLogEndpointParams.maxQueryDuration)
                    .mapCastTo[EventLog.FindEventResponse].collectV {
                      case EventLog.FoundEvent(id, Some(event)) =>
                        event.success
                      case EventLog.FoundEvent(id, None) =>
                        NotFoundProblem(s"""No event found with id "${id.value}. It might appear later.""").failure
                      case EventLog.FindEventFailed(id, problem) =>
                        problem.failure
                    }
                } yield res
              fut.completeRequestOk
          }
        }
      }
    } ~
      get {
        parameters('from ?, 'to?, 'skip ?, 'take ?) { (from, to, skip, take) =>
          implicit ctx =>
            val fut =
              for {
                (eventLogMessage) <- AlmFuture.completed {
                  for {
                    from <- from.map(_.toLocalDateTimeAlm.map(x => LocalDateTimeRange.From(x))) getOrElse LocalDateTimeRange.BeginningOfTime.success
                    to <- to.map(_.toLocalDateTimeAlm.map(x => LocalDateTimeRange.To(x))) getOrElse LocalDateTimeRange.EndOfTime.success
                    skip <- skip.map(_.toIntAlm.map(TraverseWindow.Skip(_))) getOrElse TraverseWindow.SkipNone.success
                    take <- take.map(_.toIntAlm.map(TraverseWindow.Take(_))) getOrElse TraverseWindow.TakeAll.success
                  } yield {
                    EventLog.FetchEvents(LocalDateTimeRange(from, to), TraverseWindow(skip, take))
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