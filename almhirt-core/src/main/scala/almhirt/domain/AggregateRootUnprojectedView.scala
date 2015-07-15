package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.almvalidation.kit._
import almhirt.context.AlmhirtContext
import play.api.libs.iteratee.{ Enumerator, Iteratee }
import almhirt.snapshots.SnapshotRepository

object AggregateRootViewMessages {
  final case class ApplyAggregateRootEvent(event: AggregateRootEvent)
  case object AggregateRootEventHandled
  case object GetAggregateRootProjection
  final case class GetAggregateRootProjectionFor(id: AggregateRootId)

}

object AggregateRootUnprojectedView {
  def makeViewConstructorFactory(makeViewConstructor: (Option[FiniteDuration], Option[FiniteDuration], Option[FiniteDuration]) ⇒ AlmValidation[AggregateRootViews.ViewConstructor]): com.typesafe.config.Config ⇒ AlmValidation[AggregateRootViews.ViewConstructor] = {
    import almhirt.configuration._
    (config: com.typesafe.config.Config) ⇒
      for {
        returnToUnitializedAfter ← config.magicOption[FiniteDuration]("return-to-unitialized-after")
        rebuildTimeout ← config.magicOption[FiniteDuration]("rebuild-timeout")
        rebuildRetryDelay ← config.magicOption[FiniteDuration]("rebuild-retry-delay")
        ctor ← makeViewConstructor(returnToUnitializedAfter, rebuildTimeout, rebuildRetryDelay)
      } yield ctor
  }
}

private[almhirt] object AggregateRootViewInternal {
  case object ReturnToUninitialized
}

/** View an unprojected aggregate root that is updated vial the aggregate event log or aggregate events as they come in */
abstract class AggregateRootUnprojectedView[T <: AggregateRoot, E <: AggregateRootEvent](
    override val aggregateRootId: AggregateRootId,
    override val aggregateEventLog: ActorRef,
    override val snapshotStorage: Option[ActorRef],
    override val onDispatchSuccess: (AggregateRootLifecycle[T], ActorRef) ⇒ Unit,
    override val onDispatchFailure: (Problem, ActorRef) ⇒ Unit,
    override val returnToUnitializedAfter: Option[FiniteDuration],
    override val rebuildTimeout: Option[FiniteDuration],
    override val rebuildRetryDelay: Option[FiniteDuration])(implicit override val futuresContext: ExecutionContext, override val eventTag: ClassTag[E], override val arTag: ClassTag[T]) extends Actor with AggregateRootUnprojectedViewSkeleton[T, E] { me: AggregateRootEventHandler[T, E] ⇒

  override def receive: Receive = me.receiveUninitialized

  override def confirmAggregateRootEventHandled() {
    context.parent ! AggregateRootViewMessages.AggregateRootEventHandled
  }
}

