package almhirt.parts

import akka.actor._
import almhirt._
import almhirt.domain._
import almhirt.environment.AlmhirtContext

sealed trait HasRepositoriesCmd
case class GetRepositoryForAggregateRootQry(arType: Class[_ <: AggregateRoot[_,_]]) extends HasRepositoriesCmd
case class RegisterForAggregateRootCmd[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](arType: Class[_ <: AggregateRoot[_,_]], repository: AggregateRootRepository[AR, TEvent]) extends HasRepositoriesCmd

sealed trait HasRepositoriesRsp
case class RepositoryForAggregateRootRsp(arType: Class[_ <: AggregateRoot[_,_]], repository: AlmValidation[AnyRef]) extends HasRepositoriesRsp

trait HasRepositories extends almhirt.ActorBased {
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_,_]]): AlmFuture[AnyRef]
  def getForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]): AlmFuture[AggregateRootRepository[AR, TEvent]] =
    getForAggregateRootByType(m.erasure.asInstanceOf[Class[AR]]).map(_.asInstanceOf[AggregateRootRepository[AR, TEvent]])
  /** Registers a new repository. Has replace semantics. 
   */
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: Manifest[AR]): Unit
}

object HasRepositories {
  import scalaz.syntax.validation._
  def apply()(implicit context: AlmhirtContext): AlmValidation[HasRepositories] = {
    val actor = context.system.actorSystem.actorOf(Props[impl.RepositoryRegistryActor], "HasRepositories")
    new impl.RepositoryRegistryActorHull(actor, context).success
  }
}

