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

  def apply(settings: AlmCircuitBreakerSettings, executionContext: ExecutionContext, scheduler: Scheduler): AlmCircuitBreaker =
    new AlmCircuitBreakerImpl(settings, executionContext, scheduler)

  sealed trait State
  final case class Closed(failureCount: Int, maxFailures: Int, warningLevel: Option[Int]) extends State {
    override def toString: String =
      if (failureCount == 0)
        "Closed"
      else
        warningLevel match {
          case None => s"Closed(failures: $failureCount, maxFailures: $maxFailures)"
          case Some(wl) => s"Closed(failures: $failureCount, maxFailures: $maxFailures, warn at $wl)"
        }
  }
  final case class HalfOpen(ongoingRecoverAttempt: Boolean) extends State {
    override def toString: String =
      if (ongoingRecoverAttempt)
        s"HalfOpen(attempting  to recover)"
      else
        "HalfOpen(waiting for recovery attempt)"
  }
  final case class Open(remaining: FiniteDuration) extends State {
    override def toString: String =
      s"Open(remaining: ${remaining.defaultUnitString})"
  }
  final case class FuseRemoved(duration: FiniteDuration) extends State {
    override def toString: String = s"FuseRemoved(${duration.defaultUnitString})"
  }
  final case class FuseDestroyed(duration: FiniteDuration) extends State {
    override def toString: String = s"FuseRemoved(${duration.defaultUnitString})"
  }

  implicit class AlmCircuitBreakerOps(self: AlmCircuitBreaker) {
    def defaultActorListeners(actor: akka.actor.ActorRef): AlmCircuitBreaker =
      self.onOpened(() => actor ! ActorMessages.CircuitOpened)
        .onHalfOpened(() => actor ! ActorMessages.CircuitHalfOpened)
        .onClosed(() => actor ! ActorMessages.CircuitClosed)
        .onFuseRemoved(() => actor ! ActorMessages.CircuitClosed)
        .onFuseDestroyed(() => actor ! ActorMessages.CircuitClosed)

  }
}

trait AlmCircuitBreaker {
  def fused[T](body: ⇒ AlmFuture[T]): AlmFuture[T]
  def reset(): Boolean
  def removeFuse(): Boolean
  def destroyFuse(): Boolean
  def insertFuse(): Boolean
  def state: AlmCircuitBreaker.State

  def onOpened(listener: () => Unit): AlmCircuitBreaker
  def onHalfOpened(listener: () => Unit): AlmCircuitBreaker
  def onClosed(listener: () => Unit): AlmCircuitBreaker
  def onFuseRemoved(listener: () => Unit): AlmCircuitBreaker
  def onFuseDestroyed(listener: () => Unit): AlmCircuitBreaker
  def onWarning(listener: (Int, Int) => Unit): AlmCircuitBreaker
}

/**
 * Circuit breaker implementation. This is in great parts copied from the akka one but adjusted to almhirt's needs.
 * This is also done to learn more about java concurrency.
 * Consider this stolen from akka.
 */
