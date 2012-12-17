package almhirt.parts

import akka.actor._
import almhirt.common._
import almhirt.domain._
import almhirt.environment.AlmhirtContext
import almhirt.common.AlmFuture
import almhirt.parts.impl.ConcurrentRepositoryRegistry

trait HasRepositories {
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_,_]]): AlmValidation[AnyRef]
  def getForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmValidation[AggregateRootRepository[AR, TEvent]] =
    getForAggregateRootByType(m.erasure.asInstanceOf[Class[AR]]).map(_.asInstanceOf[AggregateRootRepository[AR, TEvent]])
  /** Registers a new repository. Has replace semantics. 
   */
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: Manifest[AR]): Unit
}

object HasRepositories {
  import scalaz.syntax.validation._
  def apply(): AlmValidation[HasRepositories] = {
    new ConcurrentRepositoryRegistry().success
  }
}