private[almhirt] trait AggregateRootUnprojectedViewSkeleton[T <: AggregateRoot, E <: AggregateRootEvent] { me: Actor with AggregateRootEventHandler[T, E] ⇒
  import almhirt.eventlog.AggregateRootEventLog._
  import AggregateRootViewMessages._

  def futuresContext: ExecutionContext
  def aggregateEventLog: ActorRef
  def snapshotStorage: Option[ActorRef]
  def aggregateRootId: AggregateRootId
  def returnToUnitializedAfter: Option[FiniteDuration]
  def rebuildTimeout: Option[FiniteDuration]
  def rebuildRetryDelay: Option[FiniteDuration]
  def rebuildWarnDuration: Option[FiniteDuration] = Some(0.1.seconds)
  implicit def eventTag: ClassTag[E]
  implicit def arTag: ClassTag[T]

  def confirmAggregateRootEventHandled()

  /**
   * Implement this to communicate the current state to the outside world.
   *  Each request to this Actor will be responded with either a success or a failure.
   *  In this case it is a success.
   */
  def onDispatchSuccess: (AggregateRootLifecycle[T], ActorRef) ⇒ Unit
  /**
   * Implement this to communicate a failure to the outside world.
   *  Each request to this Actor will be responded with either a success or a failure.
   *  In this case it is a failure.
   */
  def onDispatchFailure: (Problem, ActorRef) ⇒ Unit

  private case class InternalEventlogArBuildResult(ar: AggregateRootLifecycle[T])
  private case class InternalEventlogBuildArFailed(error: Throwable)

  private case class RebuildTimesOutNow(after: FiniteDuration)

  private var rebuildStartedOn: Deadline = null

  protected def receiveUninitialized: Receive = {
    case GetAggregateRootProjection ⇒
      rebuildStartedOn = Deadline.now
      snapshotStorage match {
        case None ⇒
          updateFromEventlog(Vacat, Vector(sender()), delay = None)
        case Some(snaphots) ⇒
          context.become(receiveRebuildFromSnapshot(Vector(sender())))
          snaphots ! SnapshotRepository.FindSnapshot(aggregateRootId)
      }
    case ApplyAggregateRootEvent(event) ⇒
      rebuildStartedOn = Deadline.now
      confirmAggregateRootEventHandled()
      snapshotStorage match {
        case None ⇒
          updateFromEventlog(Vacat, Vector.empty, delay = None)
        case Some(snaphots) ⇒
          context.become(receiveRebuildFromSnapshot(Vector.empty))
          snaphots ! SnapshotRepository.FindSnapshot(aggregateRootId)
      }
  }

  private def receiveRebuildFromEventlog(currentState: AggregateRootLifecycle[T], enqueuedRequests: Vector[ActorRef], enqueuedEvents: Vector[E]): Receive = {
    case FetchedAggregateRootEvents(eventsEnumerator) ⇒
      val iteratee: Iteratee[AggregateRootEvent, AggregateRootLifecycle[T]] = Iteratee.fold[AggregateRootEvent, AggregateRootLifecycle[T]](currentState) {
        case (acc, event) ⇒
          applyEventLifecycleAgnostic(acc, event.specificUnsafe[E])
      }(futuresContext)

      eventsEnumerator.run(iteratee).onComplete {
        case scala.util.Success(arState) ⇒
          self ! InternalEventlogArBuildResult(arState)
        case scala.util.Failure(error) ⇒
          self ! InternalEventlogBuildArFailed(error)
      }(futuresContext)

      context.become(receiveEvaluateEventlogRebuildResult(enqueuedRequests, enqueuedEvents))

    case GetAggregateRootEventsFailed(problem) ⇒
      problem match {
        case ServiceNotReadyProblem(p) ⇒
          val newProb = ServiceNotReadyProblem("Not yet ready to answer questions..", cause = Some(p))
          context.parent ! AggregateRootViewsInternals.ReportViewError("An error occured rebuilding the aggregate root.", newProb)
          enqueuedRequests.foreach(receiver ⇒ onDispatchFailure(newProb, receiver))
          updateFromEventlog(currentState, Vector.empty, rebuildRetryDelay)
        case _ ⇒
          onError(enqueuedRequests, enqueuedEvents.size)(AggregateRootEventStoreFailedReadingException(aggregateRootId, s"An error has occured fetching the aggregate root events:\n$problem"))
      }

    case ApplyAggregateRootEvent(event) ⇒
      context.become(receiveRebuildFromEventlog(currentState, enqueuedRequests, enqueuedEvents :+ event.specificUnsafeWithHandler[E](onError(enqueuedRequests, enqueuedEvents.size + 1))))

    case GetAggregateRootProjection ⇒
      context.become(receiveRebuildFromEventlog(currentState, enqueuedRequests :+ sender(), enqueuedEvents))

    case RebuildTimesOutNow(after) ⇒
      onError(enqueuedRequests, enqueuedEvents.size)(AggregateRootEventStoreFailedReadingException(aggregateRootId, s"Rebuilding the aggregate root(${aggregateRootId.value}) timed out after ${after.defaultUnitString}."))

  }

  private def receiveRebuildFromSnapshot(enqueuedRequests: Vector[ActorRef]): Receive = {
    case SnapshotRepository.FoundSnapshot(untypedAr) ⇒
      untypedAr.castTo[T].fold(
        fail ⇒ {
          context.parent ! AggregateRootViewsInternals.ReportViewWarning(s"Failed to cast snapshot for ${untypedAr.id.value}. Rebuild all from log.", fail)
          updateFromEventlog(Vacat, enqueuedRequests, delay = None)
        },
        ar ⇒ {
          updateFromEventlog(Vivus(ar), enqueuedRequests, delay = None)
        })

    case SnapshotRepository.SnapshotNotFound(id) ⇒
      updateFromEventlog(Vacat, enqueuedRequests, delay = None)

    case SnapshotRepository.AggregateRootWasDeleted(id, version) ⇒
      context.parent ! AggregateRootViewsInternals.ReportViewDebug(s"Snapshot for ${id.value} with version ${version.value} was marked as deleted.")
      enqueuedRequests.foreach(receiver ⇒ onDispatchFailure(AggregateRootDeletedProblem(id), receiver))
      context.become(receiveServe(Mortuus(id, version)))

    case SnapshotRepository.FindSnapshotFailed(id, prob) ⇒
      context.parent ! AggregateRootViewsInternals.ReportViewWarning(s"Failed to load a snapshot for ${id.value}. Rebuild all from log.", prob)
      updateFromEventlog(Vacat, enqueuedRequests, delay = None)

    case ApplyAggregateRootEvent(event) ⇒
      // Do nothing. The event must exist in the event log.
      ()

    case GetAggregateRootProjection ⇒
      context.become(receiveRebuildFromSnapshot(enqueuedRequests :+ sender()))

  }

  private def receiveEvaluateEventlogRebuildResult(enqueuedRequests: Vector[ActorRef], enqueuedEvents: Vector[E]): Receive = {
    case InternalEventlogArBuildResult(arState) ⇒
      rebuildWarnDuration.foreach { rbdur ⇒
        rebuildStartedOn.whenTooLate(rbdur, dur ⇒ context.parent ! AggregateRootViewsInternals.ReportViewDebug(s"""Rebuild took more than ${rbdur.defaultUnitString}: ${dur.defaultUnitString}"""))
      }
      enqueuedEvents.foreach(_ ⇒ confirmAggregateRootEventHandled())
      val toApply = enqueuedEvents.filter(_.aggVersion >= arState.version)
      if (toApply.isEmpty) {
        dispatchState(arState, enqueuedRequests: _*)
        becomeReceiveServe(arState)
      } else {
        if (toApply.head.aggVersion == arState.version) {
          val newState = toApply.foldLeft(arState) { case (state, nextEvent) ⇒ applyEventLifecycleAgnostic(state, nextEvent) }
          dispatchState(newState, enqueuedRequests: _*)
          becomeReceiveServe(newState)
        } else {
          updateFromEventlog(arState, enqueuedRequests, delay = None)
        }
      }

    case InternalEventlogBuildArFailed(error: Throwable) ⇒
      onError(enqueuedRequests, enqueuedEvents.size)(RebuildAggregateRootFailedException(aggregateRootId, "An error has occured rebuilding the aggregate root.", error))

    case ApplyAggregateRootEvent(event) ⇒
      context.become(receiveEvaluateEventlogRebuildResult(enqueuedRequests, enqueuedEvents :+ event.specificUnsafeWithHandler[E](onError(enqueuedRequests, enqueuedEvents.size + 1))))

    case GetAggregateRootProjection ⇒
      context.become(receiveEvaluateEventlogRebuildResult(enqueuedRequests :+ sender(), enqueuedEvents))

    case RebuildTimesOutNow(after) ⇒
      onError(enqueuedRequests, enqueuedEvents.size)(AggregateRootEventStoreFailedReadingException(aggregateRootId, s"Rebuilding the aggregate root(${aggregateRootId.value}) timed out after ${after.defaultUnitString}."))
  }

  private def receiveServe(currentState: AggregateRootLifecycle[T]): Receive = {
    case GetAggregateRootProjection ⇒
      dispatchState(currentState, sender)

    case ApplyAggregateRootEvent(event) ⇒
      currentState match {
        case s: Antemortem[T] ⇒
          if (event.aggVersion == s.version) {
            confirmAggregateRootEventHandled()
            becomeReceiveServe(applyEventAntemortem(s, event.specificUnsafeWithHandler[E](onError(Vector.empty, 0))))
          } else {
            confirmAggregateRootEventHandled()
            updateFromEventlog(currentState, Vector.empty, delay = None)
          }
        case Mortuus(id, v) ⇒
          onError(Vector.empty, 1)(RebuildAggregateRootFailedException(aggregateRootId, "An error has occured building the aggregate root. Nothing can be built from a dead aggregate root."))
      }

    case AggregateRootViewInternal.ReturnToUninitialized ⇒
      context.parent ! AggregateRootHiveInternals.CargoJettisoned(aggregateRootId)
      context.become(receiveUninitialized)

  }

  private def becomeReceiveServe(currentState: AggregateRootLifecycle[T]) {
    returnToUnitializedAfter.foreach(dur ⇒
      context.system.scheduler.scheduleOnce(dur, self, AggregateRootViewInternal.ReturnToUninitialized)(context.dispatcher))
    context.become(receiveServe(currentState))
  }

  /** Ends with termination */
  private def onError(enqueuedRequests: Vector[ActorRef], eventsStillToConfirm: Int)(ex: AggregateRootDomainException): Nothing = {
    (1 to eventsStillToConfirm).foreach(_ ⇒ confirmAggregateRootEventHandled())
    val problem = UnspecifiedProblem(s"""Escalating! Something terrible happened: "${ex.getMessage}"""", cause = Some(ex))
    enqueuedRequests.foreach(receiver ⇒ onDispatchFailure(problem, receiver))
    context.parent ! AggregateRootViewsInternals.ReportViewError("An error occured", ex)
    throw ex
  }

  private def updateFromEventlog(state: AggregateRootLifecycle[T], enqueuedRequests: Vector[ActorRef], delay: Option[FiniteDuration]) {
    state match {
      case Vacat ⇒
        val action = () ⇒ { aggregateEventLog ! GetAggregateRootEventsFor(aggregateRootId, FromStart, ToEnd, skip.none takeAll) }
        delay match {
          case Some(d) ⇒
            context.system.scheduler.scheduleOnce(d)(action())(context.dispatcher)
          case None ⇒
            action()
        }
      case Vivus(ar) ⇒
        val action = () ⇒ { aggregateEventLog ! GetAggregateRootEventsFor(aggregateRootId, FromVersion(ar.version), ToEnd, skip.none takeAll) }
        delay match {
          case Some(d) ⇒
            context.system.scheduler.scheduleOnce(d)(action())(context.dispatcher)
          case None ⇒
            action()
        }
      case Mortuus(id, v) ⇒
        onError(enqueuedRequests, 0)(RebuildAggregateRootFailedException(aggregateRootId, "An error has occured building the aggregate root. Nothing can be built from a dead aggregate root."))
    }
    rebuildTimeout.foreach(timeout ⇒ context.system.scheduler.scheduleOnce(timeout, self, RebuildTimesOutNow(timeout))(futuresContext))
    context.become(receiveRebuildFromEventlog(state, enqueuedRequests, Vector.empty))
  }

  private def dispatchState(currentState: AggregateRootLifecycle[T], to: ActorRef*) {
    to.foreach(receiver ⇒ onDispatchSuccess(currentState, receiver))
  }
}