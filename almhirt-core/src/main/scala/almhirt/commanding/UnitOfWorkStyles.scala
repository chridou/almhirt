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
import almhirt.environment.AlmhirtContext
import almhirt.util._
import almhirt.common.AlmFuture

trait CreatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] { self: UnitOfWork[AR, TEvent] =>
  protected def executeHandler(com: TCom, context: AlmhirtContext): AlmFuture[(AR, List[TEvent])]
  def handle(com: DomainCommand, repositories: HasRepositories, context: AlmhirtContext, ticket: Option[TrackingTicket]) {
    if (com.isCreator) {
      val command = com.asInstanceOf[TCom]
      repositories.getForAggregateRootByType(self.aggregateRootType).map(_.asInstanceOf[AggregateRootRepository[AR, TEvent]]).fold(
        fail =>
          (),
        repo => {
          executeHandler(command, context).fold(
            fail => {
              context.problemChannel.post(Message.createWithUuid(fail))
              ticket match {
                case Some(t) => context.reportOperationState(NotExecuted(t, fail))
                case None => ()
              }
            },
            succ =>
              repo.actor ! StoreAggregateRootCmd(succ._1, succ._2, ticket))
        })
    } else {
      val p = ArgumentProblem("Not a creator command: %s".format(com.getClass.getName), severity = Major)
      context.problemChannel.post(Message.createWithUuid(p))
      ticket match {
        case Some(t) => context.reportOperationState(NotExecuted(t, p))
        case None => ()
      }
    }
  }
}

trait CreatorUnitOfWorkStyleFuture[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] extends CreatorUnitOfWorkStyle[AR, TEvent, TCom] { self: UnitOfWork[AR, TEvent] =>
  def handler: CreatorCommandHandlerFuture[AR, TEvent, TCom]
  protected def executeHandler(com: TCom, context: AlmhirtContext): AlmFuture[(AR, List[TEvent])] = handler(com, context.system.futureDispatcher)

}
trait CreatorUnitOfWorkStyleValidation[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] extends CreatorUnitOfWorkStyle[AR, TEvent, TCom] { self: UnitOfWork[AR, TEvent] =>
  def handler: CreatorCommandHandler[AR, TEvent, TCom]
  protected def executeHandler(com: TCom, context: AlmhirtContext): AlmFuture[(AR, List[TEvent])] =
    AlmFuture(handler(com))(context.system.futureDispatcher)
}

trait MutatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] { self: UnitOfWork[AR, TEvent] =>
  protected def executeHandler(com: TCom, ar: AR, context: AlmhirtContext): AlmFuture[(AR, List[TEvent])]
  def handle(untypedcom: DomainCommand, repositories: HasRepositories, context: AlmhirtContext, ticket: Option[TrackingTicket]) {
    implicit val executionContext = context.system.futureDispatcher
    val step1 =
      AlmFuture {
        checkCommandType(untypedcom).bind(com =>
          getIdAndVersion(com).map(idAndVersion =>
            (com, idAndVersion)))
      }.flatMap { case (com, idAndVersion) => getRepository(repositories).map(repo => (com, repo, idAndVersion)) }
    val step2 =
      step1.flatMap {
        case (com, repository, (id, version)) =>
          getAggregateRoot(repository, id).mapV(ar =>
            checkArId(ar, id, com).bind(ar =>
              checkVersion(ar, version, com))).flatMap { ar =>
            executeHandler(com, ar, context).map((repository, _))
          }
      }
    step2.onComplete(
      f => updateFailedOperationState(context, f, ticket),
      {
        case (repository, (ar, events)) =>
          repository.actor ! StoreAggregateRootCmd(ar, events, ticket)
      })
  }

  private def updateFailedOperationState(context: AlmhirtContext, p: Problem, ticket: Option[TrackingTicket]) {
    context.reportProblem(p)
    ticket match {
      case Some(t) => context.reportOperationState(NotExecuted(t, p))
      case None => ()
    }
  }

  private def checkCommandType(cmd: DomainCommand): AlmValidation[TCom] =
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

  private def getRepository(repositories: HasRepositories): AlmFuture[AggregateRootRepository[AR, TEvent]] =
    repositories
      .getForAggregateRootByType(self.aggregateRootType)
      .mapV(x => inTryCatch(x.asInstanceOf[AggregateRootRepository[AR, TEvent]]))

  private def getAggregateRoot(repository: AggregateRootRepository[AR, TEvent], id: UUID): AlmFuture[AR] =
    repository.get(id)
}

trait MutatorUnitOfWorkStyleFuture[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] extends MutatorUnitOfWorkStyle[AR, TEvent, TCom] { self: UnitOfWork[AR, TEvent] =>
  def handler: MutatorCommandHandlerFuture[AR, TEvent, TCom]
  protected def executeHandler(com: TCom, ar: AR, context: AlmhirtContext): AlmFuture[(AR, List[TEvent])] = handler(com, ar, context.system.futureDispatcher)

}
trait MutatorUnitOfWorkStyleValidation[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] extends MutatorUnitOfWorkStyle[AR, TEvent, TCom] { self: UnitOfWork[AR, TEvent] =>
  def handler: MutatorCommandHandler[AR, TEvent, TCom]
  protected def executeHandler(com: TCom, ar: AR, context: AlmhirtContext): AlmFuture[(AR, List[TEvent])] =
    AlmFuture(handler(com, ar))(context.system.futureDispatcher)
}


