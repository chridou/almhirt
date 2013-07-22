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
  implicit def theAlmhirt: Almhirt
  def repositories: AggregateRootRepositoryRegistry
  def domainCommandsSequencer: ActorRef

  implicit val executionContext = theAlmhirt.futuresExecutor
  val futuresMaxDuration = theAlmhirt.durations.longDuration

  override def receiveCommandExecutorMessage: Receive = {
    case cmd: Command =>
      theAlmhirt.messageBus.publish(CommandReceived(cmd))
      initiateExecution(cmd)
    case DomainCommandsSequencer.DomainCommandsSequenceCreated(domainCommandSequence) =>
      executeDomainCommandSequence(domainCommandSequence)
    case DomainCommandsSequencer.DomainCommandsSequenceNotCreated(grouplabel: String, problem: Problem) =>
      ()
  }

  def initiateExecution(cmd: Command) {
    cmd match {
      case cmd: DomainCommand =>
        if (cmd.isPartOfAGroup)
          domainCommandsSequencer ! DomainCommandsSequencer.SequenceDomainCommand(cmd)
        else {
          if (cmd.canBeTracked)
            theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionStarted(cmd.trackingId)))
          executeDomainCommand(cmd)
        }
      case cmd: Command =>
        if (cmd.canBeTracked)
          theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionStarted(cmd.trackingId)))
        executeGenericCommand(cmd)
    }
  }

  def executeGenericCommand(cmd: Command) {
    handlers.get(cmd.getClass()).fold(
      fail => handleFailure(cmd, fail),
      handler => {
        if (cmd.canBeTracked)
          theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionInProcess(cmd.trackingId)))
        handler.asInstanceOf[GenericCommandHandler](cmd).onComplete(
          fail => handleFailure(cmd, fail),
          succMsg => {
            theAlmhirt.messageBus.publish(CommandExecuted(cmd.id))
            if (cmd.canBeTracked) theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionSuccessful(cmd.trackingId, succMsg)))
          })
      })
  }

  def executeDomainCommand(cmd: DomainCommand) {
    handlerAndRepoForDomainCommand(cmd).fold(
      fail => handleFailure(cmd, fail),
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
      handler <- handlers.getDomainCommandHandler(cmd)
      repo <- repositories.get(handler.typeOfAr)
    } yield (handler, repo)

  def executeCreatingDomainCommand(cmd: DomainCommand, handler: CreatingDomainCommandHandler, repository: ActorRef) {
    (for {
      res <- handler(cmd)
      _ <- AlmFuture.promise {
        if (cmd.targettedVersion != 0L)
          CollisionProblem("A command to create a new aggregate root must have a version of 0!").failure
        else
          ().success
      }
      repoRes <- (repository ? UpdateAggregateRoot(res._1, res._2))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
      updatedAr <- AlmFuture.promise(evaluateRepoUpdateResponse(repoRes))
    } yield updatedAr).onComplete(
      fail => handleFailure(cmd, fail),
      ar => {
        theAlmhirt.messageBus.publish(CommandExecuted(cmd.id))
        if (cmd.canBeTracked)
          theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionSuccessful(
            cmd.trackingId,
            s"""The aggregate root of type "${ar.getClass().getName()}" with id "${ar.id}" was succesfully created with version "${ar.version}"""",
            Map("aggregate-root-id" -> ar.id.toString(), "aggregate-root-version" -> ar.version.toString))))
      })
  }

  def executeMutatingDomainCommand(cmd: DomainCommand, handler: MutatingDomainCommandHandler, repository: ActorRef) {
    (for {
      repoGetResp <- (repository ? GetAggregateRoot(cmd.targettedAggregateRootId))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
      currentState <- AlmFuture.promise(evaluateRepoGetResponse(repoGetResp, cmd))
      _ <- AlmFuture.promise {
        if (cmd.targettedVersion != currentState.version)
          CollisionProblem(s"""A command to mutate an aggregate root must target its version(${currentState.version}). The command targets version ${cmd.targettedVersion}.""").failure
        else
          ().success
      }
      res <- handler(currentState, cmd)
      repoRes <- (repository ? UpdateAggregateRoot(res._1, res._2))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
      updatedAr <- AlmFuture.promise(evaluateRepoUpdateResponse(repoRes))
    } yield updatedAr).onComplete(
      fail => handleFailure(cmd, fail),
      ar => {
        theAlmhirt.messageBus.publish(CommandExecuted(cmd.id))
        if (cmd.canBeTracked)
          theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionSuccessful(
            cmd.trackingId,
            s"""The aggregate root of type "${ar.getClass().getName()}" with id "${ar.id}" was succesfully mutated ending with version "${ar.version}"""",
            Map("aggregate-root-id" -> ar.id.toString(), "aggregate-root-version" -> ar.version.toString))))
      })
  }

  // Contract: The sequence must not be empty!
  private def executeDomainCommandSequence(domainCommandSequence: Iterable[DomainCommand]) {
    val headCommand = domainCommandSequence.head
    val trackingId = headCommand.tryGetTrackingId
    trackingId.foreach(trId => theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionStarted(trId))))
    (for {
      initialHandler <- AlmFuture.promise(handlers.getDomainCommandHandler(headCommand))
      repository <- AlmFuture.promise(repositories.get(initialHandler.typeOfAr))
      initialState <- {
        trackingId.foreach(trId => theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionInProcess(trId))))
        initialHandler match {
          case hdl: CreatingDomainCommandHandler =>
            if (headCommand.targettedVersion != 0L)
              AlmFuture.promise(CollisionProblem("A command to create a new aggregate root must have a version of 0!").failure)
            else
              hdl(headCommand)
          case hdl: MutatingDomainCommandHandler =>
            for {
              repoGetResp <- (repository ? GetAggregateRoot(headCommand.targettedAggregateRootId))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
              fetchedState <- AlmFuture.promise { evaluateRepoGetResponse(repoGetResp, headCommand) }
              _ <- AlmFuture.promise {
                if (headCommand.targettedVersion != fetchedState.version)
                  CollisionProblem(s"""A command to mutate an aggregate root must target its version(${fetchedState.version}). The command targets version ${headCommand.targettedVersion}.""").failure
                else
                  ().success
              }
              res <- hdl(fetchedState, headCommand)
            } yield res
        }
      } andThen (
        fail => theAlmhirt.messageBus.publish(CommandNotExecuted(headCommand.id, fail)),
        succ => theAlmhirt.messageBus.publish(CommandExecuted(headCommand.id)))
      finalState <- appendMutatingCommandSequence(domainCommandSequence.tail, initialState._1, initialState._2)
      updateRes <- (repository ? UpdateAggregateRoot(finalState._1, finalState._2))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
      updatedAr <- AlmFuture { evaluateRepoUpdateResponse(updateRes) }
    } yield updatedAr).onComplete(
      fail => handleFailure(trackingId, fail),
      ar => trackingId.foreach(trId =>
        theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionSuccessful(
          trId,
          s"""The aggregate root of type "${ar.getClass().getName()}" with id "${ar.id}" was succesfully mutated(and maybe created) ending with version "${ar.version} from ${domainCommandSequence.size} commands."""",
          Map("aggregate-root-id" -> ar.id.toString(), "aggregate-root-version" -> ar.version.toString))))))
  }

  private def appendMutatingCommandSequence(commands: Iterable[DomainCommand], currentState: IsAggregateRoot, previousEvents: IndexedSeq[DomainEvent]): AlmFuture[(IsAggregateRoot, IndexedSeq[DomainEvent])] = {
    def appendCommandResult(previousState: (IsAggregateRoot, IndexedSeq[DomainEvent]), command: DomainCommand): AlmFuture[(IsAggregateRoot, IndexedSeq[DomainEvent])] = {
      for {
        handler <- AlmFuture.promise { handlers.getMutatingDomainCommandHandler(command) }
        handlerRes <- handler(previousState._1, command) andThen (
          fail => theAlmhirt.messageBus.publish(CommandNotExecuted(command.id, fail)),
          succ => theAlmhirt.messageBus.publish(CommandExecuted(command.id)))
      } yield (handlerRes._1, previousState._2 ++ handlerRes._2)
    }
    commands.foldLeft(AlmFuture.successful((currentState, previousEvents)))((acc, cur) => acc.flatMap(previousState =>
      appendCommandResult(previousState, cur)))
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

  private def handleFailure(trackingId: Option[String], problem: Problem) {
    trackingId.foreach(id => theAlmhirt.messageBus.publish(ExecutionStateChanged(ExecutionFailed(id, problem))))
  }

  private def handleFailure(cmd: Command, problem: Problem) {
    theAlmhirt.messageBus.publish(CommandNotExecuted(cmd.header.id, problem))
    handleFailure(cmd.tryGetTrackingId, problem)
  }

}