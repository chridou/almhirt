package almhirt.akkax

import scala.reflect.ClassTag
import scala.concurrent.duration._
import akka.actor.ActorRef
import almhirt.common._

trait CircuitControl {
  def attemptClose(): Unit
  def removeFuse(): Unit
  def destroy(): Unit
  def circumvent(): Unit
  def state: AlmFuture[CircuitState]

  def onOpened(listener: () => Unit): CircuitControl
  def onHalfOpened(listener: () => Unit): CircuitControl
  def onClosed(listener: () => Unit): CircuitControl
  def onFuseRemoved(listener: () => Unit): CircuitControl
  def onDestroyed(listener: () => Unit): CircuitControl
  def onCircumvented(listener: () => Unit): CircuitControl
  def onWarning(listener: (Int, Int) => Unit): CircuitControl
}

object CircuitControl {
  implicit class CircuitControlOps(self: CircuitControl) {
    def defaultActorListeners(actor: akka.actor.ActorRef): CircuitControl =
      self.onOpened(() => actor ! ActorMessages.CircuitOpened)
        .onHalfOpened(() => actor ! ActorMessages.CircuitHalfOpened)
        .onClosed(() => actor ! ActorMessages.CircuitClosed)
        .onFuseRemoved(() => actor ! ActorMessages.CircuitFuseRemoved)
        .onDestroyed(() => actor ! ActorMessages.CircuitDestroyed)
        .onCircumvented(() => actor ! ActorMessages.CircuitCircumvented)
  }
}

trait FusedCircuit {
  def fused[T](body: ⇒ AlmFuture[T]): AlmFuture[T]

  /**
   *  In case of a fail fast, return the surrogate instead of the standard result defined by the implementation
   */
  def fusedWithSurrogate[T](surrogate: ⇒ AlmFuture[T])(body: ⇒ AlmFuture[T]): AlmFuture[T]
  
  def ask[T: ClassTag](actor:ActorRef, message: Any): AlmFuture[T]
  
  def askWithSurrogate[T: ClassTag](surrogate: ⇒ AlmFuture[T])(actor:ActorRef, message: Any): AlmFuture[T]
  
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
  sealed trait AllWillFailState extends CircuitState
  sealed trait NotAllWillFailState extends CircuitState
  final case class Closed(failureCount: Int, maxFailures: Int, warningLevel: Option[Int]) extends NotAllWillFailState {
    override def toString: String =
      if (failureCount == 0)
        "Closed"
      else
        warningLevel match {
          case None => s"Closed(failures: $failureCount, maxFailures: $maxFailures)"
          case Some(wl) => s"Closed(failures: $failureCount, maxFailures: $maxFailures, warn at $wl)"
        }
  }
  final case class HalfOpen(ongoingRecoverAttempt: Boolean) extends NotAllWillFailState {
    override def toString: String =
      if (ongoingRecoverAttempt)
        s"HalfOpen(attempting  to recover)"
      else
        "HalfOpen(waiting for recovery attempt)"
  }
  final case class Open(remaining: FiniteDuration) extends AllWillFailState {
    override def toString: String =
      s"Open(remaining: ${remaining.defaultUnitString})"
  }
  final case class FuseRemoved(duration: FiniteDuration) extends AllWillFailState {
    override def toString: String = s"FuseRemoved(${duration.defaultUnitString})"
  }
  final case class Destroyed(duration: FiniteDuration) extends AllWillFailState {
    override def toString: String = s"Destroyed(${duration.defaultUnitString})"
  }
  final case class Circumvented(duration: FiniteDuration) extends NotAllWillFailState {
    override def toString: String = s"Circumvented(${duration.defaultUnitString})"
  }
}
