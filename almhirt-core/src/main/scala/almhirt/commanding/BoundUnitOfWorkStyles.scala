package almhirt.commanding

import java.util.UUID
import scalaz.syntax.validation._
import scalaz.std._
import almhirt.common._
import almhirt.core._
import almhirt.syntax.almvalidation._
import almhirt.syntax.almfuture._
import almhirt.almvalidation.funs.inTryCatch
import almhirt.messaging._
import almhirt.domain._
import almhirt.parts.HasRepositories
import almhirt.environment._
import almhirt.util._
import almhirt.common.AlmFuture

trait CreatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] { 
  protected def getRepository(): AlmValidation[AggregateRootRepository[AR, TEvent]]
  /** This effectively executes the command handler which must return a future */
  protected def executeHandler(com: TCom)(implicit theAlmhirt: Almhirt): AlmFuture[(AR, List[TEvent])]
  def handleBoundCommand(com: BoundDomainCommand, ticket: Option[TrackingTicket])(implicit theAlmhirt: Almhirt) {
    if (com.isCreator) {
      val command = com.asInstanceOf[TCom]
      getRepository().fold(
        fail =>
          (),
        repo => {
          executeHandler(command).fold(
            fail => {
              theAlmhirt.reportProblem(fail)
              ticket match {
                case Some(t) => theAlmhirt.reportOperationState(NotExecuted(t, fail))
                case None => ()
              }
            },
            succ =>
              repo.actor ! StoreAggregateRootCmd(succ._1, succ._2, ticket))
        })
    } else {
      val p = ArgumentProblem("Not a creator command: %s".format(com.getClass.getName), severity = Major)
      theAlmhirt.reportProblem(p)
      ticket match {
        case Some(t) => theAlmhirt.reportOperationState(NotExecuted(t, p))
        case None => ()
      }
    }
  }
}

trait CreatorUnitOfWorkStyleFuture[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand] extends CreatorUnitOfWorkStyle[AR, TEvent, TCom] { self: BoundUnitOfWork[AR, TEvent] =>
  def handler: CreatorCommandHandlerFuture[AR, TEvent, TCom]
  /** Call the handler which itself creates the required future
   */
  protected def executeHandler(com: TCom)(implicit theAlmhirt: Almhirt): AlmFuture[(AR, List[TEvent])] = handler(com,theAlmhirt)
}

trait CreatorUnitOfWorkStyleValidation[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand] extends CreatorUnitOfWorkStyle[AR, TEvent, TCom] { self: BoundUnitOfWork[AR, TEvent] =>
  def handler: CreatorCommandHandler[AR, TEvent, TCom]
  /** Call the handler which creates a validation. Wrap the handler into a future
   */
  protected def executeHandler(com: TCom)(implicit theAlmhirt: Almhirt): AlmFuture[(AR, List[TEvent])] =
    AlmFuture(handler(com, theAlmhirt))
}

trait MutatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand] { 
  protected def getRepository(): AlmValidation[AggregateRootRepository[AR, TEvent]]
  /** This effectively executes the command handler which must return a future */
  protected def executeHandler(com: TCom, ar: AR)(implicit theAlmhirt: Almhirt): AlmFuture[(AR, List[TEvent])]
  def handleBoundCommand(untypedcom: BoundDomainCommand, ticket: Option[TrackingTicket])(implicit theAlmhirt: Almhirt) {
    implicit val executionContext = theAlmhirt.executionContext
    val step1 =
      AlmFuture {
        checkCommandType(untypedcom).flatMap(com =>
          getIdAndVersion(com).map(idAndVersion =>
            (com, idAndVersion)).flatMap {
            case (com, idAndVersion) =>
              getRepository().map(repo => (com, repo, idAndVersion))
          })
      }
    val step2 =
      step1.flatMap {
        case (com, repository, (id, version)) =>
          getAggregateRoot(repository, id).mapV(ar =>
            checkArId(ar, id, com).flatMap(ar =>
              checkVersion(ar, version, com))).flatMap { ar =>
            executeHandler(com, ar).map((repository, _))
          }
      }
    step2.onComplete(
      f => updateFailedOperationState(theAlmhirt, f, ticket),
      {
        case (repository, (ar, events)) =>
          repository.actor ! StoreAggregateRootCmd(ar, events, ticket)
      })
  }

  private def updateFailedOperationState(baseOps: Almhirt, p: Problem, ticket: Option[TrackingTicket]) {
    baseOps.reportProblem(p)
    ticket match {
      case Some(t) => baseOps.reportOperationState(NotExecuted(t, p))
      case None => ()
    }
  }

  private def checkCommandType(cmd: BoundDomainCommand): AlmValidation[TCom] =
    boolean.fold(
      cmd.isMutator,
      inTryCatch(cmd.asInstanceOf[TCom]).bimap(f => f.withSeverity(Major), x => x),
      UnspecifiedProblem("not a mutator: %s".format(cmd.getClass.getName), severity = Major).failure)

  private def getIdAndVersion(cmd: TCom): AlmValidation[(UUID, Long)] =
    option.cata(cmd.aggRootRef)(
      s => (s.id, s.version).success,
      UnspecifiedProblem("Mutator without aggregate root ref: %s".format(cmd.getClass.getName)).failure)

  private def checkArId(ar: AR, id: UUID, cmd: TCom): AlmValidation[AR] =
    boolean.fold(ar.id == id, ar.success, UnspecifiedProblem("Refused to handle command: Ids do not match. The refused command is '%s'".format(cmd), severity = Major).failure)

  private def checkVersion(ar: AR, version: Long, cmd: TCom): AlmValidation[AR] =
    boolean.fold(version == ar.version,
      ar.success,
      CollisionProblem("Refused to handle command: Versions do not match. Current version is '%d', targetted version is '%d'. The refused command is '%s'".format(ar.version, version, cmd), severity = Minor).failure)

  private def getAggregateRoot(repository: AggregateRootRepository[AR, TEvent], id: UUID): AlmFuture[AR] =
    repository.get(id)
}

trait MutatorUnitOfWorkStyleFuture[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand] extends MutatorUnitOfWorkStyle[AR, TEvent, TCom] { self: BoundUnitOfWork[AR, TEvent] =>
  /** Call the handler which itself creates the required future
   */
  def handler: MutatorCommandHandlerFuture[AR, TEvent, TCom]
  protected def executeHandler(com: TCom, ar: AR)(implicit theAlmhirt: Almhirt): AlmFuture[(AR, List[TEvent])] = handler(com, ar, theAlmhirt)

}

trait MutatorUnitOfWorkStyleValidation[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand] extends MutatorUnitOfWorkStyle[AR, TEvent, TCom] { self: BoundUnitOfWork[AR, TEvent] =>
  /** Call the handler which creates a validation. Wrap the handler into a future
   */
  def handler: MutatorCommandHandler[AR, TEvent, TCom]
  protected def executeHandler(com: TCom, ar: AR)(implicit theAlmhirt: Almhirt): AlmFuture[(AR, List[TEvent])] =
    AlmFuture(handler(com, ar, theAlmhirt))
}