private[almhirt] class AlmCircuitBreakerImpl(settings: AlmCircuitBreaker.AlmCircuitBreakerSettings, executionContext: ExecutionContext, scheduler: Scheduler) extends AbstractAlmCircuitBreaker with AlmCircuitBreaker {
  import java.util.concurrent.atomic.{ AtomicInteger, AtomicBoolean, AtomicLong }
  import java.util.concurrent.CopyOnWriteArrayList
  import akka.util.Unsafe
  import AlmCircuitBreaker._

  val AlmCircuitBreakerSettings(maxFailures, failuresWarnThreshold, callTimeout, resetTimeout) = settings

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

  override def reset(): Boolean = currentState.attemptManualReset()

  override def removeFuse(): Boolean = currentState.attemptManualReset()

  override def destroyFuse(): Boolean = currentState.attemptManualRemoveFuse()

  override def insertFuse(): Boolean = currentState.attemptManualInsertFuse()

  override def state: AlmCircuitBreaker.State = currentState.publicState

  override def onOpened(listener: () => Unit): AlmCircuitBreaker = {
    InternalOpen addListener new Runnable { def run() { listener() } }
    this
  }

  override def onHalfOpened(listener: () => Unit): AlmCircuitBreaker = {
    InternalHalfOpen addListener new Runnable { def run() { listener() } }

    this
  }

  override def onClosed(listener: () => Unit): AlmCircuitBreaker = {
    InternalClosed addListener new Runnable { def run() { listener() } }

    this
  }

  override def onFuseRemoved(listener: () => Unit): AlmCircuitBreaker = {
    InternalFuseRemoved addListener new Runnable { def run() { listener() } }

    this
  }

  override def onFuseDestroyed(listener: () => Unit): AlmCircuitBreaker = {
    InternalFuseDestroyed addListener new Runnable { def run() { listener() } }

    this
  }

  override def onWarning(listener: (Int, Int) => Unit): AlmCircuitBreaker = {
    InternalClosed addWarningListener (currentFailures => new Runnable { def run() { listener(currentFailures, maxFailures) } })
    this
  }

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
  private def tripBreaker(fromState: InternalState): Unit =
    if (resetTimeout.isDefined)
      transition(fromState, InternalOpen)
    else
      transition(fromState, InternalFuseRemoved)

  /**
   * Resets breaker to a closed state.  This is valid from an Half-Open state only.
   *
   */
  private def resetBreaker(): Unit = transition(InternalHalfOpen, InternalClosed)

  private def attemptReset(): Boolean =
    if (swapState(InternalOpen, InternalHalfOpen)) {
      InternalHalfOpen.enter()
      true
    } else
      false

  private def attemptInsertFuse(): Boolean =
    if (swapState(InternalFuseRemoved, InternalHalfOpen)) {
      InternalHalfOpen.enter()
      true
    } else
      false

  private def attemptDestroyFuse(from: InternalState): Boolean =
    if (swapState(from, InternalFuseDestroyed)) {
      true
    } else
      false

  private def attemptRemoveFuse(from: InternalState): Boolean =
    if (swapState(from, InternalFuseRemoved)) {
      true
    } else
      false

  private sealed trait InternalState {
    private val transitionListeners = new CopyOnWriteArrayList[Runnable]

    def addListener(listener: Runnable): Unit = transitionListeners add listener

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
      notifyTransitionListeners()
    }

    protected def _enter(): Unit

    protected def notifyTransitionListeners() {
      if (!transitionListeners.isEmpty()) {
        val iterator = transitionListeners.iterator
        while (iterator.hasNext) {
          val listener = iterator.next
          executionContext.execute(listener)
        }
      }
    }

    def attemptManualReset(): Boolean = false
    def attemptManualDestroyFuse(): Boolean
    def attemptManualRemoveFuse(): Boolean
    def attemptManualInsertFuse(): Boolean = false
  }

  private case object InternalClosed extends AtomicInteger with InternalState {
    private val warningListeners = new CopyOnWriteArrayList[Int => Runnable]

    def addWarningListener(listener: Int => Runnable): Unit = warningListeners add listener

    override def publicState = Closed(get, maxFailures, failuresWarnThreshold)

    override def invoke[T](body: ⇒ AlmFuture[T]): AlmFuture[T] = callThrough(body)

    override def callSucceeds() {
      set(0)
    }

    override def callFails() {
      val currentFailures = incrementAndGet()
      if (currentFailures == maxFailures)
        tripBreaker(InternalClosed)
      failuresWarnThreshold.foreach(wt =>
        if (currentFailures == wt)
          notifyWarningListeners(currentFailures))
    }

    protected def notifyWarningListeners(failures: Int) {
      if (!warningListeners.isEmpty()) {
        val iterator = warningListeners.iterator
        while (iterator.hasNext) {
          val listener = iterator.next()(failures)
          executionContext.execute(listener)
        }
      }
    }

    override def attemptManualDestroyFuse(): Boolean = attemptDestroyFuse(InternalClosed)
    override def attemptManualRemoveFuse(): Boolean = attemptRemoveFuse(InternalClosed)

    override def _enter() {
      set(0)
    }
  }

  private case object InternalHalfOpen extends AtomicBoolean with InternalState {
    override def publicState = HalfOpen(!get)

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

    override def attemptManualDestroyFuse(): Boolean = attemptDestroyFuse(InternalHalfOpen)
    override def attemptManualRemoveFuse(): Boolean = attemptRemoveFuse(InternalHalfOpen)

  }

  private case object InternalOpen extends AtomicLong with InternalState {
    private val myResetTimeout: FiniteDuration = resetTimeout getOrElse (Duration.Zero)

    override def publicState = Open(remainingDuration())

    override def invoke[T](body: ⇒ AlmFuture[T]): AlmFuture[T] =
      AlmFuture.failed(CircuitBreakerOpenProblem())

    override def callSucceeds() {
    }

    private def remainingDuration(): FiniteDuration = {
      val elapsedNanos = System.nanoTime() - get
      if (elapsedNanos <= 0L) Duration.Zero
      else myResetTimeout - elapsedNanos.nanos
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

    override def attemptManualReset(): Boolean = attemptReset()

    override def attemptManualDestroyFuse(): Boolean = attemptDestroyFuse(InternalOpen)
    override def attemptManualRemoveFuse(): Boolean = attemptRemoveFuse(InternalOpen)

  }

  private case object InternalFuseRemoved extends AtomicLong with InternalState {
    override def publicState = FuseRemoved(forDuration)

    override def invoke[T](body: ⇒ AlmFuture[T]): AlmFuture[T] =
      AlmFuture.failed(CircuitBreakerOpenProblem())

    override def callSucceeds() {
    }

    override def callFails() {
    }

    private def forDuration(): FiniteDuration = {
      val elapsedNanos = System.nanoTime() - get
      if (elapsedNanos <= 0L) Duration.Zero
      else elapsedNanos.nanos
    }

    override def _enter() {
      set(System.nanoTime())
    }

    override def attemptManualDestroyFuse(): Boolean = attemptDestroyFuse(InternalFuseRemoved)
    override def attemptManualRemoveFuse(): Boolean = false
    override def attemptManualInsertFuse(): Boolean = attemptInsertFuse()

  }

  private case object InternalFuseDestroyed extends AtomicLong with InternalState {
    override def publicState = FuseDestroyed(forDuration)

    override def invoke[T](body: ⇒ AlmFuture[T]): AlmFuture[T] =
      AlmFuture.failed(CircuitBreakerOpenProblem())

    override def callSucceeds() {
    }

    override def callFails() {
    }

    private def forDuration(): FiniteDuration = {
      val elapsedNanos = System.nanoTime() - get
      if (elapsedNanos <= 0L) Duration.Zero
      else elapsedNanos.nanos
    }

    override def _enter() {
      set(System.nanoTime())
    }

    override def attemptManualReset(): Boolean = false
    override def attemptManualDestroyFuse(): Boolean = false
    override def attemptManualRemoveFuse(): Boolean = false
  }

}

private[almhirt] object SameThreadExecutionContext extends ExecutionContext {
  override def execute(runnable: Runnable) { runnable.run() }
  override def reportFailure(t: Throwable): Unit =
    throw new IllegalStateException("exception in sameThreadExecutionContext", t)
}
