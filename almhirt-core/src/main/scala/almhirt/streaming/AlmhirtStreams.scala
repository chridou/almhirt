package almhirt.streaming

import scala.concurrent.duration._
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
trait CanDispatchCommands {
  def commandBroker: StreamBroker[Command]
}
trait EventStream {
  def eventStream: Publisher[Event]
}
trait CommandStream {
  def commandStream: Publisher[Command]
}

trait AlmhirtStreams extends EventStream with CommandStream with CanDispatchEvents with CanDispatchCommands

object AlmhirtStreams {
  import akka.stream.scaladsl2._
  import akka.stream.MaterializerSettings
  def apply(supervisorName: String, maxDur: FiniteDuration = 2.seconds)(implicit actorRefFactory: ActorRefFactory): AlmFuture[AlmhirtStreams with Stoppable] =
    create(
      supervisorName,
      maxDur,
      actorRefFactory,
      None,
      1,
      16,
      1,
      16,
      true,
      true)

  private[almhirt] def createInternal(actorRefFactory: ActorRefFactory, config: com.typesafe.config.Config): AlmFuture[AlmhirtStreams with Stoppable] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    AlmFuture.completed {
      for {
        configSection <- config.v[com.typesafe.config.Config]("almhirt.context.streams")
        useDedicatedDispatcher <- configSection.v[Boolean]("use-dedicated-dispatcher")
        soakCommands <- configSection.v[Boolean]("soak-commands")
        soakEvents <- configSection.v[Boolean]("soak-events")
        initialFanoutCommands <- configSection.v[Int]("initial-commands-fanout-buffer-size").constrained(x => AlmMath.nextPowerOf2(x) == x, x => s"initial-commands-fanout-buffer-size must be a power of 2 and not $x.")
        maxFanoutCommands <- configSection.v[Int]("max-commands-fanout-buffer-size").constrained(x => AlmMath.nextPowerOf2(x) == x, x => s"max-commands-fanout-buffer-size must be a power of 2 and not $x.")
        initialFanoutEvents <- configSection.v[Int]("initial-events-fanout-buffer-size").constrained(x => AlmMath.nextPowerOf2(x) == x, x => s"initial-events-fanout-buffer-size must be a power of 2 and not $x.")
        maxFanoutEvents <- configSection.v[Int]("max-events-fanout-buffer-size").constrained(x => AlmMath.nextPowerOf2(x) == x, x => s"imax-events-fanout-buffer-size must be a power of 2 and not $x.")
      } yield (
        useDedicatedDispatcher,  
        soakCommands,
        soakEvents,
        initialFanoutCommands,
        maxFanoutCommands,
        initialFanoutEvents,
        maxFanoutEvents)
    }.flatMap {
      case (useDedicatedDispatcher, soakCommands, soakEvents, initialFanoutCommands, maxFanoutCommands, initialFanoutEvents, maxFanoutEvents) =>
        val dispatcherName = if(useDedicatedDispatcher) Some("almhirt.context.dispatchers.streaming-dispatcher") else None
        create("streams", 2.seconds, actorRefFactory, dispatcherName,
          initialFanoutEvents, maxFanoutEvents, initialFanoutCommands, maxFanoutCommands, soakEvents, soakCommands)
    }(actorRefFactory.dispatcher)
  }

  private def create(
    supervisorName: String,
    maxDur: FiniteDuration,
    actorRefFactory: ActorRefFactory,
    dispatcherName: Option[String],
    initialFanoutEvents: Int,
    maxFanoutEvents: Int,
    initialFanoutCommands: Int,
    maxFanoutCommands: Int,
    soakEvents: Boolean,
    soakCommands: Boolean): AlmFuture[AlmhirtStreams with Stoppable] = {
    implicit val ctx = actorRefFactory.dispatcher
    val supervisorProps = Props(new Actor with ImplicitFlowMaterializer {
      def receive: Receive = {
        case "get_streams" ⇒
          val streams: AlmhirtStreams with Stoppable = {
            val eventShipperActor = context.actorOf(StreamShipper.props(), "event-broker")
            val (eventShipperIn, eventShipperOut, stopEventShipper) = StreamShipper[Event](eventShipperActor)

            val eventPub = cheat(FlowFrom[Event](eventShipperOut), initialFanoutEvents, maxFanoutEvents, "eventPub")(context)
            if (soakEvents)
              FlowFrom(eventPub).withSink(BlackholeSink).run()

            // commands

            val commandShipperActor = actorRefFactory.actorOf(StreamShipper.props(), "command-broker")
            val (commandShipperIn, commandShipperOut, stopCommandShipper) = StreamShipper[Command](commandShipperActor)

            val commandPub = cheat(FlowFrom[Command](commandShipperOut), initialFanoutCommands, maxFanoutCommands, "commandPub")(context)
            if (soakCommands)
              FlowFrom(commandPub).withSink(BlackholeSink).run()

            new AlmhirtStreams with Stoppable {
              override val eventBroker = eventShipperIn
              override val eventStream = eventPub
              override val commandBroker = commandShipperIn
              override val commandStream = commandPub
              def stop() {
                context.stop(self)
              }
            }
          }
          sender() ! streams
      }
    })
    val props =
      dispatcherName match {
        case None => supervisorProps
        case Some(dpname) => supervisorProps.withDispatcher(dpname)
      }
    val supervisor = actorRefFactory.actorOf(props, supervisorName)

    (supervisor ? "get_streams")(maxDur).successfulAlmFuture[AlmhirtStreams with Stoppable]
  }

  private def cheat[T](f: FlowWithSource[T, T], initialFanout: Int, maxFanout: Int, name: String)(factory: ActorRefFactory): Publisher[T] = {
    implicit val ctx = factory.dispatcher
    val actor = factory.actorOf(Props(new Actor with ImplicitFlowMaterializer {
      def receive: Receive = {
        case "!" ⇒
          sender() ! (f.toFanoutPublisher(initialFanout, maxFanout))
      }
    }), name)

    (actor ? "!")(1.second).successfulAlmFuture[Publisher[T]].awaitResultOrEscalate(1.second)

  }

}


