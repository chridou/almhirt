package almhirt.akkax

import almhirt.common._
import akka.actor._
import almhirt.context.HasAlmhirtContext
import akka.event.LoggingAdapter

object FusedActor {
  def wrap(fusedActor: ActorRef): CircuitControl =
    ???
}

private[almhirt] object InternalFusedActorMessage {
  case object ReportState
  case object AttemptClose
  case object RemoveFuse
  case object DestroyFuse
}

trait SyncFusedActor { me: Actor with HasAlmhirtContext =>
  import java.util.concurrent.CopyOnWriteArrayList
  import AlmCircuitBreaker._

  def settings: CircuitControlSettings
  def callbackExecutorSelector: ExtendedExecutionContextSelector
  def loggingAdapter: Option[LoggingAdapter] = None

  private val CircuitControlSettings(maxFailures, failuresWarnThreshold, callTimeout, resetTimeout) = settings
  private val callbackExecutor = callbackExecutorSelector.select(this.almhirtContext, this.context)
  private var currentState: InternalState = null

  def fused[T](body: => AlmValidation[T]): AlmValidation[T]
  def fusedWithSurrogate[T](surrogate: ⇒ AlmValidation[T])(body: ⇒ AlmValidation[T]): AlmFuture[T]
  def state: CircuitState = currentState.publicState

  private def moveTo(newState: InternalState) {
    newState.enter()
  }
  

  private val internalReceive: Receive = {
    case InternalFusedActorMessage.ReportState =>
      sender() ! currentState.publicState

    case InternalFusedActorMessage.AttemptClose =>
      currentState.attemptManualClose

    case InternalFusedActorMessage.RemoveFuse =>
      currentState.attemptManualRemoveFuse

    case InternalFusedActorMessage.DestroyFuse =>
      currentState.attemptManualDestroyFuse

  }

  protected implicit class ContextOps(self: ActorContext) {
    def becomeFused(handler: Receive, discardOld: Boolean) {
      self.become(internalReceive orElse handler, discardOld)
    }

    def becomeFused(handler: Receive) {
      becomeFused(handler, true)
    }
  }

  private sealed trait InternalState {
    private val transitionListeners = new CopyOnWriteArrayList[Runnable]

    def addListener(listener: Runnable): Unit = transitionListeners add listener

    def publicState: CircuitState
    def invoke[T](surrogate: ⇒ AlmValidation[T], body: ⇒ AlmFuture[T]): AlmValidation[T]

    def callThrough[T](body: ⇒ AlmValidation[T]): AlmValidation[T] = {
      val deadline = callTimeout.fromNow
      val bodyValidation = try body catch { case scala.util.control.NonFatal(exn) ⇒ scalaz.Failure(ExceptionCaughtProblem(exn)) }
      bodyValidation.fold(
        fail => callFails(),
        succ =>
          if (!deadline.isOverdue)
            callSucceeds()
          else callFails())
      bodyValidation
    }

    def callSucceeds(): Unit

    def callFails(): Unit

    def enter(): Unit = {
      _enter()
      notifyTransitionListeners()
    }

    protected def _enter(): Unit

    protected def notifyTransitionListeners() {
      if (!transitionListeners.isEmpty()) {
        val iterator = transitionListeners.iterator
        while (iterator.hasNext) {
          val listener = iterator.next
          callbackExecutor.execute(listener)
        }
      }
    }

    def attemptManualClose(): Unit
    def attemptManualDestroyFuse(): Unit
    def attemptManualRemoveFuse(): Unit
  }

}