package almhirt.streaming

import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import org.reactivestreams.{ Subscriber, Subscription, Publisher }
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }
import almhirt.common._
import almhirt.almfuture.all._
import akka.stream.OverflowStrategy

trait CanDispatchEvents {
  def eventBroker: StreamBroker[Event]
}

trait CanDispatchCommands {
  def commandBroker: StreamBroker[Command]
}

trait EventStreams {
  def eventStream: Publisher[Event]
}

trait CommandStreams {
  def commandStream: Publisher[Command]
}

trait AlmhirtStreams extends EventStreams with CommandStreams with CanDispatchEvents with CanDispatchCommands

object AlmhirtStreams {
  import akka.stream.scaladsl2._
  import akka.stream.MaterializerSettings
  def apply(supervisorName: String, maxDur: FiniteDuration = 2.seconds)(implicit actorRefFactory: ActorRefFactory): AlmFuture[AlmhirtStreams with Stoppable] = {
    implicit val ctx = actorRefFactory.dispatcher
    val supervisorProps = Props(new Actor with ImplicitFlowMaterializer {
      def receive: Receive = {
        case "get_streams" =>
          val streams: AlmhirtStreams with Stoppable = {
            val eventShipperActor = context.actorOf(StreamShipper.props(), "event-broker")
            val (eventShipperIn, eventShipperOut, stopEventShipper) = StreamShipper[Event](eventShipperActor)

            val eventPub = cheat(FlowFrom[Event](eventShipperOut), "eventPub")(context)
            
            // commands
            
            val commandShipperActor = actorRefFactory.actorOf(StreamShipper.props(), "command-broker")
            val (commandShipperIn, commandShipperOut, stopCommandShipper) = StreamShipper[Command](commandShipperActor)

            val commandPub = cheat(FlowFrom[Command](commandShipperOut), "commandPub")(context)

            new AlmhirtStreams with Stoppable {
              override val eventBroker = eventShipperIn
              override val eventStream = eventPub
              override val commandBroker = commandShipperIn
              override val commandStream = commandPub
              def stop() {
                stopEventShipper.stop()
                stopCommandShipper.stop()
                context.stop(self)
              }
            }
          }
          sender() ! streams
      }
    })
    val supervisor = actorRefFactory.actorOf(supervisorProps, supervisorName)

    (supervisor ? "get_streams")(maxDur).successfulAlmFuture[AlmhirtStreams with Stoppable]
  }

  private def cheat[T](f: FlowWithSource[T, T], name: String)(factory: ActorRefFactory): Publisher[T] = {
    implicit val ctx = factory.dispatcher
    val actor = factory.actorOf(Props(new Actor with ImplicitFlowMaterializer {
      def receive: Receive = {
        case "!" =>
          sender() ! (f.toFanoutPublisher(1, 32))
      }
    }), name)
    
    (actor ? "!")(1.second).successfulAlmFuture[Publisher[T]].awaitResultOrEscalate(1.second)
    
  }

}

private[streaming] class DevNullSubscriber[T]() extends Subscriber[T] {
  private[this] var sub: Option[Subscription] = None

  private def requestMore() = sub.map(_.request(1))

  override def onError(cause: Throwable): Unit =
    scala.sys.error(cause.getMessage)

  override def onSubscribe(subscription: Subscription): Unit = {
    sub = Some(subscription)
    requestMore()
  }

  override def onComplete(): Unit = {
  }

  override def onNext(element: T): Unit = {
    requestMore()
  }
}
