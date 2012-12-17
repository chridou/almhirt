package almhirt.commanding

import almhirt.common._
import almhirt.domain._
import almhirt.parts.HasRepositories
import almhirt.environment._
import almhirt.messaging.Message
import almhirt.util._

abstract class BoundUnitOfWork[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](val baseOps: AlmhirtBaseOps, val getRepository: () => AlmValidation[AggregateRootRepository[AR, TEvent]])(implicit m: Manifest[AR]) extends HandlesCommand {
  def this(almhirt: Almhirt)(implicit m: Manifest[AR]) = {
    this(almhirt, () => almhirt.getService[HasRepositories].bind(hasRepos => hasRepos.getForAggregateRoot[AR, TEvent]))
  }

  def this(baseOps: AlmhirtBaseOps, hasRepositories: HasRepositories)(implicit m: Manifest[AR]) = {
    this(baseOps, () => hasRepositories.getForAggregateRoot[AR, TEvent])
  }
  
  val aggregateRootType = m.erasure.asInstanceOf[Class[AR]]
  def handle(com: DomainCommand, ticket: Option[TrackingTicket]) {
    com match {
      case bcmd: BoundDomainCommand => handleBoundCommand(bcmd, ticket)
      case wrongType =>
        val p = ArgumentProblem("Not a BoundDomainCommand: %s".format(wrongType.getClass.getName), severity = Major)
        baseOps.reportProblem(p)
        ticket match {
          case Some(t) => baseOps.reportOperationState(NotExecuted(t, p))
          case None => ()
        }
    }
  }
  def handleBoundCommand(com: BoundDomainCommand, ticket: Option[TrackingTicket]): Unit
}


