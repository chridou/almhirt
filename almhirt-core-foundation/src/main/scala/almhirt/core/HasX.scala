package almhirt.core

import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext
import almhirt.common._
import almhirt.messaging._
import com.typesafe.config.Config

trait HasActorSystem {
  def actorSystem: akka.actor.ActorSystem
}

trait HasCanCreateUuidsAndDateTimes {
  implicit def ccuad: CanCreateUuidsAndDateTimes
}

trait HasAlmhirt extends HasCanCreateUuidsAndDateTimes {
  def theAlmhirt: Almhirt
  implicit override def ccuad = theAlmhirt
}

trait HasMessageBus {
  def messageBus: MessageBus
}

//trait HasEventChannel {
//  def eventChannel : EventChannel
//}
//
//trait HasDomainEventChannel {
//  def domainEventChannel : DomainEventChannel
//}
//
//trait HasCommandChannel {
//  def commandChannel : CommandChannel
//}

trait HasChannelRegistry {
  def channelRegistry: ChannelRegistry
}

trait HasFuturesExecutor {
  def futuresExecutor : ExecutionContext
}

/**
 * Exists because looking ub an ExecutionContext from an ActorSystem might create a new dispatcher on each call of lookup
 */
trait HasNumberCruncher {
  def numberCruncher : ExecutionContext
}

/**
 * Exists because looking ub an ExecutionContext from an ActorSystem might create a new dispatcher on each call of lookup
 */
trait HasSyncIoWorker {
  def syncIoWorker : ExecutionContext
}

trait HasConfig {
  def config: Config
}

trait HasLoggingAdapter {
  def log: akka.event.LoggingAdapter
}
