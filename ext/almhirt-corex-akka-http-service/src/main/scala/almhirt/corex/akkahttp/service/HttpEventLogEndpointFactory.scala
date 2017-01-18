package almhirt.corex.akkahttp.service

import scala.language.postfixOps
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.context.AlmhirtContext
import almhirt.httpx.akkahttp.marshalling._
import almhirt.httpx.akkahttp.service.AlmHttpEndpoint
import almhirt.akkax._
import almhirt.context.HasAlmhirtContext
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshalling._
import play.api.libs.iteratee._


object HttpEventLogEndpointFactory {
  final case class HttpEventLogEndpointParams(
    eventLog: ActorRef,
    maxQueryDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    eventMarshaller: ToEntityMarshaller[Event],
    eventsMarshaller: ToEntityMarshaller[Seq[Event]],
    problemMarshaller: ToEntityMarshaller[Problem])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(ActorRef, ToEntityMarshaller[Event], ToEntityMarshaller[Seq[Event]], ToEntityMarshaller[Problem]) ⇒ HttpEventLogEndpointParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section ← ctx.config.v[Config]("almhirt.http.endpoints.event-log-endpoint")
      maxQueryDuration ← section.v[FiniteDuration]("max-query-duration")
      selector ← section.v[ExtendedExecutionContextSelector]("execution-context-selector")
    } yield {
      (eventLog: ActorRef, eventMarshaller: ToEntityMarshaller[Event], eventsMarshaller: ToEntityMarshaller[Seq[Event]], problemMarshaller: ToEntityMarshaller[Problem]) ⇒
        HttpEventLogEndpointParams(eventLog, maxQueryDuration, selector, eventMarshaller, eventsMarshaller, problemMarshaller)
    }
  }
}

trait HttpEventLogEndpointFactory extends Directives { me: Actor with AlmHttpEndpoint with HasAlmhirtContext ⇒
  import almhirt.eventlog.EventLog

  def createEventLogEndpoint(params: HttpEventLogEndpointFactory.HttpEventLogEndpointParams) = {

    implicit val execCtx = params.exectionContextSelector.select(me.almhirtContext, me.context)
    implicit val eventMarshaller = params.eventMarshaller
    implicit val eventsMarshaller = params.eventsMarshaller
    implicit val problemMarshaller = params.problemMarshaller

    pathPrefix("event-log") {
      pathPrefix(Segment) { eventId ⇒
        pathEnd {
          parameter('uuid ?) { uuid ⇒
            get {
              implicit ctx ⇒
                val fut =
                  for {
                    validatedEventId ← AlmFuture.completed {
                      (uuid.map(_.toLowerCase()) match {
                        case Some("true") ⇒
                          almhirt.converters.MiscConverters.uuidStringToBase64(eventId)
                        case Some("false") ⇒
                          eventId.success
                        case Some(x) ⇒
                          BadDataProblem(s""""?uuid=$x" is not allowed for ?uuid. Only "true" or "false".""").failure
                        case None ⇒
                          eventId.success
                      }).flatMap(ValidatedEventId(_))
                    }
                    res ← (params.eventLog ? EventLog.FindEvent(validatedEventId))(params.maxQueryDuration)
                      .mapCastTo[EventLog.FindEventResponse].collectV {
                        case EventLog.FoundEvent(id, Some(event)) ⇒
                          event.success
                        case EventLog.FoundEvent(id, None) ⇒
                          NotFoundProblem(s"""No event found with id "${id.value}. It might appear later.""").failure
                        case EventLog.FindEventFailed(id, problem) ⇒
                          problem.failure
                      }
                  } yield res
                fut.completeRequestOk
            }
          }
        }
      } ~
        get {
          parameters('from ?, 'to?, 'skip ?, 'take ?) { (from, to, skip, take) ⇒
            implicit ctx ⇒
              val fut =
                for {
                  (eventLogMessage) ← AlmFuture.completed {
                    for {
                      from ← from.map(_.toLocalDateTimeAlm.map(x ⇒ LocalDateTimeRange.From(x))) getOrElse LocalDateTimeRange.BeginningOfTime.success
                      to ← to.map(_.toLocalDateTimeAlm.map(x ⇒ LocalDateTimeRange.To(x))) getOrElse LocalDateTimeRange.EndOfTime.success
                      traverseWindow ← TraverseWindow.parseFromStringOptions(skip, take)
                    } yield {
                      EventLog.FetchEvents(LocalDateTimeRange(from, to), traverseWindow)
                    }
                  }
                  rsp ← (params.eventLog ? eventLogMessage)(params.maxQueryDuration).mapCastTo[EventLog.FetchEventsResponse]
                  events ← rsp match {
                    case EventLog.FetchedEvents(enumerator) ⇒
                      enumerator.run(Iteratee.fold[Event, Vector[Event]](Vector.empty) { case (acc, elem) ⇒ acc :+ elem }).toAlmFuture
                    case EventLog.FetchEventsFailed(problem) ⇒
                      AlmFuture.failed(problem)
                  }
                } yield events
              fut.completeRequestOk
          }
        }
    }
  }
}