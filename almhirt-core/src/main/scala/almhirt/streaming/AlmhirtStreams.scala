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
  def systemEventStream: Publisher[SystemEvent]
  def domainEventStream: Publisher[DomainEvent]
  def aggregateEventStream: Publisher[AggregateRootEvent]
}
trait CommandStreams {
  def commandStream: Publisher[Command]
  def systemCommandStream: Publisher[SystemCommand]
  def domainCommandStream: Publisher[DomainCommand]
  def aggregateCommandStream: Publisher[AggregateRootCommand]
}

trait AlmhirtStreams extends EventStreams with CommandStreams with CanDispatchEvents with CanDispatchCommands

object AlmhirtStreams {
  import akka.stream.scaladsl2._
  import akka.stream.MaterializerSettings
  def apply(supervisorName: String, maxDur: FiniteDuration = 2.seconds)(implicit actorRefFactory: ActorRefFactory): AlmFuture[AlmhirtStreams with Stoppable] = 
    create(supervisorName, maxDur, actorRefFactory)
    
  private def create(supervisorName: String, maxDur: FiniteDuration = 2.seconds, actorRefFactory: ActorRefFactory): AlmFuture[AlmhirtStreams with Stoppable] = {
    implicit val ctx = actorRefFactory.dispatcher
    val supervisorProps = Props(new Actor with ImplicitFlowMaterializer {
      def receive: Receive = {
        case "get_streams" =>
          val streams: AlmhirtStreams with Stoppable = {
            val eventShipperActor = context.actorOf(StreamShipper.props(), "event-broker")
            val (eventShipperIn, eventShipperOut, stopEventShipper) = StreamShipper[Event](eventShipperActor)

            val eventPub = cheat(FlowFrom[Event](eventShipperOut), "eventPub")(context)
            FlowFrom(eventPub).withSink(BlackholeSink).run()

            val systemEventPub = cheat(FlowFrom[Event](eventPub).collect { case e: SystemEvent => e }, "systemEventPub")(context)
            FlowFrom(systemEventPub).withSink(BlackholeSink).run()

            val domainEventPub = cheat(FlowFrom[Event](eventPub).collect { case e: DomainEvent => e }, "domainEventPub")(context)
            FlowFrom(domainEventPub).withSink(BlackholeSink).run()

            val arEventPub = cheat(FlowFrom[Event](eventPub).collect { case e: AggregateRootEvent => e }, "arEventPub")(context)
            FlowFrom(arEventPub).withSink(BlackholeSink).run()

            // commands

            val commandShipperActor = actorRefFactory.actorOf(StreamShipper.props(), "command-broker")
            val (commandShipperIn, commandShipperOut, stopCommandShipper) = StreamShipper[Command](commandShipperActor)

            val commandPub = cheat(FlowFrom[Command](commandShipperOut), "commandPub")(context)
            FlowFrom(commandPub).withSink(BlackholeSink).run()

            val systemCommandPub = cheat(FlowFrom[Command](commandPub).collect { case e: SystemCommand => e }, "systemCommandPub")(context)
            FlowFrom(systemCommandPub).withSink(BlackholeSink).run()

            val domainCommandPub = cheat(FlowFrom[Command](commandPub).collect { case e: DomainCommand => e }, "domainCommandPub")(context)
            FlowFrom(domainCommandPub).withSink(BlackholeSink).run()

            val arCommandPub = cheat(FlowFrom[Command](commandPub).collect { case e: AggregateRootCommand => e }, "arCommandPub")(context)
            FlowFrom(arCommandPub).withSink(BlackholeSink).run()

            new AlmhirtStreams with Stoppable {
              override val eventBroker = eventShipperIn
              override val eventStream = eventPub
              override val systemEventStream = systemEventPub
              override val domainEventStream = domainEventPub
              override val aggregateEventStream = arEventPub
              override val commandBroker = commandShipperIn
              override val commandStream = commandPub
              override val systemCommandStream = systemCommandPub
              override val domainCommandStream = domainCommandPub
              override val aggregateCommandStream = arCommandPub
              def stop() {
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


