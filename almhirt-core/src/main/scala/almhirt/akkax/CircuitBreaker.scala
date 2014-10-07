package almhirt.akkax

import scala.concurrent.duration.FiniteDuration
import almhirt.common._
import scala.concurrent.ExecutionContext
import akka.actor.Scheduler

object AlmCircuitBreaker {
  final case class AlmCircuitBreakerSettings(
    maxFailures: Int,
    failuresWarnThreshold: Option[Int],
    callTimeout: FiniteDuration,
    resetTimeout: Option[FiniteDuration])

  final case class AlmCircuitBreakerParams(
    settings: AlmCircuitBreakerSettings,
    onOpened: () => Unit,
    onHalfOpened: () => Unit,
    onClosed: () => Unit)

  sealed trait State
  case object Closed extends State
  case object HalfOpen extends State
  case object Open extends State
}

trait AlmCircuitBreaker {
  def fused[T](f: () => AlmFuture[T]): AlmFuture[T]
  def state: AlmCircuitBreaker.State
}

private[almhirt] final class AlmCircuitBreakerImpl(params: AlmCircuitBreaker.AlmCircuitBreakerParams, executionContext: ExecutionContext, scheduler: Scheduler) extends AlmCircuitBreaker {
  import AlmCircuitBreaker._

  private trait InternalState
  private case object InternalClosed extends InternalState
  private case object InternalHalfOpen extends InternalState
  private case object InternalOpen extends InternalState
  
  val AlmCircuitBreakerSettings(maxFailures, failuresWarnThreshold, callTimeout, resetTimeout) = params.settings
  
  override def fused[T](f: () => AlmFuture[T]): AlmFuture[T] = {
    f()
  }
  
  override def state: AlmCircuitBreaker.State = 
    ???
}