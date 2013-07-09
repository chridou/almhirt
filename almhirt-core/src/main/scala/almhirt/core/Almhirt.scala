package almhirt.core

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import almhirt.common._
import almhirt.messaging._
import almhirt.domain.DomainEvent

trait Almhirt
  extends HasMessageBus
  with HasCommandStream
  with HasEventStream
  with HasDomainEventStream
  with CanCreateUuidsAndDateTimes
  with HasDurations
  with HasConfig
  with HasNumberCruncher
  with HasSyncIoWorker
  with HasLoggingAdapter

object Almhirt {
  def apply(system: ActorSystem): AlmFuture[(Almhirt, CloseHandle)] = {
    implicit val executionContext = system.dispatchers.defaultGlobalDispatcher
    val theDurations = Durations()
    for {
      theMessageBus <- MessageBus(system)
      theEventStream <- theMessageBus._1.channel[Event]
      theDomainEventStream <- theMessageBus._1.channel[DomainEvent]
      theCommandStream <- theMessageBus._1.channel[Command]
    } yield {
      val closeHandle = new CloseHandle { def close { theMessageBus._2.close } }
      val theAlmhirt = new Almhirt {
        val messageBus = theMessageBus._1
        val commandStream = theCommandStream
        val eventStream = theEventStream
        val domainEventStream = theDomainEventStream
        val durations = theDurations
        val config = ConfigFactory.load()
        val numberCruncher = system.dispatchers.defaultGlobalDispatcher
        val syncIoWorker = system.dispatchers.defaultGlobalDispatcher
        val loggingAdapter = system.log
      }
      (theAlmhirt, closeHandle)
    }
  }
}