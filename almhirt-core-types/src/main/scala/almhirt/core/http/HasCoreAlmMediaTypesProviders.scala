package almhirt.core.http

import almhirt.core.types._
import almhirt.http.MediaTypeVendorProvider
import almhirt.http.AlmMediaTypesProvider


trait HasCoreAlmMediaTypesProviders {
  implicit def domainEventAlmMediaTypesProvider: AlmMediaTypesProvider[DomainEvent]
  implicit def executionStateAlmMediaTypesProvider: AlmMediaTypesProvider[ExecutionState]
  implicit def domainEventsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[DomainEvent]]
  implicit def domainCommandsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[DomainCommand]]
  implicit def executionStatesAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[ExecutionState]]
}

trait DelegatingCoreAlmMediaTypesProviders { self: HasCoreAlmMediaTypesProviders =>
  def coreAlmMediaTypesProviders: HasCoreAlmMediaTypesProviders
  override lazy val domainEventAlmMediaTypesProvider = coreAlmMediaTypesProviders.domainEventAlmMediaTypesProvider
  override lazy val executionStateAlmMediaTypesProvider = coreAlmMediaTypesProviders.executionStateAlmMediaTypesProvider
  override lazy val domainEventsAlmMediaTypesProvider = coreAlmMediaTypesProviders.domainEventsAlmMediaTypesProvider
  override lazy val domainCommandsAlmMediaTypesProvider = coreAlmMediaTypesProviders.domainCommandsAlmMediaTypesProvider
  override lazy val executionStatesAlmMediaTypesProvider = coreAlmMediaTypesProviders.executionStatesAlmMediaTypesProvider

}

trait VendorBasedCoreAlmMediaTypesProviders { self: HasCoreAlmMediaTypesProviders =>
  implicit def vendorProvider: MediaTypeVendorProvider
  override lazy val domainEventAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[DomainEvent]("DomainEvent").withGenericTargets
  override lazy val executionStateAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[ExecutionState]("ExecutionState").withGenericTargets
  override lazy val domainEventsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[DomainEvent]]("DomainEvents").withGenericTargets
  override lazy val domainCommandsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[DomainCommand]]("DomainCommands").withGenericTargets
  override lazy val executionStatesAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[ExecutionState]]("ExecutionStates").withGenericTargets
}