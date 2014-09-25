package almhirt.components

import akka.actor._
import almhirt.common._
import almhirt.context.AlmhirtContext
import org.reactivestreams.Publisher
import akka.stream.scaladsl2._
import akka.stream.actor.ActorSubscriber
import akka.stream.OverflowStrategy

object EventSinkHub {
  /** [Name, (Props, Option[Filter])] */
  type EventSinkHubMemberFactories = Map[String, (Props, Option[ProcessorFlow[Event, Event]])]

  def props(factories: EventSinkHub.EventSinkHubMemberFactories, eventPublisher: Publisher[Event], buffersize: Option[Int]): Props =
    Props(new EventSinksSupervisorImpl(factories, eventPublisher, buffersize))

  def props(factories: EventSinkHub.EventSinkHubMemberFactories)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    for {
      configSection <- ctx.config.v[com.typesafe.config.Config]("almhirt.components.event-sink-hub")
      buffersize <- configSection.v[Int]("buffer-size").constrained(_ >= 0, n => s""""buffer-size" must be greater or equal than 0, not $n.""")
    } yield props(factories, ctx.eventStream, Some(buffersize))
  }

  val actorname = "event-sink-hub"
}

private[almhirt] class EventSinksSupervisorImpl(factories: EventSinkHub.EventSinkHubMemberFactories, eventPublisher: Publisher[Event], buffersize: Option[Int]) extends Actor with ActorLogging {
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
    if (!factories.isEmpty) {
      implicit val mat = FlowMaterializer()
      val fanout =
        buffersize match {
          case Some(bfs) if bfs > 0 =>
            FlowFrom[Event](eventPublisher).buffer(bfs, OverflowStrategy.backpressure).toFanoutPublisher(1, AlmMath.nextPowerOf2(factories.size))
          case None =>
            FlowFrom[Event](eventPublisher).toFanoutPublisher(1, AlmMath.nextPowerOf2(factories.size))
        }
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
    } else {
      log.warning("No members. Nothing will be subscribed")
    }
  }

  override def preStart() {
    self ! Start
  }

}