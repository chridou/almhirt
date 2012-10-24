package almhirt.parts

import almhirt.AlmValidation
import almhirt.domain._

trait HasRepositories {
  def getByType(repoType: Class[_ <: AggregateRootRepository[_,_]]): AlmValidation[AnyRef]
  def get[T <: AggregateRootRepository[_,_]](implicit m: Manifest[T]): AlmValidation[T] =
    getByType(m.erasure.asInstanceOf[Class[T]]).map(_.asInstanceOf[T])
  /** Registers a new repository. Has replace semantics. 
   */
  def register[T <: AggregateRootRepository[_,_]](repo: T)(implicit m: Manifest[T]): Unit
}

