package almhirt.components

import akka.actor._
import almhirt.common._
import akka.stream.scaladsl2._
import org.reactivestreams.Publisher
import akka.stream.actor.ActorSubscriber
import almhirt.context.AlmhirtContext

object EventSinkHub {
  /** [Name, (Props, Option[Filter])] */
  type EventSinkHubMemberFactories = Map[String, (Props, Option[ProcessorFlow[Event, Event]])]
  
  def props(factories: EventSinkHub.EventSinkHubMemberFactories, eventPublisher: Publisher[Event], buffersize: Option[Int]): Props =
    Props(new EventSinkHubImpl(factories, eventPublisher, buffersize))

  def props(factories: EventSinkHub.EventSinkHubMemberFactories, eventPublisher: Publisher[Event])(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    ???
  }
    
  val actorname = "event-sink-hub"
}

private[almhirt] class EventSinkHubImpl(factories: EventSinkHub.EventSinkHubMemberFactories, eventPublisher: Publisher[Event], buffersize: Option[Int]) extends Actor with ActorLogging {
  case object Start

  def receiveInitialize: Receive = {
    case Start ⇒
      createInitialMembers()
      context.become(receiveRunning)
  }

  def receiveRunning: Receive = {
    case Terminated(actor) ⇒
      log.info(s"Member ${actor.path.name} terminated.")
  }

  def receive: Receive = receiveInitialize

  private def createInitialMembers() {
    implicit val mat = FlowMaterializer()
    val fanout = FlowFrom[Event](eventPublisher).toFanoutPublisher(1, AlmMath.nextPowerOf2(factories.size))
    factories.foreach {
      case (name, (props, filterOpt)) ⇒
        val actor = context.actorOf(props, name)
        context watch actor
        val subscriber = ActorSubscriber[Event](actor)
        filterOpt match {
          case Some(filter) =>
            filter.withSource(PublisherSource(fanout)).publishTo(subscriber)
          case None =>
            FlowFrom(fanout).publishTo(subscriber)
        }
    }
  }

  override def preStart() {
    self ! Start
  }

}