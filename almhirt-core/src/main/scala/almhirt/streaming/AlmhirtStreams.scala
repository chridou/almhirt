package almhirt.streaming

import scala.concurrent.duration._
import scalaz.Validation.FlatMap._
import akka.dispatch.Dispatcher
import akka.actor._
import akka.pattern._
import org.reactivestreams.{ Subscriber, Subscription, Publisher }
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }
import almhirt.common._
import almhirt.almfuture.all._

trait CanDispatchEvents {
  def eventBroker: StreamBroker[Event]
}
trait EventStream {
  def eventStream: Publisher[Event]
}

trait AlmhirtStreams extends EventStream with CanDispatchEvents

object AlmhirtStreams {
  import akka.stream.scaladsl._
  import akka.stream.OverflowStrategy

  def apply(supervisorName: String)(maxDur: FiniteDuration)(implicit actorRefFactory: ActorRefFactory): AlmFuture[AlmhirtStreams with Stoppable] =
    create(
      supervisorName,
      maxDur,
      actorRefFactory,
      0,
      1,
      16,
      0,
      1,
      16,
      true,
      true)

  def apply(
    supervisorName: String,
    eventBufferSize: Int,
    initialFanoutEvents: Int,
    maxFanoutEvents: Int,
    commandBufferSize: Int,
    initialFanoutCommands: Int,
    maxFanoutCommands: Int,
    soakEvents: Boolean,
    soakCommands: Boolean)(maxDur: FiniteDuration)(implicit actorRefFactory: ActorRefFactory): AlmFuture[AlmhirtStreams with Stoppable] =
    create(
      supervisorName,
      maxDur,
      actorRefFactory,
      eventBufferSize,
      initialFanoutEvents,
      maxFanoutEvents,
      commandBufferSize,
      initialFanoutCommands,
      maxFanoutCommands,
      soakEvents,
      soakCommands)

  private[almhirt] def createInternal(actorRefFactory: ActorRefFactory, config: com.typesafe.config.Config): AlmFuture[AlmhirtStreams with Stoppable] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    AlmFuture.completed {
      for {
        configSection ← config.v[com.typesafe.config.Config]("almhirt.streams")
        soakCommands ← configSection.v[Boolean]("soak-commands")
        soakEvents ← configSection.v[Boolean]("soak-events")
        commandBufferSize ← configSection.v[Int]("command-buffer-size")
          .constrained(_ >= 0, x ⇒ s"command-buffer-size must be grater or equal 0, not $x.")
        eventBufferSize ← configSection.v[Int]("event-buffer-size")
          .constrained(_ >= 0, x ⇒ s"event-buffer-size must be grater or equal 0, not $x.")
        initialFanoutCommands ← configSection.v[Int]("initial-commands-fanout-buffer-size")
          .constrained(x ⇒ AlmMath.nextPowerOf2(x) == x, x ⇒ s"initial-commands-fanout-buffer-size must be a power of 2 and not $x.")
        maxFanoutCommands ← configSection.v[Int]("max-commands-fanout-buffer-size")
          .constrained(x ⇒ AlmMath.nextPowerOf2(x) == x, x ⇒ s"max-commands-fanout-buffer-size must be a power of 2 and not $x.")
        initialFanoutEvents ← configSection.v[Int]("initial-events-fanout-buffer-size")
          .constrained(x ⇒ AlmMath.nextPowerOf2(x) == x, x ⇒ s"initial-events-fanout-buffer-size must be a power of 2 and not $x.")
        maxFanoutEvents ← configSection.v[Int]("max-events-fanout-buffer-size")
          .constrained(x ⇒ AlmMath.nextPowerOf2(x) == x, x ⇒ s"imax-events-fanout-buffer-size must be a power of 2 and not $x.")
      } yield (
        soakCommands,
        soakEvents,
        commandBufferSize,
        eventBufferSize,
        initialFanoutCommands,
        maxFanoutCommands,
        initialFanoutEvents,
        maxFanoutEvents)
    }.flatMap {
      case (soakCommands, soakEvents, commandBufferSize, eventBufferSize, initialFanoutCommands, maxFanoutCommands, initialFanoutEvents, maxFanoutEvents) ⇒
        create("streams", 2.seconds, actorRefFactory,
          eventBufferSize, initialFanoutEvents, maxFanoutEvents,
          commandBufferSize, initialFanoutCommands, maxFanoutCommands,
          soakEvents, soakCommands)
    }(actorRefFactory.dispatcher)
  }

  private def create(
    supervisorName: String,
    maxDur: FiniteDuration,
    actorRefFactory: ActorRefFactory,
    eventBufferSize: Int,
    initialFanoutEvents: Int,
    maxFanoutEvents: Int,
    commandBufferSize: Int,
    initialFanoutCommands: Int,
    maxFanoutCommands: Int,
    soakEvents: Boolean,
    soakCommands: Boolean): AlmFuture[AlmhirtStreams with Stoppable] = {
    implicit val ctx = actorRefFactory.dispatcher
    val supervisorProps = Props(new Actor with ActorLogging {
      override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 0) {
        case scala.util.control.NonFatal(e) ⇒
          log.error(e, "Escalating!")
          SupervisorStrategy.Escalate
      }

      implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)
      def receiveInit: Receive = {
        case "get_streams" ⇒
          val streams: AlmhirtStreams with Stoppable = {
            val eventShipperActor = context.actorOf(StreamShipper.props(), "event-broker")
            val (eventShipperIn, eventShipperOut, stopEventShipper) = StreamShipper[Event](eventShipperActor)

            val eventsSink = Sink.asPublisher[Event](fanout = true)
            val eventFlow =
              if (eventBufferSize > 0)
                Flow[Event].buffer(eventBufferSize, OverflowStrategy.backpressure)
              else
                Flow[Event]
            val (_, eventsPub) = eventFlow.runWith(Source.fromPublisher(eventShipperOut), eventsSink)

            if (soakEvents)
              Source.fromPublisher(eventsPub).to(Sink.ignore)

            new AlmhirtStreams with Stoppable {
              override val eventBroker = eventShipperIn
              override val eventStream = eventsPub
              def stop() {
                context.stop(self)
              }
            }
          }
          sender() ! streams
          context.become(receiveRunning)

      }

      def receiveRunning: Receive = {
        case x ⇒ sys.error(s"Received a message ${x}")
      }

      def receive: Receive = receiveInit

      override def preRestart(reason: Throwable, message: Option[Any]) {
        sys.error(s"""Not restartable! Error: "${reason.getMessage} Message: $message""")
      }

    })
    val props = supervisorProps
    val supervisor = actorRefFactory.actorOf(props, supervisorName)

    (supervisor ? "get_streams")(maxDur).mapCastTo[AlmhirtStreams with Stoppable]
  }
}


