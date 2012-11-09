package almhirt.parts.impl

import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.Duration._
import almhirt._
import almhirt.almfuture.all._
import almhirt.environment._
import almhirt.domain._
import almhirt.parts._

class RepositoryRegistryActor extends Actor {
  private val repos = scala.collection.mutable.Map[String, AnyRef]()

  def receive: Receive = {
    case GetRepositoryForAggregateRootQry(arType) =>
      val res =
        repos.get(arType.getName) match {
          case Some(r) => r.success
          case None => NotFoundProblem("Repository for aggregate root '%s' not found".format(arType.getName)).failure
        }
      sender ! RepositoryForAggregateRootRsp(arType, res)
    case RegisterForAggregateRootCmd(arType, repo) =>
      repos.put(arType.getName, repo)
  }
}

/**
 */
class RepositoryRegistryActorHull(val actor: ActorRef, context: AlmhirtContext) extends HasRepositories {
  private implicit val executionContext = context.system.futureDispatcher
  private val dur = context.system.shortDuration
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_, _]]): AlmFuture[AnyRef] = {
    val cmd = GetRepositoryForAggregateRootQry(arType)
    (actor ? cmd)(dur).mapTo[RepositoryForAggregateRootRsp].map(x => x.repository)
  }
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: Manifest[AR]) {
    actor ! RegisterForAggregateRootCmd[AR, TEvent](m.erasure.asInstanceOf[Class[AggregateRoot[_, _]]], repo)
  }
}