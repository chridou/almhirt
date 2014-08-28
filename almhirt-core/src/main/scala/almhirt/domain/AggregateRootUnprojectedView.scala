package almhirt.domain

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.almvalidation.kit._
import play.api.libs.iteratee.{ Enumerator, Iteratee }
import almhirt.common.AggregateRootEvent

final case class ApplyAggregateEvent(event: AggregateRootEvent)
case object GetAggregateRootProjection

/** View an unprojected aggregate root that is updated vial the aggregate event log or aggregate events as they come in */ 
abstract class AggregateRootUnprojectedView[T <: AggregateRoot, E <: AggregateRootEvent](
  override val aggregateRootId: AggregateRootId,
  override val aggregateEventLog: ActorRef,
  override val snapshotStorage: Option[ActorRef],
  override val onDispatchSuccess: (AggregateRootLifecycle[T], ActorRef) ⇒ Unit,
  override val onDispatchFailure: (Problem, ActorRef) ⇒ Unit)(implicit override val futuresContext: ExecutionContext, override val eventTag: ClassTag[E]) extends Actor with ActorLogging with AggregateRootUnprojectedViewSkeleton[T, E] { me: AggregateRootEventHandler[T, E] ⇒

  override def receive: Receive = me.receiveUninitialized
}

private[almhirt] trait AggregateRootUnprojectedViewSkeleton[T <: AggregateRoot, E <: AggregateRootEvent] { me: Actor with ActorLogging with AggregateRootEventHandler[T, E] ⇒
  import almhirt.eventlog.AggregateEventLog._

  def futuresContext: ExecutionContext
  def aggregateEventLog: ActorRef
  def snapshotStorage: Option[ActorRef]
  def aggregateRootId: AggregateRootId
  implicit def eventTag: ClassTag[E]

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

  protected def receiveUninitialized: Receive = {
    case GetAggregateRootProjection ⇒
      logDebug(s"[receiveUninitialized]: Request for aggregate root. Intializing from eventlog.")
      snapshotStorage match {
        case None ⇒
          updateFromEventlog(Vacat, Vector(sender()))
        case Some(snaphots) ⇒
          ???
      }
    case ApplyAggregateEvent(event) ⇒
      logDebug(s"[receiveUninitialized]: Intializing from eventlog. Dropping event $event")
      snapshotStorage match {
        case None ⇒
          updateFromEventlog(Vacat, Vector.empty)
        case Some(snaphots) ⇒
          ???
      }
  }

  private def receiveRebuildFromEventlog(currentState: AggregateRootLifecycle[T], enqueuedRequests: Vector[ActorRef], enqueuedEvents: Vector[E]): Receive = {
    case FetchedAggregateEvents(eventsEnumerator) ⇒
      val iteratee: Iteratee[AggregateRootEvent, AggregateRootLifecycle[T]] = Iteratee.fold[AggregateRootEvent, AggregateRootLifecycle[T]](currentState) {
        case (acc, event) ⇒
          applyEventLifecycleAgnostic(acc, event.specific[E])
      }(futuresContext)

      eventsEnumerator.run(iteratee).onComplete {
        case scala.util.Success(arState) ⇒
          self ! InternalEventlogArBuildResult(arState)
        case scala.util.Failure(error) ⇒
          self ! InternalEventlogBuildArFailed(error)
      }(futuresContext)

      context.become(receiveEvaluateEventlogRebuildResult(enqueuedRequests, enqueuedEvents))

    case GetAggregateEventsFailed(problem) ⇒
      onError(enqueuedRequests)(AggregateEventStoreFailedReadingException(aggregateRootId, "An error has occured fetching the aggregate root events:\n$problem"))

    case ApplyAggregateEvent(event) ⇒
      context.become(receiveRebuildFromEventlog(currentState, enqueuedRequests, enqueuedEvents :+ event.specificWithHandler[E](onError(enqueuedRequests))))

    case GetAggregateRootProjection ⇒
      context.become(receiveRebuildFromEventlog(currentState, enqueuedRequests :+ sender(), enqueuedEvents))
  }

  private def receiveRebuildFromSnapshot(enqueuedRequests: Vector[ActorRef]): Receive = {
    case _ ⇒ ()
  }

  private def receiveEvaluateEventlogRebuildResult(enqueuedRequests: Vector[ActorRef], enqueuedEvents: Vector[E]): Receive = {
    case InternalEventlogArBuildResult(arState) ⇒
      val toApply = enqueuedEvents.filter(_.aggVersion >= arState.version)
      if (toApply.isEmpty) {
        dispatchState(arState, enqueuedRequests: _*)
        context.become(receiveServe(arState))
      } else {
        if (toApply.head.aggVersion == arState.version) {
          logDebug(s"[receiveEvaluateEventlogRebuildResult]: Applying ${toApply.size} enqueued events.")
          val newState = toApply.foldLeft(arState) { case (state, nextEvent) ⇒ applyEventLifecycleAgnostic(state, nextEvent) }
          dispatchState(newState, enqueuedRequests: _*)
          context.become(receiveServe(newState))
        } else {
          logDebug(s"[receiveEvaluateEventlogRebuildResult]: Version gap. Updating from eventlog.")
          updateFromEventlog(arState, enqueuedRequests)
        }
      }

    case InternalEventlogBuildArFailed(error: Throwable) ⇒
      onError(enqueuedRequests)(RebuildAggregateRootFailedException(aggregateRootId, "An error has occured rebuilding the aggregate root.", error))

    case ApplyAggregateEvent(event) ⇒
      context.become(receiveEvaluateEventlogRebuildResult(enqueuedRequests, enqueuedEvents :+ event.specificWithHandler[E](onError(enqueuedRequests))))

    case GetAggregateRootProjection ⇒
      context.become(receiveEvaluateEventlogRebuildResult(enqueuedRequests :+ sender(), enqueuedEvents))
  }

  private def receiveServe(currentState: AggregateRootLifecycle[T]): Receive = {
    case GetAggregateRootProjection ⇒
      dispatchState(currentState, sender)

    case ApplyAggregateEvent(event) ⇒
      currentState match {
        case s: Antemortem[T] ⇒
          if (event.aggVersion == s.version) {
            logDebug(s"[receiveServe]: Applying event $event")
            context.become(receiveServe(applyEventAntemortem(s, event.specificWithHandler[E](onError(Vector.empty)))))
          } else {
            logDebug(s"[receiveServe]: Version mismatch. $event causes updating from eventlog.")
            updateFromEventlog(currentState, Vector.empty)
          }
        case Mortuus(id, v) ⇒
          onError(Vector.empty)(RebuildAggregateRootFailedException(aggregateRootId, "An error has occured building the aggregate root. Nothing can be built from a dead aggregate root."))
      }
  }

  /** Ends with termination */
  private def onError(enqueuedRequests: Vector[ActorRef])(ex: AggregateRootDomainException): Nothing = {
    log.error(s"Escalating! Something terrible happened:\n$ex")
    val problem = UnspecifiedProblem(s"""Escalating! Something terrible happened: "${ex.getMessage}"""", cause = Some(ex))
    enqueuedRequests.foreach(receiver ⇒ onDispatchFailure(problem, receiver))
    throw ex
  }

  final def logDebug(msg: => String) {
    if (log.isDebugEnabled)
      log.debug(msg)
  }

  private def updateFromEventlog(state: AggregateRootLifecycle[T], enqueuedRequests: Vector[ActorRef]) {
    state match {
      case Vacat ⇒
        aggregateEventLog ! GetAllAggregateEventsFor(aggregateRootId)
      case Vivus(ar) ⇒
        aggregateEventLog ! GetAggregateEventsFrom(aggregateRootId, ar.version)
      case Mortuus(id, v) ⇒
        onError(enqueuedRequests)(RebuildAggregateRootFailedException(aggregateRootId, "An error has occured building the aggregate root. Nothing can be built from a dead aggregate root."))
    }
    context.become(receiveRebuildFromEventlog(state, enqueuedRequests, Vector.empty))
  }

  private def dispatchState(currentState: AggregateRootLifecycle[T], to: ActorRef*) {
    to.foreach(receiver ⇒ onDispatchSuccess(currentState, receiver))
  }
}