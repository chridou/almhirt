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

trait CreatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] { self: BoundUnitOfWork[AR, TEvent] =>
  protected def executeHandler(com: TCom): AlmFuture[(AR, List[TEvent])]
  def handleBoundCommand(com: BoundDomainCommand, ticket: Option[TrackingTicket]) {
    if (com.isCreator) {
      val command = com.asInstanceOf[TCom]
      self.getRepository().fold(
        fail =>
          (),
        repo => {
          executeHandler(command).fold(
            fail => {
              almhirt.reportProblem(fail)
              ticket match {
                case Some(t) => almhirt.reportOperationState(NotExecuted(t, fail))
                case None => ()
              }
            },
            succ =>
              repo.actor ! StoreAggregateRootCmd(succ._1, succ._2, ticket))(almhirt.executionContext)
        })
    } else {
      val p = ArgumentProblem("Not a creator command: %s".format(com.getClass.getName), severity = Major)
      almhirt.reportProblem(p)
      ticket match {
        case Some(t) => almhirt.reportOperationState(NotExecuted(t, p))
        case None => ()
      }
    }
  }
}

trait CreatorUnitOfWorkStyleFuture[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand] extends CreatorUnitOfWorkStyle[AR, TEvent, TCom] { self: BoundUnitOfWork[AR, TEvent] =>
  def handler: CreatorCommandHandlerFuture[AR, TEvent, TCom]
  protected def executeHandler(com: TCom): AlmFuture[(AR, List[TEvent])] = handler(com)

}
trait CreatorUnitOfWorkStyleValidation[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand] extends CreatorUnitOfWorkStyle[AR, TEvent, TCom] { self: BoundUnitOfWork[AR, TEvent] =>
  def handler: CreatorCommandHandler[AR, TEvent, TCom]
  protected def executeHandler(com: TCom): AlmFuture[(AR, List[TEvent])] =
    AlmFuture(handler(com))(almhirt.executionContext)
}

trait MutatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand] { self: BoundUnitOfWork[AR, TEvent] =>
  protected def executeHandler(com: TCom, ar: AR): AlmFuture[(AR, List[TEvent])]
  def handleBoundCommand(untypedcom: BoundDomainCommand, ticket: Option[TrackingTicket]) {
    implicit val executionContext = almhirt.executionContext
    val step1 =
      AlmFuture {
        checkCommandType(untypedcom).flatMap(com =>
          getIdAndVersion(com).map(idAndVersion =>
            (com, idAndVersion)).flatMap {
            case (com, idAndVersion) =>
              self.getRepository().map(repo => (com, repo, idAndVersion))
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
      f => updateFailedOperationState(almhirt, f, ticket),
      {
        case (repository, (ar, events)) =>
          repository.actor ! StoreAggregateRootCmd(ar, events, ticket)
      })
  }

  private def updateFailedOperationState(baseOps: AlmhirtBaseOps, p: Problem, ticket: Option[TrackingTicket]) {
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
  def handler: MutatorCommandHandlerFuture[AR, TEvent, TCom]
  protected def executeHandler(com: TCom, ar: AR): AlmFuture[(AR, List[TEvent])] = handler(com, ar)

}

trait MutatorUnitOfWorkStyleValidation[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: BoundDomainCommand] extends MutatorUnitOfWorkStyle[AR, TEvent, TCom] { self: BoundUnitOfWork[AR, TEvent] =>
  def handler: MutatorCommandHandler[AR, TEvent, TCom]
  protected def executeHandler(com: TCom, ar: AR): AlmFuture[(AR, List[TEvent])] =
    AlmFuture(handler(com, ar))(almhirt.executionContext)
}


