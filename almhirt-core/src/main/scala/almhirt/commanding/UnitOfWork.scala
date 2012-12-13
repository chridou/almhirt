package almhirt.commanding

import almhirt.common._
import almhirt.domain._
import almhirt.parts.HasRepositories
import almhirt.environment.AlmhirtContext
import almhirt.messaging.Message
import almhirt.util._

abstract class BoundUnitOfWork[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](implicit m: Manifest[AR]) extends HandlesCommand {
  val aggregateRootType = m.erasure.asInstanceOf[Class[AR]]
  def handle(com: DomainCommand, repositories: HasRepositories, context: AlmhirtContext, ticket: Option[TrackingTicket]) {
    com match {
      case bcmd: BoundDomainCommand => handleBoundCommand(bcmd, repositories, context, ticket)
      case wrongType =>
        val p = ArgumentProblem("Not a creator BoundDomainCommand: %s".format(wrongType.getClass.getName), severity = Major)
        context.problemChannel.post(Message.createWithUuid(p))
        ticket match {
          case Some(t) => context.reportOperationState(NotExecuted(t, p))
          case None => ()
        }
    }
  }
  def handleBoundCommand(com: BoundDomainCommand, repositories: HasRepositories, context: AlmhirtContext, ticket: Option[TrackingTicket]): Unit
}

