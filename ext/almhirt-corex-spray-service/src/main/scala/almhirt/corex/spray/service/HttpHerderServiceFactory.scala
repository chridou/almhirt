package almhirt.corex.spray.service

import java.time.LocalDateTime
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almvalidation.kit._
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
import spray.routing.RequestContext
import spray.routing.HttpService

object HttpHerderServiceFactory {
  final case class HttpHerderServiceParams(
    maxCallDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    problemMarshaller: Marshaller[Problem])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(Marshaller[Problem]) ⇒ HttpHerderServiceParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section ← ctx.config.v[Config]("almhirt.http.endpoints.herder-service")
      maxCallDuration ← section.v[FiniteDuration]("max-call-duration")
      selector ← section.v[ExtendedExecutionContextSelector]("execution-context-selector")
    } yield {
      (problemMarshaller: Marshaller[Problem]) ⇒ HttpHerderServiceParams(maxCallDuration, selector, problemMarshaller)
    }
  }

}

object HttpHerderService {
  def propsRaw(
    maxCallDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    problemMarshaller: Marshaller[Problem])(implicit ctx: AlmhirtContext): Props =
    Props(new HttpHerderServiceActor(HttpHerderServiceFactory.HttpHerderServiceParams(maxCallDuration, exectionContextSelector, problemMarshaller)))

  def propsRaw(params: HttpHerderServiceFactory.HttpHerderServiceParams)(implicit ctx: AlmhirtContext): Props =
    Props(new HttpHerderServiceActor(params))

  def props(problemMarshaller: Marshaller[Problem])(implicit ctx: AlmhirtContext): AlmValidation[Props] =
    HttpHerderServiceFactory.paramsFactory.map(f ⇒ propsRaw(f(problemMarshaller)))

  def componentFactory(problemMarshaller: Marshaller[Problem])(implicit ctx: AlmhirtContext): AlmValidation[ComponentFactory] =
    props(problemMarshaller).map(props ⇒ ComponentFactory(props, actorname))

  val actorname = "herder-service"
}

private[almhirt] class HttpHerderServiceActor(params: HttpHerderServiceFactory.HttpHerderServiceParams)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with HttpService with HttpHerderServiceFactory {
  override def actorRefFactory = this.context

  val route = this.createHerderServiceEndpoint(params)

  override def receive = runRoute(route)
}

trait HttpHerderServiceFactory extends Directives { me: AlmActor with AlmActorLogging ⇒
  import almhirt.components.EventSinkHubMessage

