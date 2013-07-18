package almhirt.commanding

import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.core.Almhirt
import almhirt.domain._
import almhirt.components.AggregateRootRepositoryRegistry

trait CommandExecutor { actor: Actor with ActorLogging =>
  def receiveCommandExecutorMessage: Receive
}

trait CommandExecutorTemplate { actor: CommandExecutor with Actor with ActorLogging =>
  import DomainMessages._

  def handlers: CommandHandlerRegistry
  def theAlmhirt: Almhirt
  def repositories: AggregateRootRepositoryRegistry

  implicit val executionContext = theAlmhirt.futuresExecutor
  val futuresMaxDuration = theAlmhirt.durations.longDuration

  override def receiveCommandExecutorMessage: Receive = {
    case cmd: Command =>
      executeCommand(cmd)
  }

  def executeCommand(cmd: Command) {
    cmd match {
      case cmd: DomainCommand => executeDomainCommand(cmd)
      case cmd: Command => executeGenericCommand(cmd)
    }
  }

  def executeGenericCommand(cmd: Command) {
    handlers.get(cmd.getClass()).fold(
      fail => ???,
      handler => handler.asInstanceOf[GenericCommandHandler](cmd, repositories, theAlmhirt))
  }

  def executeDomainCommand(cmd: DomainCommand) {
    handlerAndRepoForDomainCommand(cmd).fold(
      fail => ???,
      {
        case (handler, repo) =>
          handler match {
            case hdl: CreatingDomainCommandHandler => executeCreatingDomainCommand(cmd, hdl, repo)
            case hdl: MutatingDomainCommandHandler => executeMutatingDomainCommand(cmd, hdl, repo)
          }
      })
  }

  def handlerAndRepoForDomainCommand(cmd: DomainCommand): AlmValidation[(DomainCommandHandler, ActorRef)] =
    for {
      handler <- handlers.get(cmd.getClass()).map(unspecified =>
        if (cmd.creates)
          unspecified.asInstanceOf[CreatingDomainCommandHandler]
        else
          unspecified.asInstanceOf[MutatingDomainCommandHandler])
      repo <- repositories.get(handler.typeOfAr)
    } yield (handler, repo)

  def executeCreatingDomainCommand(cmd: DomainCommand, handler: CreatingDomainCommandHandler, repository: ActorRef) {
    (for {
      res <- handler(cmd, theAlmhirt)
      repoRes <- (repository ? UpdateAggregateRoot(res._1, res._2))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
      updatedAr <- AlmFuture { evaluateRepoUpdateResponse(repoRes) }
    } yield updatedAr).onComplete(
      fail => ???,
      succ => ???)
  }

  def executeMutatingDomainCommand(cmd: DomainCommand, handler: MutatingDomainCommandHandler, repository: ActorRef) {
    (for {
      repoGetResp <- (repository ? GetAggregateRoot(cmd.targettedAggregateRoot))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
      currentState <- AlmFuture { evaluateRepoGetResponse(repoGetResp, cmd) }
      res <- handler(currentState, cmd, theAlmhirt)
      repoRes <- (repository ? UpdateAggregateRoot(res._1, res._2))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
      updatedAr <- AlmFuture { evaluateRepoUpdateResponse(repoRes) }
    } yield updatedAr).onComplete(
      fail => ???,
      succ => ???)
  }

  def evaluateRepoGetResponse(response: DomainMessage, againstCommand: DomainCommand): AlmValidation[IsAggregateRoot] =
    response match {
      case RequestedAggregateRoot(ar) => ar.success
      case AggregateRootNotFound(arId) => ???
      case AggregateRootFetchFailed(arId, problem) => ???
      case IncompatibleAggregateRoot(ar, expected) => ???
      case IncompatibleDomainEvent(expected) => ???
      case x => ???
    }

  def evaluateRepoUpdateResponse(response: DomainMessage): AlmValidation[IsAggregateRoot] =
    response match {
      case AggregateRootUpdated(ar) => ar.success
      case AggregateRootUpdateFailed(problem) => ???
      case AggregateRootNotFound(arId) => ???
      case AggregateRootFetchFailed(arId, problem) => ???
      case IncompatibleAggregateRoot(ar, expected) => ???
      case IncompatibleDomainEvent(expected) => ???
      case x => ???
    }

}