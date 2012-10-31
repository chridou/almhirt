package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.environment._
import almhirt.domain._
import almhirt.parts.HasRepositories

/**
 * A repository registry that is thread safe
 */
class ConcurrentRepositoryRegistry(context: AlmhirtContext) extends HasRepositories {
  import collection.JavaConversions._
  private implicit val executionContext = context.system.futureDispatcher
  private val repos = new java.util.concurrent.ConcurrentHashMap[String, AnyRef]
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_, _]]): AlmFuture[AnyRef] =
    AlmPromise {
      repos.get(arType.getName) match {
        case Some(r) => r.asInstanceOf[AnyRef].success
        case None => NotFoundProblem("Repository for aggregate root '%s' not found".format(arType.getName)).failure
      }
    }
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, T <: AggregateRootRepository[_, _]](repo: T)(implicit m: Manifest[AR]) =
    repos.put(m.erasure.getName, repo)
}