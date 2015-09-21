package almhirt.context

import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.almvalidation.kit._
import almhirt.context._
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._

object AlmhirtReporter {
  def componentFactory()(implicit almhirtContext: AlmhirtContext): AlmValidation[ComponentFactory] =
    (ComponentFactory(Props(new AlmhirtReporter()), actorname)).success

  val actorname = "almhirt-reporter"
}

private[almhirt] class AlmhirtReporter()(implicit override val almhirtContext: AlmhirtContext) extends AlmActor() with AlmActorSupport with HasAlmhirtContext with AlmActorLogging with ActorLogging with ControllableActor with StatusReportingActor {

  import akka.actor.SupervisorStrategy._

  override def componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))

  implicit val executor = almhirtContext.futuresContext

  def receiveRunning: Receive = running() {
    reportsStatusF(onReportRequested = createStatusReport)(Actor.emptyBehavior)
  }

  override def receive: Receive = receiveRunning

  def createComponentsStatusReport(options: StatusReportOptions): AlmFuture[StatusReport] = {
    val rep = StatusReport()
    for {
      commandStatusTracker ← context.actorSelection(almhirtContext.localActorPaths.components / almhirt.components.CommandStatusTracker.actorname).resolveOne(1.second).map(Some(_)).recover({ case _ ⇒ None })
      commandStatusTrackerReport ← queryReportFromActorOpt(commandStatusTracker, options, timeout = 2.seconds)
      commandEndpoint ← context.actorSelection(almhirtContext.localActorPaths.components / almhirt.components.CommandEndpoint.actorname).resolveOne(1.second).map(Some(_)).recover({ case _ ⇒ None })
      commandEndpointReport ← queryReportFromActorOpt(commandEndpoint, options, timeout = 2.seconds)
    } yield {
      rep addMany ("command-endpoint-report" -> commandEndpointReport, "command-status-tracker-report" -> commandStatusTrackerReport)
    }
  }

  def createStatusReport(options: StatusReportOptions): AlmFuture[StatusReport] = {
    val rep = StatusReport("Almhirt-Report")
    for {
      nexusRef ← context.actorSelection(almhirtContext.localActorPaths.components / almhirt.domain.AggregateRootNexus.actorname).resolveOne(1.second).map(Some(_)).recover({ case _ ⇒ None })
      nexusReport ← queryReportFromActorOpt(nexusRef, options, timeout = 2.seconds)
      components ← createComponentsStatusReport(options)
    } yield {
      rep addMany ("nexus-status" -> nexusReport, "components-report" -> components)
    }
  }

  override def preStart() {
    super.preStart()
    registerStatusReporter(description = Some("All system reports"))
    registerComponentControl()
    logInfo("Starting..")
  }

  override def postStop() {
    super.postStop()
    deregisterStatusReporter()
    deregisterComponentControl()
    logWarning("Stopped")
  }
}