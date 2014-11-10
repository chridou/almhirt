package almhirt.domain

import scala.concurrent.duration._
import akka.actor._
import org.reactivestreams.Publisher
import almhirt.common._
import almhirt.akkax._
import almhirt.almvalidation.kit._
import almhirt.context.AlmhirtContext
import org.reactivestreams.Subscriber
import akka.stream.actor._
import akka.stream.scaladsl2._
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
  hiveFactory: AggregateRootHiveFactory)(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with AlmActorLogging with ActorSubscriber with ActorPublisher[AggregateRootCommand] with ActorLogging with ImplicitFlowMaterializer {

  import akka.actor.SupervisorStrategy._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case exn: Exception ⇒
        logError(s"Handling escalated error for ${sender.path.name} with a action Escalate.", exn)
        Escalate
    }

  override val requestStrategy = ZeroRequestStrategy

  case object Start

  def receiveInitialize: Receive = {
    case Start ⇒
      commandsPublisher.foreach(cmdPub ⇒
        Source[Command](cmdPub).collect { case e: AggregateRootCommand ⇒ e }.connect(Sink(ActorSubscriber[AggregateRootCommand](self))).run())

      createInitialHives()
      request(1)
      context.become(receiveRunning(None))
  }

  def receiveRunning(buffered: Option[AggregateRootCommand]): Receive = {
    case ActorSubscriberMessage.OnNext(next: AggregateRootCommand) ⇒
      buffered match {
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
      buffered match {
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
      logInfo("The fanout publisher cancelled it's subscription. Propagating cancellation.")
      cancel()

    case Terminated(actor) ⇒
      logInfo(s"Hive ${actor.path.name} terminated.")
  }

  def receive: Receive = receiveInitialize

  private def createInitialHives() {
    val fanout = Source(ActorPublisher[AggregateRootCommand](self)).runWith(PublisherDrain.withFanout[AggregateRootCommand](1, AlmMath.nextPowerOf2(hiveSelector.size)))
    hiveSelector.foreach {
      case (descriptor, f) ⇒
        val props = hiveFactory.props(descriptor).resultOrEscalate
        val actor = context.actorOf(props, s"hive-${descriptor.value}")
        context watch actor
        val consumer = ActorSubscriber[AggregateRootCommand](actor)
        Source(fanout).filter(cmd ⇒ f(cmd)).connect(Sink(consumer)).run()
    }
  }

  override def preStart() {
    super.preStart()
    self ! Start
  }
}
