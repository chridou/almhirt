package almhirt.commanding

import java.util.UUID
import scalaz.syntax.validation._
import scalaz.std._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.almvalidation.funs._
import almhirt.messaging._
import almhirt.domain._
import almhirt.parts.HasRepositories
import almhirt.environment.AlmhirtContext

trait UnitOfWork[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent] extends HandlesCommand {
  def repositoryType: Class[_ <: AggregateRootRepository[AR, TEvent]]
}

trait CreatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] { self: UnitOfWork[AR, TEvent] =>
  def handler: CreatorCommandHandler[AR, TEvent, TCom]
  def handle(com: DomainCommand, repositories: HasRepositories, context: AlmhirtContext, ticket: Option[String]) {
    if (com.isCreator) {
      val command = com.asInstanceOf[TCom]
      repositories.getByType(repositoryType).map(_.asInstanceOf[AggregateRootRepository[AR, TEvent]]).fold(
        fail =>
          (),
        repo => {
          handler(command).fold(
            fail => {
              context.problemChannel.post(Message.createWithUuid(fail))
              ticket match {
                case Some(t) => context.operationStateChannel.post(Message.createWithUuid(NotExecuted(t, fail)))
                case None => ()
              }
            },
            succ =>
              repo.store(succ._1, succ._2, ticket))
        })
    } else {
      val p = ArgumentProblem("Not a creator command: %s".format(com.getClass.getName), severity = Major)
      context.problemChannel.post(Message.createWithUuid(p))
      ticket match {
        case Some(t) => context.operationStateChannel.post(Message.createWithUuid(NotExecuted(t, p)))
        case None => ()
      }
    }
  }
}

trait MutatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] { self: UnitOfWork[AR, TEvent] =>
  def handler: MutatorCommandHandler[AR, TEvent, TCom]
  def handle(untypedcom: DomainCommand, repositories: HasRepositories, context: AlmhirtContext, ticket: Option[String]) {
    //implicit val executionContext = context.akkaContext.futureDispatcher
    checkCommandType(untypedcom).bind(com =>
      getIdAndVersion(com).bind(idAndVersion =>
        getRepository(repositories).sideEffect(
          p =>  updateFailedOperationState(context, p, ticket),
          repository =>
            getAggregateRoot(repository, idAndVersion._1).mapV(ar =>
              checkArId(ar, idAndVersion._1).bind(ar =>
                checkVersion(ar, idAndVersion._2).bind(ar =>
                  handler(com, ar).sideEffect(
                    p => updateFailedOperationState(context, p, ticket),
                    res => repository.store(res._1, res._2, ticket))))))))
  }

  private def updateFailedOperationState(context: AlmhirtContext, p: Problem, ticket: Option[String]) {
      context.problemChannel.post(Message.createWithUuid(p))
      ticket match {
        case Some(t) => context.operationStateChannel.post(Message.createWithUuid(NotExecuted(t, p)))
        case None => ()
      }
  }
  
  private def checkCommandType(cmd: DomainCommand): AlmValidation[TCom] =
    boolean.fold(
      cmd.isMutator,
      inTryCatch(cmd.asInstanceOf[TCom]).bimap(f => f.withSeverity(Major), x => x),
      UnspecifiedProblem("not a mutator: %s".format(cmd.getClass.getName), severity = Major).failure)

  private def getIdAndVersion(cmd: TCom): AlmValidation[(UUID, Option[Long])] =
    option.cata(cmd.aggRootRef)(
        s => (s.id, s.version).success, 
        UnspecifiedProblem("Mutator without aggregate root ref: %s".format(cmd.getClass.getName)).failure)
      
  private def checkArId(ar: AR, id: UUID): AlmValidation[AR] =
    boolean.fold(ar.id == id, ar.success, UnspecifiedProblem("ids do not match", severity = Major).failure)

  private def checkVersion(ar: AR, version: Option[Long]): AlmValidation[AR] =
    option.cata(version)(
      v => boolean.fold(v == ar.version, ar.success, CollisionProblem("versions do not match", severity = Minor).failure),
      ar.success)

  private def getRepository(repositories: HasRepositories): AlmValidation[AggregateRootRepository[AR, TEvent]] =
    repositories
      .getByType(repositoryType)
      .bind(x => inTryCatch(x.asInstanceOf[AggregateRootRepository[AR, TEvent]]))

  private def getAggregateRoot(repository: AggregateRootRepository[AR, TEvent], id: UUID): AlmFuture[AR] =
    repository.get(id)
}

