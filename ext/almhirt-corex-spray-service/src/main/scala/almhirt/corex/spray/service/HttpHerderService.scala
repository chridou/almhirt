package almhirt.corex.spray.service

import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.akkax.ExtendedExecutionContextSelector
import almhirt.akkax.AlmCircuitBreaker
import almhirt.context.AlmhirtContext
import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.context.HasAlmhirtContext
import almhirt.httpx.spray.service.AlmHttpProblemTerminator
import spray.routing.Directives
import spray.httpx.marshalling.Marshaller
import spray.http.StatusCodes

object HttpHerderService {
  final case class HttpHerderServiceParams(
    herder: ActorRef,
    maxCallDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    problemMarshaller: Marshaller[Problem])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(ActorRef, Marshaller[Problem]) => HttpHerderServiceParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section <- ctx.config.v[Config]("almhirt.http.endpoints.herder-service")
      maxCallDuration <- section.v[FiniteDuration]("max-call-duration")
      selector <- section.v[ExtendedExecutionContextSelector]("execution-context-selector")
    } yield {
      (herder: ActorRef, problemMarshaller: Marshaller[Problem]) => HttpHerderServiceParams(herder, maxCallDuration, selector, problemMarshaller)
    }
  }

}

trait HttpHerderService extends Directives { me: Actor with AlmHttpEndpoint with HasAlmhirtContext =>
  import almhirt.components.EventSinkHubMessage

  def httpHerderServiceParams: HttpHerderService.HttpHerderServiceParams

  implicit private lazy val execCtx = httpHerderServiceParams.exectionContextSelector.select(me.almhirtContext, me.context)
  implicit private lazy val problemMarshaller = httpHerderServiceParams.problemMarshaller
  private val herder = httpHerderServiceParams.herder
  private val maxCallDuration = httpHerderServiceParams.maxCallDuration

  val herderTerminator = pathPrefix("herder") {
    pathPrefix("event-sink-hub") {
      pathEnd {
        parameter('ui.?) { uiEnabledP =>
          val isUiEnabled =
            uiEnabledP match {
              case None => false
              case Some(str) =>
                str.trim().isEmpty || str.trim() == "true"
            }
          get { ctx =>
            val fut = (herder ? EventSinkHubMessage.ReportEventSinkStates)(maxCallDuration).mapCastTo[EventSinkHubMessage.ReportEventSinkStatesRsp].collectV({
              case EventSinkHubMessage.ReportEventSinkStatesFailed(prob) => scalaz.Failure(prob)
              case EventSinkHubMessage.EventSinkStates(states) => scalaz.Success(states)
            })
            fut.fold(
              problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
              status => if (isUiEnabled) {
                ctx.complete(StatusCodes.OK, createEventSinkHubUi(status))
              } else {
                ctx.complete(StatusCodes.OK, status.map { case (name, state) => s"$name -> $state" }.mkString("\n"))
              })
          }
        }
      } ~ pathPrefix("attempt-reset" / Segment) { componentName =>
        pathEnd {
          get {
            herder ! EventSinkHubMessage.AttemptResetComponentCircuit(componentName)
            complete(StatusCodes.Accepted, s"attempting to reset $componentName")
          }
        }
      }
    }
  }

  import scala.xml._
  private def createEventSinkHubUi(state: Map[String, AlmCircuitBreaker.State]) = {
    def createStateItem(state: AlmCircuitBreaker.State) = {
      state match {
        case x: AlmCircuitBreaker.Closed =>
          <td color="#19E448">{ x.toString }</td>
        case x: AlmCircuitBreaker.HalfOpen =>
          <td color="#EE8C14">{ x.toString }</td>
        case x: AlmCircuitBreaker.Open =>
          <td color="#E41B1B">{ x.toString }</td>
      }
    }

    def createStateAction(name: String, state: AlmCircuitBreaker.State) = {
      state match {
        case x: AlmCircuitBreaker.Open =>
          <td><a href="./attempt-reset/{name}">attempt reset</a></td>
        case _ =>
          <td>no action</td>
      }
    }

    def createRow(name: String, state: AlmCircuitBreaker.State) = {
      <tr>
        <td>name</td>
        { createStateItem(state) }
        { createStateAction(name, state) }
      </tr>
    }

    <html>
      <head>
        <title>Event Sink Hub Status</title>
      </head>
      <body>
        <table border="0">
          <tr>
            <th>Sink</th>
            <th>State</th>
            <th>Action</th>
          </tr>
          { state.map { case (name, state) => createRow(name, state) } }
        </table>
      </body>
    </html>
  }
}
