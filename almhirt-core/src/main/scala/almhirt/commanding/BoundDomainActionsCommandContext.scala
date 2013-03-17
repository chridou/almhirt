package almhirt.commanding

import java.util.{ UUID => JUUID }
import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
//import scalaz._, Scalaz._
import scalaz.syntax.validation._
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

  sealed trait BoundCommandAction
  trait BoundCreatorAction extends BoundCommandAction
  trait BoundMutatorAction extends BoundCommandAction

  type CreatingActionHandler[TAct <: BoundCreatorAction] = (TAct, Almhirt) => UpdateRecorder[TAR, TEvent]
  type MutatingActionHandler[TAct <: BoundMutatorAction] = (TAct, TAR, Almhirt) => UpdateRecorder[TAR, TEvent]

  def actionHandlers: HasActionHandlers

  trait BoundDomainActionsCommand extends DomainCommand {
    def aggRef: Option[AggregateRootRef]
    def actions: List[BoundCommandAction]
  }

  def flattenCreatingActionHandler[TAct <: BoundCreatorAction](handler: CreatingActionHandler[TAct])(implicit tag: ClassTag[TAct]): CreatingActionHandler[BoundCreatorAction] = {
    (action, theAlmhirt) =>
      action.castTo[TAct].fold(
        fail => UpdateRecorder.reject(fail),
        succ => handler(succ, theAlmhirt))
  }

  def flattenMutatingActionHandler[TAct <: BoundMutatorAction](handler: MutatingActionHandler[TAct])(implicit tag: ClassTag[TAct]): MutatingActionHandler[BoundMutatorAction] = {
    (action, ar, theAlmhirt) =>
      action.castTo[TAct].fold(
        fail => UpdateRecorder.reject(fail),
        succ => handler(succ, ar, theAlmhirt))
  }

  trait HasActionHandlers {
    def getCreatingHandler(action: BoundCreatorAction): AlmValidation[CreatingActionHandler[BoundCreatorAction]]
    def getMutatingHandler(action: BoundMutatorAction): AlmValidation[MutatingActionHandler[BoundMutatorAction]]
  }

  def createHasActionHandlers(creators: List[(Class[_], CreatingActionHandler[BoundCreatorAction])], mutators: List[(Class[_], MutatingActionHandler[BoundMutatorAction])]): HasActionHandlers = {
    val creatorsByClass = creators.toMap
    val mutatorsByClass = mutators.toMap

    new HasActionHandlers {
      override def getCreatingHandler(action: BoundCreatorAction): AlmValidation[CreatingActionHandler[BoundCreatorAction]] =
        option.cata(creatorsByClass.get(action.getClass()))(
          handler => handler.success,
          NoSuchElementProblem(s"No creating handler found for action ${action.getClass().getName()}").failure)

      override def getMutatingHandler(action: BoundMutatorAction): AlmValidation[MutatingActionHandler[BoundMutatorAction]] =
        option.cata(mutatorsByClass.get(action.getClass()))(
          handler => handler.success,
          NoSuchElementProblem(s"No mutating handler found for action ${action.getClass().getName()}").failure)
    }
  }

  def prevalidate(com: BoundDomainActionsCommand): AlmValidation[BoundDomainActionsCommand] =
    boolean.fold(com.actions.isEmpty,
      com.success,
      option.cata(com.aggRef)(
        aggRef =>
          boolean.fold(com.actions.forall(_.isInstanceOf[BoundMutatorAction]),
            com.success,
            IllegalOperationProblem("When an AggregateRootRef is supplied, all actions must be mutating actions!").failure),
        {
          val h :: tail = com.actions
          boolean.fold(h.isInstanceOf[BoundCreatorAction],
            boolean.fold(tail.forall(_.isInstanceOf[BoundMutatorAction]),
              com.success,
              IllegalOperationProblem("When the first action is a create action, all others must be mutating actions!").failure),
            IllegalOperationProblem("When no AggregateRootRef is supplied, the first action must be a creating action!").failure)
        }))

  def validateAgainstAggregateRoot(ar: TAR, aggRef: AggregateRootRef): AlmValidation[Unit] =
    (aggRef.id == ar.id, aggRef.version == ar.version) match {
      case (true, true) => ().success
      case (true, false) => IllegalOperationProblem(s"Versions of command(${aggRef.version}) and AR(${ar.version}) do not match!").failure
      case (false, true) => IllegalOperationProblem(s"Ids of command(${aggRef.id}) and AR(${ar.id}) do not match!").failure
      case (false, false) => IllegalOperationProblem(s"Neither versions nor ids do match on AR and Command(${aggRef.id} <> ${ar.id} & ${aggRef.version} <> ${ar.version}) !").failure
    }

  trait BoundUnitOfWork extends HandlesCommand {
    implicit def theAlmhirt: Almhirt
    protected def getAR(id: JUUID): AlmFuture[TAR]
    protected def storeAR(ar: TAR, uncommittedEvents: IndexedSeq[TEvent], ticket: Option[TrackingTicket]): Unit
    def actionHandlers: HasActionHandlers
    def handle(com: DomainCommand, ticket: Option[TrackingTicket]) {
      com match {
        case bcmd: BoundDomainActionsCommand =>
          handleBoundActionsCommand(bcmd, ticket)(theAlmhirt)
        case wrongType =>
          reportFailure(ArgumentProblem("Not a BoundDomainActionsCommand: %s".format(wrongType.getClass.getName), severity = Major), ticket)
      }
    }

    protected def handleBoundActionsCommand(com: BoundDomainActionsCommand, ticket: Option[TrackingTicket])(implicit theAlmhirt: Almhirt) {
      com.actions match {
        case Nil => ticket.foreach(t => theAlmhirt.publishOperationState(Executed(t, PerformedUnspecifiedAction), Map.empty))
        case _ => prevalidate(com).fold(
          prob => reportFailure(prob, ticket),
          succ => executeUnitOfWork(com).onComplete(
            fail => reportFailure(fail, ticket),
            recorder => recorder.result.fold(
              fail => reportFailure(fail, ticket),
              succ => storeAR(succ._1, succ._2, ticket))))
      }
    }

    protected def executeUnitOfWork(com: BoundDomainActionsCommand): AlmFuture[UpdateRecorder[TAR, TEvent]] = {
      for {
        initialAndRest <- option.cata(com.aggRef)(
          aggRef => startFromRepo(aggRef).map(recorder => (recorder, com.actions.asInstanceOf[List[BoundMutatorAction]])),
          {
            val creatorAction :: mutate = com.actions
            startWithNew(creatorAction.asInstanceOf[BoundCreatorAction]).map(recorder => (recorder, mutate.asInstanceOf[List[BoundMutatorAction]]))
          })
        resultOfMutators <- executeMutatorsOn(initialAndRest._1, initialAndRest._2)
      } yield resultOfMutators
    }

    protected def startWithNew(creatorAction: BoundCreatorAction): AlmFuture[UpdateRecorder[TAR, TEvent]] = {
      AlmFuture.promise(actionHandlers.getCreatingHandler(creatorAction).map(handler => handler(creatorAction, theAlmhirt)))
    }

    protected def startFromRepo(aggRef: AggregateRootRef): AlmFuture[UpdateRecorder[TAR, TEvent]] = {
      for {
        ar <- getAR(aggRef.id)
        recorder <- AlmFuture.promise {
          validateAgainstAggregateRoot(ar, aggRef).map(_ => UpdateRecorder.startWith[TAR, TEvent](ar))
        }
      } yield recorder
    }

    protected def executeMutatorsOn(initial: UpdateRecorder[TAR, TEvent], actions: List[BoundMutatorAction]): AlmFuture[UpdateRecorder[TAR, TEvent]] = {
      import akka.pattern._
      import scalaz.syntax.traverse._
      import scalaz.std.list.listInstance
      AlmFuture {
        val actionsAndHandlersV = actions.map(action => actionHandlers.getMutatingHandler(action).toAgg.map(h => (action, h)))
        val actionsAndHandlers = actionsAndHandlersV.sequence[AlmValidationAP, (BoundMutatorAction, (BoundMutatorAction, TAR, Almhirt) => UpdateRecorder[TAR, TEvent])]
        actionsAndHandlers.fold(
          fail => fail.failure,
          succ => {
            val result = succ.foldLeft(initial) {
              case (acc, (action, handler)) =>
                acc.flatMap(curAr =>
                  handler(action, curAr, theAlmhirt))
            }
            result.success
          })
      }
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

  import akka.pattern._

  def createBasicUow(cmdType: Class[_ <: DomainCommand], getsAnAR: JUUID => AlmFuture[TAR], storesAnAr: (TAR, IndexedSeq[TEvent], Option[TrackingTicket]) => Unit, theActionHandlers: Option[HasActionHandlers])(implicit anAlmhirt: Almhirt): this.BoundUnitOfWork =
    new BoundUnitOfWork {
      val commandType = cmdType
      protected override def getAR(id: JUUID) = getsAnAR(id)
      protected override def storeAR(ar: TAR, uncommittedEvents: IndexedSeq[TEvent], ticket: Option[TrackingTicket]) = storesAnAr(ar, uncommittedEvents, ticket)
      val actionHandlers = theActionHandlers.getOrElse(BoundDomainActionsCommandContext.this.actionHandlers)
      val theAlmhirt = anAlmhirt
    }

  def createBasicUowWithRepoActor(cmdType: Class[_ <: DomainCommand], repoActor: ActorRef, theActionHandlers: Option[HasActionHandlers], maxGetDuration: Option[FiniteDuration] = None)(implicit anAlmhirt: Almhirt): this.BoundUnitOfWork = {
    val duration = option.cata(maxGetDuration)(dur => dur, anAlmhirt.defaultDuration)
    def get(id: JUUID) = 
      (repoActor ? GetAggregateRootQry(id))(duration).mapToAlmFutureOver((rsp: GetAggregateRootRsp) => rsp.ar.flatMap(_.castTo[TAR]))
    def store(ar: TAR, uncommittedEvents: IndexedSeq[TEvent], ticket: Option[TrackingTicket]) = repoActor ! StoreAggregateRootCmd(ar, uncommittedEvents, ticket)
    createBasicUow(cmdType, get _, store _, theActionHandlers)
  }

  def createBasicUowWithRepo(cmdType: Class[_ <: DomainCommand], theRepository: AggregateRootRepository[TAR, TEvent], theActionHandlers: Option[HasActionHandlers], maxGetDuration: Option[FiniteDuration] = None)(implicit anAlmhirt: Almhirt): this.BoundUnitOfWork =
    createBasicUowWithRepoActor(cmdType, theRepository.actor, theActionHandlers, maxGetDuration)

  def createBasicUowFromServices(cmdType: Class[_ <: DomainCommand], theServiceRegistry: ServiceRegistry, theActionHandlers: Option[HasActionHandlers], maxGetDuration: Option[FiniteDuration] = None)(implicit anAlmhirt: Almhirt): AlmValidation[this.BoundUnitOfWork] =
    for {
      hasRepos <- theServiceRegistry.getServiceByType(classOf[HasRepositories]).flatMap(_.castTo[HasRepositories])
      repo <- hasRepos.getForAggregateRoot[TAR, TEvent]
    } yield createBasicUowWithRepo(cmdType, repo, theActionHandlers, maxGetDuration)

  trait BoundDomainCommandFactory[TCom <: BoundDomainActionsCommand] {
    def apply(aggRef: Option[AggregateRootRef], actions: List[BoundCommandAction])(implicit resources: CanCreateUuid): TCom

    def creator(creator: BoundCreatorAction)(implicit resources: CanCreateUuid): TCom =
      apply(None, List(creator))

    def creatorAndMutator(creator: BoundCreatorAction, mutator: BoundMutatorAction)(implicit resources: CanCreateUuid): TCom =
      apply(None, creator :: mutator :: Nil)

    def creatorAndMutators(creator: BoundCreatorAction, mutators: List[BoundMutatorAction])(implicit resources: CanCreateUuid): TCom =
      apply(None, creator :: mutators)

    def mutator(aggRef: AggregateRootRef, mutator: BoundMutatorAction)(implicit resources: CanCreateUuid): TCom =
      apply(Some(aggRef), List(mutator))

    def mutators(aggRef: AggregateRootRef, mutators: List[BoundMutatorAction])(implicit resources: CanCreateUuid): TCom =
      apply(Some(aggRef), mutators)

  }

}