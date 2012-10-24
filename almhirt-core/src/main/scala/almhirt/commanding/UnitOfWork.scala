package almhirt.commanding

import almhirt._
import almhirt.messaging._
import almhirt.domain._
import almhirt.parts.HasRepositories
import almhirt.environment.AlmhirtContext

trait UnitOfWork[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent] extends HandlesCommand {
  def repositoryType: Class[_ <: AggregateRootRepository[AR, TEvent]]
}

trait CreatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] { self: UnitOfWork[AR, TEvent] =>
  def handler: CreatorCommandHandler[AR, TEvent, TCom]
  def handle(com: DomainCommand, repositories: HasRepositories, context: AlmhirtContext) {
    if (com.isCreator) {
      val command = com.asInstanceOf[TCom]
      repositories.getByType(repositoryType).map(_.asInstanceOf[AggregateRootRepository[AR, TEvent]]).fold(
        fail =>
          (),
        repo => {
          handler(command).fold(
            fail => {
              context.problemChannel.post(Message.createWithUuid(fail))
              command.ticket match {
                case Some(t) => context.operationStateChannel.post(Message.createWithUuid(NotExecuted(t, fail)))
                case None => ()
              }
            },
            succ =>
              repo.store(succ._1, succ._2, command.ticket))
        })
    } else {
      val p = ArgumentProblem("Not a creator command: %s".format(com.getClass.getName), severity = Major)
      context.problemChannel.post(Message.createWithUuid(p))
      com.ticket match {
        case Some(t) => context.operationStateChannel.post(Message.createWithUuid(NotExecuted(t, p)))
        case None => ()
      }
    }
  }
}

trait MutatorUnitOfWorkStyle[AR <: AggregateRoot[AR, TEvent], TEvent <: DomainEvent, TCom <: DomainCommand] { self: UnitOfWork[AR, TEvent] =>
  def handler: MutatorCommandHandler[AR, TEvent, TCom]
  def handle(com: DomainCommand, repositories: HasRepositories, context: AlmhirtContext) {
    if (com.isMutator) {
      val command = com.asInstanceOf[TCom]
      val arRef = command.aggRootRef.get
      repositories.getByType(repositoryType).map(_.asInstanceOf[AggregateRootRepository[AR, TEvent]]).fold(
        fail =>
          (),
        repo => {
          repo.get(arRef.id).map { aggRoot =>
            handler(command, aggRoot).fold(
              fail => {
                context.problemChannel.post(Message.createWithUuid(fail))
                command.ticket match {
                  case Some(t) => context.operationStateChannel.post(Message.createWithUuid(NotExecuted(t, fail)))
                  case None => ()
                }
              },
              succ =>
                repo.store(succ._1, succ._2, command.ticket))
          }
        })
    } else {
      val p = ArgumentProblem("Not a mutator command: %s".format(com.getClass.getName), severity = Major)
      context.problemChannel.post(Message.createWithUuid(p))
      com.ticket match {
        case Some(t) => context.operationStateChannel.post(Message.createWithUuid(NotExecuted(t, p)))
        case None => ()
      }
    }
  }
}

