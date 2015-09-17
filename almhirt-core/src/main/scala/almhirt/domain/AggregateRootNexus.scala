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
import org.reactivestreams.Subscriber
import akka.stream.actor._
import akka.stream.scaladsl._
import akka.stream.impl.ActorProcessor

object AggregateRootNexus {
  def apply(nexusActor: ActorRef): Subscriber[AggregateRootCommand] =
    ActorSubscriber[AggregateRootCommand](nexusActor)

  def propsRaw(hiveSelector: HiveSelector, hiveFactory: AggregateRootHiveFactory, commandsPublisher: Option[Publisher[Command]])(implicit ctx: AlmhirtContext): Props =
    Props(new AggregateRootNexus(commandsPublisher, hiveSelector, hiveFactory))

  def propsRaw(hiveSelector: HiveSelector, hiveFactory: AggregateRootHiveFactory)(implicit ctx: AlmhirtContext): Props =
    Props(new AggregateRootNexus(Some(ctx.commandStream), hiveSelector, hiveFactory))

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
    commandsPublisher: Option[Publisher[Command]],
    hiveSelector: HiveSelector,
    hiveFactory: AggregateRootHiveFactory)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorSubscriber with ActorPublisher[AggregateRootCommand] with ActorLogging with ControllableActor with StatusReportingActor {

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)

  implicit val executor = almhirtContext.futuresContext

  override val componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))

  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn: Exception ⇒
        logError(s"Handling escalated error for ${sender.path.name} with a action Escalate.", exn)
        reportCriticalFailure(exn)
        Escalate
    }

  private var hives: List[ActorRef] = Nil

  override val requestStrategy = ZeroRequestStrategy

  case object Start

  private var commandsReceived = 0L

  def receiveInitialize: Receive = startup() {
    reportsStatusF(onReportRequested = createStatusReport) {
      case Start ⇒
        commandsPublisher.foreach(cmdPub ⇒
          Source[Command](cmdPub).collect { case e: AggregateRootCommand ⇒ e }.to(Sink(ActorSubscriber[AggregateRootCommand](self))).run())

        hives = createInitialHives()
        request(1)
        context.become(receiveRunning(None))
    }
  }

  def receiveRunning(inFlight: Option[AggregateRootCommand]): Receive = running() {
    reportsStatusF(onReportRequested = createStatusReport) {
      case ActorSubscriberMessage.OnNext(next: AggregateRootCommand) ⇒
        commandsReceived = commandsReceived + 1
        inFlight match {
          case None if totalDemand > 0 ⇒
            onNext(next)
            request(1)
          case None ⇒
            context.become(receiveRunning(Some(next)))
          case Some(buff) ⇒
            val msg = "Received an element even though my buffer is full."
            onError(new Exception(msg))
            cancel()
        }

      case ActorPublisherMessage.Request(amount) ⇒
        inFlight match {
          case Some(buf) if totalDemand > 0 ⇒
            request(1)
            onNext(buf)
            context.become(receiveRunning(None))
          case _ ⇒
            ()
        }

      case ActorSubscriberMessage.OnError(exn) ⇒
        logError("Received an error via the stream.", exn)
        throw exn

      case ActorPublisherMessage.Cancel ⇒
        logWarning("The fanout publisher cancelled it's subscription. Propagating cancellation.")
        cancel()

      case Terminated(actor) ⇒
        logWarning(s"Hive ${actor.path.name} terminated.")
    }
  }

  def receive: Receive = receiveInitialize

  private def createInitialHives(): List[ActorRef] = {
    import akka.stream.OverflowStrategy
    val commandsSource = Source(ActorPublisher[AggregateRootCommand](self)).runWith(Sink.fanoutPublisher[AggregateRootCommand](1, AlmMath.nextPowerOf2(hiveSelector.size)))
    hiveSelector.map {
      case (descriptor, f) ⇒
        val props = hiveFactory.props(descriptor).resultOrEscalate
        val actor = context.actorOf(props, s"hive-${descriptor.value}")
        context watch actor
        val hive = Sink(ActorSubscriber[AggregateRootCommand](actor))
        Source(commandsSource).buffer(16, OverflowStrategy.backpressure).filter(cmd ⇒ f(cmd)).to(hive).run()
        actor
    }.toList
  }

  def createStatusReport(options: ReportOptions): AlmFuture[StatusReport] = {
    val rep = StatusReport(s"AggregateRootNexus-Report") ~
      ("number-of-commands-received" -> commandsReceived)

    val hiveReportsFs = hives.map(hive ⇒ queryReportFromActor(hive, options).materializedValidation.map(res ⇒ (s"${hive.path.name}-status", res)))
    for {
      hivesResults ← AlmFuture.sequence(hiveReportsFs)
    } yield {
      rep ~~ (hivesResults.map { tuple ⇒ toFieldFromValidation(tuple) })
    }
  }

  override def preStart() {
    super.preStart()
    logInfo("Start")
    registerComponentControl()
    registerStatusReporter(description = None)
    self ! Start
  }

  override def postStop() {
    super.postStop()
    deregisterStatusReporter()
    deregisterComponentControl()
    logWarning("Stopped.")
  }

}
