package almhirt.commanding.impl

import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.core.Almhirt
import almhirt.domain._
import almhirt.commanding._
import almhirt.components.AggregateRootRepositoryRegistry

trait CommandExecutorTemplate { actor: CommandExecutor with Actor with ActorLogging =>
  import DomainMessages._

  def handlers: CommandHandlerRegistry
  implicit def theAlmhirt: Almhirt
  def repositories: AggregateRootRepositoryRegistry
  def domainCommandsSequencer: ActorRef

  def messagePublisher: almhirt.messaging.MessagePublisher

  implicit val executionContext = theAlmhirt.futuresExecutor
  val futuresMaxDuration = theAlmhirt.durations.longDuration

  override def receiveCommandExecutorMessage: Receive = {
    case cmd: Command =>
      log.debug(s"""Received command with id "${cmd.commandId}" of type "${cmd.getClass().getName()}"""")
      messagePublisher.publish(CommandReceived(cmd))
      initiateExecution(cmd)
    case DomainCommandsSequencer.DomainCommandsSequenceCreated(groupLabel, domainCommandSequence) =>
      log.debug(s"""Received command sequence "$groupLabel"(${domainCommandSequence.size}).""")
      executeDomainCommandSequence(domainCommandSequence)
    case DomainCommandsSequencer.DomainCommandsSequenceNotCreated(grouplabel: String, problem: Problem) =>
      log.error(s""""$grouplabel":${problem.message}""")
      throw new Exception(problem.message)
  }

  def initiateExecution(cmd: Command) {
    cmd match {
      case cmd: DomainCommand =>
        if (cmd.isPartOfAGroup)
          domainCommandsSequencer ! DomainCommandsSequencer.SequenceDomainCommand(cmd)
        else {
          if (cmd.canBeTracked)
            messagePublisher.publish(ExecutionStateChanged(ExecutionStarted(cmd.trackingId)))
          executeDomainCommand(cmd)
        }
      case cmd: Command =>
        if (cmd.canBeTracked)
          messagePublisher.publish(ExecutionStateChanged(ExecutionStarted(cmd.trackingId)))
        executeGenericCommand(cmd)
    }
  }

  def executeGenericCommand(cmd: Command) {
    handlers.get(cmd.getClass()).fold(
      fail => handleFailure(cmd, fail),
      handler => {
        if (cmd.canBeTracked)
          messagePublisher.publish(ExecutionStateChanged(ExecutionInProcess(cmd.trackingId)))
        handler.asInstanceOf[GenericCommandHandler](cmd).onComplete(
          fail => handleFailure(cmd, fail),
          succMsg => {
            messagePublisher.publish(CommandExecuted(cmd.commandId))
            if (cmd.canBeTracked) messagePublisher.publish(ExecutionStateChanged(ExecutionSuccessful(cmd.trackingId, succMsg)))
          })
      })
  }

  def executeDomainCommand(cmd: DomainCommand) {
    handlerAndRepoForDomainCommand(cmd).fold(
      fail => handleFailure(cmd, fail),
      {
        case (handler, repo) =>
          if (cmd.canBeTracked)
            messagePublisher.publish(ExecutionStateChanged(ExecutionInProcess(cmd.trackingId)))
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
          CollisionProblem(s"""A command to create a new aggregate root must have a version of 0! The current command of type "${cmd.getClass().getName()}" has a version of ${cmd.targettedVersion}.""").failure
        else
          ().success
      }
      repoRes <- (repository ? UpdateAggregateRoot(res._1, res._2))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
      updatedAr <- AlmFuture.promise(evaluateRepoUpdateResponse(repoRes))
    } yield updatedAr).onComplete(
      fail => handleFailure(cmd, fail),
      ar => {
        messagePublisher.publish(CommandExecuted(cmd.commandId))
        if (cmd.canBeTracked)
          messagePublisher.publish(ExecutionStateChanged(ExecutionSuccessful(
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
        messagePublisher.publish(CommandExecuted(cmd.commandId))
        if (cmd.canBeTracked)
          messagePublisher.publish(ExecutionStateChanged(ExecutionSuccessful(
            cmd.trackingId,
            s"""The aggregate root of type "${ar.getClass().getName()}" with id "${ar.id}" was succesfully mutated ending with version "${ar.version}"""",
            Map("aggregate-root-id" -> ar.id.toString(), "aggregate-root-version" -> ar.version.toString))))
      })
  }

  // Contract: The sequence must not be empty!
  private def executeDomainCommandSequence(domainCommandSequence: Iterable[DomainCommand]) {
    val headCommand = domainCommandSequence.head
    val trackingId = headCommand.tryGetTrackingId
    trackingId.foreach(trId => messagePublisher.publish(ExecutionStateChanged(ExecutionStarted(trId))))
    (for {
      initialHandler <- AlmFuture.promise(handlers.getDomainCommandHandler(headCommand))
      repository <- AlmFuture.promise(repositories.get(initialHandler.typeOfAr))
      initialState <- {
        trackingId.foreach(trId => messagePublisher.publish(ExecutionStateChanged(ExecutionInProcess(trId))))
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
      }
      finalState <- appendMutatingCommandSequence(domainCommandSequence.tail, initialState._1, initialState._2)
      updateRes <- (repository ? UpdateAggregateRoot(finalState._1, finalState._2))(futuresMaxDuration).successfulAlmFuture[DomainMessage]
      updatedAr <- AlmFuture { evaluateRepoUpdateResponse(updateRes) }
    } yield updatedAr).onComplete(
      fail => {
        handleFailure(trackingId, fail)
        domainCommandSequence.foreach(cmd => messagePublisher.publish(CommandNotExecuted(cmd.commandId, fail)))
      },
      ar => {
        domainCommandSequence.foreach(cmd => messagePublisher.publish(CommandExecuted(cmd.commandId)))
        trackingId.foreach(trId =>
          messagePublisher.publish(ExecutionStateChanged(ExecutionSuccessful(
            trId,
            s"""The aggregate root of type "${ar.getClass().getName()}" with id "${ar.id}" was succesfully mutated(and maybe created) ending with version "${ar.version} from ${domainCommandSequence.size} commands."""",
            Map("aggregate-root-id" -> ar.id.toString(), "aggregate-root-version" -> ar.version.toString)))))

      })
  }

  private def appendMutatingCommandSequence(commands: Iterable[DomainCommand], currentState: IsAggregateRoot, previousEvents: IndexedSeq[DomainEvent]): AlmFuture[(IsAggregateRoot, IndexedSeq[DomainEvent])] = {
    def appendCommandResult(previousState: (IsAggregateRoot, IndexedSeq[DomainEvent]), command: DomainCommand): AlmFuture[(IsAggregateRoot, IndexedSeq[DomainEvent])] = {
      for {
        handler <- AlmFuture.promise { handlers.getMutatingDomainCommandHandler(command) }
        handlerRes <- handler(previousState._1, command)
      } yield (handlerRes._1, previousState._2 ++ handlerRes._2)
    }
    commands.foldLeft(AlmFuture.successful((currentState, previousEvents))) { (acc, cur) =>
      acc.flatMap { previousState =>
        appendCommandResult(previousState, cur)
      }
    }
  }

  def evaluateRepoGetResponse(response: DomainMessage, againstCommand: DomainCommand): AlmValidation[IsAggregateRoot] =
    response match {
      case RequestedAggregateRoot(ar) => ar.success
      case AggregateRootNotFound(arId) => almhirt.domain.AggregateRootNotFoundProblem(arId).failure
      case AggregateRootFetchFailed(arId, problem) => UnspecifiedProblem(s"""Could not fetch aggregate root $arId.""", cause = Some(problem)).failure
      case IncompatibleAggregateRoot(ar, expected) => UnspecifiedProblem(s"""Wrong type of aggregate root(${ar.getClass().getName()}) queried. I exepected a "$expected". This is an internal problem.""").failure
      case IncompatibleDomainEvent(expected) => UnspecifiedProblem(s"""Wrong type of domain event. I exepected a "$expected". This is an internal problem.""").failure
      case x => throw new Exception(s"""Invalid message received: "${x.getClass().getName()}"""")
    }

  def evaluateRepoUpdateResponse(response: DomainMessage): AlmValidation[IsAggregateRoot] =
    response match {
      case AggregateRootUpdated(ar) => ar.success
      case AggregateRootUpdateFailed(arid, problem) => problem.failure
      case AggregateRootNotFound(arId) => almhirt.domain.AggregateRootNotFoundProblem(arId).failure
      case AggregateRootFetchFailed(arId, problem) => problem.failure
      case IncompatibleAggregateRoot(ar, expected) => UnspecifiedProblem(s"""Wrong type of aggregate root(${ar.getClass().getName()}). I exepected a "$expected". This is an internal problem.""").failure
      case IncompatibleDomainEvent(expected) => UnspecifiedProblem(s"""Wrong type of domain event. I exepected a "$expected". This is an internal problem.""").failure
      case x => throw new Exception(s"""Invalid message received: "${x.getClass().getName()}"""")
    }

  private def handleFailure(trackingId: Option[String], problem: Problem) {
    trackingId.foreach { id =>
      messagePublisher.publish(ExecutionStateChanged(ExecutionFailed(id, problem)))
    }
  }

  private def handleFailure(cmd: Command, problem: Problem) {
    log.error(s"""An error occured executing command of type ${cmd.getClass().getName()} with id ${cmd.commandId}:\n${problem.toString()}""")
    messagePublisher.publish(CommandNotExecuted(cmd.header.id, problem))
    handleFailure(cmd.tryGetTrackingId, problem)
  }

}