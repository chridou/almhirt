package almhirt.components.impl

import scalaz.syntax.validation._
import akka.actor.ActorRef
import almhirt.common._
import almhirt.domain.AggregateRoot
import almhirt.components.AggregateRootRepositoryRegistry

class AggregateRootRepositoryRegistryImpl extends AggregateRootRepositoryRegistry {
  private val repositories = new java.util.concurrent.ConcurrentHashMap[Class[_ <: AggregateRoot[_, _]], ActorRef](128)

  final override def register(arType: Class[_ <: AggregateRoot[_, _]], repository: ActorRef) {
    repositories.put(arType, repository)
  }

  final override def get(arType: Class[_ <: AggregateRoot[_, _]]): AlmValidation[ActorRef] =
    repositories.get(arType) match {
      case null => UnspecifiedProblem(s"""No repository found for "${arType.getName()}"""").failure
      case repo => repo.success
    }
}