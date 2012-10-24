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
  
  def getByType(repoType: Class[_ <: AggregateRootRepository[_,_]]): AlmValidation[AnyRef] =
    repos.get(repoType.getName) match {
      case Some(r) => r.success
      case None => NotFoundProblem("Repository of type '%s' not found".format(repoType.getName)).failure
    }

  def register[T <: AggregateRootRepository[_,_]](repo: T)(implicit m: Manifest[T]) {
    repos.put(m.erasure.getName, repo)
  }
}