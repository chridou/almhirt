package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository
import almhirt.parts.HasRepositories
import almhirt.domain.AggregateRoot
import almhirt.domain.DomainEvent

class DevNullRepositoryRegistry(context: AlmhirtContext) extends HasRepositories {
  private implicit val executionContext = context.system.futureDispatcher
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_, _]]): AlmFuture[AnyRef] = AlmPromise { NotFoundProblem("Repository for aggregate root  '%s' not found".format(arType.getName)).failure }
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, T <: AggregateRootRepository[_, _]](repo: T)(implicit m: Manifest[AR]) {}
}