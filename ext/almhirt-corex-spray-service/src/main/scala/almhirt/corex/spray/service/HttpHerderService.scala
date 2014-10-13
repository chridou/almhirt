package almhirt.corex.spray.service

import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.akkax._
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
    pathPrefix("circuits") {
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
            val fut = (herder ? HerderMessage.ReportCircuitStates)(maxCallDuration).mapCastTo[HerderMessage.CircuitStates].map(_.states)
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
            herder ! HerderMessage.AttemptCloseCircuit(componentName)
            complete(StatusCodes.Accepted, s"attempting to close circuit $componentName")
          }
        }
      } ~ pathPrefix("attempt-remove-fuse" / Segment) { componentName =>
        pathEnd {
          get {
            val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
            herder ! HerderMessage.RemoveFuseFromCircuit(componentName)
            complete(StatusCodes.Accepted, s"attempting to remove fuse in circuit $componentName")
          }
        }
      } ~ pathPrefix("attempt-destroy-fuse" / Segment) { componentName =>
        pathEnd {
          get {
            val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
            herder ! HerderMessage.DestroyFuseInCircuit(componentName)
            complete(StatusCodes.Accepted, s"attempting to destry fuse in circuit $componentName")
          }
        }
      }
    }
  }

  import scala.xml._
  private def createEventSinkHubUi(state: Map[String, CircuitState]) = {
    def createStateItem(state: CircuitState) = {
      state match {
        case x: CircuitState.Closed =>
          <td style="background-color:#19E448">{ x.toString }</td>
        case x: CircuitState.HalfOpen =>
          <td style="background-color:#EE8C14">{ x.toString }</td>
        case x: CircuitState.Open =>
          <td style="background-color:#E41B1B">{ x.toString }</td>
        case x: CircuitState.FuseRemoved =>
          <td style="background-color:#E41B1B">{ x.toString }</td>
        case x: CircuitState.FuseDestroyed =>
          <td style="background-color:#E41B1B">{ x.toString }</td>
      }
    }

    def createStateResetAction(name: String, state: CircuitState) = {
      state match {
        case st: CircuitState.AllWillFailState =>
          val att = new UnprefixedAttribute("href", s"./circuits/attempt-reset/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("reset"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createStateRemoveAction(name: String, state: CircuitState) = {
      state match {
        case x: CircuitState.Open =>
          val att = new UnprefixedAttribute("href", s"./circuits/attempt-remove-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.HalfOpen =>
          val att = new UnprefixedAttribute("href", s"./circuits/attempt-remove-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.Closed =>
          val att = new UnprefixedAttribute("href", s"./circuits/attempt-remove-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createStateDestroyAction(name: String, state: CircuitState) = {
      state match {
        case x: CircuitState.Open =>
          val att = new UnprefixedAttribute("href", s"./circuits/attempt-destroy-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.HalfOpen =>
          val att = new UnprefixedAttribute("href", s"./circuits/attempt-destroy-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.Closed =>
          val att = new UnprefixedAttribute("href", s"./circuits/attempt-destroy-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.FuseRemoved =>
          val att = new UnprefixedAttribute("href", s"./circuits/attempt-destroy-fuse/$name", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createRow(name: String, state: CircuitState) = {
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
        <title>Circuits</title>
      </head>
      <body>
        <table border="0">
          <tr>
            <th>Circuit Name</th>
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
