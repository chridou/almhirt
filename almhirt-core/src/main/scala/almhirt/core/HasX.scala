package almhirt.core

import scala.concurrent.ExecutionContext
import almhirt.messaging.MessageBus
import com.typesafe.config.Config

trait HasMessageBus {
  def messageBus: MessageBus
}

trait HasEventStream {
  def eventStream : EventStream
}

trait HasDomainEventStream {
  def domainEventStream : DomainEventStream
}

trait HasCommandStream {
  def commandStream : CommandStream
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
  def loggingAdapter: akka.event.LoggingAdapter
}

