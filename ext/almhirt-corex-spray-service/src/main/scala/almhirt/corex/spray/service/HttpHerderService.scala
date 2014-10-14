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
    pathEnd {
      get { ctx =>
        val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
        val futCircuits = (herder ? HerderMessage.ReportCircuitStates)(maxCallDuration).mapCastTo[HerderMessage.CircuitStates].map(_.states)
        val futMissedEvents = (herder ? HerderMessage.ReportMissedEvents)(maxCallDuration).mapCastTo[HerderMessage.MissedEvents].map(_.missed)
        val futHtml =
          for {
            circuitStates <- futCircuits
            missedEvents <- futMissedEvents
          } yield createStatusReport(circuitStates, missedEvents)
        futHtml.fold(
          problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
          html => ctx.complete(StatusCodes.OK, html))
      }
    } ~ pathPrefix("circuits") {
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
                ctx.complete(StatusCodes.OK, createCircuitsUi(states))
              } else {
                ctx.complete(StatusCodes.OK, states.map { case (name, state) => s"$name -> $state" }.mkString("\n"))
              })
          }
        }
      } ~ pathPrefix(Segment) { componentName =>
        pathPrefix("attempt-reset") {
          pathEnd {
            get { ctx =>
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              herder ! HerderMessage.AttemptCloseCircuit(componentName)
              ctx.complete(StatusCodes.Accepted, s"attempting to close circuit $componentName")
            }
          }
        } ~ pathPrefix("remove-fuse") {
          pathEnd {
            get { ctx =>
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              herder ! HerderMessage.RemoveFuseFromCircuit(componentName)
              ctx.complete(StatusCodes.Accepted, s"attempting to remove fuse in circuit $componentName")
            }
          }
        } ~ pathPrefix("destroy-fuse") {
          pathEnd {
            get { ctx =>
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              herder ! HerderMessage.DestroyFuseInCircuit(componentName)
              ctx.complete(StatusCodes.Accepted, s"attempting to destroy fuse in circuit $componentName")
            }
          }
        }
      }
    } ~ pathPrefix("missed-events") {
      pathEnd {
        get { ctx =>
          val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
          val fut = (herder ? HerderMessage.ReportMissedEvents)(maxCallDuration).mapCastTo[HerderMessage.MissedEvents].map(_.missed)
          fut.fold(
            problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
            missed => ctx.complete(StatusCodes.OK, createMissedEventsReport(missed)))
        }
      }
    }
  }

  import scala.xml._
  private def createCircuitsUi(state: Map[String, CircuitState]) = {
    <html>
      <head>
        <title>Circuits</title>
      </head>
      <body>
        { createCircuitsContent(state, false) }
        <br><a href="../herder">Dashboard</a></br>
        <br>{ almhirtContext.getUtcTimestamp.toString }</br>
      </body>
    </html>
  }

  private def createCircuitsContent(state: Map[String, CircuitState], isReport: Boolean) = {
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
          val att = new UnprefixedAttribute("href", s"./circuits/$name/attempt-reset", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("reset"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createStateRemoveAction(name: String, state: CircuitState) = {
      state match {
        case x: CircuitState.Open =>
          val att = new UnprefixedAttribute("href", s"./circuits/$name/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.HalfOpen =>
          val att = new UnprefixedAttribute("href", s"./circuits/$name/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.Closed =>
          val att = new UnprefixedAttribute("href", s"./circuits/$name/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createStateDestroyAction(name: String, state: CircuitState) = {
      state match {
        case x: CircuitState.Open =>
          val att = new UnprefixedAttribute("href", s"./circuits/$name/destroy-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.HalfOpen =>
          val att = new UnprefixedAttribute("href", s"./circuits/$name/destroy-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.Closed =>
          val att = new UnprefixedAttribute("href", s"./circuits/$name/destroy-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.FuseRemoved =>
          val att = new UnprefixedAttribute("href", s"./circuits/$name/destroy-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createRow(name: String, state: CircuitState, isReport: Boolean) = {
      if (!isReport) {
        <tr>
          <td>{ name }</td>
          { createStateItem(state) }
          { createStateResetAction(name, state) }
          { createStateRemoveAction(name, state) }
          { createStateDestroyAction(name, state) }
        </tr>
      } else {
        <tr>
          <td>{ name }</td>
          { createStateItem(state) }
        </tr>
      }
    }

    if (isReport) {
      <table border="0">
        <tr>
          <th>Circuit Name</th>
          <th>Circuit State</th>
        </tr>
        { state.map { case (name, state) => createRow(name, state, true) } }
      </table>
    } else {
      <table border="0">
        <tr>
          <th>Circuit Name</th>
          <th>Circuit State</th>
          <th colspan="3">Actions</th>
        </tr>
        { state.map { case (name, state) => createRow(name, state, false) } }
      </table>
    }
  }

  def createMissedEventsReport(missedEvents: Map[String, (almhirt.problem.Severity, Int)]) = {
    <html>
      <head>
        <title>Missed Events</title>
      </head>
      <body>
        { createMissedEventsReportContent(missedEvents) }
        <br>{ almhirtContext.getUtcTimestamp.toString }</br>
      </body>
    </html>
  }

  def createMissedEventsReportContent(missedEvents: Map[String, (almhirt.problem.Severity, Int)]) = {
    def createSeverityItem(severity: almhirt.problem.Severity) = {
      severity match {
        case MinorSeverity => <td style="background-color:#F6EE09">Minor</td>
        case MajorSeverity => <td style="background-color:#F6A309">Major</td>
        case CriticalSeverity => <td style="background-color:#F61D09">Critical</td>
      }
    }
    <table border="1">
      <tr>
        <th>Component Name</th>
        <th>Severity</th>
        <th>Missed Events</th>
      </tr>
      {
        missedEvents.map {
          case (name, (severity, count)) =>
            <tr>
              <td>{ name }</td>
              { createSeverityItem(severity) }
              <td>{ count }</td>
            </tr>
        }
      }
    </table>
  }

  def createStatusReport(circuitsState: Map[String, CircuitState], missedEvents: Map[String, (almhirt.problem.Severity, Int)]) = {
    <html>
      <head>
        <title>Status Report</title>
      </head>
      <body>
        <h1>Status</h1>
        <h2>Circuits</h2>
        { createCircuitsContent(circuitsState, true) }
        <br><a href="./herder/circuits?ui">Circuit control</a></br>
        <h2>Missed Events</h2>
        { createMissedEventsReportContent(missedEvents) }
        <br>{ almhirtContext.getUtcTimestamp.toString }</br>
      </body>
    </html>
  }

}
