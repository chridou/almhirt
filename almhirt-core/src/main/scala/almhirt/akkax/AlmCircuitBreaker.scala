package almhirt.akkax

import scala.concurrent.duration._
import almhirt.common._
import scala.concurrent.ExecutionContext
import akka.actor.Scheduler

object AlmCircuitBreaker {
  final case class AlmCircuitBreakerSettings(
    maxFailures: Int,
    failuresWarnThreshold: Option[Int],
    callTimeout: FiniteDuration,
    resetTimeout: Option[FiniteDuration])

  val defaultSettings = AlmCircuitBreakerSettings(5, Some(3), 10.seconds, Some(5.minutes))

  final case class AlmCircuitBreakerParams(
    settings: AlmCircuitBreakerSettings,
    onOpened: Option[() => Unit],
    onHalfOpened: Option[() => Unit],
    onClosed: Option[() => Unit],
    onWarning: Option[(Int, Int) => Unit])

  def apply(params: AlmCircuitBreaker.AlmCircuitBreakerParams, executionContext: ExecutionContext, scheduler: Scheduler): AlmCircuitBreaker =
    new AlmCircuitBreakerImpl(params, executionContext, scheduler)

  sealed trait State
  final case class Closed(failureCount: Int) extends State {
    override def toString: String =
      if (failureCount == 0)
        "Closed"
      else
        s"Closed(failiures: $failureCount)"
  }
  final case class HalfOpen(recovering: Boolean) extends State {
    override def toString: String =
      if (recovering)
        s"HalfOpen(recovering)"
      else
        "HalfOpen"
  }
  final case class Open(remaining: FiniteDuration) extends State {
    override def toString: String = s"Open(remaining: ${remaining.defaultUnitString})"
  }
}

trait AlmCircuitBreaker {
  def fused[T](body: ⇒ AlmFuture[T]): AlmFuture[T]
  def reset(): Boolean
  def state: AlmCircuitBreaker.State
}

/**
 * Circuit breaker implementation. This is in great parts copied from the akka one but adjusted to almhirt's needs.
 * This is also done to learn more about java concurrency.
 * Consider this stolen from akka.
 */
