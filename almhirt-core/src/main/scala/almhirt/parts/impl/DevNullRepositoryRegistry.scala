package almhirt.parts.impl

import scala.reflect.ClassTag
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository
import almhirt.parts.HasRepositories
import almhirt.domain.AggregateRoot
import almhirt.domain.DomainEvent
import almhirt.common.AlmFuture

class DevNullRepositoryRegistry() extends HasRepositories {
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_, _]]): AlmValidation[AnyRef] = { NotFoundProblem("Repository for aggregate root  '%s' not found".format(arType.getName)).failure }
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: ClassTag[AR]) {}
}