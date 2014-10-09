package almhirt.components

import scala.concurrent.duration._
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.tracking.CorrelationId
import almhirt.context.AlmhirtContext
import almhirt.akkax._
import almhirt.streaming.ActorDevNullSubscriberWithAutoSubscribe
import org.reactivestreams.Publisher
import akka.stream.scaladsl2._
import akka.stream.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl2.ImplicitFlowMaterializer

object EventSinkHubMessage {
   case object ReportEventSinkStates
   sealed trait ReportEventSinkStatesRsp
   final case class EventSinkStates(states: Map[String, AlmCircuitBreaker.State]) extends ReportEventSinkStatesRsp
   final case class ReportEventSinkStatesFailed(problem: Problem) extends ReportEventSinkStatesRsp
   
   final case class AttemptResetComponentCircuit(name: String)
}


object EventSinkHub {
  /** [Name, (Props, Option[Filter])] */
  type EventSinkHubMemberFactories = Map[String, (Props, Option[ProcessorFlow[Event, Event]])]

  def propsRaw(factories: EventSinkHub.EventSinkHubMemberFactories, buffersize: Option[Int], withBlackHoleIfEmpty: Boolean)(implicit ctx: AlmhirtContext): Props =
    Props(new EventSinksSupervisorImpl(factories, buffersize, withBlackHoleIfEmpty))

  def props(factories: EventSinkHub.EventSinkHubMemberFactories)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    for {
      section <- ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.event-sink-hub")
      enabled <- section.v[Boolean]("enabled")
      buffersize <- section.v[Int]("buffer-size").constrained(_ >= 0, n ⇒ s""""buffer-size" must be greater or equal than 0, not $n.""")
      withBlackHoleIfEmpty <- section.v[Boolean]("with-black-hole-if-empty")
    } yield propsRaw(if (enabled) factories else Map.empty, Some(buffersize), withBlackHoleIfEmpty)
  }

  val actorname = "event-sink-hub"
}

private[almhirt] class EventSinksSupervisorImpl(factories: EventSinkHub.EventSinkHubMemberFactories, buffersize: Option[Int], withBlackHoleIfEmpty: Boolean)(implicit ctx: AlmhirtContext) extends ActorSubscriber with ActorLogging with ImplicitFlowMaterializer {
  override val requestStrategy = ZeroRequestStrategy
  case object Start

  def receiveInitialize: Receive = {
    case Start ⇒
      createInitialMembers()
      context.become(receiveRunning)
      
    case EventSinkHubMessage.ReportEventSinkStates =>
      reportEventSinkStates(sender())
      
    case EventSinkHubMessage.AttemptResetComponentCircuit(name) =>
      context.child(name).foreach(_ ! ActorMessages.AttemptResetCircuitBreaker)
  }

  def receiveRunning: Receive = {
    case Terminated(actor) ⇒
      log.info(s"Member ${actor.path.name} terminated.")
      
    case EventSinkHubMessage.ReportEventSinkStates =>
      reportEventSinkStates(sender())
      
    case EventSinkHubMessage.AttemptResetComponentCircuit(name) =>
      context.child(name).foreach(_ ! ActorMessages.AttemptResetCircuitBreaker)
  }

  def receive: Receive = receiveInitialize

  private def createInitialMembers() {
    if (!factories.isEmpty) {
      val fanout =
        buffersize match {
          case Some(bfs) if bfs > 0 ⇒
            FlowFrom[Event](ctx.eventStream).buffer(bfs, OverflowStrategy.backpressure).toFanoutPublisher(1, AlmMath.nextPowerOf2(factories.size))
          case None ⇒
            FlowFrom[Event](ctx.eventStream).toFanoutPublisher(1, AlmMath.nextPowerOf2(factories.size))
        }
      factories.foreach {
        case (name, (props, filterOpt)) ⇒
          val actor = context.actorOf(props, name)
          context watch actor
          log.info(s"""Create subrcriber "$name".""")
          val subscriber = ActorSubscriber[Event](actor)
          filterOpt match {
            case Some(filter) ⇒
              filter.withSource(PublisherSource(fanout)).publishTo(subscriber)
            case None ⇒
              FlowFrom(fanout).publishTo(subscriber)
          }
      }
    } else if (factories.isEmpty && withBlackHoleIfEmpty) {
      log.warning("No members, but I created a black hole!")
      FlowFrom[Event](ctx.eventStream).withSink(BlackholeSink)
    } else {
      log.warning("No members. Nothing will be subscribed")
    }
  }
  
  private def reportEventSinkStates(receiver: ActorRef) {
    implicit val executor = ctx.futuresContext
    val futures = context.children.map(child => 
      (child ? ActorMessages.ReportCircuitBreakerState(CorrelationId(child.path.name)))(3.seconds)
      	.mapCastTo[ActorMessages.CurrentCircuitBreakerState].map(rsp => (rsp.id.value, rsp.state)))
      
    AlmFuture.sequence(futures.toSeq).onComplete(
       problem => receiver ! EventSinkHubMessage.ReportEventSinkStatesFailed(problem),
       states => receiver ! EventSinkHubMessage.EventSinkStates(states.toMap))
  }

  override def preStart() {
    self ! Start
  }

}