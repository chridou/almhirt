package almhirt.akkax

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import almhirt.common._
import akka.event.LoggingAdapter
import akka.actor._
import akka.pattern._
import almhirt.context.HasAlmhirtContext
import almhirt.herder.HerderMessage

object FusedActor {
  def wrap(fusedActor: ActorRef)(timeout: FiniteDuration)(implicit executor: ExecutionContext): CircuitControl =
    new CircuitControl {
      import almhirt.almfuture.all._
      def attemptClose() { fusedActor ! InternalFusedActorMessage.AttemptClose }

      def removeFuse() { fusedActor ! InternalFusedActorMessage.RemoveFuse }

      def destroyFuse() { fusedActor ! InternalFusedActorMessage.DestroyFuse }

      def state: AlmFuture[CircuitState] =
        (fusedActor ? InternalFusedActorMessage.ReportState)(timeout).mapCastTo[CircuitState]

      def onOpened(listener: () => Unit): CircuitControl = {
        fusedActor ! InternalFusedActorMessage.OnOpened(listener)
        this
      }

      def onHalfOpened(listener: () => Unit): CircuitControl = {
        fusedActor ! InternalFusedActorMessage.OnHalfOpened(listener)
        this
      }

      def onClosed(listener: () => Unit): CircuitControl = {
        fusedActor ! InternalFusedActorMessage.OnClosed(listener)
        this
      }

      def onFuseRemoved(listener: () => Unit): CircuitControl = {
        fusedActor ! InternalFusedActorMessage.OnFuseRemoved(listener)
        this
      }

      def onFuseDestroyed(listener: () => Unit): CircuitControl = {
        fusedActor ! InternalFusedActorMessage.OnFuseDestroyed(listener)
        this
      }

      def onWarning(listener: (Int, Int) => Unit): CircuitControl = {
        fusedActor ! InternalFusedActorMessage.OnWarning(listener)
        this
      }
    }
}

private[almhirt] object InternalFusedActorMessage {
  case object ReportState
  case object AttemptClose
  case object RemoveFuse
  case object DestroyFuse
  final case class OnOpened(listener: () => Unit)
  final case class OnHalfOpened(listener: () => Unit)
  final case class OnClosed(listener: () => Unit)
  final case class OnFuseRemoved(listener: () => Unit)
  final case class OnFuseDestroyed(listener: () => Unit)
  final case class OnWarning(listener: (Int, Int) => Unit)
}

trait SyncFusedActor { me: Actor with HasAlmhirtContext =>
  import java.util.concurrent.CopyOnWriteArrayList
  import AlmCircuitBreaker._

  def circuitControlSettings: CircuitControlSettings
  def circuitControlCallbackExecutorSelector: ExtendedExecutionContextSelector
  def circuitControlLoggingAdapter: Option[LoggingAdapter] = None
  def circuitStateReportingInterval: Option[FiniteDuration]
  def sendStateChangedEvents: Boolean
  private val CircuitControlSettings(maxFailures, failuresWarnThreshold, callTimeout, resetTimeout) = circuitControlSettings
  private val callbackExecutor = circuitControlCallbackExecutorSelector.select(this.almhirtContext, this.context)
  private var currentState: InternalState = InternalClosed

  def fused[T](body: => AlmValidation[T]): AlmValidation[T] =
    fusedWithSurrogate(scalaz.Failure(CircuitOpenProblem("The circuit is open.")))(body)

  def fusedWithSurrogate[T](surrogate: ⇒ AlmValidation[T])(body: ⇒ AlmValidation[T]): AlmValidation[T] =
    currentState.invoke(surrogate, body)

  def registerCircuitControl(): Unit =
    context.actorSelection(almhirtContext.localActorPaths.herder) ! HerderMessage.RegisterCircuitControl(self, FusedActor.wrap(self)(10.seconds)(almhirtContext.futuresContext))

  def deregisterCircuitControl(): Unit =
    context.actorSelection(almhirtContext.localActorPaths.herder) ! HerderMessage.DeregisterCircuitControl(self)

  def state: CircuitState = currentState.publicState

  private object ReportState

  private def moveTo(newState: InternalState) {
    currentState = newState
    newState.enter()
  }

  private def attemptTransition(oldState: InternalState, newState: InternalState): Boolean = {
    if (currentState == oldState) {
      moveTo(newState)
      true
    } else {
      circuitControlLoggingAdapter.foreach(log =>
        s"""Attempted transition from $oldState to $newState failed. Current state was $currentState.""")
      false
    }
  }

