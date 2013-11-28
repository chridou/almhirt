package almhirt.corex.spray

import almhirt.httpx.spray.MediaTypesProvider
import almhirt.httpx.spray.MediaTypeVendorProvider
import almhirt.core.types._


trait HasCoreMediaTypesProviders {
  implicit def domainEventMediaTypesProvider: MediaTypesProvider[DomainEvent]
  implicit def executionStateMediaTypesProvider: MediaTypesProvider[ExecutionState]
  implicit def domainEventsMediaTypesProvider: MediaTypesProvider[Seq[DomainEvent]]
  implicit def executionStatesMediaTypesProvider: MediaTypesProvider[Seq[ExecutionState]]
}

trait DelegatingCoreMediaTypesProviders extends HasCoreMediaTypesProviders {
  def coreMediaTypesProviders: HasCoreMediaTypesProviders
  override lazy val domainEventMediaTypesProvider = coreMediaTypesProviders.domainEventMediaTypesProvider
  override lazy val executionStateMediaTypesProvider = coreMediaTypesProviders.executionStateMediaTypesProvider
  override lazy val domainEventsMediaTypesProvider = coreMediaTypesProviders.domainEventsMediaTypesProvider
  override lazy val executionStatesMediaTypesProvider = coreMediaTypesProviders.executionStatesMediaTypesProvider

}

trait VendorBasedCoreMediaTypesProviders extends HasCoreMediaTypesProviders {
  implicit def vendorProvider: MediaTypeVendorProvider
  override lazy val domainEventMediaTypesProvider = MediaTypesProvider.registeredDefaults[DomainEvent]("DomainEvent")
  override lazy val executionStateMediaTypesProvider = MediaTypesProvider.registeredDefaults[ExecutionState]("ExecutionState")
  override lazy val domainEventsMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[DomainEvent]]("DomainEvents")
  override lazy val executionStatesMediaTypesProvider = MediaTypesProvider.registeredDefaults[Seq[ExecutionState]]("ExecutionStates")
}