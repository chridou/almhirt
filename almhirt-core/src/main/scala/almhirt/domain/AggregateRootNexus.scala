package almhirt.domain

import akka.actor._
import org.reactivestreams.Publisher
import akka.stream.actor.ActorSubscriber
import akka.stream.scaladsl2._
import almhirt.common._
import almhirt.almvalidation.kit._

class AggregateRootNexus(
  override val commandsPublisher: Publisher[Command],
  override val hiveSelector: HiveSelector,
  override val hiveFactory: AggregateRootHiveFactory) extends Actor with ActorLogging with AggregateRootNexusSkeleton {
  
  override def preStart() {
    super.preStart()
    self ! AggregateRootNexusInternal.Start
  }
}

private[almhirt] object AggregateRootNexusInternal {
  case object Start
}

private[almhirt] trait AggregateRootNexusSkeleton { me: Actor with ActorLogging ⇒
  import AggregateRootNexusInternal._

  def commandsPublisher: Publisher[Command]
  def hiveFactory: AggregateRootHiveFactory

  /**
   * Each function must return true, if the command is a match for the given [[HiveDescriptor]].
   * Remember that the [[AggregateRootHiveFactory]] must return a hive for each possible descriptor.
   * Do not design your filters in a way, that multiple hives may contain the same aggregate root!
   */
  def hiveSelector: HiveSelector

  def receiveInitialize: Receive = {
    case Start ⇒
      createInitialHives()
      context.become(receiveRunning)
  }

  def receiveRunning: Receive = {
    case Terminated(actor) ⇒
      log.info(s"Hive ${actor.path.name} terminated.")
  }

  def receive: Receive = receiveInitialize

  private def createInitialHives() {
    implicit val mat = FlowMaterializer()
    val fanout = FlowFrom[Command](commandsPublisher).collect{ case e: AggregateRootCommand => e}.toFanoutPublisher(1, AlmMath.nextPowerOf2(hiveSelector.size))
    hiveSelector.foreach {
      case (descriptor, f) ⇒
        val props = hiveFactory.props(descriptor).resultOrEscalate
        val actor = context.actorOf(props, s"hive-${descriptor.value}")
        context watch actor
        val consumer = ActorSubscriber[AggregateRootCommand](actor)
        FlowFrom(fanout).filter(cmd ⇒ f(cmd)).publishTo(consumer)
    }
  }
}