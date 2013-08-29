package almhirt.core

import scala.reflect.ClassTag
import akka.actor.ActorSystem
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.configuration._
import almhirt.messaging._
import almhirt.domain.DomainEvent
import com.typesafe.config._

trait Almhirt
  extends HasActorSystem
  with HasMessageBus
  with HasCommandChannel
  with HasEventChannel
  with HasDomainEventChannel
  with HasChannelRegistry
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
  def dispatcherPath(name: String): AlmValidation[String]
}

object DispatcherNames {
  val `futures` = "futures"
  val `sync-io` = "sync-io"
  val `number-cruncher` = "number-cruncher"
}

object Almhirt {
  def apply(system: ActorSystem, theConfig: Config): AlmFuture[(Almhirt, CloseHandle)] = {
    import almhirt.almfuture.all._
    implicit val execContext = system.dispatchers.defaultGlobalDispatcher
    implicit val ccuad = CanCreateUuidsAndDateTimes()
    for {
      configSection <- AlmFuture.promise(theConfig.v[Config]("almhirt"))
      theDurations <- AlmFuture.promise(configSection.v[Durations]("durations"))
      futuresExecutorPath <- AlmFuture.promise(configSection.v[String]("executors.futures"))
      syncIoExecutorPath <- AlmFuture.promise(configSection.v[String]("executors.sync-io"))
      cruncherExecutorPath <- AlmFuture.promise(configSection.v[String]("executors.number-cruncher"))
      theMessageBus <- MessageBus(system)
      theEventChannel <- theMessageBus._1.channel[Event]
      theDomainEventChannel <- theMessageBus._1.channel[DomainEvent]
      theCommandChannel <- theMessageBus._1.channel[Command]
    } yield {
      system.log.info(s"""Durations:(${theDurations.toString})""")
      val theChannelRegistry = ChannelRegistry()
      val executorNames = Map((DispatcherNames.`futures` -> futuresExecutorPath), (DispatcherNames.`sync-io` -> syncIoExecutorPath), (DispatcherNames.`number-cruncher` -> cruncherExecutorPath)).lift
      theChannelRegistry.addChannel(theEventChannel)
      theChannelRegistry.addChannel(theDomainEventChannel)
      theChannelRegistry.addChannel(theCommandChannel)
      val closeHandle = new CloseHandle { def close { theMessageBus._2.close } }
      val theAlmhirt = new Almhirt {
        val actorSystem = system
        val messageBus = theMessageBus._1
        val commandChannel = theCommandChannel
        val eventChannel = theEventChannel
        val domainEventChannel = theDomainEventChannel
        val durations = theDurations
        val config = theConfig
        val futuresExecutor = system.dispatchers.lookup(futuresExecutorPath)
        val numberCruncher = system.dispatchers.lookup(syncIoExecutorPath)
        val syncIoWorker = system.dispatchers.lookup(cruncherExecutorPath)
        val log = system.log
        val commandConsumer = new CommandConsumer { def consume(command: Command) { messageBus.publish(command) } }
        val eventConsumer = new EventConsumer { def consume(event: Event) { messageBus.publish(event) } }
        val domainEventConsumer = new DomainEventConsumer { def consume(domainEvent: DomainEvent) { messageBus.publish(domainEvent) } }
        def getUuid = ccuad.getUuid
        def getUniqueString = ccuad.getUniqueString
        def getDateTime = ccuad.getDateTime
        def getUtcTimestamp = ccuad.getUtcTimestamp
        def channelRegistry = theChannelRegistry
        def dispatcherPath(name: String): AlmValidation[String] = executorNames >! (name)
      }
      (theAlmhirt, closeHandle)
    }
  }

  def apply(system: ActorSystem): AlmFuture[(Almhirt, CloseHandle)] = {
    inTryCatch { ConfigFactory.load() }.fold(
      fail => AlmFuture.failed(fail),
      config => Almhirt(system, config))
  }

  def apply(): AlmFuture[(Almhirt, CloseHandle)] = {
    val preReqs =
      for {
        config <- inTryCatch { ConfigFactory.load() }
        systemName <- config.v[String]("almhirt.system-name")
        actorSystem <- inTryCatch { ActorSystem(systemName) }
      } yield (actorSystem, config)
    preReqs.fold(
      fail => AlmFuture.failed(fail),
      actorSystemAndConfig => Almhirt(actorSystemAndConfig._1, actorSystemAndConfig._2).map {
        case (ah, ch) =>
          (ah, new CloseHandle { def close() { ch(); actorSystemAndConfig._1.shutdown(); actorSystemAndConfig._1.awaitTermination(); } })
      }(actorSystemAndConfig._1.dispatchers.defaultGlobalDispatcher))
  }

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
      val executorNames = Map((DispatcherNames.`futures` -> "nothing"), (DispatcherNames.`sync-io` -> "nothing"), (DispatcherNames.`number-cruncher` -> "nothing")).lift
      val theFuturesExecutor = system.dispatchers.defaultGlobalDispatcher
      val theChannelRegistry = ChannelRegistry()
      theChannelRegistry.addChannel(theEventChannel)
      theChannelRegistry.addChannel(theDomainEventChannel)
      theChannelRegistry.addChannel(theCommandChannel)
      val closeHandle = new CloseHandle { def close { theMessageBus._2.close } }
      val theAlmhirt = new Almhirt {
        val actorSystem = system
        val messageBus = theMessageBus._1
        val commandChannel = theCommandChannel
        val eventChannel = theEventChannel
        val domainEventChannel = theDomainEventChannel
        val durations = theDurations
        val config = ConfigFactory.load()
        val futuresExecutor = theFuturesExecutor
        val numberCruncher = system.dispatchers.defaultGlobalDispatcher
        val syncIoWorker = system.dispatchers.defaultGlobalDispatcher
        val log = system.log
        val commandConsumer = new CommandConsumer { def consume(command: Command) { messageBus.publish(command) } }
        val eventConsumer = new EventConsumer { def consume(event: Event) { messageBus.publish(event) } }
        val domainEventConsumer = new DomainEventConsumer { def consume(domainEvent: DomainEvent) { messageBus.publish(domainEvent) } }
        def getUuid = ccuad.getUuid
        def getUniqueString = ccuad.getUniqueString
        def getDateTime = ccuad.getDateTime
        def getUtcTimestamp = ccuad.getUtcTimestamp
        def channelRegistry = theChannelRegistry
        def dispatcherPath(name: String): AlmValidation[String] = executorNames >! (name)
      }
      (theAlmhirt, closeHandle)
    }
  }
}