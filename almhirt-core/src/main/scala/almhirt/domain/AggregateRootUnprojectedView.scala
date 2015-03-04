package almhirt.domain

import scala.language.postfixOps
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.almvalidation.kit._
import almhirt.context.AlmhirtContext
import play.api.libs.iteratee.{ Enumerator, Iteratee }

object AggregateRootViewMessages {
  final case class ApplyAggregateRootEvent(event: AggregateRootEvent)
  case object AggregateRootEventHandled
  case object GetAggregateRootProjection
  final case class GetAggregateRootProjectionFor(id: AggregateRootId)

}

object AggregateRootUnprojectedView {
  def propsRawMaker(returnToUnitializedAfter: Option[FiniteDuration], rebuildTimeout: Option[FiniteDuration], maker: (Option[FiniteDuration], Option[FiniteDuration]) ⇒ Props): Props = {
    maker(returnToUnitializedAfter, rebuildTimeout)
  }

  def propsMaker(
    maker: (Option[FiniteDuration], Option[FiniteDuration]) ⇒ Props,
    viewConfigName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    val path = "almhirt.components.views.unprojected-view" + viewConfigName.map("." + _).getOrElse("")
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      returnToUnitializedAfter ← section.magicOption[FiniteDuration]("return-to-unitialized-after")
      rebuildTimeout ← section.magicOption[FiniteDuration]("rebuild-timeout")
    } yield propsRawMaker(returnToUnitializedAfter, rebuildTimeout, maker)
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
  override val rebuildTimeout: Option[FiniteDuration])(implicit override val futuresContext: ExecutionContext, override val eventTag: ClassTag[E]) extends Actor with AggregateRootUnprojectedViewSkeleton[T, E] { me: AggregateRootEventHandler[T, E] ⇒

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
  implicit def eventTag: ClassTag[E]

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

  protected def receiveUninitialized: Receive = {
    case GetAggregateRootProjection ⇒
      snapshotStorage match {
        case None ⇒
          updateFromEventlog(Vacat, Vector(sender()))
        case Some(snaphots) ⇒
          ???
      }
    case ApplyAggregateRootEvent(event) ⇒
      confirmAggregateRootEventHandled()
      snapshotStorage match {
        case None ⇒
          updateFromEventlog(Vacat, Vector.empty)
        case Some(snaphots) ⇒
          ???
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
      onError(enqueuedRequests, enqueuedEvents.size)(AggregateRootEventStoreFailedReadingException(aggregateRootId, s"An error has occured fetching the aggregate root events:\n$problem"))

    case ApplyAggregateRootEvent(event) ⇒
      context.become(receiveRebuildFromEventlog(currentState, enqueuedRequests, enqueuedEvents :+ event.specificUnsafeWithHandler[E](onError(enqueuedRequests, enqueuedEvents.size + 1))))

    case GetAggregateRootProjection ⇒
      context.become(receiveRebuildFromEventlog(currentState, enqueuedRequests :+ sender(), enqueuedEvents))

    case RebuildTimesOutNow(after) =>
      onError(enqueuedRequests, enqueuedEvents.size)(AggregateRootEventStoreFailedReadingException(aggregateRootId, s"Rebuilding the aggregate root(${aggregateRootId.value}) timed out after ${after.defaultUnitString}."))

  }

  private def receiveRebuildFromSnapshot(enqueuedRequests: Vector[ActorRef]): Receive = {
    case _ ⇒ ()
  }

  private def receiveEvaluateEventlogRebuildResult(enqueuedRequests: Vector[ActorRef], enqueuedEvents: Vector[E]): Receive = {
    case InternalEventlogArBuildResult(arState) ⇒
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
          updateFromEventlog(arState, enqueuedRequests)
        }
      }

    case InternalEventlogBuildArFailed(error: Throwable) ⇒
      onError(enqueuedRequests, enqueuedEvents.size)(RebuildAggregateRootFailedException(aggregateRootId, "An error has occured rebuilding the aggregate root.", error))

    case ApplyAggregateRootEvent(event) ⇒
      context.become(receiveEvaluateEventlogRebuildResult(enqueuedRequests, enqueuedEvents :+ event.specificUnsafeWithHandler[E](onError(enqueuedRequests, enqueuedEvents.size + 1))))

    case GetAggregateRootProjection ⇒
      context.become(receiveEvaluateEventlogRebuildResult(enqueuedRequests :+ sender(), enqueuedEvents))

    case RebuildTimesOutNow(after) =>
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
            updateFromEventlog(currentState, Vector.empty)
          }
        case Mortuus(id, v) ⇒
          onError(Vector.empty, 1)(RebuildAggregateRootFailedException(aggregateRootId, "An error has occured building the aggregate root. Nothing can be built from a dead aggregate root."))
      }

    case AggregateRootViewInternal.ReturnToUninitialized ⇒
      context.parent !  AggregateRootHiveInternals.CargoJettisoned(aggregateRootId)
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

  private def updateFromEventlog(state: AggregateRootLifecycle[T], enqueuedRequests: Vector[ActorRef]) {
    state match {
      case Vacat ⇒
        aggregateEventLog ! GetAggregateRootEventsFor(aggregateRootId, FromStart, ToEnd, skip.none takeAll)
      case Vivus(ar) ⇒
        aggregateEventLog ! GetAggregateRootEventsFor(aggregateRootId, FromVersion(ar.version), ToEnd, skip.none takeAll)
      case Mortuus(id, v) ⇒
        onError(enqueuedRequests, 0)(RebuildAggregateRootFailedException(aggregateRootId, "An error has occured building the aggregate root. Nothing can be built from a dead aggregate root."))
    }
    rebuildTimeout.foreach(timeout => context.system.scheduler.scheduleOnce(timeout, self, RebuildTimesOutNow(timeout))(futuresContext))
    context.become(receiveRebuildFromEventlog(state, enqueuedRequests, Vector.empty))
  }

  private def dispatchState(currentState: AggregateRootLifecycle[T], to: ActorRef*) {
    to.foreach(receiver ⇒ onDispatchSuccess(currentState, receiver))
  }
}