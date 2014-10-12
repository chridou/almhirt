//package almhirt.akkax
//
//import almhirt.common._
//import akka.actor.Actor
//
//object FusedActor {
//}
//
//trait SyncFusedActor { self: Actor =>
//  import java.util.concurrent.CopyOnWriteArrayList
//  import AlmCircuitBreaker._
//
//  def settings: AlmCircuitBreaker.AlmCircuitBreakerSettings
//  
//  private val AlmCircuitBreakerSettings(maxFailures, failuresWarnThreshold, callTimeout, resetTimeout) = settings
//  
//  private var numFailedConsecutively = 0
//  
//  def fused[T](f: => AlmValidation[T]): AlmValidation[T]
//  
//  private def call[T](f: => AlmValidation[T]): AlmValidation[T] = 
//    ???
//  
//  
//  def whenClosed: Receive
//  
//  def whenOpened: Receive
//  
//  private sealed trait InternalState {
//    private val transitionListeners = new CopyOnWriteArrayList[Runnable]
//
//    def addListener(listener: Runnable): Unit = transitionListeners add listener
//
//    def publicState: State
//    def invoke[T](surrogate: ⇒ AlmFuture[T], body: ⇒ AlmFuture[T]): AlmFuture[T]
//
//    /**
//     * Shared implementation of call across all states.  Thrown exception or execution of the call beyond the allowed
//     * call timeout is counted as a failed call, otherwise a successful call
//     *
//     * @param body Implementation of the call
//     * @tparam T Return type of the call's implementation
//     * @return Future containing the result of the call
//     */
//    def callThrough[T](body: ⇒ AlmFuture[T]): AlmFuture[T] = {
//      val deadline = callTimeout.fromNow
//      val bodyFuture = try body catch { case scala.util.control.NonFatal(exn) ⇒ AlmFuture.failed(ExceptionCaughtProblem(exn)) }
//      bodyFuture.onComplete(
//        fail => callFails(),
//        succ =>
//          if (!deadline.isOverdue)
//            callSucceeds()
//          else callFails())(SameThreadExecutionContext)
//      bodyFuture
//    }
//
//    def callSucceeds(): Unit
//
//    def callFails(): Unit
//
//    def enter(): Unit = {
//      _enter()
//      notifyTransitionListeners()
//    }
//
//    protected def _enter(): Unit
//
//    protected def notifyTransitionListeners() {
//      if (!transitionListeners.isEmpty()) {
//        val iterator = transitionListeners.iterator
//        while (iterator.hasNext) {
//          val listener = iterator.next
//          executionContext.execute(listener)
//        }
//      }
//    }
//
//    def attemptManualClose(): Boolean = false
//    def attemptManualDestroyFuse(): Boolean
//    def attemptManualRemoveFuse(): Boolean
//  }
//
//  /**
//   * Valid transitions:
//   * -> Open
//   * -> FuseRemoved
//   * -> Destroyed
//   */
//  private case object InternalClosed extends InternalState {
//    def numConsecutiveFailures: Int = 0
//    
//    private val warningListeners = new CopyOnWriteArrayList[Int => Runnable]
//
//    def addWarningListener(listener: Int => Runnable): Unit = warningListeners add listener
//
//    override def publicState = Closed(get, maxFailures, failuresWarnThreshold)
//
//    override def invoke[T](surrogate: ⇒ AlmFuture[T], body: ⇒ AlmFuture[T]): AlmFuture[T] = callThrough(body)
//
//    override def callSucceeds() {
//      set(0)
//    }
//
//    override def callFails() {
//      val currentFailures = incrementAndGet()
//      if (currentFailures == maxFailures)
//        tripBreaker(InternalClosed)
//      failuresWarnThreshold.foreach(wt =>
//        if (currentFailures == wt)
//          notifyWarningListeners(currentFailures))
//    }
//
//    protected def notifyWarningListeners(failures: Int) {
//      if (!warningListeners.isEmpty()) {
//        val iterator = warningListeners.iterator
//        while (iterator.hasNext) {
//          val listener = iterator.next()(failures)
//          executionContext.execute(listener)
//        }
//      }
//    }
//
//    override def attemptManualDestroyFuse(): Boolean = attemptDestroyFuse(InternalClosed)
//    override def attemptManualRemoveFuse(): Boolean = attemptRemoveFuse(InternalClosed)
//
//    override def _enter() {
//      set(0)
//    }
//  }
//
//  /**
//   * Valid transitions:
//   * HalfOpen -> Closed
//   * HalfOpen -> Opened
//   * HalfOpen -> FuseRemoved
//   * HalfOpen -> Destroyed
//   */
//  private case object InternalHalfOpen extends AtomicBoolean with InternalState {
//    override def publicState = HalfOpen(!get)
//
//    override def invoke[T](surrogate: ⇒ AlmFuture[T], body: ⇒ AlmFuture[T]): AlmFuture[T] =
//      if (compareAndSet(true, false)) callThrough(body) else surrogate
//
//    override def callSucceeds() {
//      resetBreaker()
//    }
//
//    override def callFails() {
//      tripBreaker(InternalHalfOpen)
//    }
//
//    override def _enter() {
//      set(true)
//    }
//
//    override def attemptManualDestroyFuse(): Boolean = attemptDestroyFuse(InternalHalfOpen)
//    override def attemptManualRemoveFuse(): Boolean = attemptRemoveFuse(InternalHalfOpen)
//
//  }
//
//  /**
//   * Valid transitions:
//   * Open -> HalfOpen
//   * Open -> FuseRemoved
//   * Open -> Destroyed
//   */
//  private case object InternalOpen extends AtomicLong with InternalState {
//    private val myResetTimeout: FiniteDuration = resetTimeout getOrElse (Duration.Zero)
//
//    override def publicState = Open(remainingDuration())
//
//    override def invoke[T](surrogate: ⇒ AlmFuture[T], body: ⇒ AlmFuture[T]): AlmFuture[T] =
//      surrogate
//
//    override def callSucceeds() {
//    }
//
//    private def remainingDuration(): FiniteDuration = {
//      val elapsedNanos = System.nanoTime() - get
//      if (elapsedNanos <= 0L) Duration.Zero
//      else myResetTimeout - elapsedNanos.nanos
//    }
//
//    override def callFails() {
//    }
//
//    override def _enter() {
//      set(System.nanoTime())
//      resetTimeout.foreach { rt =>
//        scheduler.scheduleOnce(rt) {
//          attemptReset(InternalOpen)
//        }(executionContext)
//      }
//    }
//
//    override def attemptManualClose(): Boolean = attemptReset(InternalOpen)
//    override def attemptManualDestroyFuse(): Boolean = attemptDestroyFuse(InternalOpen)
//    override def attemptManualRemoveFuse(): Boolean = attemptRemoveFuse(InternalOpen)
//
//  }
//
//  /**
//   * Valid transitions:
//   * FuseRemoved -> HalfOpen
//   * FuseRemoved -> Destroyed
//   */
//  private case object InternalFuseRemoved extends AtomicLong with InternalState {
//    override def publicState = FuseRemoved(forDuration)
//
//    override def invoke[T](surrogate: ⇒ AlmFuture[T], body: ⇒ AlmFuture[T]): AlmFuture[T] =
//      surrogate
//
//    override def callSucceeds() {
//    }
//
//    override def callFails() {
//    }
//
//    private def forDuration(): FiniteDuration = {
//      val elapsedNanos = System.nanoTime() - get
//      if (elapsedNanos <= 0L) Duration.Zero
//      else elapsedNanos.nanos
//    }
//
//    override def _enter() {
//      set(System.nanoTime())
//    }
//
//    override def attemptManualClose(): Boolean = attemptReset(InternalOpen)
//    override def attemptManualDestroyFuse(): Boolean = attemptDestroyFuse(InternalFuseRemoved)
//    override def attemptManualRemoveFuse(): Boolean = false
//
//  }
//
//  /** No transitions */
//  private case object InternalFuseDestroyed extends AtomicLong with InternalState {
//    override def publicState = FuseDestroyed(forDuration)
//
//    override def invoke[T](surrogate: ⇒ AlmFuture[T], body: ⇒ AlmFuture[T]): AlmFuture[T] =
//      surrogate
//
//    override def callSucceeds() {
//    }
//
//    override def callFails() {
//    }
//
//    private def forDuration(): FiniteDuration = {
//      val elapsedNanos = System.nanoTime() - get
//      if (elapsedNanos <= 0L) Duration.Zero
//      else elapsedNanos.nanos
//    }
//
//    override def _enter() {
//      set(System.nanoTime())
//    }
//
//    override def attemptManualClose(): Boolean = false
//    override def attemptManualDestroyFuse(): Boolean = false
//    override def attemptManualRemoveFuse(): Boolean = false
//  }
//  
//}