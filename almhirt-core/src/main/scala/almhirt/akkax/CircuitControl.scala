package almhirt.akkax

import scala.concurrent.duration._
import almhirt.common._

trait CircuitControl {
  def attemptClose(): Boolean
  def removeFuse(): Boolean
  def destroyFuse(): Boolean
  def state: CircuitState

  def onOpened(listener: () => Unit): AlmCircuitBreaker
  def onHalfOpened(listener: () => Unit): AlmCircuitBreaker
  def onClosed(listener: () => Unit): AlmCircuitBreaker
  def onFuseRemoved(listener: () => Unit): AlmCircuitBreaker
  def onFuseDestroyed(listener: () => Unit): AlmCircuitBreaker
  def onWarning(listener: (Int, Int) => Unit): AlmCircuitBreaker
}

object CircuitControl {
  implicit class AlmCircuitBreakerOps(self: AlmCircuitBreaker) {
    def defaultActorListeners(actor: akka.actor.ActorRef): AlmCircuitBreaker =
      self.onOpened(() => actor ! ActorMessages.CircuitOpened)
        .onHalfOpened(() => actor ! ActorMessages.CircuitHalfOpened)
        .onClosed(() => actor ! ActorMessages.CircuitClosed)
        .onFuseRemoved(() => actor ! ActorMessages.CircuitFuseRemoved)
        .onFuseDestroyed(() => actor ! ActorMessages.CircuitFuseDestroyed)
  }
}

trait FusedCircuit {
  def fused[T](body: ⇒ AlmFuture[T]): AlmFuture[T]
  
  /** 
   *  In case of a fail fast, return the surrogate instead of the standard result defined by the implementation
   */
  def fusedWithSurrogate[T](surrogate: ⇒ AlmFuture[T])(body: ⇒ AlmFuture[T]): AlmFuture[T]
}

final case class CircuitControlSettings(
  maxFailures: Int,
  failuresWarnThreshold: Option[Int],
  callTimeout: FiniteDuration,
  resetTimeout: Option[FiniteDuration])
  
object CircuitControlSettings {
   val defaultSettings = CircuitControlSettings(5, Some(3), 10.seconds, Some(5.minutes))
}

sealed trait CircuitState
object CircuitState {
  final case class Closed(failureCount: Int, maxFailures: Int, warningLevel: Option[Int]) extends CircuitState {
    override def toString: String =
      if (failureCount == 0)
        "Closed"
      else
        warningLevel match {
          case None => s"Closed(failures: $failureCount, maxFailures: $maxFailures)"
          case Some(wl) => s"Closed(failures: $failureCount, maxFailures: $maxFailures, warn at $wl)"
        }
  }
  final case class HalfOpen(ongoingRecoverAttempt: Boolean) extends CircuitState {
    override def toString: String =
      if (ongoingRecoverAttempt)
        s"HalfOpen(attempting  to recover)"
      else
        "HalfOpen(waiting for recovery attempt)"
  }
  final case class Open(remaining: FiniteDuration) extends CircuitState {
    override def toString: String =
      s"Open(remaining: ${remaining.defaultUnitString})"
  }
  final case class FuseRemoved(duration: FiniteDuration) extends CircuitState {
    override def toString: String = s"FuseRemoved(${duration.defaultUnitString})"
  }
  final case class FuseDestroyed(duration: FiniteDuration) extends CircuitState {
    override def toString: String = s"FuseRemoved(${duration.defaultUnitString})"
  }
}
