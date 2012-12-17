package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.domain._
import almhirt.parts._

class ConcurrentRepositoryRegistry extends HasRepositories {
  private val repos = new java.util.concurrent.ConcurrentHashMap[Class[_ <: AggregateRoot[_,_]], AnyRef]

  def getForAggregateRootByType(clazz: Class[_ <: AggregateRoot[_,_]]): AlmValidation[AnyRef] = {
    repos.get(clazz) match {
      case null => ServiceNotFoundProblem("No implementation found for service '%s'".format(clazz.getName())).failure
      case service => service.success
    }
    
  }
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: Manifest[AR]){
    repos.put(m.asInstanceOf[Class[_<: AggregateRoot[AR, TEvent]]], repo)
  }

}