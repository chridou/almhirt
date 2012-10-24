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
  
  def getByType(repoType: Class[_ <: AggregateRootRepository[_,_]]): AlmValidation[AnyRef] =
    repos.get(repoType.getName) match {
      case Some(r) => r.asInstanceOf[AnyRef].success
      case None => NotFoundProblem("Repository of type '%s' not found".format(repoType.getName)).failure
    }
  
  def register[T <: AggregateRootRepository[_,_]](repo: T)(implicit m: Manifest[T]) {
    repos.put(m.erasure.getName, repo)
  }
}