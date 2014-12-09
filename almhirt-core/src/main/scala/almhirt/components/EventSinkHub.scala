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
import akka.stream.scaladsl._
import akka.stream.actor._
import akka.stream.OverflowStrategy

object EventSinkHubMessage {
}

object EventSinkHub {
  /** [Name, (Props, Option[Filter])] */
  type EventSinkHubMemberFactories = Map[String, (Props, Option[Flow[Event, Event]])]

  def propsRaw(factories: EventSinkHub.EventSinkHubMemberFactories, buffersize: Option[Int], withBlackHoleIfEmpty: Boolean)(implicit ctx: AlmhirtContext): Props =
    Props(new EventSinksSupervisorImpl(factories, buffersize, withBlackHoleIfEmpty))

  def props(factories: EventSinkHub.EventSinkHubMemberFactories)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    for {
      section ← ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.event-sink-hub")
      enabled ← section.v[Boolean]("enabled")
      buffersize ← section.v[Int]("buffer-size").constrained(_ >= 0, n ⇒ s""""buffer-size" must be greater or equal than 0, not $n.""")
      withBlackHoleIfEmpty ← section.v[Boolean]("with-black-hole-if-empty")
    } yield propsRaw(if (enabled) factories else Map.empty, Some(buffersize), withBlackHoleIfEmpty)
  }

  val actorname = "event-sink-hub"
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.misc(root) / actorname
}

private[almhirt] class EventSinksSupervisorImpl(factories: EventSinkHub.EventSinkHubMemberFactories, buffersize: Option[Int], withBlackHoleIfEmpty: Boolean)(implicit ctx: AlmhirtContext) extends ActorSubscriber with ActorLogging with ImplicitFlowMaterializer {
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
      val eventsSourceFannedOut = Source(ctx.eventStream).runWith(Sink.fanoutPublisher[Event](1, AlmMath.nextPowerOf2(factories.size)))
      val flow =
        buffersize match {
          case Some(bfs) if bfs > 0 ⇒
            Flow[Event].buffer(bfs, OverflowStrategy.backpressure)
          case None ⇒
            Flow[Event]
        }
      factories.foreach {
        case (name, (props, filterOpt)) ⇒
          val actor = context.actorOf(props, name)
          context watch actor
          log.info(s"""Create subscriber "$name".""")
          val subscriber = ActorSubscriber[Event](actor)
          filterOpt match {
            case Some(filter) ⇒
              Source(eventsSourceFannedOut).via(filter).to(Sink(subscriber)).run()
            case None ⇒
              Source(eventsSourceFannedOut).to(Sink(subscriber)).run()
          }
      }
    } else if (factories.isEmpty && withBlackHoleIfEmpty) {
      log.warning("No members, but I created a black hole!")
      Source(ctx.eventStream).foreach(_ ⇒ ())
    } else {
      log.warning("No members. Nothing will be subscribed")
    }
  }

  override def preStart() {
    self ! Start
  }

}