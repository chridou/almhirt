package almhirt.components

import akka.actor._
import almhirt.common._
import almhirt.context.AlmhirtContext
import org.reactivestreams.Publisher
import akka.stream.scaladsl2._
import akka.stream.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl2.ImplicitFlowMaterializer

object EventSinkHub {
  /** [Name, (Props, Option[Filter])] */
  type EventSinkHubMemberFactories = Map[String, (Props, Option[ProcessorFlow[Event, Event]])]

  def propsRaw(factories: EventSinkHub.EventSinkHubMemberFactories, buffersize: Option[Int])(implicit ctx: AlmhirtContext): Props =
    Props(new EventSinksSupervisorImpl(factories, buffersize))

  def props(factories: EventSinkHub.EventSinkHubMemberFactories)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    for {
      section <- ctx.config.v[com.typesafe.config.Config]("almhirt.components.event-sink-hub")
      buffersize <- section.v[Int]("buffer-size").constrained(_ >= 0, n => s""""buffer-size" must be greater or equal than 0, not $n.""")
    } yield propsRaw(factories, Some(buffersize))
  }

  val actorname = "event-sink-hub"
}

private[almhirt] class EventSinksSupervisorImpl(factories: EventSinkHub.EventSinkHubMemberFactories, buffersize: Option[Int])(implicit ctx: AlmhirtContext) extends ActorSubscriber with ActorLogging with ImplicitFlowMaterializer {
  override val requestStrategy = ZeroRequestStrategy
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
      val fanout =
        buffersize match {
          case Some(bfs) if bfs > 0 =>
            FlowFrom[Event](ctx.eventStream).buffer(bfs, OverflowStrategy.backpressure).toFanoutPublisher(1, AlmMath.nextPowerOf2(factories.size))
          case None =>
            FlowFrom[Event](ctx.eventStream).toFanoutPublisher(1, AlmMath.nextPowerOf2(factories.size))
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