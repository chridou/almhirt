package almhirt.commanding

import almhirt.common._
import almhirt.domain._
import almhirt.parts.HasRepositories
import almhirt.environment._
import almhirt.messaging.Message
import almhirt.util._

abstract class BoundUnitOfWork[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent](private val theAlmhirt: Almhirt, getTheRepository: () => AlmValidation[AggregateRootRepository[AR, TEvent]])(implicit m: Manifest[AR]) extends HandlesCommand {
  def getRepository(): AlmValidation[AggregateRootRepository[AR, TEvent]] = getTheRepository()
  
  def this(almhirt: Almhirt)(implicit m: Manifest[AR]) = {
    this(almhirt, () => almhirt.getService[HasRepositories].flatMap(hasRepos => hasRepos.getForAggregateRoot[AR, TEvent]))
  }

  def this(almhirt: Almhirt, hasRepositories: HasRepositories)(implicit m: Manifest[AR]) = {
    this(almhirt, () => hasRepositories.getForAggregateRoot[AR, TEvent])
  }
  
  val aggregateRootType = m.runtimeClass.asInstanceOf[Class[AR]]
  def handle(com: DomainCommand, ticket: Option[TrackingTicket]) {
    com match {
      case bcmd: BoundDomainCommand => handleBoundCommand(bcmd, ticket, theAlmhirt)
      case wrongType =>
        val p = ArgumentProblem("Not a BoundDomainCommand: %s".format(wrongType.getClass.getName), severity = Major)
        theAlmhirt.reportProblem(p)
        ticket match {
          case Some(t) => theAlmhirt.reportOperationState(NotExecuted(t, p))
          case None => ()
        }
    }
  }
  
  /** handle delegates to this operation if the submitted command is a [[almhirt.commanding.BoundDomainCommand]]
   */
  protected def handleBoundCommand(com: BoundDomainCommand, ticket: Option[TrackingTicket], theAlmhirt: Almhirt): Unit
}

object BoundUnitOfWork {
  def createCreatorStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand](theHandler: CreatorCommandHandler[AR, TEvent, TCom])(implicit theAlmhirt: Almhirt, m: Manifest[AR], mCom: Manifest[TCom]): BoundUnitOfWork[AR, TEvent] =
    new BoundUnitOfWork[AR, TEvent](theAlmhirt)(m) with CreatorUnitOfWorkStyleValidation[AR, TEvent, TCom] { val commandType = mCom.runtimeClass.asInstanceOf[Class[_ <: DomainCommand]]; val handler = theHandler }

  def createCreatorStyleFuture[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand](theHandler: CreatorCommandHandlerFuture[AR, TEvent, TCom])(implicit theAlmhirt: Almhirt, m: Manifest[AR], mCom: Manifest[TCom]): BoundUnitOfWork[AR, TEvent] =
    new BoundUnitOfWork[AR, TEvent](theAlmhirt)(m) with CreatorUnitOfWorkStyleFuture[AR, TEvent, TCom] { val commandType = mCom.runtimeClass.asInstanceOf[Class[_ <: DomainCommand]]; val handler = theHandler }

  def createMutatorStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand](theHandler: MutatorCommandHandler[AR, TEvent, TCom])(implicit theAlmhirt: Almhirt, m: Manifest[AR], mCom: Manifest[TCom]): BoundUnitOfWork[AR, TEvent] =
    new BoundUnitOfWork[AR, TEvent](theAlmhirt)(m) with MutatorUnitOfWorkStyleValidation[AR, TEvent, TCom] { val commandType = mCom.runtimeClass.asInstanceOf[Class[_ <: DomainCommand]]; val handler = theHandler }

  def createMutatorStyleFuture[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand](theHandler: MutatorCommandHandlerFuture[AR, TEvent, TCom])(implicit theAlmhirt: Almhirt, m: Manifest[AR], mCom: Manifest[TCom]): BoundUnitOfWork[AR, TEvent] =
    new BoundUnitOfWork[AR, TEvent](theAlmhirt)(m) with MutatorUnitOfWorkStyleFuture[AR, TEvent, TCom] { val commandType = mCom.runtimeClass.asInstanceOf[Class[_ <: DomainCommand]]; val handler = theHandler }
}


