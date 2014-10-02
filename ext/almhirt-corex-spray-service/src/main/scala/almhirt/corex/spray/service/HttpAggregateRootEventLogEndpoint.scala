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
    aggragateRootEventLog: ActorRef,
    maxQueryDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    eventMarshaller: Marshaller[AggregateRootEvent],
    eventsMarshaller: Marshaller[Seq[AggregateRootEvent]])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(ActorRef, Marshaller[AggregateRootEvent], Marshaller[Seq[AggregateRootEvent]]) => HttpAggregateRootEventLogQueryEndpointParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section <- ctx.config.v[Config]("almhirt.http.endpoints.ggregate-root-event-log-endpoint")
      maxQueryDuration <- section.v[FiniteDuration]("max-query-duration")
      selector <- section.v[ExtendedExecutionContextSelector]("execution-context-selector")
    } yield {
      (aggragateRootEeventLog: ActorRef, eventMarshaller: Marshaller[AggregateRootEvent], eventsMarshaller: Marshaller[Seq[AggregateRootEvent]]) =>
        HttpAggregateRootEventLogQueryEndpointParams(aggragateRootEeventLog, maxQueryDuration, selector, eventMarshaller, eventsMarshaller)
    }
  }
}

trait HttpAggregateRootEventLogQueryEndpoint extends Directives { me: Actor with AlmHttpEndpoint with HasProblemMarshaller with HasAlmhirtContext =>
  import almhirt.eventlog.AggregateRootEventLog

  def httpAggregateRootEventLogQueryEndpointParams: HttpAggregateRootEventLogQueryEndpoint.HttpAggregateRootEventLogQueryEndpointParams

  implicit private lazy val execCtx = httpAggregateRootEventLogQueryEndpointParams.exectionContextSelector.select(me.almhirtContext, me.context)
  implicit private val eventMarshaller = httpAggregateRootEventLogQueryEndpointParams.eventMarshaller
  implicit private val eventsMarshaller = httpAggregateRootEventLogQueryEndpointParams.eventsMarshaller

  val AggregateRootEventLogQueryTerminator = pathPrefix("aggregate-root-event-log") {
    get {
      parameters('skip ?, 'take ?) { (skip, take) =>
        implicit ctx =>
          val fut = (httpAggregateRootEventLogQueryEndpointParams.aggragateRootEventLog ? AggregateRootEventLog.GetAllAggregateRootEvents)(httpAggregateRootEventLogQueryEndpointParams.maxQueryDuration)
            .mapCastTo[AggregateRootEventLog.GetManyAggregateRootEventsResponse].collectF {
              case AggregateRootEventLog.FetchedAggregateRootEvents(enumerator) =>
                enumerator.run(Iteratee.fold[AggregateRootEvent, Vector[AggregateRootEvent]](Vector.empty) { case (acc, elem) => acc :+ elem }).toAlmFuture
              case AggregateRootEventLog.GetAggregateRootEventsFailed(problem) =>
                AlmFuture.failed(problem)
            }
          fut.completeRequestOk
      }
    } ~
      path(Segment) { id =>
        get {
          implicit ctx =>
            val fut = (httpAggregateRootEventLogQueryEndpointParams.aggragateRootEventLog ? AggregateRootEventLog.GetAggregateRootEvent(EventId(id)))(httpAggregateRootEventLogQueryEndpointParams.maxQueryDuration)
              .mapCastTo[AggregateRootEventLog.GetAggregateRootEventResponse].collectV {
                case AggregateRootEventLog.FetchedAggregateRootEvent(id, Some(event)) =>
                  event.success
                case AggregateRootEventLog.FetchedAggregateRootEvent(id, None) =>
                  NotFoundProblem(s"""No aggregate root event found with id "${id.value}. It might appear later.""").failure
                case AggregateRootEventLog.GetAggregateRootEventFailed(id, problem) =>
                  problem.failure
              }
            fut.completeRequestOk
        }
      } ~ path("aggregate" / Segment) { unvalidatedId =>
        get {
          parameters('fromversion ?, 'toversion?, 'skip ?, 'take ?) { (fromVersion, toVersion, skip, take) =>
            implicit ctx =>
              val fut =
                for {
                  (eventLogMessage) <- AlmFuture.completed {
                    for {
                      fromVersion <- fromVersion.map(_.toLongAlm.map(AggregateRootVersion(_))).validationOut
                      toVersion <- toVersion.map(_.toLongAlm.map(AggregateRootVersion(_))).validationOut
                      id <- ValidatedAggregateRootId(unvalidatedId)
                      skip <- skip.map(_.toIntAlm).validationOut
                      take <- take.map(_.toIntAlm).validationOut
                    } yield {
                      val msg: AggregateRootEventLog.AggregateRootEventLogMessage = (fromVersion, toVersion) match {
                        case (Some(fromVersion), Some(toVersion)) =>
                          AggregateRootEventLog.GetAggregateRootEventsFromTo(id, fromVersion, toVersion)
                        case (Some(fromVersion), None) =>
                          AggregateRootEventLog.GetAggregateRootEventsFrom(id, fromVersion)
                        case (None, Some(toVersion)) =>
                          AggregateRootEventLog.GetAggregateRootEventsTo(id, toVersion)
                        case (None, None) =>
                          AggregateRootEventLog.GetAllAggregateRootEventsFor(id)
                      }
                    }
                  }
                  rsp <- (httpAggregateRootEventLogQueryEndpointParams.aggragateRootEventLog ? eventLogMessage)(httpAggregateRootEventLogQueryEndpointParams.maxQueryDuration).mapCastTo[AggregateRootEventLog.GetManyAggregateRootEventsResponse]
                  events <- rsp match {
                    case AggregateRootEventLog.FetchedAggregateRootEvents(enumerator) =>
                      enumerator.run(Iteratee.fold[AggregateRootEvent, Vector[AggregateRootEvent]](Vector.empty) { case (acc, elem) => acc :+ elem }).toAlmFuture
                    case AggregateRootEventLog.GetAggregateRootEventsFailed(problem) =>
                      AlmFuture.failed(problem)
                  }
                } yield events
              fut.completeRequestOk
          }
        }
      }
  }
}