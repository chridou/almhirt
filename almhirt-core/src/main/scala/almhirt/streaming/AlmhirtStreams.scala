package almhirt.streaming

import scala.concurrent.duration._
import akka.actor._
import akka.pattern._
import akka.stream.scaladsl.{ Flow, Duct }
import org.reactivestreams.api.{ Consumer, Producer }
import org.reactivestreams.spi.{ Subscription, Subscriber }
import akka.stream.actor.{ ActorProducer, ActorConsumer }
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
  def eventStream: Producer[Event]
  def systemEventStream: Producer[SystemEvent]
  def domainEventStream: Producer[DomainEvent]
  def aggregateEventStream: Producer[AggregateEvent]
}

trait CommandStreams {
  def commandStream: Producer[Command]
  def systemCommandStream: Producer[SystemCommand]
  def domainCommandStream: Producer[DomainCommand]
  def aggregateCommandStream: Producer[AggregateCommand]
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

  def apply(eventStreamConsumerName: String, commandStreamConsumerName: String)(implicit actorRefFactory: ActorRefFactory): AlmhirtStreams with Stoppable = {

    val (eventConsumer, eventProducer) = Duct[Event].build(FlowMaterializer(MaterializerSettings()))

    val eventShipperActor = actorRefFactory.actorOf(StreamShipper.props(), eventStreamConsumerName)
    val (eventShipperIn, eventShipperOut, stopEventShipper) = StreamShipper[Event](eventShipperActor)
    eventShipperOut.produceTo(eventConsumer)

    val eventDevNullConsumer = ActorDevNullConsumer.create(s"$eventStreamConsumerName-dev-null")
    eventProducer.produceTo(ActorConsumer(eventDevNullConsumer))

    val systemEventFlow = Flow(eventProducer).collect { case e: SystemEvent ⇒ e }
    val systemEventProducer: Producer[SystemEvent] = systemEventFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    val systemEventDevNullConsumer = ActorDevNullConsumer.create(s"$eventStreamConsumerName-system-dev-null")
    systemEventProducer.produceTo(ActorConsumer(systemEventDevNullConsumer))

    val domainEventFlow = Flow(eventProducer).collect { case e: DomainEvent ⇒ e }
    val domainEventProducer: Producer[DomainEvent] = domainEventFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    val domainEventDevNullConsumer = ActorDevNullConsumer.create(s"$eventStreamConsumerName-domain-dev-null")
    domainEventProducer.produceTo(ActorConsumer(domainEventDevNullConsumer))

    val aggregateEventFlow = Flow(eventProducer).collect { case e: AggregateEvent ⇒ e }
    val aggregateEventProducer: Producer[AggregateEvent] = aggregateEventFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    val aggregateEventDevNullConsumer = ActorDevNullConsumer.create(s"$eventStreamConsumerName-aggregate-dev-null")
    aggregateEventProducer.produceTo(ActorConsumer(aggregateEventDevNullConsumer))

    val (commandConsumer, commandProducer) = Duct[Command].build(FlowMaterializer(MaterializerSettings()))

    val commandShipperActor = actorRefFactory.actorOf(StreamShipper.props(), commandStreamConsumerName)
    val (commandShipperIn, commandShipperOut, stopCommandShipper) = StreamShipper[Command](commandShipperActor)
    commandShipperOut.produceTo(commandConsumer)

    val commandDevNullConsumer = ActorDevNullConsumer.create(s"$commandStreamConsumerName-dev-null")
    commandProducer.produceTo(ActorConsumer(commandDevNullConsumer))

    val systemCommandFlow = Flow(commandProducer).collect { case e: SystemCommand ⇒ e }
    val systemCommandProducer: Producer[SystemCommand] = systemCommandFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    val systemCommandDevNullConsumer = ActorDevNullConsumer.create(s"$commandStreamConsumerName-system-dev-null")
    systemCommandProducer.produceTo(ActorConsumer(systemCommandDevNullConsumer))

    val domainCommandFlow = Flow(commandProducer).collect { case e: DomainCommand ⇒ e }
    val domainCommandProducer: Producer[DomainCommand] = domainCommandFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    val domainCommandDevNullConsumer = ActorDevNullConsumer.create(s"$commandStreamConsumerName-domain-dev-null")
    domainCommandProducer.produceTo(ActorConsumer(domainCommandDevNullConsumer))

    val aggregateCommandFlow = Flow(commandProducer).collect { case e: AggregateCommand ⇒ e }
    val aggregateCommandProducer: Producer[AggregateCommand] = aggregateCommandFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    val aggregateCommandDevNullConsumer = ActorDevNullConsumer.create(s"$commandStreamConsumerName-aggregate-dev-null")
    aggregateCommandProducer.produceTo(ActorConsumer(aggregateCommandDevNullConsumer))

    new AlmhirtStreams with Stoppable {
      val eventBroker = eventShipperIn
      val eventStream = eventProducer
      val systemEventStream = systemEventProducer
      val domainEventStream = domainEventProducer
      val aggregateEventStream = aggregateEventProducer
      val commandBroker = commandShipperIn
      val commandStream = commandProducer
      val systemCommandStream = systemCommandProducer
      val domainCommandStream = domainCommandProducer
      val aggregateCommandStream = aggregateCommandProducer
      def stop() {
        stopEventShipper.stop()
        stopCommandShipper.stop()
        actorRefFactory.stop(eventDevNullConsumer)
        actorRefFactory.stop(systemEventDevNullConsumer)
        actorRefFactory.stop(domainEventDevNullConsumer)
        actorRefFactory.stop(aggregateEventDevNullConsumer)
        actorRefFactory.stop(commandDevNullConsumer)
        actorRefFactory.stop(systemCommandDevNullConsumer)
        actorRefFactory.stop(domainCommandDevNullConsumer)
        actorRefFactory.stop(aggregateCommandDevNullConsumer)
      }
    }
  }
}

private[streaming] class DevNullConsumer[T]() extends Consumer[T] {
  private[this] var sub: Option[Subscription] = None

  private def requestMore() = sub.map(_.requestMore(1))

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
