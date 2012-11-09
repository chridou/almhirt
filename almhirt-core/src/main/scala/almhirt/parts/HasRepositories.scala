package almhirt.parts

import almhirt._
import almhirt.domain._

trait HasRepositories {
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_,_]]): AlmFuture[AnyRef]
  def getForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmFuture[AggregateRootRepository[AR, TEvent]] =
    getForAggregateRootByType(m.erasure.asInstanceOf[Class[AR]]).map(_.asInstanceOf[AggregateRootRepository[AR, TEvent]])
  /** Registers a new repository. Has replace semantics. 
   */
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: Manifest[AR]): Unit
}

