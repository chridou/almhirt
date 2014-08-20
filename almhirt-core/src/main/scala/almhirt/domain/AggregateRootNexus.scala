package almhirt.domain

import akka.actor._
import org.reactivestreams.api.Producer
import akka.stream.actor.ActorConsumer
import akka.stream.scaladsl.Flow
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import almhirt.common._

class AggregateRootNexus(
  override val aggregateCommandsProducer: Producer[AggregateCommand],
  override val filters: Seq[Int => AggregateCommand => Boolean],
  override val hiveFactory: AggregateRootHiveFactory) extends Actor with ActorLogging with AggregateRootNexusInternal

private[almhirt] object AggregateRootNexusInternal {
  case object Start
}

private[almhirt] trait AggregateRootNexusInternal { me: Actor with ActorLogging =>
  import AggregateRootNexusInternal._

  def aggregateCommandsProducer: Producer[AggregateCommand]
  def hiveFactory: AggregateRootHiveFactory

  /**
   * Must be n filters that that should return true when the supplied Int matches the filter
   */
  def filters: Seq[Int => AggregateCommand => Boolean]

  def receiveInitialize: Receive = {
    case Start =>
      createInitialHives()
      context.become(receiveRunning)
  }

  def receiveRunning: Receive = {
    case Terminated(actor) =>
      log.info(s"Hive $actor terminated")
  }

  def receive: Receive = receiveInitialize

  private def createInitialHives() {
    val mat = FlowMaterializer(MaterializerSettings())
    filters.zipWithIndex.foreach {
      case (f, n) =>
        val props = hiveFactory.props
        val actor = context.actorOf(props, s"hive-$n")
        context watch actor
        val consumer = ActorConsumer[AggregateCommand](actor)
        Flow(aggregateCommandsProducer).filter(f(n)).produceTo(mat, consumer)
    }
  }
}