package almhirt.corex.spray.service

import org.joda.time.LocalDateTime
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.problem._
import almhirt.almfuture.all._
import almhirt.akkax._
import almhirt.herder._
import almhirt.herder.HerderMessages._
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
    pathPrefix("ui") {
      pathEnd {
        get { ctx =>
          ctx.complete("Cool app!")
        }
      }
    } ~
      pathEnd {
        get { ctx =>
          val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
          val futCircuits = (herder ? CircuitMessages.ReportCircuitStates)(maxCallDuration).mapCastTo[CircuitMessages.CircuitStates].map(_.states)
          val futMissedEvents = (herder ? EventMessages.ReportMissedEvents)(maxCallDuration).mapCastTo[EventMessages.MissedEvents].map(_.missedEvents)
          val futFailures = (herder ? FailureMessages.ReportFailures)(maxCallDuration).mapCastTo[FailureMessages.ReportedFailures].map(_.failures)
          val futHtml =
            for {
              circuitStates <- futCircuits
              missedEvents <- futMissedEvents
              failures <- futFailures
            } yield createStatusReport(circuitStates, missedEvents, failures, "./herder")
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
              val fut = (herder ? CircuitMessages.ReportCircuitStates)(maxCallDuration).mapCastTo[CircuitMessages.CircuitStates].map(_.states)
              fut.fold(
                problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                states => if (isUiEnabled) {
                  ctx.complete(StatusCodes.OK, createCircuitsUi(states))
                } else {
                  ctx.complete(StatusCodes.OK, states.map { case (name, state) => s"$name -> $state" }.mkString("\n"))
                })
            }
          }
        } ~ pathPrefix(Segment / Segment) { (appName, componentName) =>
          pathPrefix("attempt-reset") {
            pathEnd {
              get { ctx =>
                val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                herder ! CircuitMessages.AttemptCloseCircuit(ComponentId(AppName(appName), ComponentName(componentName)))
                ctx.complete(StatusCodes.Accepted, s"attempting to close circuit $componentName")
              }
            }
          } ~ pathPrefix("remove-fuse") {
            pathEnd {
              get { ctx =>
                val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                herder ! CircuitMessages.RemoveFuseFromCircuit(ComponentId(AppName(appName), ComponentName(componentName)))
                ctx.complete(StatusCodes.Accepted, s"attempting to remove fuse in circuit $componentName")
              }
            }
          } ~ pathPrefix("destroy-fuse") {
            pathEnd {
              get { ctx =>
                val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                herder ! CircuitMessages.DestroyFuseInCircuit(ComponentId(AppName(appName), ComponentName(componentName)))
                ctx.complete(StatusCodes.Accepted, s"attempting to destroy fuse in circuit $componentName")
              }
            }
          }
        }
      } ~ pathPrefix("missed-events") {
        pathEnd {
          get { ctx =>
            val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
            val fut = (herder ? EventMessages.ReportMissedEvents)(maxCallDuration).mapCastTo[EventMessages.MissedEvents].map(_.missedEvents)
            fut.fold(
              problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
              missed => ctx.complete(StatusCodes.OK, createMissedEventsReport(missed, "../herder")))
          }
        } ~ pathPrefix(Segment / Segment / IntNumber) { (appName, componentName, num) =>
          pathEnd {
            get { ctx =>
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              val fut = (herder ? EventMessages.ReportMissedEventsFor(ComponentId(AppName(appName), ComponentName(componentName))))(maxCallDuration).mapCastTo[EventMessages.ReportedMissedEventsFor]
              fut.fold(
                problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                res => res.missedEvents match {
                  case Some(e) => ctx.complete(StatusCodes.OK, createComponentMissedEventsReport(res.id, e, num, "../../../herder"))
                  case None => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, NotFoundProblem(s"No component with name ($appName/$componentName) found."))
                })
            }
          }
        }
      } ~ pathPrefix("failures") {
        pathEnd {
          get { ctx =>
            val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
            val fut = (herder ? FailureMessages.ReportFailures)(maxCallDuration).mapCastTo[FailureMessages.ReportedFailures].map(_.failures)
            fut.fold(
              problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
              entries => ctx.complete(StatusCodes.OK, createFailuresReport(entries, "../herder")))
          }
        } ~ pathPrefix(Segment / Segment / IntNumber) { (appName, componentName, num) =>
          pathEnd {
            get { ctx =>
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              val fut = (herder ? FailureMessages.ReportFailuresFor(ComponentId(AppName(appName), ComponentName(componentName))))(maxCallDuration).mapCastTo[FailureMessages.ReportedFailuresFor]
              fut.fold(
                problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                res => res.entry match {
                  case Some(e) => ctx.complete(StatusCodes.OK, createComponentFailuresReport(res.id, e, num, "../../../herder"))
                  case None => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, NotFoundProblem(s"No component with name ($appName/$componentName) found."))
                })
            }
          }
        }
      }
  }

  import scala.xml._
  private def createCircuitsUi(state: Seq[(ComponentId, CircuitState)]) = {
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

  private def createCircuitsContent(state: Seq[(ComponentId, CircuitState)], isReport: Boolean) = {
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

    def createStateResetAction(component: ComponentId, state: CircuitState) = {
      state match {
        case st: CircuitState.AllWillFailState =>
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/attempt-reset", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("reset"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createStateRemoveAction(component: ComponentId, state: CircuitState) = {
      state match {
        case x: CircuitState.Open =>
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.HalfOpen =>
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.Closed =>
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createStateDestroyAction(component: ComponentId, state: CircuitState) = {
      state match {
        case x: CircuitState.Open =>
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/destroy-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.HalfOpen =>
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/destroy-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.Closed =>
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/destroy-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.FuseRemoved =>
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/destroy-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy fuse"))
          <td>{ anchor }</td>
        case _ =>
          <td>no action</td>
      }
    }

    def createRow(component: ComponentId, state: CircuitState, isReport: Boolean) = {
      if (!isReport) {
        <tr>
          <td>{ component.app.value }</td>
          <td>{ component.component.value }</td>
          { createStateItem(state) }
          { createStateResetAction(component, state) }
          { createStateRemoveAction(component, state) }
          { createStateDestroyAction(component, state) }
        </tr>
      } else {
        <tr>
          <td>{ component.app.value }</td>
          <td>{ component.component.value }</td>
          { createStateItem(state) }
        </tr>
      }
    }

    if (isReport) {
      <table border="1">
        <tr>
          <th>App</th>
          <th>Component</th>
          <th>Circuit State</th>
        </tr>
        { state.map { case (component, state) => createRow(component, state, true) } }
      </table>
    } else {
      <table border="1">
        <tr>
          <th>App</th>
          <th>Component</th>
          <th colspan="3">Actions</th>
        </tr>
        { state.map { case (component, state) => createRow(component, state, false) } }
      </table>
    }
  }

  def createMissedEventsReport(missedEvents: Seq[(ComponentId, BadThingsHistory[MissedEventsEntry])], pathToHerder: String) = {
    <html>
      <head>
        <title>Missed Events</title>
      </head>
      <body>
        { createMissedEventsReportContent(missedEvents, false, pathToHerder) }
        <br/>
        { almhirtContext.getUtcTimestamp.toString }
      </body>
    </html>
  }

  def createMissedEventsReportContent(missedEvents: Seq[(ComponentId, BadThingsHistory[MissedEventsEntry])], abridged: Boolean, pathToHerder: String) = {
    def createHistoryLine(item: MissedEventsEntry) = {
      <tr>
        <td>{ item._4.toString() }</td>
        <td>{ s"${item._1.getClass().getSimpleName().toString}(${item._1.eventId.value})" }</td>
        <td>{ createSeverityItem(item._3) }</td>
        <td>{
          item._2 match {
            case CauseIsProblem(p) => p.problemType.toString()
            case CauseIsThrowable(HasAThrowable(exn)) => exn.getClass().getName()
            case CauseIsThrowable(HasAThrowableDescribed(className, _, _, _)) => className
          }
        }</td>
      </tr>
    }

    <table border="1">
      <tr>
        <th>App</th>
        <th>Component</th>
        <th>Missed Events</th>
        <th>Max Severity</th>
        {
          if (!abridged)
            <th>Last 3 missed events</th>
          else
            <th>Last missed event</th>
        }
        <th>more</th>
      </tr>
      {
        missedEvents.map {
          case (ComponentId(app, component), history) =>
            <tr>
              <td>{ app.value }</td>
              <td>{ component.value }</td>
              <td>{ history.occurencesCount }</td>
              <td>{ history.maxSeverity.map(createSeverityItem).getOrElse(<span>-</span>) }</td>
              {
                <td>
                  <table border="0">
                    { history.lastOccurences.take(if (abridged) 1 else 3).map(createHistoryLine) }
                  </table>
                </td>
              }
              <td>
                {
                  if (!abridged) {
                    {
                      val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events/${app.value}/${component.value}/1", xml.Null)
                      val anchor = Elem(null, "a", att, TopScope, true, Text("last"))
                      <span>{ anchor }</span><br/>
                    }
                    {
                      val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events/${app.value}/${component.value}/5", xml.Null)
                      val anchor = Elem(null, "a", att, TopScope, true, Text("last 5"))
                      <span>{ anchor }</span><br/>
                    }
                    {
                      val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events/${app.value}/${component.value}/20", xml.Null)
                      val anchor = Elem(null, "a", att, TopScope, true, Text("last 20"))
                      <span>{ anchor }</span><br/>
                    }
                    {
                      val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events/${app.value}/${component.value}/50", xml.Null)
                      val anchor = Elem(null, "a", att, TopScope, true, Text("last 50"))
                      <span>{ anchor }</span><br/>
                    }
                    {
                      val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events/${app.value}/${component.value}/100", xml.Null)
                      val anchor = Elem(null, "a", att, TopScope, true, Text("last 100"))
                      <span>{ anchor }</span><br/>
                    }
                  } else {
                    val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events/${app.value}/${component.value}/5", xml.Null)
                    val anchor = Elem(null, "a", att, TopScope, true, Text("last 5"))
                    <span>{ anchor }</span>
                  }
                }
              </td>
            </tr>
        }
      }
    </table>
  }

  def createComponentMissedEventsReport(component: ComponentId, entry: BadThingsHistory[MissedEventsEntry], maxItems: Int, pathToHerder: String) = {
    <html>
      <head>
        <title>Reported missed events for { component }</title>
      </head>
      <body>
        <h1>Reported missed events for { component }</h1>
        <span>Total missed events: { entry.occurencesCount }</span>
        <br/>
        <span>Max severity ever: { entry.maxSeverity.map(_.toString()).getOrElse("-") }</span>
        <br/>
        <table border="1">
          <tr>
            <th>Timestamp</th>
            <th>Severity</th>
            <th>Event</th>
            <th>Failure</th>
          </tr>
          {
            val items = entry.lastOccurences
            items.drop(Math.max(items.size - maxItems, 0)).map {
              case (event, cause, severity, timestamp) =>
                <tr>
                  <td>{ timestamp.toString }</td>
                  <td>{ createSeverityItem(severity) }</td>
                  <td>
                    {
                      event match {
                        case e: AggregateRootEvent =>
                          <span>{ s"${event.getClass().getName().toString}(${event.eventId.value})" }</span>
                          <br/>
                          <span>{ s"Aggregate root id: ${e.aggId.value}" }</span>
                          <br/>
                          <span>{ s"Aggregate root version: ${e.aggVersion.value}" }</span>
                        case e =>
                          <span>{ s"${event.getClass().getName().toString}(${event.eventId.value})" }</span>
                      }
                    }
                  </td>
                  <td>{ createFailureDetail(cause) }</td>
                </tr>
            }
          }
        </table>
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Missed events report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Dashboard"))
        }
        <br/>
        { almhirtContext.getUtcTimestamp.toString }
      </body>
    </html>
  }

  def createFailuresReport(entries: Seq[(ComponentId, BadThingsHistory[FailuresEntry])], pathToHerder: String) = {
    <html>
      <head>
        <title>Failures</title>
      </head>
      <body>
        { createFailuresReportContent(entries, false, pathToHerder) }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Dashboard"))
        }
        <br/>
        { almhirtContext.getUtcTimestamp.toString }
      </body>
    </html>
  }

  def createFailuresReportContent(entries: Seq[(ComponentId, BadThingsHistory[FailuresEntry])], abridged: Boolean, pathToHerder: String) = {
    import almhirt.problem._

    def createEntry(component: ComponentId, entry: BadThingsHistory[FailuresEntry]) = {
      def createSummaryLine(item: (ProblemCause, Severity, LocalDateTime)) = {
        <tr>
          <td>{ item._3.toString }</td>
          <td>{ createSeverityItem(item._2) }</td>
          <td>{
            item._1 match {
              case CauseIsProblem(p) => p.problemType.toString()
              case CauseIsThrowable(HasAThrowable(exn)) => exn.getClass().getName()
              case CauseIsThrowable(HasAThrowableDescribed(className, _, _, _)) => className
            }
          }</td>
        </tr>
      }

      <tr>
        <td>{ component.app.value }</td>
        <td>{ component.component.value }</td>
        <td>{ entry.occurencesCount }</td>
        <td>{ entry.maxSeverity.map(createSeverityItem).getOrElse(<span>-</span>) }</td>
        <td>
          <table border="0">
            { entry.lastOccurences.take(if (abridged) 1 else 3).map(line => createSummaryLine(line._1, line._2, line._3)) }
          </table>
        </td>
        <td>
          {
            if (!abridged) {
              {
                val att = new UnprefixedAttribute("href", s"$pathToHerder/failures/${component.app.value}/${component.component.value}/1", xml.Null)
                val anchor = Elem(null, "a", att, TopScope, true, Text("last"))
                <span>{ anchor }</span><br/>
              }
              {
                val att = new UnprefixedAttribute("href", s"$pathToHerder/failures/${component.app.value}/${component.component.value}/5", xml.Null)
                val anchor = Elem(null, "a", att, TopScope, true, Text("last 5"))
                <span>{ anchor }</span><br/>
              }
              {
                val att = new UnprefixedAttribute("href", s"$pathToHerder/failures/${component.app.value}/${component.component.value}/20", xml.Null)
                val anchor = Elem(null, "a", att, TopScope, true, Text("last 20"))
                <span>{ anchor }</span><br/>
              }
              {
                val att = new UnprefixedAttribute("href", s"$pathToHerder/failures/${component.app.value}/${component.component.value}/50", xml.Null)
                val anchor = Elem(null, "a", att, TopScope, true, Text("last 50"))
                <span>{ anchor }</span><br/>
              }
              {
                val att = new UnprefixedAttribute("href", s"$pathToHerder/failures/${component.app.value}/${component.component.value}/100", xml.Null)
                val anchor = Elem(null, "a", att, TopScope, true, Text("last 100"))
                <span>{ anchor }</span><br/>
              }
            } else {
              val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events/${component.app.value}/${component.component.value}/5", xml.Null)
              val anchor = Elem(null, "a", att, TopScope, true, Text("last 5"))
              <span>{ anchor }</span>
            }
          }
        </td>
      </tr>
    }

    <table border="1">
      <tr>
        <th>App</th>
        <th>Component</th>
        <th>Total</th>
        <th>Max Severity</th>
        {
          if (!abridged)
            <th>Last 3 failures</th>
          else
            <th>Last failure</th>
        }
        <th>more</th>
      </tr>
      {
        { entries.map { case (name, entry) => createEntry(name, entry) } }
      }
    </table>
  }

  def createComponentFailuresReport(component: ComponentId, entry: BadThingsHistory[FailuresEntry], maxItems: Int, pathToHerder: String) = {
    <html>
      <head>
        <title>Reported failures for { component }</title>
      </head>
      <body>
        <h1>Reported failures for { component }</h1>
        <span>Total failures: { entry.occurencesCount }</span>
        <br/>
        <span>Max severity ever: { entry.maxSeverity.map(_.toString()).getOrElse("-") }</span>
        <br/>
        <table border="1">
          <tr>
            <th>Timestamp</th>
            <th>Severity</th>
            <th>Failure</th>
          </tr>
          {
            val items = entry.lastOccurences
            items.drop(Math.max(items.size - maxItems, 0)).map {
              case (cause, severity, timestamp) =>
                <tr>
                  <td>{ timestamp.toString }</td>
                  <td>{ createSeverityItem(severity) }</td>
                  <td>{ createFailureDetail(cause) }</td>
                </tr>
            }
          }
        </table>
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/failures", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Failures Report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Dashboard"))
        }
        <br/>
        { almhirtContext.getUtcTimestamp.toString }
      </body>
    </html>
  }

  def createStatusReport(
    circuitsState: Seq[(ComponentId, CircuitState)],
    missedEvents: Seq[(ComponentId, BadThingsHistory[MissedEventsEntry])],
    failures: Seq[(ComponentId, BadThingsHistory[FailuresEntry])],
    pathToHerder: String) = {
    <html>
      <head>
        <title>Status Report</title>
      </head>
      <body>
        <h1>Status</h1>
        <h2>Circuits</h2>
        { createCircuitsContent(circuitsState, true) }
        <br/>
        <a href="./herder/circuits?ui">Circuit control</a>
        <h2>Missed Events</h2>
        { createMissedEventsReportContent(missedEvents, true, pathToHerder) }
        <br/>
        <h2>Reported Failures</h2>
        { createFailuresReportContent(failures, true, pathToHerder) }
        <br/>
        { almhirtContext.getUtcTimestamp.toString }
      </body>
    </html>
  }

  def createSeverityItem(severity: almhirt.problem.Severity) = {
    severity match {
      case almhirt.problem.Minor => <span style="background-color:#F6EE09">Minor</span>
      case almhirt.problem.Major => <span style="background-color:#F6A309">Major</span>
      case almhirt.problem.Critical => <span style="background-color:#F61D09">Critical</span>
    }
  }

  def createFailureDetail(failure: ProblemCause) = {
    <span>
      {
        (failure match {
          case CauseIsProblem(p) => p.toString
          case CauseIsThrowable(HasAThrowable(exn)) => exn.toString
          case CauseIsThrowable(x: HasAThrowableDescribed) => x.toString
        }).split("\\r?\\n").map(line => <span>{ line }<br/></span>)
      }
    </span>
  }

}
