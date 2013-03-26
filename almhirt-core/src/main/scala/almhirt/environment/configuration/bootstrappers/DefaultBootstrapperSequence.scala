package almhirt.environment.configuration.bootstrappers

import almhirt.core.HasConfig
import almhirt.core.HasServiceRegistry
import almhirt.environment.configuration.Bootstrapper
import com.typesafe.config.Config
import almhirt.core.impl.SimpleConcurrentServiceRegistry

trait DefaultBootstrapperSequence
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
  with CreatesAndRegistersDomainEventLog
  with CreatesAndRegistersCommandEndpoint
  with CreatesClassicProblemLogger

object DefaultBootstrapperSequence {
  def apply(aConfig: Config): Bootstrapper =
    new Bootstrapper with DefaultBootstrapperSequence {
      def config = aConfig
      val serviceRegistry = new SimpleConcurrentServiceRegistry()
    }
}