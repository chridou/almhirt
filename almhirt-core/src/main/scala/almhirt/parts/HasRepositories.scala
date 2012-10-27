package almhirt.parts

import almhirt.AlmValidation
import almhirt.domain._

trait HasRepositories {
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_,_]]): AlmValidation[AnyRef]
  def getForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmValidation[AggregateRootRepository[AR, TEvent]] =
    getForAggregateRootByType(m.erasure.asInstanceOf[Class[AR]]).map(_.asInstanceOf[AggregateRootRepository[AR, TEvent]])
  /** Registers a new repository. Has replace semantics. 
   */
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, T <: AggregateRootRepository[_,_]](repo: T)(implicit m: Manifest[AR]): Unit
}

