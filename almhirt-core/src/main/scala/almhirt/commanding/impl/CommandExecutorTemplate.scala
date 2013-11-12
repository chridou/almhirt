package almhirt.commanding.impl

import scalaz.syntax.validation._
import scala.concurrent.duration._
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

  def maxExecutionTimePerCommandWarnThreshold: FiniteDuration
  def maxExecutionTimePerCommandSequenceWarnThreshold: FiniteDuration

  implicit val executionContext = theAlmhirt.futuresExecutor
  val futuresMaxDuration = theAlmhirt.durations.longDuration

  def reportingDiff: Long

  protected var commandsReceived = 0L
  protected var sequencedCommandsReceived = 0L
  protected var sequencesReceived = 0L
  protected var commandsFailed = 0L

  private var countersSumOld = 0L

  private case object CommandFailed

  override def receiveCommandExecutorMessage: Receive = {
    case cmd: Command =>
      messagePublisher.publish(CommandReceived(cmd))
      initiateExecution(cmd)
    case DomainCommandsSequencer.DomainCommandsSequenceCreated(groupLabel, domainCommandSequence) =>
      sequencesReceived += 1
      reportOnDiff()
      executeDomainCommandSequence(domainCommandSequence)
    case DomainCommandsSequencer.DomainCommandsSequenceNotCreated(grouplabel: String, problem: Problem) =>
      throw new Exception(problem.message)
    case CommandFailed =>
      commandsFailed += 1
  }

  def initiateExecution(cmd: Command) {
    cmd match {
      case cmd: DomainCommand =>
        if (cmd.isPartOfAGroup) {
          domainCommandsSequencer ! DomainCommandsSequencer.SequenceDomainCommand(cmd)
          sequencedCommandsReceived += 1
          reportOnDiff()
        } else {
          if (cmd.canBeTracked)
            messagePublisher.publish(ExecutionStateChanged(ExecutionStarted(cmd.trackingId)))
          executeDomainCommand(cmd)
          commandsReceived += 1
          reportOnDiff()
        }
      case cmd: Command =>
        if (cmd.canBeTracked)
          messagePublisher.publish(ExecutionStateChanged(ExecutionStarted(cmd.trackingId)))
        commandsReceived += 1
        reportOnDiff()
        executeGenericCommand(cmd)
    }
  }

  def executeGenericCommand(cmd: Command) {
    val start = Deadline.now
    handlers.get(cmd.getClass()).fold(
      fail => handleFailure(cmd, fail),
      handler => {
        if (cmd.canBeTracked)
          messagePublisher.publish(ExecutionStateChanged(ExecutionInProcess(cmd.trackingId)))
        handler.asInstanceOf[GenericCommandHandler](cmd).onComplete(
          fail => handleFailure(cmd, fail),
          succMsg => {
            val time = start.lap
            messagePublisher.publish(CommandExecuted(cmd))
            if (cmd.canBeTracked) messagePublisher.publish(ExecutionStateChanged(ExecutionSuccessful(cmd.trackingId, succMsg)))
            if (time.exceeds(maxExecutionTimePerCommandWarnThreshold)) {
              if (cmd.canBeTracked)
                log.warning(s"""Execution of generic command "${cmd.getClass().getName()}"(id = ${cmd.commandId})(trckId "${cmd.trackingId}") took longer than ${maxExecutionTimePerCommandWarnThreshold.defaultUnitString}(${time.defaultUnitString})."""")
              else
                log.warning(s"""Execution of generic command "${cmd.getClass().getName()}"(id = ${cmd.commandId}) took longer than ${maxExecutionTimePerCommandWarnThreshold.defaultUnitString}(${time.defaultUnitString})."""")
            }
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
    val start = Deadline.now
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
        val time = start.lap
        messagePublisher.publish(CommandExecuted(cmd))
        if (cmd.canBeTracked)
          messagePublisher.publish(ExecutionStateChanged(ExecutionSuccessful(
            cmd.trackingId,
            s"""The aggregate root of type "${ar.getClass().getName()}" with id "${ar.id}" was succesfully created with version "${ar.version}"""",
            Map("aggregate-root-id" -> ar.id.toString(), "aggregate-root-version" -> ar.version.toString))))
        if (time.exceeds(maxExecutionTimePerCommandWarnThreshold)) {
          if (cmd.canBeTracked)
            log.warning(s"""Execution of creating domain command "${cmd.getClass().getName()}"(id = ${cmd.commandId})(trckId "${cmd.trackingId}") on aggregate root id "${ar.id}" took longer than ${maxExecutionTimePerCommandWarnThreshold.defaultUnitString}(${time.defaultUnitString})."""")
          else
            log.warning(s"""Execution of creating domain command "${cmd.getClass().getName()}"(id = ${cmd.commandId}) on aggregate root id "${ar.id}" took longer than ${maxExecutionTimePerCommandWarnThreshold.defaultUnitString}(${time.defaultUnitString})."""")
        }
      })
  }

  def executeMutatingDomainCommand(cmd: DomainCommand, handler: MutatingDomainCommandHandler, repository: ActorRef) {
    val start = Deadline.now
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
        val time = start.lap
        messagePublisher.publish(CommandExecuted(cmd))
        if (cmd.canBeTracked)
          messagePublisher.publish(ExecutionStateChanged(ExecutionSuccessful(
            cmd.trackingId,
            s"""The aggregate root of type "${ar.getClass().getName()}" with id "${ar.id}" was succesfully mutated ending with version "${ar.version}"""",
            Map("aggregate-root-id" -> ar.id.toString(), "aggregate-root-version" -> ar.version.toString))))
        if (time.exceeds(maxExecutionTimePerCommandWarnThreshold)) {
          if (cmd.canBeTracked)
            log.warning(s"""Execution of mutating domain command "${cmd.getClass().getName()}"(id = ${cmd.commandId})(trckId "${cmd.trackingId}") on aggregate root id "${ar.id}" took longer than ${maxExecutionTimePerCommandWarnThreshold.defaultUnitString}(${time.defaultUnitString})."""")
          else
            log.warning(s"""Execution of mutating domain command "${cmd.getClass().getName()}"(id = ${cmd.commandId}) on aggregate root id "${ar.id}" took longer than ${maxExecutionTimePerCommandWarnThreshold.defaultUnitString}(${time.defaultUnitString})."""")
        }
      })
  }

  // Contract: The sequence must not be empty!
  private def executeDomainCommandSequence(domainCommandSequence: Iterable[DomainCommand]) {
    val start = Deadline.now
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
            else {
              hdl(headCommand)
            }
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
        domainCommandSequence.foreach(cmd => messagePublisher.publish(CommandNotExecuted(cmd, fail)))
      },
      ar => {
        val lap = start.lap
        domainCommandSequence.foreach(cmd => messagePublisher.publish(CommandExecuted(cmd)))
        trackingId.foreach(trId =>
          messagePublisher.publish(ExecutionStateChanged(ExecutionSuccessful(
            trId,
            s"""The aggregate root of type "${ar.getClass().getName()}" with id "${ar.id}" was succesfully mutated(and maybe created) ending with version "${ar.version} from ${domainCommandSequence.size} commands."""",
            Map("aggregate-root-id" -> ar.id.toString(), "aggregate-root-version" -> ar.version.toString)))))
        if (lap.exceeds(maxExecutionTimePerCommandSequenceWarnThreshold)) {
          trackingId match {
            case Some(trckId) =>
              log.warning(s"""Execution of domain command sequence (trckId "$trckId") took longer than ${maxExecutionTimePerCommandSequenceWarnThreshold.defaultUnitString}(${lap.defaultUnitString})."""")
            case None =>
              log.warning(s"""Execution of domain command sequence took longer than ${maxExecutionTimePerCommandSequenceWarnThreshold.defaultUnitString}(${lap.defaultUnitString})."""")
          }
        }
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
    self ! CommandFailed
  }

  private def handleFailure(cmd: Command, problem: Problem) {
    log.error(s"""An error occured executing command of type ${cmd.getClass().getName()} with id ${cmd.commandId}:\n${problem.toString()}""")
    messagePublisher.publish(CommandNotExecuted(cmd, problem))
    handleFailure(cmd.tryGetTrackingId, problem)
  }

  private def reportOnDiff() {
    val sumNew = commandsReceived + sequencedCommandsReceived
    if (sumNew - countersSumOld >= reportingDiff) {
      log.info(s"Commands received: $commandsReceived, sequenced commands received: $sequencedCommandsReceived, command sequences received: $sequencesReceived.")
      countersSumOld = sumNew
    }
  }

}