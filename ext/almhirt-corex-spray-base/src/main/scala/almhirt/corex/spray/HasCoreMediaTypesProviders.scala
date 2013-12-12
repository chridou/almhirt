package almhirt.corex.spray

import almhirt.core.types._
import almhirt.http.MediaTypeVendorProvider
import almhirt.http.AlmMediaTypesProvider


trait HasCoreAlmMediaTypesProviders {
  implicit def domainEventAlmMediaTypesProvider: AlmMediaTypesProvider[DomainEvent]
  implicit def executionStateAlmMediaTypesProvider: AlmMediaTypesProvider[ExecutionState]
  implicit def domainEventsAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[DomainEvent]]
  implicit def executionStatesAlmMediaTypesProvider: AlmMediaTypesProvider[Seq[ExecutionState]]
}

trait DelegatingCoreAlmMediaTypesProviders { self: HasCoreAlmMediaTypesProviders =>
  def coreAlmMediaTypesProviders: HasCoreAlmMediaTypesProviders
  override lazy val domainEventAlmMediaTypesProvider = coreAlmMediaTypesProviders.domainEventAlmMediaTypesProvider
  override lazy val executionStateAlmMediaTypesProvider = coreAlmMediaTypesProviders.executionStateAlmMediaTypesProvider
  override lazy val domainEventsAlmMediaTypesProvider = coreAlmMediaTypesProviders.domainEventsAlmMediaTypesProvider
  override lazy val executionStatesAlmMediaTypesProvider = coreAlmMediaTypesProviders.executionStatesAlmMediaTypesProvider

}

trait VendorBasedCoreAlmMediaTypesProviders { self: HasCoreAlmMediaTypesProviders =>
  implicit def vendorProvider: MediaTypeVendorProvider
  override lazy val domainEventAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[DomainEvent]("DomainEvent").withGenericMarshalling
  override lazy val executionStateAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[ExecutionState]("ExecutionState").withGenericMarshalling
  override lazy val domainEventsAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[DomainEvent]]("DomainEvents").withGenericMarshalling
  override lazy val executionStatesAlmMediaTypesProvider = AlmMediaTypesProvider.registeredDefaults[Seq[ExecutionState]]("ExecutionStates").withGenericMarshalling
}