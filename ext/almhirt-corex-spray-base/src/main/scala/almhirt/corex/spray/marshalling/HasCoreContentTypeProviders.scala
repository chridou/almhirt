package almhirt.corex.spray.marshalling

import almhirt.httpx.spray.marshalling.FullContentTypeProvider
import almhirt.core.types._
import almhirt.corex.spray.HasCoreMediaTypesProviders

trait HasCoreContentTypeProviders {
  def domainEventContentTypeProvider: FullContentTypeProvider[DomainEvent]
  implicit def executionStateContentTypeProvider: FullContentTypeProvider[ExecutionState]
  
  implicit def domainEventsContentTypeProvider: FullContentTypeProvider[Seq[DomainEvent]]
  implicit def executionStatesContentTypeProvider: FullContentTypeProvider[Seq[ExecutionState]]
}

trait EmptyCoreContentTypeProviders extends HasCoreContentTypeProviders {
  override lazy val domainEventContentTypeProvider = FullContentTypeProvider.empty[DomainEvent]
  override lazy val executionStateContentTypeProvider = FullContentTypeProvider.empty[ExecutionState]
  
  override lazy val domainEventsContentTypeProvider = FullContentTypeProvider.empty[Seq[DomainEvent]]
  override lazy val executionStatesContentTypeProvider = FullContentTypeProvider.empty[Seq[ExecutionState]]
}

trait CoreContentTypeProvidersFromMediaTypes extends HasCoreContentTypeProviders { self: HasCoreMediaTypesProviders =>
  override lazy val domainEventContentTypeProvider = FullContentTypeProvider[DomainEvent]
  override lazy val executionStateContentTypeProvider = FullContentTypeProvider[ExecutionState]
  
  override lazy val domainEventsContentTypeProvider = FullContentTypeProvider[Seq[DomainEvent]]
  override lazy val executionStatesContentTypeProvider = FullContentTypeProvider[Seq[ExecutionState]]
 }