  def createHerderServiceEndpoint(params: HttpHerderServiceFactory.HttpHerderServiceParams): RequestContext ⇒ Unit = {

    implicit val execCtx = selectExecutionContext(params.exectionContextSelector)
    implicit val problemMarshaller = params.problemMarshaller
    val maxCallDuration = params.maxCallDuration
    val reportFactory = new JsonStatusReportFactory(this.context)
    val reportTerminator = reportFactory.createJsonStatusReportRoute(15.seconds)

    pathPrefix("herder") {
      pathPrefix("ui") {
        pathEnd {
          get { ctx ⇒
            ctx.complete("Cool app!")
          }
        }
      } ~
        pathEnd {
          get { ctx ⇒
            val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
            val futCircuits = (herder ? CircuitMessages.ReportCircuitStates)(maxCallDuration).mapCastTo[CircuitMessages.CircuitStates].map(_.states)
            val futComponentStates = (herder ? ComponentControlMessages.ReportComponentStates)(maxCallDuration).mapCastTo[ComponentControlMessages.ComponentStates].map(_.states)
            val futFailures = (herder ? FailureMessages.ReportFailures)(maxCallDuration).mapCastTo[FailureMessages.ReportedFailures].map(_.failures)
            val futRejectedCommands = (herder ? CommandMessages.ReportRejectedCommands)(maxCallDuration).mapCastTo[CommandMessages.RejectedCommands].map(_.rejectedCommands)
            val futMissedEvents = (herder ? EventMessages.ReportMissedEvents)(maxCallDuration).mapCastTo[EventMessages.MissedEvents].map(_.missedEvents)
            val futInfos = (herder ? InformationMessages.ReportInformation)(maxCallDuration).mapCastTo[InformationMessages.ReportedInformation].map(_.information)
            val futReporters = (herder ? StatusReportMessages.GetStatusReporters)(maxCallDuration).mapCastTo[StatusReportMessages.StatusReporters].map(_.reporters)
            val futHtml =
              for {
                circuitStates ← futCircuits
                componentStates ← futComponentStates
                failures ← futFailures
                rejectedCommands ← futRejectedCommands
                missedEvents ← futMissedEvents
                information ← futInfos
                reporters ← futReporters
              } yield createStatusReport(circuitStates, componentStates, failures, rejectedCommands, missedEvents, information, reporters, "./herder")
            futHtml.fold(
              problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
              html ⇒ ctx.complete(StatusCodes.OK, html))
          }
        } ~ pathPrefix("circuits") {
          pathEnd {
            parameter('ui.?) { uiEnabledP ⇒
              val isUiEnabled =
                uiEnabledP match {
                  case None ⇒ false
                  case Some(str) ⇒
                    str.trim().isEmpty || str.trim() == "true"
                }
              get { ctx ⇒
                val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                val fut = (herder ? CircuitMessages.ReportCircuitStates)(maxCallDuration).mapCastTo[CircuitMessages.CircuitStates].map(_.states)
                fut.fold(
                  problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                  states ⇒ {
                    val html = createCircuitsUi(states, !isUiEnabled)
                    ctx.complete(StatusCodes.OK, html)
                  } )
              }
            }
          } ~ pathPrefix(Segment / Segment) { (appName, componentName) ⇒
            pathPrefix("attempt-reset") {
              pathEnd {
                get { ctx ⇒
                  val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                  herder ! CircuitMessages.AttemptCloseCircuit(ComponentId(AppName(appName), ComponentName(componentName)))
                  ctx.complete(StatusCodes.Accepted, s"attempting to close circuit $componentName")
                }
              }
            } ~ pathPrefix("remove-fuse") {
              pathEnd {
                get { ctx ⇒
                  val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                  herder ! CircuitMessages.RemoveFuseFromCircuit(ComponentId(AppName(appName), ComponentName(componentName)))
                  ctx.complete(StatusCodes.Accepted, s"attempting to remove fuse in circuit $componentName")
                }
              }
            } ~ pathPrefix("destroy") {
              pathEnd {
                get { ctx ⇒
                  val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                  herder ! CircuitMessages.DestroyCircuit(ComponentId(AppName(appName), ComponentName(componentName)))
                  ctx.complete(StatusCodes.Accepted, s"attempting to destroy circuit $componentName")
                }
              }
            } ~ pathPrefix("circumvent") {
              pathEnd {
                get { ctx ⇒
                  val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                  herder ! CircuitMessages.CircumventCircuit(ComponentId(AppName(appName), ComponentName(componentName)))
                  ctx.complete(StatusCodes.Accepted, s"attempting to circumvent circuit $componentName")
                }
              }
            }
          }
        } ~ pathPrefix("status-reports") {
          pathEnd {
            get { ctx ⇒
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              val fut = (herder ? StatusReportMessages.GetStatusReporters)(maxCallDuration).mapCastTo[StatusReportMessages.StatusReporters].map(_.reporters)
              fut.fold(
                problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                reporters ⇒
                  ctx.complete(StatusCodes.OK, createStatusReporters(reporters, "../../../../herder")))
            }
          } ~ reportTerminator
        } ~ pathPrefix("component-controls") {
          pathEnd {
            parameter('ui.?) { uiEnabledP ⇒
              val isUiEnabled =
                uiEnabledP match {
                  case None ⇒ false
                  case Some(str) ⇒
                    str.trim().isEmpty || str.trim() == "true"
                }
              get { ctx ⇒
                val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                val fut = (herder ? ComponentControlMessages.ReportComponentStates)(maxCallDuration).mapCastTo[ComponentControlMessages.ComponentStates].map(_.states)
                fut.fold(
                  problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                  states ⇒ {
                    val html = createComponentsUi(states, !isUiEnabled)
                    ctx.complete(StatusCodes.OK, html)
                  })
              }
            }
          } ~ pathPrefix(Segment / Segment) { (appName, componentName) ⇒
            pathPrefix("attempt-pause") {
              pathEnd {
                get {
                  parameter('token.?) { token ⇒
                    ctx ⇒
                      val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                      herder ! ComponentControlMessages.AttemptComponentControlCommand(ComponentId(AppName(appName), ComponentName(componentName)), ActorMessages.Pause(token.map(PauseToken(_))))
                      ctx.complete(StatusCodes.Accepted, <div><br>s"attempting to pause $componentName"</br><br><a href="/herder">Dashboard</a></br></div>)
                  }
                }
              }
            } ~ pathPrefix("attempt-resume") {
              pathEnd {
                get {
                  parameter('token.?) { token ⇒
                    ctx ⇒
                      val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                      herder ! ComponentControlMessages.AttemptComponentControlCommand(ComponentId(AppName(appName), ComponentName(componentName)), ActorMessages.Resume(token.map(PauseToken(_))))
                      ctx.complete(StatusCodes.Accepted, <div><br>s"attempting to resume $componentName"</br><br><a href="/herder">Dashboard</a></br></div>)
                  }
                }
              }
            } ~ pathPrefix("attempt-prepare-shutdown") {
              pathEnd {
                get { ctx ⇒
                  val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                  herder ! ComponentControlMessages.AttemptComponentControlCommand(ComponentId(AppName(appName), ComponentName(componentName)), ActorMessages.PrepareForShutdown)
                  ctx.complete(StatusCodes.Accepted, <div><br>s"attempting to shutdown $componentName"</br><br><a href="/herder">Dashboard</a></br></div>)
                }
              }
            } ~ pathPrefix("attempt-restart") {
              pathEnd {
                get { ctx ⇒
                  val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                  herder ! ComponentControlMessages.AttemptComponentControlCommand(ComponentId(AppName(appName), ComponentName(componentName)), ActorMessages.Restart)
                  ctx.complete(StatusCodes.Accepted, <div><br>s"attempting to restart $componentName"</br><br><a href="/herder">Dashboard</a></br></div>)
                }
              }
            }
          }
        } ~ pathPrefix("failures") {
          pathEnd {
            get { ctx ⇒
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              val fut = (herder ? FailureMessages.ReportFailures)(maxCallDuration).mapCastTo[FailureMessages.ReportedFailures].map(_.failures)
              fut.fold(
                problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                entries ⇒ ctx.complete(StatusCodes.OK, createFailuresReport(entries, "../herder")))
            }
          } ~ pathPrefix(Segment / Segment / IntNumber) { (appName, componentName, num) ⇒
            pathEnd {
              get { ctx ⇒
                val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                val fut = (herder ? FailureMessages.ReportFailuresFor(ComponentId(AppName(appName), ComponentName(componentName))))(maxCallDuration).mapCastTo[FailureMessages.ReportedFailuresFor]
                fut.fold(
                  problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                  res ⇒ res.entry match {
                    case Some(e) ⇒ ctx.complete(StatusCodes.OK, createComponentFailuresReport(res.id, e, num, "../../../../herder"))
                    case None    ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, NotFoundProblem(s"No component with name ($appName/$componentName) found."))
                  })
              }
            }
          }
        } ~ pathPrefix("rejected-commands") {
          pathEnd {
            get { ctx ⇒
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              val fut = (herder ? CommandMessages.ReportRejectedCommands)(maxCallDuration).mapCastTo[CommandMessages.RejectedCommands].map(_.rejectedCommands)
              fut.fold(
                problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                rejected ⇒ ctx.complete(StatusCodes.OK, createRejectedCommandsReport(rejected, "../herder")))
            }
          } ~ pathPrefix(Segment / Segment / IntNumber) { (appName, componentName, num) ⇒
            pathEnd {
              get { ctx ⇒
                val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                val fut = (herder ? CommandMessages.ReportRejectedCommandsFor(ComponentId(AppName(appName), ComponentName(componentName))))(maxCallDuration).mapCastTo[CommandMessages.ReportedRejectedCommandsFor]
                fut.fold(
                  problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                  res ⇒ res.rejectedCommands match {
                    case Some(e) ⇒ ctx.complete(StatusCodes.OK, createComponentRejectedCommandsReport(res.id, e, num, "../../../../herder"))
                    case None    ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, NotFoundProblem(s"No component with name ($appName/$componentName) found."))
                  })
              }
            }
          }
        } ~ pathPrefix("missed-events") {
          pathEnd {
            get { ctx ⇒
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              val fut = (herder ? EventMessages.ReportMissedEvents)(maxCallDuration).mapCastTo[EventMessages.MissedEvents].map(_.missedEvents)
              fut.fold(
                problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                missed ⇒ ctx.complete(StatusCodes.OK, createMissedEventsReport(missed, "../herder")))
            }
          } ~ pathPrefix(Segment / Segment / IntNumber) { (appName, componentName, num) ⇒
            pathEnd {
              get { ctx ⇒
                val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                val fut = (herder ? EventMessages.ReportMissedEventsFor(ComponentId(AppName(appName), ComponentName(componentName))))(maxCallDuration).mapCastTo[EventMessages.ReportedMissedEventsFor]
                fut.fold(
                  problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                  res ⇒ res.missedEvents match {
                    case Some(e) ⇒ ctx.complete(StatusCodes.OK, createComponentMissedEventsReport(res.id, e, num, "../../../../herder"))
                    case None    ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, NotFoundProblem(s"No component with name ($appName/$componentName) found."))
                  })
              }
            }
          }
        } ~ pathPrefix("information") {
          pathEnd {
            get { ctx ⇒
              val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
              val fut = (herder ? InformationMessages.ReportInformation)(maxCallDuration).mapCastTo[InformationMessages.ReportedInformation].map(_.information)
              fut.fold(
                problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                information ⇒ ctx.complete(StatusCodes.OK, createInformationReport(information, "../herder")))
            }
          } ~ pathPrefix(Segment / Segment / IntNumber) { (appName, componentName, num) ⇒
            pathEnd {
              get { ctx ⇒
                val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
                val fut = (herder ? InformationMessages.ReportInformationFor(ComponentId(AppName(appName), ComponentName(componentName))))(maxCallDuration).mapCastTo[InformationMessages.ReportedInformationFor]
                fut.fold(
                  problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem),
                  res ⇒ res.entry match {
                    case Some(e) ⇒ ctx.complete(StatusCodes.OK, createComponentInformationReport(res.id, e, num, "../../../../herder"))
                    case None    ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, NotFoundProblem(s"No component with name ($appName/$componentName) found."))
                  })
              }
            }
          }
        }
    }
  }

  import scala.xml._

  private def createStatusReporters(reporters: Seq[(ComponentId, StatusReporter)], pathToHerder: String) = {
    def createRow(cid: ComponentId, reporter: StatusReporter) = {
      <tr>
        <td>{ cid.app.value }</td>
        <td>{ cid.component.value }</td>
        <td>{ reporter.description getOrElse "No description" }</td>
        <td>
          {
            val att = new UnprefixedAttribute("href", s"$pathToHerder/status-reports/${cid.app.value}/${cid.component.value}", xml.Null)
            Elem(null, "a", att, TopScope, true, Text("Full report"))
          }
        </td>
        <td>
          {
            val att = new UnprefixedAttribute("href", s"$pathToHerder/status-reports/${cid.app.value}/${cid.component.value}?no-noise=true", xml.Null)
            Elem(null, "a", att, TopScope, true, Text("No noise report"))
          }
        </td>
      </tr>
    }

    <html>
      <head>
        <title>Status Reporters</title>
      </head>
      <body>
        <table border="1">
          <tr>
            <th>App</th>
            <th>Component</th>
            <th>Description</th>
            <th>Full report</th>
            <th>No noise report</th>
          </tr>
          {
            reporters.map { case (cid, rep) ⇒ createRow(cid, rep) }
          }
        </table>
        <br><a href="../herder">Dashboard</a></br>
        <br>{ almhirtContext.getUtcTimestamp.toString }</br>
      </body>
    </html>
  }

  private def createCircuitsUi(state: Seq[(ComponentId, CircuitState)], isReport: Boolean) = {
    <html>
      <head>
        <title>Circuits</title>
      </head>
      <body>
        { createCircuitsContent(state, isReport) }
        {
          if (!isReport) {
            <br><a href="../herder">Dashboard</a></br>
          }
        }
        <br>{ almhirtContext.getUtcTimestamp.toString }</br>
      </body>
    </html>
  }

  private def createCircuitsContent(state: Seq[(ComponentId, CircuitState)], isReport: Boolean) = {
    def createStateItem(state: CircuitState) = {
      state match {
        case x: CircuitState.Closed ⇒
          <td style="background-color:#0EE915">{ x.toString }</td>
        case x: CircuitState.HalfOpen ⇒
          <td style="background-color:#F7B809">{ x.toString }</td>
        case x: CircuitState.Open ⇒
          <td style="background-color:#F74109">{ x.toString }</td>
        case x: CircuitState.FuseRemoved ⇒
          <td style="background-color:#DD2626">{ x.toString }</td>
        case x: CircuitState.Circumvented ⇒
          <td style="background-color:#BCB2B2">{ x.toString }</td>
        case x: CircuitState.Destroyed ⇒
          <td style="background-color:#B92121">{ x.toString }</td>
      }
    }

    def createStateResetAction(component: ComponentId, state: CircuitState) = {
      state match {
        case st: CircuitState.Destroyed ⇒
          <td>no action</td>
        case st: CircuitState.Circumvented ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/attempt-reset", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("reset"))
          <td>{ anchor }</td>
        case st: CircuitState.AllWillFailState ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/attempt-reset", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("reset"))
          <td>{ anchor }</td>
        case _ ⇒
          <td>no action</td>
      }
    }

    def createStateRemoveAction(component: ComponentId, state: CircuitState) = {
      state match {
        case x: CircuitState.Open ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.Circumvented ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.HalfOpen ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case x: CircuitState.Closed ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/remove-fuse", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("remove fuse"))
          <td>{ anchor }</td>
        case _ ⇒
          <td>no action</td>
      }
    }

    def createStateDestroyAction(component: ComponentId, state: CircuitState) = {
      state match {
        case x: CircuitState.Open ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/destroy", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy"))
          <td>{ anchor }</td>
        case x: CircuitState.Circumvented ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/destroy", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy"))
          <td>{ anchor }</td>
        case x: CircuitState.HalfOpen ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/destroy", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy"))
          <td>{ anchor }</td>
        case x: CircuitState.Closed ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/destroy", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy"))
          <td>{ anchor }</td>
        case x: CircuitState.FuseRemoved ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/destroy", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("destroy"))
          <td>{ anchor }</td>
        case _ ⇒
          <td>no action</td>
      }
    }

    def createStateCircumventAction(component: ComponentId, state: CircuitState) = {
      state match {
        case x: CircuitState.Destroyed ⇒
          <td>no action</td>
        case x: CircuitState.Circumvented ⇒
          <td>no action</td>
        case x ⇒
          val att = new UnprefixedAttribute("href", s"./circuits/${component.app.value}/${component.component.value}/circumvent", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("circumvent"))
          <td>{ anchor }</td>
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
          { createStateCircumventAction(component, state) }
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
        { state.map { case (component, state) ⇒ createRow(component, state, true) } }
      </table>
    } else {
      <table border="1">
        <tr>
          <th>App</th>
          <th>Component</th>
          <th colspan="4">Actions</th>
        </tr>
        { state.map { case (component, state) ⇒ createRow(component, state, false) } }
      </table>
    }
  }

  private def createComponentsUi(state: Seq[(ComponentId, ComponentState)], isReport: Boolean) = {
    <html>
      <head>
        <title>Components</title>
      </head>
      <body>
        { createComponentsContent(state, isReport) }
        {
          if (!isReport) {
            <br><a href="../herder">Dashboard</a></br>
          }
        }
        <br>{ almhirtContext.getUtcTimestamp.toString }</br>
      </body>
    </html>
  }

  private def createComponentsContent(state: Seq[(ComponentId, ComponentState)], isReport: Boolean) = {
    def createStateItem(state: ComponentState) = {
      state match {
        case ComponentState.Startup ⇒
          <td style="background-color:yellow">Startup</td>
        case ComponentState.WaitingForStartSignal ⇒
          <td style="background-color:yellowgreen">Waiting for start signal</td>
        case ComponentState.Running ⇒
          <td style="background-color:green">Running</td>
        case ComponentState.PreparingForPause ⇒
          <td style="background-color:orange">Preparing for pause</td>
        case ComponentState.Paused ⇒
          <td style="background-color:orangered">Paused</td>
        case ComponentState.PreparingForShutdown ⇒
          <td style="background-color:mediumblue">Preparing for shutdown</td>
        case ComponentState.ReadyForShutdown ⇒
          <td style="background-color:LightBlue">READY FOR SHUTDOWN</td>
        case ComponentState.Error(cause) ⇒
          <td style="background-color:red">ERROR: { cause.message }</td>
      }
    }

    def createPauseAction(component: ComponentId, state: ComponentState) = {
      state match {
        case ComponentState.PreparingForPause ⇒
          <td>no action</td>
        case ComponentState.Paused ⇒
          <td>no action</td>
        case ComponentState.Startup ⇒
          <td>no action</td>
        case ComponentState.WaitingForStartSignal ⇒
          <td>no action</td>
        case ComponentState.Running ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-pause", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("pause"))
          <td>{ anchor }</td>
        case ComponentState.PreparingForShutdown ⇒
          <td>no action</td>
        case ComponentState.ReadyForShutdown ⇒
          <td>no action</td>
        case ComponentState.Error(_) ⇒
          <td>no action</td>
      }
    }

    def createResumeAction(component: ComponentId, state: ComponentState) = {
      state match {
        case ComponentState.PreparingForPause ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-resume", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("resume"))
          <td>{ anchor }</td>
        case ComponentState.Paused ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-resume", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("resume"))
          <td>{ anchor }</td>
        case ComponentState.WaitingForStartSignal ⇒
          <td>no action</td>
        case ComponentState.Startup ⇒
          <td>no action</td>
        case ComponentState.Running ⇒
          <td>no action</td>
        case ComponentState.PreparingForShutdown ⇒
          <td>no action</td>
        case ComponentState.ReadyForShutdown ⇒
          <td>no action</td>
        case ComponentState.Error(_) ⇒
          <td>no action</td>
      }
    }

    def createRestartAction(component: ComponentId, state: ComponentState) = {
      state match {
        case ComponentState.PreparingForPause ⇒
          <td>no action</td>
        case ComponentState.Paused ⇒
          <td>no action</td>
        case ComponentState.Startup ⇒
          <td>no action</td>
        case ComponentState.WaitingForStartSignal ⇒
          <td>no action</td>
        case ComponentState.Running ⇒
          <td>no action</td>
        case ComponentState.PreparingForShutdown ⇒
          <td>no action</td>
        case ComponentState.ReadyForShutdown ⇒
          <td>no action</td>
        case ComponentState.Error(_) ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-restart", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("restart"))
          <td>{ anchor }</td>
      }
    }

    def createPrepareForShutdownAction(component: ComponentId, state: ComponentState) = {
      state match {
        case ComponentState.PreparingForPause ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-prepare-shutdown", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("prepare shutdown"))
          <td>{ anchor }</td>
        case ComponentState.Paused ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-prepare-shutdown", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("prepare shutdown"))
          <td>{ anchor }</td>
        case ComponentState.Startup ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-prepare-shutdown", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("prepare shutdown"))
          <td>{ anchor }</td>
        case ComponentState.WaitingForStartSignal ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-prepare-shutdown", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("prepare shutdown"))
          <td>{ anchor }</td>
        case ComponentState.Running ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-prepare-shutdown", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("prepare shutdown"))
          <td>{ anchor }</td>
        case ComponentState.PreparingForShutdown ⇒
          <td>no action</td>
        case ComponentState.ReadyForShutdown ⇒
          <td>no action</td>
        case ComponentState.Error(_) ⇒
          val att = new UnprefixedAttribute("href", s"/herder/component-controls/${component.app.value}/${component.component.value}/attempt-prepare-shutdown", xml.Null)
          val anchor = Elem(null, "a", att, TopScope, true, Text("prepare shutdown"))
          <td>{ anchor }</td>
      }
    }

    def createRow(component: ComponentId, state: ComponentState, isReport: Boolean) = {
      // Yes, they do the same...  
      if (!isReport) {
        <tr>
          <td>{ component.app.value }</td>
          <td>{ component.component.value }</td>
          { createStateItem(state) }
          { createPauseAction(component, state) }
          { createResumeAction(component, state) }
          { createRestartAction(component, state) }
          { createPrepareForShutdownAction(component, state) }
        </tr>
      } else {
        <tr>
          <td>{ component.app.value }</td>
          <td>{ component.component.value }</td>
          { createStateItem(state) }
        </tr>
      }
    }

    // Yes, they do the same... 
    if (isReport) {
      <table border="1">
        <tr>
          <th>App</th>
          <th>Component</th>
          <th>Component State</th>
        </tr>
        { state.map { case (component, state) ⇒ createRow(component, state, true) } }
      </table>
    } else {
      <table border="1">
        <tr>
          <th>App</th>
          <th>Component</th>
          <th>Component State</th>
        </tr>
        { state.map { case (component, state) ⇒ createRow(component, state, true) } }
      </table>
    }
  }

  def createFailuresReport(entries: Seq[(ComponentId, BadThingsHistory[FailuresEntry])], pathToHerder: String) = {
    <html>
      <head>
        <title>Failures</title>
      </head>
      <body>
        <h1>Failures Report</h1>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/failures", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Failures report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Missed events report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Dashboard"))
        }
        <br/>
        { createFailuresReportContent(entries, false, pathToHerder) }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/failures", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Failures report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Missed events report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
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

  def createFailuresReportContent(entries: Seq[(ComponentId, BadThingsHistory[FailuresEntry])], abridged: Boolean, pathToHerder: String) = {
    import almhirt.problem._

    def createEntry(component: ComponentId, entry: BadThingsHistory[FailuresEntry]) = {
      def createSummaryLine(item: (ProblemCause, Severity, LocalDateTime)) = {
        <tr>
          <td>{ item._3.toString }</td>
          <td style="height:100%">{ createSeverityItem(item._2) }</td>
          <td>{
            item._1 match {
              case CauseIsProblem(p) ⇒ p.problemType.toString()
              case CauseIsThrowable(HasAThrowable(exn)) ⇒ exn.getClass().getName()
              case CauseIsThrowable(HasAThrowableDescribed(className, _, _, _)) ⇒ className
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
            { entry.lastOccurences.take(if (abridged) 1 else 3).map(line ⇒ createSummaryLine(line._1, line._2, line._3)) }
          </table>
        </td>
        <td>
          {
            if (!abridged) {
              <span>
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
              </span>
            } else {
              val att = new UnprefixedAttribute("href", s"$pathToHerder/failures/${component.app.value}/${component.component.value}/5", xml.Null)
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
        { entries.map { case (name, entry) ⇒ createEntry(name, entry) } }
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
        <br/>
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
            items.take(maxItems).map {
              case (cause, severity, timestamp) ⇒
                <tr>
                  <td>{ timestamp.toString }</td>
                  <td>{ createSeverityItem(severity) }</td>
                  <td>{ createFailureDetail(cause) }</td>
                </tr>
            }
          }
        </table>
        <br/>
        { almhirtContext.getUtcTimestamp.toString }
      </body>
    </html>
  }

  def createRejectedCommandsReport(rejectedCommands: Seq[(ComponentId, BadThingsHistory[RejectedCommandsEntry])], pathToHerder: String) = {
    <html>
      <head>
        <title>Rejected Commands</title>
      </head>
      <body>
        <h1>Rejected Commands Report</h1>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/failures", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Failures report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Missed events report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Dashboard"))
        }
        <br/>
        { createRejectedCommandsContent(rejectedCommands, false, pathToHerder) }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/failures", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Failures report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Missed events report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
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

  def createRejectedCommandsContent(rejectedCommands: Seq[(ComponentId, BadThingsHistory[RejectedCommandsEntry])], abridged: Boolean, pathToHerder: String) = {
    def createHistoryLine(item: RejectedCommandsEntry) = {
      <tr>
        <td>{ item._4.toString() }</td>
        <td>{ item._1.toShortString }</td>
        <td style="height:100%">{ createSeverityItem(item._3) }</td>
        <td>{
          item._2 match {
            case CauseIsProblem(p) ⇒ p.problemType.toString()
            case CauseIsThrowable(HasAThrowable(exn)) ⇒ exn.getClass().getName()
            case CauseIsThrowable(HasAThrowableDescribed(className, _, _, _)) ⇒ className
          }
        }</td>
      </tr>
    }

    <table border="1">
      <tr>
        <th>App</th>
        <th>Component</th>
        <th>Rejected Commands</th>
        <th>Max Severity</th>
        {
          if (!abridged)
            <th>Last 3 rejected commands</th>
          else
            <th>Last rejected command</th>
        }
        <th>more</th>
      </tr>
      {
        rejectedCommands.map {
          case (ComponentId(app, component), history) ⇒
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
                    <span>
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands/${app.value}/${component.value}/1", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last"))
                        <span>{ anchor }</span><br/>
                      }
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands/${app.value}/${component.value}/5", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last 5"))
                        <span>{ anchor }</span><br/>
                      }
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands/${app.value}/${component.value}/20", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last 20"))
                        <span>{ anchor }</span><br/>
                      }
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands/${app.value}/${component.value}/50", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last 50"))
                        <span>{ anchor }</span><br/>
                      }
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands/${app.value}/${component.value}/100", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last 100"))
                        <span>{ anchor }</span><br/>
                      }
                    </span>
                  } else {
                    val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands/${app.value}/${component.value}/5", xml.Null)
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

  def createComponentRejectedCommandsReport(component: ComponentId, rejectedCommands: BadThingsHistory[RejectedCommandsEntry], maxItems: Int, pathToHerder: String) = {
    <html>
      <head>
        <title>Reported rejected commands for { component }</title>
      </head>
      <body>
        <h1>Reported rejected commands for { component }</h1>
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Dashboard"))
        }
        <br/>
        <br/>
        <span>Total rejected commands: { rejectedCommands.occurencesCount }</span>
        <br/>
        <span>Max severity ever: { rejectedCommands.maxSeverity.map(_.toString()).getOrElse("-") }</span>
        <br/>
        <table border="1">
          <tr>
            <th>Timestamp</th>
            <th>Severity</th>
            <th>Event</th>
            <th>Failure</th>
          </tr>
          {
            val items = rejectedCommands.lastOccurences
            items.take(maxItems).map {
              case (commandRepr, cause, severity, timestamp) ⇒
                <tr>
                  <td>{ timestamp.toString }</td>
                  <td>{ createSeverityItem(severity) }</td>
                  <td>
                    <span>{ commandRepr.toShortString.split("\\r?\\n").map(line ⇒ <span>{ line }<br/></span>) }</span>
                  </td>
                  <td>{ createFailureDetail(cause) }</td>
                </tr>
            }
          }
        </table>
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
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

  def createMissedEventsReport(missedEvents: Seq[(ComponentId, BadThingsHistory[MissedEventsEntry])], pathToHerder: String) = {
    <html>
      <head>
        <title>Missed Events</title>
      </head>
      <body>
        <h1>Missed Events Report</h1>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/failures", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Failures report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Missed events report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Dashboard"))
        }
        <br/>
        { createMissedEventsReportContent(missedEvents, false, pathToHerder) }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/failures", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Failures report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Missed events report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
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

  def createMissedEventsReportContent(missedEvents: Seq[(ComponentId, BadThingsHistory[MissedEventsEntry])], abridged: Boolean, pathToHerder: String) = {
    def createHistoryLine(item: MissedEventsEntry) = {
      <tr>
        <td>{ item._4.toString() }</td>
        <td>{ s"${item._1.getClass().getSimpleName().toString}(${item._1.eventId.value})" }</td>
        <td style="height:100%">{ createSeverityItem(item._3) }</td>
        <td>{
          item._2 match {
            case CauseIsProblem(p) ⇒ p.problemType.toString()
            case CauseIsThrowable(HasAThrowable(exn)) ⇒ exn.getClass().getName()
            case CauseIsThrowable(HasAThrowableDescribed(className, _, _, _)) ⇒ className
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
          case (ComponentId(app, component), history) ⇒
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
                    <span>
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
                    </span>
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
        <br/>
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
            items.take(maxItems).map {
              case (event, cause, severity, timestamp) ⇒
                <tr>
                  <td>{ timestamp.toString }</td>
                  <td>{ createSeverityItem(severity) }</td>
                  <td>
                    {
                      event match {
                        case e: AggregateRootEvent ⇒
                          <span>{ s"${event.getClass().getName().toString}(${event.eventId.value})" }</span>
                          <br/>
                          <span>{ s"Aggregate root id: ${e.aggId.value}" }</span>
                          <br/>
                          <span>{ s"Aggregate root version: ${e.aggVersion.value}" }</span>
                        case e ⇒
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

  // INFORMATION

  def createInformationReport(information: Seq[(ComponentId, ImportantThingsHistory[InformationEntry])], pathToHerder: String) = {
    <html>
      <head>
        <title>Information</title>
      </head>
      <body>
        <h1>Information Report</h1>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/failures", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Failures report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Missed events report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Dashboard"))
        }
        <br/>
        { createInformationReportContent(information, false, pathToHerder) }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/failures", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Failures report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/rejected-commands", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Rejected commands report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/missed-events", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Missed events report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
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

  def createInformationReportContent(information: Seq[(ComponentId, ImportantThingsHistory[InformationEntry])], abridged: Boolean, pathToHerder: String) = {
    def createHistoryLine(item: InformationEntry) = {
      <tr>
        <td>{ item._3.toString() }</td>
        <td>{ createImportanceItem(item._2) }</td>
        <td>{ item._1.split("\\r?\\n").map(line ⇒ <span>{ line }<br/></span>) }</td>
      </tr>
    }

    <table border="1">
      <tr>
        <th>App</th>
        <th>Component</th>
        <th>Infos</th>
        <th>Max Importance</th>
        {
          if (!abridged)
            <th>Last 3 infos</th>
          else
            <th>Last info</th>
        }
        <th>more</th>
      </tr>
      {
        information.map {
          case (ComponentId(app, component), history) ⇒
            <tr>
              <td>{ app.value }</td>
              <td>{ component.value }</td>
              <td>{ history.occurencesCount }</td>
              <td>{ history.maxImportance.map(createImportanceItem).getOrElse(<span>-</span>) }</td>
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
                    <span>
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/information/${app.value}/${component.value}/1", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last"))
                        <span>{ anchor }</span><br/>
                      }
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/information/${app.value}/${component.value}/5", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last 5"))
                        <span>{ anchor }</span><br/>
                      }
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/information/${app.value}/${component.value}/20", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last 20"))
                        <span>{ anchor }</span><br/>
                      }
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/information/${app.value}/${component.value}/50", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last 50"))
                        <span>{ anchor }</span><br/>
                      }
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/information/${app.value}/${component.value}/100", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last 100"))
                        <span>{ anchor }</span><br/>
                      }
                      {
                        val att = new UnprefixedAttribute("href", s"$pathToHerder/information/${app.value}/${component.value}/500", xml.Null)
                        val anchor = Elem(null, "a", att, TopScope, true, Text("last 500"))
                        <span>{ anchor }</span><br/>
                      }
                    </span>
                  } else {
                    val att = new UnprefixedAttribute("href", s"$pathToHerder/information/${app.value}/${component.value}/100", xml.Null)
                    val anchor = Elem(null, "a", att, TopScope, true, Text("last 100"))
                    <span>{ anchor }</span>
                  }
                }
              </td>
            </tr>
        }
      }
    </table>
  }

  def createComponentInformationReport(component: ComponentId, entry: ImportantThingsHistory[InformationEntry], maxItems: Int, pathToHerder: String) = {
    <html>
      <head>
        <title>Reported informations for { component }</title>
      </head>
      <body>
        <h1>Reportedinformation for { component }</h1>
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
        }
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Dashboard"))
        }
        <br/>
        <br/>
        <span>Total messages: { entry.occurencesCount }</span>
        <br/>
        <span>Max importance: { entry.maxImportance.map(_.toString()).getOrElse("-") }</span>
        <br/>
        <table border="1">
          <tr>
            <th>Timestamp</th>
            <th>Importance</th>
            <th>Message</th>
          </tr>
          {
            val items = entry.lastOccurences
            items.take(maxItems).map {
              case (message, importance, timestamp) ⇒
                <tr>
                  <td>{ timestamp.toString }</td>
                  <td>{ createImportanceItem(importance) }</td>
                  <td>
                    { message.split("\\r?\\n").map(line ⇒ <span>{ line }<br/></span>) }
                  </td>
                </tr>
            }
          }
        </table>
        <br/>
        {
          val att = new UnprefixedAttribute("href", s"$pathToHerder/information", xml.Null)
          Elem(null, "a", att, TopScope, true, Text("Information report"))
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

  // INFORMATION

  def createStatusReport(
    circuitsStates: Seq[(ComponentId, CircuitState)],
    componentStates: Seq[(ComponentId, ComponentState)],
    failures: Seq[(ComponentId, BadThingsHistory[FailuresEntry])],
    rejectedCommands: Seq[(ComponentId, BadThingsHistory[RejectedCommandsEntry])],
    missedEvents: Seq[(ComponentId, BadThingsHistory[MissedEventsEntry])],
    information: Seq[(ComponentId, ImportantThingsHistory[InformationEntry])],
    statusReporters: Seq[(ComponentId, StatusReporter)],
    pathToHerder: String) = {
    <html>
      <head>
        <title>Status Report</title>
      </head>
      <body>
        <h1>Status</h1>
        <table border="1">
          <tr>
            <th>
              <h2>Circuits</h2><br/><a href="./herder/circuits?ui">Circuit control</a>
            </th>
            <th>
              <h2>Components</h2><br/><a href="./herder/component-controls?ui">Components report</a>
            </th>
          </tr>
          <tr>
            <td>{ createCircuitsContent(circuitsStates, false) }</td>
            <td>{ createComponentsContent(componentStates, false) }</td>
          </tr>
          <tr>
            <th>
              <h2>Reported Failures</h2><br/><a href="./herder/failures">Failures report</a>
            </th>
            <th>
              <h2>Status Reports</h2>
            </th>
          </tr>
          <tr>
            <td>{ createFailuresReportContent(failures, true, pathToHerder) }</td>
            <td><a href="./herder/status-reports">Status reports view</a></td>
          </tr>
          <tr>
            <th><h2>Rejected Commands</h2><br/><a href="./herder/rejected-commands">Rejected commands report</a></th>
            <th><h2>Missed Events</h2><br/><a href="./herder/missed-events">Missed events report</a></th>
          </tr>
          <tr>
            <td>{ createRejectedCommandsContent(rejectedCommands, true, pathToHerder) }</td>
            <td>{ createMissedEventsReportContent(missedEvents, true, pathToHerder) }</td>
          </tr>
          <tr>
            <th colspan="2">
              <h2>Information</h2><br/><a href="./herder/information">Information report</a>
            </th>
          </tr>
          <tr>
            <td colspan="2">{ createInformationReportContent(information, true, pathToHerder) }</td>
          </tr>
        </table>
        <br/>
        { almhirtContext.getUtcTimestamp.toString }
      </body>
    </html>
  }

  def createSeverityItem(severity: almhirt.problem.Severity) = {
    severity match {
      case almhirt.problem.Minor    ⇒ <div style="background-color:yellow;height:100%">Minor</div>
      case almhirt.problem.Major    ⇒ <div style="background-color:orange;height:100%">Major</div>
      case almhirt.problem.Critical ⇒ <div style="background-color:red;height:100%">Critical</div>
    }
  }

  def createImportanceItem(importance: Importance) = {
    importance match {
      case Importance.NotWorthMentioning ⇒ <div style="background-color:lightgray;height:100%">NotWorthMentioning</div>
      case Importance.Mentionable        ⇒ <div style="background-color:lightgreen;height:100%">Mentionable</div>
      case Importance.Important          ⇒ <div style="background-color:orange;height:100%">Important</div>
      case Importance.VeryImportant      ⇒ <div style="background-color:red;height:100%">VeryImportant</div>
    }
  }

  def createFailureDetail(failure: ProblemCause) = {
    <span>
      {
        (failure match {
          case CauseIsProblem(p)⇒ p.toString
          case CauseIsThrowable(HasAThrowable(exn)) ⇒ s"""|${exn.getClass.getName}
                                                             |${exn.getMessage}
                                                             |${exn.getStackTrace.mkString("\n")}""".stripMargin
          case CauseIsThrowable(x: HasAThrowableDescribed)⇒ x.toString
        }).split("\\r?\\n").map(line ⇒ <span>{ line }<br/></span>)
      }
    </span>
  }
}
