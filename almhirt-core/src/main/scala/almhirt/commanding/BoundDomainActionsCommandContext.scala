package almhirt.commanding

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import scalaz.std._
import akka.actor.ActorRef
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.domain._
import almhirt.util._
import almhirt.parts.HasRepositories

trait BoundDomainActionsCommandContext[TAR <: AggregateRoot[TAR, TEvent], TEvent <: DomainEvent] {
  protected implicit val tagAR: ClassTag[TAR]
  protected implicit val tagEvent: ClassTag[TEvent]

  sealed trait CommandAction
  trait CreatorAction extends CommandAction
  trait MutatorAction extends CommandAction

  type CreatingActionHandler[TAct <: CreatorAction] = (TAct, Almhirt) => UpdateRecorder[TEvent, TAR]
  type MutatingActionHandler[TAct <: MutatorAction] = (TAct, TAR, Almhirt) => UpdateRecorder[TEvent, TAR]

  trait BoundDomainActionsCommand extends DomainCommand {
    def aggRef: Option[AggregateRootRef]
    def actions: List[CommandAction]
  }

  def flattenCreatingActionHandler[TAct <: CreatorAction](handler: CreatingActionHandler[TAct])(implicit tag: ClassTag[TAct]): CreatingActionHandler[CreatorAction] = {
    (action, theAlmhirt) =>
      action.castTo[TAct].fold(
        fail => UpdateRecorder.reject(fail),
        succ => handler(succ, theAlmhirt))
  }

  def flattenMutatingActionHandler[TAct <: MutatorAction](handler: MutatingActionHandler[TAct])(implicit tag: ClassTag[TAct]): MutatingActionHandler[MutatorAction] = {
    (action, ar, theAlmhirt) =>
      action.castTo[TAct].fold(
        fail => UpdateRecorder.reject(fail),
        succ => handler(succ, ar, theAlmhirt))
  }

  trait HasActionHandlers {
    def getCreatingHandler(action: CreatorAction): AlmValidation[CreatingActionHandler[CreatorAction]]
    def getMutatingHandler(action: MutatorAction): AlmValidation[MutatingActionHandler[MutatorAction]]
  }

  def createHasActionHandlers(creators: List[(Class[_], CreatingActionHandler[CreatorAction])], mutators: List[(Class[_], MutatingActionHandler[MutatorAction])]): HasActionHandlers = {
    val creatorsByClass = creators.toMap
    val mutatorsByClass = mutators.toMap

    new HasActionHandlers {
      override def getCreatingHandler(action: CreatorAction): AlmValidation[CreatingActionHandler[CreatorAction]] =
        option.cata(creatorsByClass.get(action.getClass()))(
          handler => handler.success,
          NoSuchElementProblem(s"No creating handler found for action ${action.getClass().getName()}").failure)

      override def getMutatingHandler(action: MutatorAction): AlmValidation[MutatingActionHandler[MutatorAction]] =
        option.cata(mutatorsByClass.get(action.getClass()))(
          handler => handler.success,
          NoSuchElementProblem(s"No mutating handler found for action ${action.getClass().getName()}").failure)
    }
  }

  trait BoundUnitOfWork extends HandlesCommand {
    implicit def theAlmhirt: Almhirt
    def repository: ActorRef
    def actionHandlers: HasActionHandlers
    def handle(com: DomainCommand, ticket: Option[TrackingTicket]) {
      com match {
        case bcmd: BoundDomainActionsCommand =>
          handleBoundActionsCommand(bcmd, ticket)(theAlmhirt)
        case wrongType =>
          reportFailure(ArgumentProblem("Not a BoundDomainCommand: %s".format(wrongType.getClass.getName), severity = Major), ticket)
      }
    }

    protected def handleBoundActionsCommand(com: BoundDomainActionsCommand, ticket: Option[TrackingTicket])(implicit theAlmhirt: Almhirt) {
      com.actions match {
        case Nil => ticket.foreach(t => theAlmhirt.publishOperationState(Executed(t, PerformedUnspecifiedAction), Map.empty))
        case _ => prevalidate(com).fold(
          prob => reportFailure(prob, ticket),
          succ => executeUnitOfWork(com).onComplete(
            fail => reportFailure(fail, ticket),
            recorder => recorder.recordings.fold(
              fail => reportFailure(fail, ticket),
              succ => store(succ._1, succ._2, ticket))))
      }
    }

    protected def executeUnitOfWork(com: BoundDomainActionsCommand): AlmFuture[UpdateRecorder[TEvent, TAR]] = {
      for {
        (initial, rest) <- option.cata(com.aggRef)(
          aggRef => startFromRepo(aggRef).map(recorder => (recorder, com.actions.asInstanceOf[List[MutatorAction]])),
          {
            val creatorAction :: mutate = com.actions
            startWithNew(creatorAction.asInstanceOf[CreatorAction]).map(recorder => (recorder, mutate.asInstanceOf[List[MutatorAction]]))
          })
        resultOfMutators <- executeMutatorsOn(initial, rest)
      } yield resultOfMutators
    }

