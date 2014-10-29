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
import almhirt.aggregates.{ ValidatedAggregateRootId, AggregateRootVersion }
import almhirt.context.HasAlmhirtContext
import almhirt.akkax._
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.marshalling.Marshaller
import play.api.libs.iteratee._

object HttpAggregateRootEventLogQueryEndpoint {
  final case class HttpAggregateRootEventLogQueryEndpointParams(
    aggregateRootEventLog: ActorRef,
    maxQueryDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    eventMarshaller: Marshaller[AggregateRootEvent],
    eventsMarshaller: Marshaller[Seq[AggregateRootEvent]],
    problemMarshaller: Marshaller[Problem])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(ActorRef, Marshaller[AggregateRootEvent], Marshaller[Seq[AggregateRootEvent]], Marshaller[Problem]) ⇒ HttpAggregateRootEventLogQueryEndpointParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section ← ctx.config.v[Config]("almhirt.http.endpoints.aggregate-root-event-log-endpoint")
      maxQueryDuration ← section.v[FiniteDuration]("max-query-duration")
      selector ← section.v[ExtendedExecutionContextSelector]("execution-context-selector")
    } yield {
      (aggragateRootEeventLog: ActorRef, aggregateRootEventMarshaller: Marshaller[AggregateRootEvent], aggregateRootEventsMarshaller: Marshaller[Seq[AggregateRootEvent]], problemMarshaller: Marshaller[Problem]) ⇒
        HttpAggregateRootEventLogQueryEndpointParams(aggragateRootEeventLog, maxQueryDuration, selector, aggregateRootEventMarshaller, aggregateRootEventsMarshaller, problemMarshaller)
    }
  }
}

trait HttpAggregateRootEventLogQueryEndpoint extends Directives { me: Actor with AlmHttpEndpoint with HasAlmhirtContext ⇒
  import almhirt.eventlog.AggregateRootEventLog

  def httpAggregateRootEventLogQueryEndpointParams: HttpAggregateRootEventLogQueryEndpoint.HttpAggregateRootEventLogQueryEndpointParams

  implicit private lazy val execCtx = httpAggregateRootEventLogQueryEndpointParams.exectionContextSelector.select(me.almhirtContext, me.context)
  implicit private val eventMarshaller = httpAggregateRootEventLogQueryEndpointParams.eventMarshaller
  implicit private val eventsMarshaller = httpAggregateRootEventLogQueryEndpointParams.eventsMarshaller
  implicit private val problemMarshaller = httpAggregateRootEventLogQueryEndpointParams.problemMarshaller

  val aggregateRootEventLogQueryTerminator = pathPrefix("aggregate-root-event-log") {
    pathEnd {
      get {
        parameters('skip ?, 'take ?) { (skip, take) ⇒
          implicit ctx ⇒
            val fut =
              for {
                eventLogMessage ← AlmFuture.completed {
                  for {
                    traverseWindow ← TraverseWindow.parseFromStringOptions(skip, take)
                  } yield {
                    AggregateRootEventLog.GetAllAggregateRootEvents(traverseWindow)
                  }
                }
                rsp ← (httpAggregateRootEventLogQueryEndpointParams.aggregateRootEventLog ? eventLogMessage)(httpAggregateRootEventLogQueryEndpointParams.maxQueryDuration).mapCastTo[AggregateRootEventLog.GetManyAggregateRootEventsResponse]
                events ← rsp match {
                  case AggregateRootEventLog.FetchedAggregateRootEvents(enumerator) ⇒
                    enumerator.run(Iteratee.fold[AggregateRootEvent, Vector[AggregateRootEvent]](Vector.empty) { case (acc, elem) ⇒ acc :+ elem }).toAlmFuture
                  case AggregateRootEventLog.GetAggregateRootEventsFailed(problem) ⇒
                    AlmFuture.failed(problem)
                }
              } yield events
            fut.completeRequestOk
        }
      }
    } ~
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
                    res ← (httpAggregateRootEventLogQueryEndpointParams.aggregateRootEventLog ? AggregateRootEventLog.GetAggregateRootEvent(validatedEventId))(httpAggregateRootEventLogQueryEndpointParams.maxQueryDuration)
                      .mapCastTo[AggregateRootEventLog.GetAggregateRootEventResponse].collectV {
                        case AggregateRootEventLog.FetchedAggregateRootEvent(id, Some(event)) ⇒
                          event.success
                        case AggregateRootEventLog.FetchedAggregateRootEvent(id, None) ⇒
                          NotFoundProblem(s"""No aggregate root event found with event id "${id.value}. It might appear later.""").failure
                        case AggregateRootEventLog.GetAggregateRootEventFailed(id, problem) ⇒
                          problem.failure
                      }
                  } yield res
                fut.completeRequestOk
            }
          }
        }
      } ~ pathPrefix("aggregate" / Segment) { unvalidatedAggId ⇒
        pathEnd {
          get {
            parameters('fromversion ?, 'toversion?, 'skip ?, 'take ?, 'uuid ?) { (fromVersion, toVersion, skip, take, uuid) ⇒
              implicit ctx ⇒
                import AggregateRootEventLog._
                val fut =
                  for {
                    (eventLogMessage) ← AlmFuture.completed {
                      for {
                        validatedAggIdId ← (uuid.map(_.toLowerCase()) match {
                          case Some("true") ⇒
                            almhirt.converters.MiscConverters.uuidStringToBase64(unvalidatedAggId)
                          case Some("false") ⇒
                            unvalidatedAggId.success
                          case Some(x) ⇒
                            BadDataProblem(s""""?uuid=$x" is not allowed for ?uuid. Only "true" or "false".""").failure
                          case None ⇒
                            unvalidatedAggId.success
                        }).flatMap(ValidatedAggregateRootId(_))
                        fromVersion ← AggregateRootEventLog.VersionRangeStartMarker.parseStringOption(fromVersion)
                        toVersion ← AggregateRootEventLog.VersionRangeEndMarker.parseStringOption(toVersion)
                        traverseWindow ← TraverseWindow.parseFromStringOptions(skip, take)
                      } yield {
                        AggregateRootEventLog.GetAggregateRootEventsFor(validatedAggIdId, fromVersion, toVersion, traverseWindow)
                      }
                    }
                    rsp ← (httpAggregateRootEventLogQueryEndpointParams.aggregateRootEventLog ? eventLogMessage)(httpAggregateRootEventLogQueryEndpointParams.maxQueryDuration).mapCastTo[AggregateRootEventLog.GetManyAggregateRootEventsResponse]
                    events ← rsp match {
                      case AggregateRootEventLog.FetchedAggregateRootEvents(enumerator) ⇒
                        enumerator.run(Iteratee.fold[AggregateRootEvent, Vector[AggregateRootEvent]](Vector.empty) { case (acc, elem) ⇒ acc :+ elem }).toAlmFuture
                      case AggregateRootEventLog.GetAggregateRootEventsFailed(problem) ⇒
                        AlmFuture.failed(problem)
                    }
                  } yield events
                fut.completeRequestOk
            }
          }
        }
      }
  }
}