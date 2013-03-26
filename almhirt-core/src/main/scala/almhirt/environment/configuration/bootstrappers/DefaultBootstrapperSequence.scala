package almhirt.environment.configuration.bootstrappers

import almhirt.core.HasConfig
import almhirt.core.HasServiceRegistry

trait DefaultBootstrapperSequence
  extends HasConfig
  with HasServiceRegistry
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