    protected def startWithNew(creatorAction: CreatorAction): AlmFuture[UpdateRecorder[TEvent, TAR]] = {
      AlmFuture.promise(actionHandlers.getCreatingHandler(creatorAction).map(handler => handler(creatorAction, theAlmhirt)))
    }

    protected def startFromRepo(aggRef: AggregateRootRef): AlmFuture[UpdateRecorder[TEvent, TAR]] = {
      import akka.pattern._
      for {
        ar <- (repository ? GetAggregateRootQry(aggRef.id))(theAlmhirt.defaultDuration).mapToAlmFutureOver[AggregateRootFromRepositoryRsp[TAR, TEvent], TAR](rsp => rsp.ar)
        recorder <- AlmFuture.promise {
          validateAgainstAggregateRoot(ar, aggRef).map(_ => UpdateRecorder.startWith[TEvent, TAR](ar))
        }
      } yield recorder
    }

    protected def executeMutatorsOn(initial: UpdateRecorder[TEvent, TAR], actions: List[MutatorAction]): AlmFuture[UpdateRecorder[TEvent, TAR]] = {
      import akka.pattern._
      AlmFuture {
        val actionsAndHandlersV = actions.map(action => actionHandlers.getMutatingHandler(action).toAgg.map(h => (action, h)))
        val actionsAndHandlers = actionsAndHandlersV.sequence[AlmValidationAP, (MutatorAction, (MutatorAction, TAR, Almhirt) => UpdateRecorder[TEvent, TAR])]
        actionsAndHandlers.fold(
          fail => fail.failure,
          succ => {
            val result = succ.foldLeft(initial) { case (acc, (action, handler)) => acc.flatMap(curAr => handler(action, curAr, theAlmhirt)) }
            result.success
          })
      }
    }

    protected def prevalidate(com: BoundDomainActionsCommand): AlmValidation[BoundDomainActionsCommand] = {
      option.cata(com.aggRef)(
        aggRef =>
          boolean.fold(com.actions.forall(_.isInstanceOf[MutatorAction]),
            com.success,
            IllegalOperationProblem("When an AggregateRootRef is supplied, all actions must be mutating actions!").failure),
        {
          val h :: tail = com.actions
          boolean.fold(h.isInstanceOf[CreatorAction],
            boolean.fold(tail.forall(_.isInstanceOf[MutatorAction]),
              com.success,
              IllegalOperationProblem("When the first action is a create action, all others must be mutating actions!").failure),
            IllegalOperationProblem("When no AggregateRootRef is supplied, the first action must be a creating action!").failure)
        })
    }

    protected def store(ar: TAR, events: List[TEvent], ticket: Option[TrackingTicket]) {
      repository ! StoreAggregateRootCmd[TAR, TEvent](ar, events, ticket)
    }

    protected def validateAgainstAggregateRoot(ar: TAR, aggRef: AggregateRootRef): AlmValidation[Unit] =
      (aggRef.id == ar.id, aggRef.version == ar.version) match {
        case (true, true) => ().success
        case (true, false) => IllegalOperationProblem(s"Versions of command(${aggRef.version}) and AR(${ar.version}) do not match!").failure
        case (false, true) => IllegalOperationProblem(s"Ids of command(${aggRef.id}) and AR(${ar.id}) do not match!").failure
        case (false, false) => IllegalOperationProblem(s"Neither versions nor ids do match!").failure
      }

    protected def reportFailure(prob: Problem, ticket: Option[TrackingTicket]) {
      theAlmhirt.publishProblem(prob)
      ticket match {
        case Some(t) => theAlmhirt.publishOperationState(NotExecuted(t, prob))
        case None => ()
      }
    }

    protected def reportSuccess(action: PerformedAction, ticket: Option[TrackingTicket]) {
      ticket match {
        case Some(t) => theAlmhirt.publishOperationState(Executed(t, action))
        case None => ()
      }
    }
  }

  def createBasicUow(cmdType: Class[_ <: DomainCommand], theRepository: AggregateRootRepository[TAR, TEvent], theActionHandlers: HasActionHandlers)(implicit anAlmhirt: Almhirt): this.BoundUnitOfWork =
    new BoundUnitOfWork {
      val commandType = cmdType
      val repository = theRepository.actor
      val actionHandlers = theActionHandlers
      val theAlmhirt = anAlmhirt
    }

  def createBasicUow(cmdType: Class[_ <: DomainCommand], theServiceRegistry: ServiceRegistry, theActionHandlers: HasActionHandlers)(implicit anAlmhirt: Almhirt): AlmValidation[this.BoundUnitOfWork] =
    for {
      hasRepos <- theServiceRegistry.getServiceByType(classOf[HasRepositories]).flatMap(_.castTo[HasRepositories])
      repo <- hasRepos.getForAggregateRoot[TAR, TEvent]
    } yield new BoundUnitOfWork {
      val commandType = cmdType
      val repository = repo.actor
      val actionHandlers = theActionHandlers
      val theAlmhirt = anAlmhirt
    }

}