  private case class AttemptTransition(origin: InternalState, target: InternalState)
  private val internalReceive: Receive = {
    case AttemptTransition(origin, target) =>
      attemptTransition(origin, origin)

    case InternalFusedActorMessage.ReportState =>
      sender() ! currentState.publicState

    case InternalFusedActorMessage.AttemptClose =>
      val res = currentState.attemptManualClose
      circuitControlLoggingAdapter.foreach(log =>
        if (res) log.info("Manual reset attempt succeeded")
        else log.warning(s"""Manual reset attempt failed. Current state is ${currentState.publicState}"""))

    case InternalFusedActorMessage.RemoveFuse =>
      val res = currentState.attemptManualRemoveFuse
      circuitControlLoggingAdapter.foreach(log =>
        if (res) log.warning("Manual remove fuse attempt succeeded")
        else log.warning(s"""Manual remove fuse attempt failed. Current state is ${currentState.publicState}"""))

    case InternalFusedActorMessage.DestroyFuse =>
      val res = currentState.attemptManualDestroyFuse
      circuitControlLoggingAdapter.foreach(log =>
        if (res) log.warning("Manual destroy fuse  attempt succeeded")
        else log.warning(s"""Manual  destroy fuse. Current state is ${currentState.publicState}"""))

    case InternalFusedActorMessage.OnOpened(listener) =>
      InternalOpen addListener new Runnable { def run() { listener() } }

    case InternalFusedActorMessage.OnHalfOpened(listener) =>
      InternalHalfOpen addListener new Runnable { def run() { listener() } }

    case InternalFusedActorMessage.OnClosed(listener) =>
      InternalClosed addListener new Runnable { def run() { listener() } }

    case InternalFusedActorMessage.OnFuseRemoved(listener) =>
      InternalFuseRemoved addListener new Runnable { def run() { listener() } }

    case InternalFusedActorMessage.OnFuseDestroyed(listener) =>
      InternalFuseDestroyed addListener new Runnable { def run() { listener() } }

    case InternalFusedActorMessage.OnWarning(listener) =>
      InternalClosed addWarningListener (currentFailures => new Runnable { def run() { listener(currentFailures, maxFailures) } })

    case ReportState =>
      circuitControlLoggingAdapter.flatMap(log => circuitStateReportingInterval.map((log, _))).foreach {
        case (log, interval) =>
          log.info(s"Current circuit state: ${currentState.publicState}")
          currentState match {
            case InternalOpen =>
              context.system.scheduler.scheduleOnce(interval, self, ReportState)(callbackExecutor)
            case InternalHalfOpen =>
              context.system.scheduler.scheduleOnce(interval, self, ReportState)(callbackExecutor)
            case _ =>
              ()
          }
      }
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
    def invoke[T](surrogate: ⇒ AlmValidation[T], body: ⇒ AlmValidation[T]): AlmValidation[T]

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
      self ! ReportState
    }

    private def sendStateChanged() {
      if (sendStateChangedEvents)
        this match {
          case InternalClosed => self ! ActorMessages.CircuitClosed
          case InternalHalfOpen => self ! ActorMessages.CircuitHalfOpened
          case InternalOpen => self ! ActorMessages.CircuitOpened
          case InternalFuseRemoved => self ! ActorMessages.CircuitFuseRemoved
          case InternalFuseDestroyed => self ! ActorMessages.CircuitFuseDestroyed
        }
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
      sendStateChanged()
    }

