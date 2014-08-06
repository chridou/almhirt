package almhirt.messaging

import org.reactivestreams.api.{ Consumer, Producer }
import almhirt.common._
import org.reactivestreams.spi.Subscription
import org.reactivestreams.spi.Subscriber
import akka.stream.scaladsl.Flow
import almhirt.common.EventId
import almhirt.aggregates.AggregateRootId
import almhirt.aggregates.AggregateRootVersion
import akka.stream.FlowMaterializer
import akka.stream.MaterializerSettings
import akka.actor._
import akka.stream.actor.ActorProducer

trait CloseableChannels {
  def closeChannels()
}

trait CanDispatchEvents {
  def eventStreamDispatcher: ActorRef
}

trait CanDispatchCommands {
  def commandStreamDispatcher: ActorRef
}

trait AlmhirtChannels extends CanDispatchEvents with CanDispatchCommands {
  def eventStream: Producer[Event]
  def systemEventStream: Producer[SystemEvent]
  def domainEventStream: Producer[DomainEvent]
  def aggregateEventStream: Producer[AggregateEvent]
  def commandStream: Producer[Command]
  def systemCommandStream: Producer[SystemCommand]
  def domainCommandStream: Producer[DomainCommand]
  def aggregateCommandStream: Producer[AggregateCommand]
}

object AlmhirtChannels {
  def apply()(implicit actorRefFactory: ActorRefFactory): AlmhirtChannels with CloseableChannels =
    apply("event-stream-consumer", "command-stream-dispatcher")

  def apply(eventStreamDispatcherName: String, commandStreamDispatcherName: String)(implicit actorRefFactory: ActorRefFactory): AlmhirtChannels with CloseableChannels = {
    val ccuad = CanCreateUuidsAndDateTimes()

    
    val eventsStreamConsumer = actorRefFactory.actorOf(Props(new EventStreamConsumer()), eventStreamDispatcherName)
    val eventStreamProducer = ActorProducer[Event](eventsStreamConsumer)

    val eventFlow = Flow.apply(eventStreamProducer)

    val eventsProducer: Producer[Event] = eventFlow.map(x => x).toProducer(FlowMaterializer(MaterializerSettings()))
    eventsProducer.produceTo(new DevNullConsumer())

    val systemEventFlow = Flow(eventsProducer).collect { case e: SystemEvent => e }
    val systemEventsProducer: Producer[SystemEvent] = systemEventFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    systemEventsProducer.produceTo(new DevNullConsumer())

    val domainEventFlow = Flow(eventsProducer).collect { case e: DomainEvent => e }
    val domainEventsProducer: Producer[DomainEvent] = domainEventFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    domainEventsProducer.produceTo(new DevNullConsumer())

    val aggregateEventFlow = Flow(eventsProducer).collect { case e: AggregateEvent => e }
    val aggregateEventsProducer: Producer[AggregateEvent] = aggregateEventFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    aggregateEventsProducer.produceTo(new DevNullConsumer())
    

    val commandsStreamDispatcher = actorRefFactory.actorOf(Props(new CommandStreamDispatcher(10)), commandStreamDispatcherName)
    val commandStreamProducer = ActorProducer[Command](commandsStreamDispatcher)

    val commandFlow = Flow.apply(commandStreamProducer)

    val commandsProducer: Producer[Command] = commandFlow.map(x => x).toProducer(FlowMaterializer(MaterializerSettings()))
    commandsProducer.produceTo(new DevNullConsumer())

    val systemCommandFlow = Flow(commandsProducer).collect { case e: SystemCommand => e }
    val systemCommandsProducer: Producer[SystemCommand] = systemCommandFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    systemCommandsProducer.produceTo(new DevNullConsumer())

    val domainCommandFlow = Flow(commandsProducer).collect { case e: DomainCommand => e }
    val domainCommandsProducer: Producer[DomainCommand] = domainCommandFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    domainCommandsProducer.produceTo(new DevNullConsumer())

    val aggregateCommandFlow = Flow(commandsProducer).collect { case e: AggregateCommand => e }
    val aggregateCommandsProducer: Producer[AggregateCommand] = aggregateCommandFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    aggregateCommandsProducer.produceTo(new DevNullConsumer())

    
    new AlmhirtChannels with CloseableChannels {
      val eventStreamDispatcher: ActorRef = eventsStreamConsumer
      val eventStream: Producer[Event] = eventsProducer
      val systemEventStream: Producer[SystemEvent] = systemEventsProducer
      val domainEventStream: Producer[DomainEvent] = domainEventsProducer
      val aggregateEventStream: Producer[AggregateEvent] = aggregateEventsProducer
      val commandStreamDispatcher: ActorRef = commandsStreamDispatcher
      val commandStream: Producer[Command] = commandsProducer
      val systemCommandStream: Producer[SystemCommand] = systemCommandsProducer
      val domainCommandStream: Producer[DomainCommand] = domainCommandsProducer
      val aggregateCommandStream: Producer[AggregateCommand] = aggregateCommandsProducer
      def closeChannels() { actorRefFactory.stop(eventStreamDispatcher); actorRefFactory.stop(commandStreamDispatcher) }
    }
  }
}

private[messaging] class DevNullConsumer[T]() extends Consumer[T] {
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
