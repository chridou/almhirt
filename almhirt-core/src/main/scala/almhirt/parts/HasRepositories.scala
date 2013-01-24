package almhirt.parts

import scala.reflect.ClassTag
import akka.actor._
import almhirt.common._
import almhirt.domain._
import almhirt.common.AlmFuture
import almhirt.parts.impl.ConcurrentRepositoryRegistry
import almhirt.parts.impl.DevNullRepositoryRegistry

trait HasRepositories {
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_,_]]): AlmValidation[AnyRef]
  def getForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: ClassTag[AR]): AlmValidation[AggregateRootRepository[AR, TEvent]] =
    getForAggregateRootByType(m.runtimeClass.asInstanceOf[Class[AR]]).map(_.asInstanceOf[AggregateRootRepository[AR, TEvent]])
  /** Registers a new repository. Has replace semantics. 
   */
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: ClassTag[AR]): Unit
}

object HasRepositories {
  import scalaz.syntax.validation._
  def apply(): HasRepositories = {
    concurrent()
  }
  
  def devNull(): HasRepositories = {
    new DevNullRepositoryRegistry()
  }
  
  def concurrent(): HasRepositories = {
    new ConcurrentRepositoryRegistry()
  }
}

