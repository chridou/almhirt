package almhirt.domain

import scala.concurrent.duration._
import akka.actor._
import org.reactivestreams.Publisher
import almhirt.common._
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import almhirt.almvalidation.kit._
import almhirt.context.AlmhirtContext
import almhirt.tracking.CommandNotAccepted

object AggregateRootNexus {
  def propsRaw(hiveSelector: HiveSelector, hiveFactory: AggregateRootHiveFactory)(implicit ctx: AlmhirtContext): Props =
    Props(new AggregateRootNexus(hiveSelector, hiveFactory))

  val actorname = "aggregate-root-nexus"
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.components(root) / actorname
}

/**
 * HiveSelector:
 * Each function must return true, if the command is a match for the given [[HiveDescriptor]].
 * Remember that the [[AggregateRootHiveFactory]] must return a hive for each possible descriptor.
 * Do not design your filters in a way, that multiple hives may contain the same aggregate root!
 */
private[almhirt] class AggregateRootNexus(
    hiveSelector: HiveSelector,
    hiveFactory: AggregateRootHiveFactory)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorLogging with ControllableActor with StatusReportingActor {

  implicit val executor = almhirtContext.futuresContext

  override val componentControl = LocalComponentControl(self, ComponentControlActions.pauseResume, Some(logWarning))
  override val statusReportsCollector = Some(StatusReportsCollector(this.context))

  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn: Exception ⇒
        logError(s"Handling escalated error for ${sender.path.name} with a action Escalate.", exn)
        reportCriticalFailure(exn)
        Escalate
    }

  private var hives: List[(AggregateRootCommand ⇒ Boolean, ActorRef)] = Nil

  case object Start

  private var commandsReceived = 0L

  def receiveInitialize: Receive = startup() {
    reportsStatusF(onReportRequested = createStatusReport) {
      case Start ⇒
        hives = createInitialHives()
        context.become(receiveRunning)
    }
  }

  def receiveRunning: Receive = runningWithPause(onPause = receivePause) {
    reportsStatusF(onReportRequested = createStatusReport) {
      case cmd: AggregateRootCommand ⇒
        commandsReceived = commandsReceived + 1
        hives.find(_._1(cmd)) match {
          case Some(hive) ⇒
            hive._2 forward cmd
          case None ⇒
            sender() ! CommandNotAccepted(cmd.commandId, IllegalOperationProblem(s"There is no hive for command with id ${cmd.commandId.value}."))
        }

      case Terminated(actor) ⇒
        hives = hives.filterNot(_._2 == actor)
        reportCriticalFailure(UnspecifiedProblem(s"Hive ${actor.path.name} terminated."))
        logError(s"Hive ${actor.path.name} terminated.")
    }
  }

  def receivePause: Receive = pause(onResume = receiveRunning) {
    reportsStatusF(onReportRequested = createStatusReport) {
      case cmd: AggregateRootCommand ⇒
        commandsReceived = commandsReceived + 1
        sender() ! CommandNotAccepted(cmd.commandId, ServiceNotAvailableProblem("I'm taking a break."))

      case Terminated(actor) ⇒
        hives = hives.filterNot(_._2 == actor)
        reportCriticalFailure(UnspecifiedProblem(s"Hive ${actor.path.name} terminated."))
        logError(s"Hive ${actor.path.name} terminated.")
    }
  }

  def receive: Receive = receiveInitialize

  private def createInitialHives(): List[(AggregateRootCommand ⇒ Boolean, ActorRef)] = {
    hiveSelector.map {
      case (descriptor, f) ⇒
        val props = hiveFactory.props(descriptor).resultOrEscalate
        val actor = context.actorOf(props, s"hive-${descriptor.value}")
        context watch actor
        (f, actor)
    }.toList
  }

  //  private def createInitialHives(): List[ActorRef] = {
  //    import akka.stream.OverflowStrategy
  //    val commandsSource = Source(ActorPublisher[AggregateRootCommand](self)).runWith(Sink.fanoutPublisher[AggregateRootCommand](1, AlmMath.nextPowerOf2(hiveSelector.size)))
  //    hiveSelector.map {
  //      case (descriptor, f) ⇒
  //        val props = hiveFactory.props(descriptor).resultOrEscalate
  //        val actor = context.actorOf(props, s"hive-${descriptor.value}")
  //        context watch actor
  //        val hive = Sink(ActorSubscriber[AggregateRootCommand](actor))
  //        Source(commandsSource).buffer(16, OverflowStrategy.backpressure).filter(cmd ⇒ f(cmd)).to(hive).run()
  //        actor
  //    }.toList
  //  }

  def createStatusReport(options: StatusReportOptions): AlmFuture[StatusReport] = {
    val rep = StatusReport(s"AggregateRootNexus-Report") ~
      ("number-of-commands-received" -> commandsReceived)

    appendToReportFromCollector(rep)(options)
  }

  override def preStart() {
    super.preStart()
    logInfo("Start")
    registerComponentControl()
    registerStatusReporter(description = Some("The nexus that is the owner of the hives"))
    context.parent ! ActorMessages.ConsiderMeForReporting
    self ! Start
  }

  override def postStop() {
    super.postStop()
    deregisterStatusReporter()
    deregisterComponentControl()
    logWarning("Stopped.")
  }

}
