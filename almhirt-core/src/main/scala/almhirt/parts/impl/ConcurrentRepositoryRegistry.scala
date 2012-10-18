package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.domain.AggregateRootRepository
import almhirt.parts.HasRepositories

/** A repository registry that is thread safe
 */
class ConcurrentRepositoryRegistry extends HasRepositories {
  import collection.JavaConversions._
  private val repos = new java.util.concurrent.ConcurrentHashMap[String, AnyRef]
  
  def get[T <: AggregateRootRepository[_,_]](implicit m: Manifest[T]): AlmValidation[T] = {
    repos.get(m.erasure.getName) match {
      case Some(r) => r.asInstanceOf[T].success
      case None => NotFoundProblem("Repository of type '%s' not found".format(m.erasure.getName)).failure
    }
  }
  def register[T <: AggregateRootRepository[_,_]](repo: T)(implicit m: Manifest[T]) {
    repos + ((m.erasure.getName, repo))
  }
}