    def attemptManualClose(): Boolean = false
    def attemptManualDestroyFuse(): Boolean
    def attemptManualRemoveFuse(): Boolean
  }

  private case object InternalClosed extends InternalState {
    private val warningListeners = new CopyOnWriteArrayList[Int => Runnable]

    private var failureCount = 0

    def addWarningListener(listener: Int => Runnable): Unit = warningListeners add listener

    override def publicState = CircuitState.Closed(failureCount, maxFailures, failuresWarnThreshold)

    def invoke[T](surrogate: ⇒ AlmValidation[T], body: ⇒ AlmValidation[T]): AlmValidation[T] =
      callThrough(body)

    override def callSucceeds() {
      failureCount = 0
    }

    override def callFails() {
      failureCount += 1
      if (failureCount == maxFailures)
        moveTo(InternalOpen)
      failuresWarnThreshold.foreach(wt =>
        if (failureCount == wt)
          notifyWarningListeners(failureCount))
    }

    protected def notifyWarningListeners(failures: Int) {
      if (!warningListeners.isEmpty()) {
        val iterator = warningListeners.iterator
        while (iterator.hasNext) {
          val listener = iterator.next()(failures)
          callbackExecutor.execute(listener)
        }
      }
    }

    override def attemptManualDestroyFuse(): Boolean = attemptTransition(InternalClosed, InternalFuseDestroyed)
    override def attemptManualRemoveFuse(): Boolean = attemptTransition(InternalClosed, InternalFuseRemoved)

    override def _enter() {
      failureCount = 0
    }
  }

  private case object InternalHalfOpen extends InternalState {
    private var recovering = false

    override def publicState = CircuitState.HalfOpen(recovering)

    def invoke[T](surrogate: ⇒ AlmValidation[T], body: ⇒ AlmValidation[T]): AlmValidation[T] =
      if (!recovering) {
        recovering = true
        callThrough(body)
      } else {
        surrogate
      }

    override def callSucceeds() {
      moveTo(InternalClosed)
    }

    override def callFails() {
      moveTo(InternalOpen)
    }

    override def _enter() {
      recovering = false
    }

    override def attemptManualDestroyFuse(): Boolean = attemptTransition(InternalClosed, InternalFuseDestroyed)
    override def attemptManualRemoveFuse(): Boolean = attemptTransition(InternalClosed, InternalFuseRemoved)

  }

  private case object InternalOpen extends InternalState {
    private var entered: Long = 0L
    private val myResetTimeout: FiniteDuration = resetTimeout getOrElse (Duration.Zero)

    override def publicState = CircuitState.Open(remainingDuration())

    def invoke[T](surrogate: ⇒ AlmValidation[T], body: ⇒ AlmValidation[T]): AlmValidation[T] =
      surrogate

    override def callSucceeds() {
    }

    private def remainingDuration(): FiniteDuration = {
      val elapsedNanos = System.nanoTime() - entered
      if (elapsedNanos <= 0L) Duration.Zero
      else myResetTimeout - elapsedNanos.nanos
    }

    override def callFails() {
    }

    override def _enter() {
      entered = System.nanoTime()
      resetTimeout.foreach { rt =>
        context.system.scheduler.scheduleOnce(rt) {
          self ! AttemptTransition(InternalOpen, InternalHalfOpen)
        }(callbackExecutor)
      }
    }

    override def attemptManualClose(): Boolean = attemptTransition(InternalOpen, InternalHalfOpen)
    override def attemptManualDestroyFuse(): Boolean = attemptTransition(InternalOpen, InternalFuseDestroyed)
    override def attemptManualRemoveFuse(): Boolean = attemptTransition(InternalOpen, InternalFuseRemoved)

  }
  /**
   * Valid transitions:
   * FuseRemoved -> HalfOpen
   * FuseRemoved -> Destroyed
   */
  private case object InternalFuseRemoved extends InternalState {
    private var enteredNanos = 0L

    override def publicState = CircuitState.FuseRemoved(forDuration)

    def invoke[T](surrogate: ⇒ AlmValidation[T], body: ⇒ AlmValidation[T]): AlmValidation[T] =
      surrogate

    override def callSucceeds() {
    }

    override def callFails() {
    }

    private def forDuration(): FiniteDuration = {
      val elapsedNanos = System.nanoTime() - enteredNanos
      if (elapsedNanos <= 0L) Duration.Zero
      else elapsedNanos.nanos
    }

    override def _enter() {
      enteredNanos = System.nanoTime()
    }

    override def attemptManualClose(): Boolean = attemptTransition(InternalFuseRemoved, InternalHalfOpen)
    override def attemptManualDestroyFuse(): Boolean = attemptTransition(InternalFuseRemoved, InternalFuseDestroyed)
    override def attemptManualRemoveFuse(): Boolean = false

  }

  /** No transitions */
  private case object InternalFuseDestroyed extends InternalState {
    private var enteredNanos = 0L
    override def publicState = CircuitState.FuseDestroyed(forDuration)

    def invoke[T](surrogate: ⇒ AlmValidation[T], body: ⇒ AlmValidation[T]): AlmValidation[T] =
      surrogate

    override def callSucceeds() {
    }

    override def callFails() {
    }

    private def forDuration(): FiniteDuration = {
      val elapsedNanos = System.nanoTime() - enteredNanos
      if (elapsedNanos <= 0L) Duration.Zero
      else elapsedNanos.nanos
    }

    override def _enter() {
      enteredNanos = System.nanoTime()
    }

    override def attemptManualClose(): Boolean = false
    override def attemptManualDestroyFuse(): Boolean = false
    override def attemptManualRemoveFuse(): Boolean = false
  }

}