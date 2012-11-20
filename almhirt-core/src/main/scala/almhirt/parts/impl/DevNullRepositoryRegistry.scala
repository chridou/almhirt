package almhirt.parts.impl

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.environment._
import almhirt.domain.AggregateRootRepository
import almhirt.parts.HasRepositories
import almhirt.domain.AggregateRoot
import almhirt.domain.DomainEvent
import almhirt.common.AlmFuture

class DevNullRepositoryRegistry(context: AlmhirtContext) extends HasRepositories {
  private implicit val executionContext = context.system.futureDispatcher
  val actor = context.system.actorSystem.actorOf(Props(new Actor { def receive: Receive = { case _ => () } }))
  def getForAggregateRootByType(arType: Class[_ <: AggregateRoot[_, _]]): AlmFuture[AnyRef] = AlmPromise { NotFoundProblem("Repository for aggregate root  '%s' not found".format(arType.getName)).failure }
  def registerForAggregateRoot[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](repo: AggregateRootRepository[AR, TEvent])(implicit m: Manifest[AR]) {}
}