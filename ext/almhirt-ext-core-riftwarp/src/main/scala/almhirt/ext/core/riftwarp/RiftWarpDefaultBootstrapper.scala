package almhirt.ext.core.riftwarp

import com.typesafe.config.Config
import almhirt.core._
import almhirt.environment.configuration.Bootstrapper
import almhirt.environment.configuration.bootstrappers._
import almhirt.core.impl.SimpleConcurrentServiceRegistry

trait RiftWarpDefaultBootstrapperSequence
  extends HasConfig
  with HasServiceRegistry
  with RegistersConfiguration
  with RegistersServiceRegistry
  with CreatesActorSystemFromConfig
  with CreatesAlmhirtFromConfigAndActorSystem
  with CreatesAndRegistersDefaultChannels
  with CreatesAndRegistersOperationStateTracker
  with CreatesAndRegistersHasRepositories
  with CreatesAndRegistersHasCommandHandlers
  with CreatesAndRegistersCommandExecutor
  with RiftWarpBootstrapper
  with CreatesAndRegistersDomainEventLog
  with CreatesAndRegistersCommandEndpoint
  with CreatesClassicProblemLogger

object RiftWarpDefaultBootstrapperSequence {
  def apply(aConfig: Config): Bootstrapper =
    new Bootstrapper with RiftWarpDefaultBootstrapperSequence {
      def config = aConfig
      val serviceRegistry = new SimpleConcurrentServiceRegistry()
    }
}