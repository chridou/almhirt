package almhirt.streaming

import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import akka.stream.scaladsl.{ Flow, Duct }
import org.reactivestreams.{ Subscriber, Subscription, Publisher }
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import almhirt.common._
import almhirt.almfuture.all._

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
  def apply()(implicit actorRefFactory: ActorRefFactory): AlmhirtStreams with Stoppable =
    apply("event-stream-input", "command-stream-input")

  def supervised(supervisorName: String, maxDur: FiniteDuration = 2.seconds)(implicit actorRefFactory: ActorRefFactory): AlmFuture[(AlmhirtStreams, Stoppable)] = {
    implicit val ctx = actorRefFactory.dispatcher
    val supervisorProps = Props(new Actor {
      val streams = apply("event-stream", "command-stream")(this.context)

      def receive: Receive = {
        case StopStreaming =>
          streams.stop()
        case "get_streams" =>
          sender() ! streams
      }
    })
    val supervisor = actorRefFactory.actorOf(supervisorProps, supervisorName)
    (supervisor ? "get_streams")(maxDur).successfulAlmFuture[AlmhirtStreams].map { streams =>
      val stop = new Stoppable {
        def stop() {
          supervisor ! StopStreaming
        }
      }
      (streams, stop)
    }
  }

  def apply(eventStreamSubscriberName: String, commandStreamSubscriberName: String)(implicit actorRefFactory: ActorRefFactory): AlmhirtStreams with Stoppable = {
    implicit val mat = FlowMaterializer(MaterializerSettings())
    
    val (eventSubscriber, eventPublisher) = Duct[Event].build()

    val eventShipperActor = actorRefFactory.actorOf(StreamShipper.props(), eventStreamSubscriberName)
    val (eventShipperIn, eventShipperOut, stopEventShipper) = StreamShipper[Event](eventShipperActor)
    eventShipperOut.subscribe(eventSubscriber)

    val eventDevNullSubscriber = ActorDevNullSubscriber.create(s"$eventStreamSubscriberName-dev-null")
    eventPublisher.subscribe(ActorSubscriber(eventDevNullSubscriber))

    val systemEventFlow = Flow(eventPublisher).collect { case e: SystemEvent ⇒ e }
    val systemEventPublisher: Publisher[SystemEvent] = systemEventFlow.toPublisher()
    val systemEventDevNullSubscriber = ActorDevNullSubscriber.create(s"$eventStreamSubscriberName-system-dev-null")
    systemEventPublisher.subscribe(ActorSubscriber(systemEventDevNullSubscriber))

    val domainEventFlow = Flow(eventPublisher).collect { case e: DomainEvent ⇒ e }
    val domainEventPublisher: Publisher[DomainEvent] = domainEventFlow.toPublisher()
    val domainEventDevNullSubscriber = ActorDevNullSubscriber.create(s"$eventStreamSubscriberName-domain-dev-null")
    domainEventPublisher.subscribe(ActorSubscriber(domainEventDevNullSubscriber))

    val aggregateEventFlow = Flow(eventPublisher).collect { case e: AggregateRootEvent ⇒ e }
    val aggregateEventPublisher: Publisher[AggregateRootEvent] = aggregateEventFlow.toPublisher()
    val aggregateEventDevNullSubscriber = ActorDevNullSubscriber.create(s"$eventStreamSubscriberName-aggregate-dev-null")
    aggregateEventPublisher.subscribe(ActorSubscriber(aggregateEventDevNullSubscriber))

    val (commandSubscriber, commandPublisher) = Duct[Command].build()

    val commandShipperActor = actorRefFactory.actorOf(StreamShipper.props(), commandStreamSubscriberName)
    val (commandShipperIn, commandShipperOut, stopCommandShipper) = StreamShipper[Command](commandShipperActor)
    commandShipperOut.subscribe(commandSubscriber)

    val commandDevNullSubscriber = ActorDevNullSubscriber.create(s"$commandStreamSubscriberName-dev-null")
    commandPublisher.subscribe(ActorSubscriber(commandDevNullSubscriber))

    val systemCommandFlow = Flow(commandPublisher).collect { case e: SystemCommand ⇒ e }
    val systemCommandPublisher: Publisher[SystemCommand] = systemCommandFlow.toPublisher()
    val systemCommandDevNullSubscriber = ActorDevNullSubscriber.create(s"$commandStreamSubscriberName-system-dev-null")
    systemCommandPublisher.subscribe(ActorSubscriber(systemCommandDevNullSubscriber))

    val domainCommandFlow = Flow(commandPublisher).collect { case e: DomainCommand ⇒ e }
    val domainCommandPublisher: Publisher[DomainCommand] = domainCommandFlow.toPublisher()
    val domainCommandDevNullSubscriber = ActorDevNullSubscriber.create(s"$commandStreamSubscriberName-domain-dev-null")
    domainCommandPublisher.subscribe(ActorSubscriber(domainCommandDevNullSubscriber))

    val aggregateCommandFlow = Flow(commandPublisher).collect { case e: AggregateRootCommand ⇒ e }
    val aggregateCommandPublisher: Publisher[AggregateRootCommand] = aggregateCommandFlow.toPublisher()
    val aggregateCommandDevNullSubscriber = ActorDevNullSubscriber.create(s"$commandStreamSubscriberName-aggregate-dev-null")
    aggregateCommandPublisher.subscribe(ActorSubscriber(aggregateCommandDevNullSubscriber))

    new AlmhirtStreams with Stoppable {
      val eventBroker = eventShipperIn
      val eventStream = eventPublisher
      val systemEventStream = systemEventPublisher
      val domainEventStream = domainEventPublisher
      val aggregateEventStream = aggregateEventPublisher
      val commandBroker = commandShipperIn
      val commandStream = commandPublisher
      val systemCommandStream = systemCommandPublisher
      val domainCommandStream = domainCommandPublisher
      val aggregateCommandStream = aggregateCommandPublisher
      def stop() {
        stopEventShipper.stop()
        stopCommandShipper.stop()
        actorRefFactory.stop(eventDevNullSubscriber)
        actorRefFactory.stop(systemEventDevNullSubscriber)
        actorRefFactory.stop(domainEventDevNullSubscriber)
        actorRefFactory.stop(aggregateEventDevNullSubscriber)
        actorRefFactory.stop(commandDevNullSubscriber)
        actorRefFactory.stop(systemCommandDevNullSubscriber)
        actorRefFactory.stop(domainCommandDevNullSubscriber)
        actorRefFactory.stop(aggregateCommandDevNullSubscriber)
      }
    }
  }
}

private[streaming] class DevNullSubscriber[T]() extends Subscriber[T] {
  private[this] var sub: Option[Subscription] = None

  private def requestMore() = sub.map(_.request(1))

  override def getSubscriber: Subscriber[T] = new Subscriber[T] {

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
}
