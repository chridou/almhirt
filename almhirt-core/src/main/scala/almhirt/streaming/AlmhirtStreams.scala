package almhirt.streaming

import org.reactivestreams.api.{ Consumer, Producer }
import org.reactivestreams.spi.{ Subscription, Subscriber }
import akka.stream.scaladsl.{ Flow, Duct }
import akka.actor._
import akka.stream.actor.ActorProducer
import akka.stream.{ FlowMaterializer, MaterializerSettings }
import almhirt.common._

trait CloseableStreams {
  def closeStreams()
}

trait CanDispatchEvents {
  def eventConsumer: SuppliesBroker[Event]
}

trait CanDispatchCommands {
  def commandConsumer: SuppliesBroker[Command]
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
  def apply()(implicit actorRefFactory: ActorRefFactory): AlmhirtStreams with CloseableStreams =
    apply("event-stream-input", "command-stream-input")

  def apply(eventStreamConsumerName: String, commandStreamConsumerName: String)(implicit actorRefFactory: ActorRefFactory): AlmhirtStreams with CloseableStreams = {

    val (eventConsumer, eventProducer) = Duct[Event].build(FlowMaterializer(MaterializerSettings()))
    
    val eventTransporterActor = actorRefFactory.actorOf(SuppliesTransporter.props(), eventStreamConsumerName) 
    val (eventTransIn, eventTransOut)  = SuppliesTransporter[Event](eventTransporterActor)
    eventTransOut.produceTo(eventConsumer)
    
    eventProducer.produceTo(new DevNullConsumer())

    val systemEventFlow = Flow(eventProducer).collect { case e: SystemEvent => e }
    val systemEventProducer: Producer[SystemEvent] = systemEventFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    systemEventProducer.produceTo(new DevNullConsumer())

    val domainEventFlow = Flow(eventProducer).collect { case e: DomainEvent => e }
    val domainEventProducer: Producer[DomainEvent] = domainEventFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    domainEventProducer.produceTo(new DevNullConsumer())

    val aggregateEventFlow = Flow(eventProducer).collect { case e: AggregateEvent => e }
    val aggregateEventProducer: Producer[AggregateEvent] = aggregateEventFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    aggregateEventProducer.produceTo(new DevNullConsumer())
    

    val (commandConsumer, commandProducer) = Duct[Command].build(FlowMaterializer(MaterializerSettings()))

    val commandTransporterActor = actorRefFactory.actorOf(SuppliesTransporter.props(), commandStreamConsumerName) 
    val (commandTransIn, commandTransOut)  = SuppliesTransporter[Command](commandTransporterActor)
    commandTransOut.produceTo(commandConsumer)

    commandProducer.produceTo(new DevNullConsumer())

    val systemCommandFlow = Flow(commandProducer).collect { case e: SystemCommand => e }
    val systemCommandProducer: Producer[SystemCommand] = systemCommandFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    systemCommandProducer.produceTo(new DevNullConsumer())

    val domainCommandFlow = Flow(commandProducer).collect { case e: DomainCommand => e }
    val domainCommandProducer: Producer[DomainCommand] = domainCommandFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    domainCommandProducer.produceTo(new DevNullConsumer())

    val aggregateCommandFlow = Flow(commandProducer).collect { case e: AggregateCommand => e }
    val aggregateCommandProducer: Producer[AggregateCommand] = aggregateCommandFlow.toProducer(FlowMaterializer(MaterializerSettings()))
    aggregateCommandProducer.produceTo(new DevNullConsumer())

    
    new AlmhirtStreams with CloseableStreams {
      val eventConsumer = eventTransIn
      val eventStream = eventProducer
      val systemEventStream = systemEventProducer
      val domainEventStream = domainEventProducer
      val aggregateEventStream = aggregateEventProducer
      val commandConsumer = commandTransIn
      val commandStream = commandProducer
      val systemCommandStream = systemCommandProducer
      val domainCommandStream = domainCommandProducer
      val aggregateCommandStream = aggregateCommandProducer
      def closeStreams() { actorRefFactory.stop(eventTransporterActor); actorRefFactory.stop(commandTransporterActor) }
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
