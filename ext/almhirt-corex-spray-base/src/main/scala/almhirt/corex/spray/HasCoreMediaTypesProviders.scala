package almhirt.corex.spray

import almhirt.httpx.spray.MediaTypesProvider
import almhirt.core.types._
import almhirt.http.MediaTypeVendorProvider


trait HasCoreMediaTypesProviders {
  implicit def domainEventMediaTypesProvider: MediaTypesProvider[DomainEvent]
  implicit def executionStateMediaTypesProvider: MediaTypesProvider[ExecutionState]
  implicit def domainEventsMediaTypesProvider: MediaTypesProvider[Seq[DomainEvent]]
  implicit def executionStatesMediaTypesProvider: MediaTypesProvider[Seq[ExecutionState]]
}

trait DelegatingCoreMediaTypesProviders { self: HasCoreMediaTypesProviders =>
  def coreMediaTypesProviders: HasCoreMediaTypesProviders
  override lazy val domainEventMediaTypesProvider = coreMediaTypesProviders.domainEventMediaTypesProvider
  override lazy val executionStateMediaTypesProvider = coreMediaTypesProviders.executionStateMediaTypesProvider
  override lazy val domainEventsMediaTypesProvider = coreMediaTypesProviders.domainEventsMediaTypesProvider
  override lazy val executionStatesMediaTypesProvider = coreMediaTypesProviders.executionStatesMediaTypesProvider

}

trait VendorBasedCoreMediaTypesProviders { self: HasCoreMediaTypesProviders =>
  implicit def vendorProvider: MediaTypeVendorProvider
  override lazy val domainEventMediaTypesProvider = MediaTypesProvider.defaults[DomainEvent]("DomainEvent")
  override lazy val executionStateMediaTypesProvider = MediaTypesProvider.defaults[ExecutionState]("ExecutionState")
  override lazy val domainEventsMediaTypesProvider = MediaTypesProvider.defaults[Seq[DomainEvent]]("DomainEvents")
  override lazy val executionStatesMediaTypesProvider = MediaTypesProvider.defaults[Seq[ExecutionState]]("ExecutionStates")
}