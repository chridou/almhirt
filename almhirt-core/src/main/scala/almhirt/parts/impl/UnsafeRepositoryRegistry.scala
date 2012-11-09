package almhirt.parts.impl

import scalaz.syntax.validation._
import almhirt._
import almhirt.environment._
import almhirt.domain._
import almhirt.parts.HasRepositories

/**
 * A repository registry that is __NOT__ thread safe. Do not mutate, once almhirt is running!
 */
class UnsafeRepositoryRegistry(context: AlmhirtContext) extends HasRepositories {
  private implicit val executionContext = context.system.futureDispatcher
  private val repos = scala.collection.mutable.Map[String, AnyRef]()
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_, _]]): AlmFuture[AnyRef] =
    AlmPromise {
      repos.get(arType.getName) match {
        case Some(r) => r.success
        case None => NotFoundProblem("Repository for aggregate root '%s' not found".format(arType.getName)).failure
      }
    }
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: Manifest[AR]) =
    repos.put(m.erasure.getName, repo)
}