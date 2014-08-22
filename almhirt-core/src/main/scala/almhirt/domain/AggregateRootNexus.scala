package almhirt.domain

import akka.actor._
import org.reactivestreams.api.Producer
import akka.stream.actor.ActorConsumer
import akka.stream.scaladsl.{ Flow, Duct }
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import almhirt.common._
import almhirt.almvalidation.kit._



class AggregateRootNexus(
  override val aggregateCommandsProducer: Producer[AggregateCommand],
  override val hiveSelector: HiveSelector,
  override val hiveFactory: AggregateRootHiveFactory) extends Actor with ActorLogging with AggregateRootNexusInternal {
  
  override def preStart() {
    super.preStart()
    self ! AggregateRootNexusInternal.Start
  }
}

private[almhirt] object AggregateRootNexusInternal {
  case object Start
}

private[almhirt] trait AggregateRootNexusInternal { me: Actor with ActorLogging =>
  import AggregateRootNexusInternal._

  def aggregateCommandsProducer: Producer[AggregateCommand]
  def hiveFactory: AggregateRootHiveFactory

  /**
   * Each function must return true, if the command is a match for the given [[HiveDescriptor]].
   * Remember that the [[AggregateRootHiveFactory]] must return a hive for each possible descriptor.
   * Do not design your filters in a way, that multiple hives may contain the same aggregate root!
   */
  def hiveSelector: HiveSelector

  def receiveInitialize: Receive = {
    case Start =>
      createInitialHives()
      context.become(receiveRunning)
  }

  def receiveRunning: Receive = {
    case Terminated(actor) =>
      log.info(s"Hive ${actor.path.name} terminated.")
  }

  def receive: Receive = receiveInitialize

  private def createInitialHives() {
    val mat = FlowMaterializer(MaterializerSettings())
    val (theConsumer, theProducer) = Duct[AggregateCommand].build(mat)
    hiveSelector.foreach {
      case (descriptor, f) =>
        val props = hiveFactory.props(descriptor).resultOrEscalate
        val actor = context.actorOf(props, s"hive-${descriptor.value}")
        context watch actor
        val consumer = ActorConsumer[AggregateCommand](actor)
        Flow(theProducer).filter(cmd => f(cmd)).produceTo(mat, consumer)
    }
    aggregateCommandsProducer.produceTo(theConsumer)
  }
}