private[almhirt] class AlmCircuitBreakerImpl(params: AlmCircuitBreaker.AlmCircuitBreakerParams, executionContext: ExecutionContext, scheduler: Scheduler) extends AbstractAlmCircuitBreaker with AlmCircuitBreaker {
  import java.util.concurrent.atomic.{ AtomicInteger, AtomicBoolean, AtomicLong }
  import akka.util.Unsafe
  import AlmCircuitBreaker._

  val AlmCircuitBreakerSettings(maxFailures, failuresWarnThreshold, callTimeout, resetTimeout) = params.settings

  @volatile
  private[this] var _currentStateDoNotCallMeDirectly: InternalState = InternalClosed

  /**
   * Helper method for access to underlying state via Unsafe
   *
   * @param oldState Previous state on transition
   * @param newState Next state on transition
   * @return Whether the previous state matched correctly
   */
  @inline
  private[this] def swapState(oldState: InternalState, newState: InternalState): Boolean =
    Unsafe.instance.compareAndSwapObject(this, AbstractAlmCircuitBreaker.stateOffset, oldState, newState)

  /**
   * Helper method for accessing underlying state via Unsafe
   *
   * @return Reference to current state
   */
  @inline
  private[this] def currentState: InternalState =
    Unsafe.instance.getObjectVolatile(this, AbstractAlmCircuitBreaker.stateOffset).asInstanceOf[InternalState]

  override def fused[T](body: ⇒ AlmFuture[T]): AlmFuture[T] = {
    currentState.invoke(body)
  }

  override def reset(): Boolean = attemptReset() 

  /**
   * Implements consistent transition between states
   *
   * @param fromState State being transitioning from
   * @param toState State being transitioning from
   * @throws IllegalStateException if an invalid transition is attempted
   */
  private def transition(fromState: InternalState, toState: InternalState): Unit =
    if (swapState(fromState, toState))
      toState.enter()
    else
      throw new IllegalStateException("Illegal transition attempted from: " + fromState + " to " + toState)

  /**
   * Trips breaker to an open state.  This is valid from Closed or Half-Open states.
   *
   * @param fromState State we're coming from (Closed or Half-Open)
   */
  private def tripBreaker(fromState: InternalState): Unit = transition(fromState, InternalOpen)

  /**
   * Resets breaker to a closed state.  This is valid from an Half-Open state only.
   *
   */
  private def resetBreaker(): Unit = transition(InternalHalfOpen, InternalClosed)

  /**
   * Attempts to reset breaker. Works only, if the current state is Open.
   *
   */
  private def attemptReset(): Boolean = currentState.attemptManualReset()

  override def state: AlmCircuitBreaker.State =
    currentState.publicState

  private sealed trait InternalState {
    protected def listener: Option[Runnable]
    def publicState: State
    def invoke[T](body: ⇒ AlmFuture[T]): AlmFuture[T]

    /**
     * Shared implementation of call across all states.  Thrown exception or execution of the call beyond the allowed
     * call timeout is counted as a failed call, otherwise a successful call
     *
     * @param body Implementation of the call
     * @tparam T Return type of the call's implementation
     * @return Future containing the result of the call
     */
    def callThrough[T](body: ⇒ AlmFuture[T]): AlmFuture[T] = {
      val deadline = callTimeout.fromNow
      val bodyFuture = try body catch { case scala.util.control.NonFatal(exn) ⇒ AlmFuture.failed(ExceptionCaughtProblem(exn)) }
      bodyFuture.onComplete(
        fail => callFails(),
        succ =>
          if (!deadline.isOverdue)
            callSucceeds()
          else callFails())(SameThreadExecutionContext)
      bodyFuture
    }

    def callSucceeds(): Unit

    def callFails(): Unit

    def enter(): Unit = {
      _enter()
      notifyTransitionListener
    }

    protected def _enter(): Unit

    protected def notifyTransitionListener() {
      listener.foreach(l =>
        executionContext.execute(l))
    }

    def attemptManualReset(): Boolean = false
  }

  private case object InternalClosed extends AtomicInteger with InternalState {
    protected val listener = params.onClosed.map(callback => new Runnable { def run() = callback() })

    private val warningListener: Option[Int => Runnable] = params.onWarning.map(callback => x => new Runnable { def run() = callback(x, maxFailures) })

    override def publicState = Closed(get)

    override def invoke[T](body: ⇒ AlmFuture[T]): AlmFuture[T] = callThrough(body)

    override def callSucceeds() {
      set(0)
    }

    override def callFails() {
      val currentFailures = incrementAndGet()
      if (currentFailures == maxFailures)
        tripBreaker(InternalClosed)
      warningListener.flatMap(wListener =>
        failuresWarnThreshold.map(wt => (wListener, wt))).foreach {
        case (wListener, wt) =>
          if (currentFailures == wt)
            executionContext.execute(wListener(currentFailures))
      }
    }

    override def _enter() {
      set(0)
    }
  }

  private case object InternalHalfOpen extends AtomicBoolean with InternalState {
    protected val listener = params.onHalfOpened.map(callback => new Runnable { def run() = callback() })

    override val publicState = HalfOpen(!get)

    override def invoke[T](body: ⇒ AlmFuture[T]): AlmFuture[T] =
      if (compareAndSet(true, false)) callThrough(body) else AlmFuture.failed(CircuitBreakerOpenProblem("Trying to recover."))

    override def callSucceeds() {
      resetBreaker()
    }

    override def callFails() {
      tripBreaker(InternalHalfOpen)
    }

    override def _enter() {
      set(true)
    }
  }

  private case object InternalOpen extends AtomicLong with InternalState {
    protected val listener = params.onOpened.map(callback => new Runnable { def run() = callback() })

    override def publicState = Open(remainingDuration())

    override def invoke[T](body: ⇒ AlmFuture[T]): AlmFuture[T] =
      AlmFuture.failed(CircuitBreakerOpenProblem())

    override def callSucceeds() {
    }

    private def remainingDuration(): FiniteDuration = {
      val diff = System.nanoTime() - get
      if (diff <= 0L) Duration.Zero
      else diff.nanos
    }

    override def callFails() {
    }

    override def _enter() {
      set(System.nanoTime())
      resetTimeout.foreach { rt =>
        scheduler.scheduleOnce(rt) {
          attemptReset()
        }(executionContext)
      }
    }

    override def attemptManualReset(): Boolean = swapState(InternalOpen, InternalHalfOpen)
  }

}

private[almhirt] object SameThreadExecutionContext extends ExecutionContext {
  override def execute(runnable: Runnable) { runnable.run() }
  override def reportFailure(t: Throwable): Unit =
    throw new IllegalStateException("exception in sameThreadExecutionContext", t)
}
