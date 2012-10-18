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
  
  def get[AR <: AggregateRoot[AR,Event], Event <: DomainEvent](implicit m: Manifest[AR]): AlmValidation[AggregateRootRepository[AR, Event]] = {
    repos.get(m.erasure.getName) match {
      case Some(r) => r.asInstanceOf[AggregateRootRepository[AR, Event]].success
      case None => NotFoundProblem("Repository of type '%s' not found".format(m.erasure.getName)).failure
    }
  }
  def register[AR <: AggregateRoot[AR,Event], Event <: DomainEvent, T <: AggregateRootRepository[AR,Event]](repo: T)(implicit m: Manifest[T]) {
    repos += ((m.erasure.getName, repo))
  }
}