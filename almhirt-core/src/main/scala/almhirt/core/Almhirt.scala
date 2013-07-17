package almhirt.core

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import almhirt.common._
import almhirt.messaging._
import almhirt.domain.DomainEvent

trait Almhirt
  extends HasActorSystem
  with HasMessageBus
  with HasCommandChannel
  with HasEventChannel
  with HasDomainEventChannel
  with CanCreateUuidsAndDateTimes
  with HasDurations
  with HasConfig
  with HasFuturesExecutor
  with HasNumberCruncher
  with HasSyncIoWorker
  with HasLoggingAdapter {
  def commandConsumer: Consumer[Command]
  def eventConsumer: Consumer[Event]
  def domainEventConsumer: Consumer[DomainEvent]
}

object Almhirt {
  def notFromConfig(system: ActorSystem): AlmFuture[(Almhirt, CloseHandle)] = {
    implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
    val theDurations = Durations()
    implicit val ccuad = CanCreateUuidsAndDateTimes()
    for {
      theMessageBus <- MessageBus(system)
      theEventChannel <- theMessageBus._1.channel[Event]
      theDomainEventChannel <- theMessageBus._1.channel[DomainEvent]
      theCommandChannel <- theMessageBus._1.channel[Command]
    } yield {
      val closeHandle = new CloseHandle { def close { theMessageBus._2.close } }
      val theAlmhirt = new Almhirt {
        val actorSystem = system
        val messageBus = theMessageBus._1
        val commandChannel = theCommandChannel
        val eventChannel = theEventChannel
        val domainEventChannel = theDomainEventChannel
        val durations = theDurations
        val config = ConfigFactory.load()
        val futuresExecutor = system.dispatchers.defaultGlobalDispatcher
        val numberCruncher = system.dispatchers.defaultGlobalDispatcher
        val syncIoWorker = system.dispatchers.defaultGlobalDispatcher
        val log = system.log
        val commandConsumer = new CommandConsumer { def consume(command: Command) { messageBus.publish(command) } }
        val eventConsumer = new EventConsumer { def consume(event: Event) { messageBus.publish(event) } }
        val domainEventConsumer = new DomainEventConsumer { def consume(domainEvent: DomainEvent) { messageBus.publish(domainEvent) } }
        def getUuid = ccuad.getUuid
        def getDateTime = ccuad.getDateTime
      }
      (theAlmhirt, closeHandle)
    }
  }
}