package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.domain.AggregateRootRepository
import almhirt.parts.HasRepositories
import almhirt.domain.AggregateRoot
import almhirt.domain.DomainEvent

/** A repository registry that is __NOT__ thread safe. Do not mutate, once almhirt is running!
 */
class UnsafeRepositoryRegistry extends HasRepositories {
  private val repos = scala.collection.mutable.Map[String, AnyRef]()
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_,_]]): AlmValidation[AnyRef] =
    repos.get(arType.getName) match {
      case Some(r) => r.success
      case None => NotFoundProblem("Repository for aggregate root '%s' not found".format(arType.getName)).failure
    }
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, T <: AggregateRootRepository[AR,TEvent]](repo: T)(implicit m: Manifest[AR]) =
    repos.put(m.erasure.getName, repo)
}