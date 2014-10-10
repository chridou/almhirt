package almhirt.corex.spray.service

import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.akkax.ExtendedExecutionContextSelector
import almhirt.akkax.AlmCircuitBreaker
import almhirt.herder.HerderMessage
import almhirt.context.AlmhirtContext
import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.context.HasAlmhirtContext
import almhirt.httpx.spray.service.AlmHttpProblemTerminator
import spray.routing.Directives
import spray.httpx.marshalling.Marshaller
import spray.http.StatusCodes

object HttpHerderService {
  final case class HttpHerderServiceParams(
    maxCallDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    problemMarshaller: Marshaller[Problem])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(Marshaller[Problem]) => HttpHerderServiceParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section <- ctx.config.v[Config]("almhirt.http.endpoints.herder-service")
      maxCallDuration <- section.v[FiniteDuration]("max-call-duration")
      selector <- section.v[ExtendedExecutionContextSelector]("execution-context-selector")
    } yield {
      (problemMarshaller: Marshaller[Problem]) => HttpHerderServiceParams(maxCallDuration, selector, problemMarshaller)
    }
  }

}

trait HttpHerderService extends Directives { me: Actor with AlmHttpEndpoint with HasAlmhirtContext =>
  import almhirt.components.EventSinkHubMessage

  def httpHerderServiceParams: HttpHerderService.HttpHerderServiceParams

  implicit private lazy val execCtx = httpHerderServiceParams.exectionContextSelector.select(me.almhirtContext, me.context)
  implicit private lazy val problemMarshaller = httpHerderServiceParams.problemMarshaller
  private val maxCallDuration = httpHerderServiceParams.maxCallDuration

  val herderTerminator = pathPrefix("herder") {
    pathPrefix("circuit-breakers") {
      pathEnd {
        parameter('ui.?) { uiEnabledP =>
          val isUiEnabled =
            uiEnabledP match {
              case None => false
              case Some(str) =>
                str.trim().isEmpty || str.trim() == "true"
            }
          get { ctx =>
            val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
            val fut = (herder ? HerderMessage.ReportCircuitBreakerStates)(maxCallDuration).mapCastTo[HerderMessage.CircuitBreakerStates].map(_.states)
            fut.fold(
              problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
              states => if (isUiEnabled) {
                ctx.complete(StatusCodes.OK, createEventSinkHubUi(states))
              } else {
                ctx.complete(StatusCodes.OK, states.map { case (name, state) => s"$name -> $state" }.mkString("\n"))
              })
          }
        }
      } ~ pathPrefix("attempt-close" / Segment) { componentName =>
        pathEnd {
          get {
            val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
            herder ! HerderMessage.AttemptCloseCircuitBreaker(componentName)
            complete(StatusCodes.Accepted, s"attempting to close circuit breaker $componentName")
          }
        }
      } ~ pathPrefix("attempt-remove-fuse" / Segment) { componentName =>
        pathEnd {
          get {
            val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
            herder ! HerderMessage.RemoveFuseFromCircuitBreaker(componentName)
            complete(StatusCodes.Accepted, s"attempting to remove fuse in circuit breaker $componentName")
          }
        }
      } ~ pathPrefix("attempt-destroy-fuse" / Segment) { componentName =>
        pathEnd {
          get {
            val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
            herder ! HerderMessage.DestroyFuseInCircuitBreaker(componentName)
            complete(StatusCodes.Accepted, s"attempting to destry fuse in circuit breaker $componentName")
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
          <td style="background-color:#19E448">{ x.toString }</td>
        case x: AlmCircuitBreaker.HalfOpen =>
          <td style="background-color:#EE8C14">{ x.toString }</td>
        case x: AlmCircuitBreaker.Open =>
          <td style="background-color:#E41B1B">{ x.toString }</td>
        case x: AlmCircuitBreaker.FuseRemoved =>
          <td style="background-color:#E41B1B">{ x.toString }</td>
        case x: AlmCircuitBreaker.FuseDestroyed =>
          <td style="background-color:#E41B1B">{ x.toString }</td>
      }
    }

    def createStateResetAction(name: String, state: AlmCircuitBreaker.State) = {
      state match {
        case x: AlmCircuitBreaker.Open =>
          val att = new UnprefixedAttribute("href", s"./circuit-breakers/attempt-reset/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("reset"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createStateRemoveAction(name: String, state: AlmCircuitBreaker.State) = {
      state match {
        case x: AlmCircuitBreaker.Open =>
          val att = new UnprefixedAttribute("href", s"./circuit-breakers/attempt-remove-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: AlmCircuitBreaker.HalfOpen =>
          val att = new UnprefixedAttribute("href", s"./circuit-breakers/attempt-remove-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: AlmCircuitBreaker.Closed =>
          val att = new UnprefixedAttribute("href", s"./circuit-breakers/attempt-remove-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createStateDestroyAction(name: String, state: AlmCircuitBreaker.State) = {
      state match {
        case x: AlmCircuitBreaker.Open =>
          val att = new UnprefixedAttribute("href", s"./circuit-breakers/attempt-destroy-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: AlmCircuitBreaker.HalfOpen =>
          val att = new UnprefixedAttribute("href", s"./circuit-breakers/attempt-destroy-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: AlmCircuitBreaker.Closed =>
          val att = new UnprefixedAttribute("href", s"./circuit-breakers/attempt-destroy-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: AlmCircuitBreaker.FuseRemoved =>
          val att = new UnprefixedAttribute("href", s"./circuit-breakers/attempt-destroy-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createRow(name: String, state: AlmCircuitBreaker.State) = {
      <tr>
        <td>{ name }</td>
        { createStateItem(state) }
        { createStateResetAction(name, state) }
        { createStateRemoveAction(name, state) }
        { createStateDestroyAction(name, state) }
      </tr>
    }

    <html>
      <head>
        <title>Event Sink Hub Status</title>
      </head>
      <body>
        <table border="0">
          <tr>
            <th>Circuit Breaker</th>
            <th>Circuit State</th>
            <th colspan="3">Actions</th>
          </tr>
          { state.map { case (name, state) => createRow(name, state) } }
        </table>
        <br>{ almhirtContext.getUtcTimestamp.toString }</br>
      </body>
    </html>
  }
}
