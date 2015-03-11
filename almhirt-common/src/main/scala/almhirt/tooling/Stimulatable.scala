package almhirt.tooling

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import almhirt.common.Stoppable
import java.util.concurrent.atomic.AtomicLong

trait PeriodicSchedulingMagnet[T] {
  def schedule(to: T, initialDelay: FiniteDuration, interval: FiniteDuration, action: () ⇒ Unit)(implicit executor: ExecutionContext): Stoppable
}

trait Stimulatable {
  def stimulate(): Unit
}

object UnpatientCrybaby {
  def apply[T: PeriodicSchedulingMagnet](
    interval: FiniteDuration,
    minimumPatience: FiniteDuration,
    noMorePatienceAction: FiniteDuration ⇒ Unit,
    periodicScheduler: T)(implicit executor: ExecutionContext): Stimulatable with Stoppable =
    new MyUnpatientCrybaby(interval, minimumPatience, noMorePatienceAction, periodicScheduler)
}

private[tooling] class MyUnpatientCrybaby[T: PeriodicSchedulingMagnet](
  interval: FiniteDuration,
  minimumPatience: FiniteDuration,
  noMorePatienceAction: FiniteDuration ⇒ Unit,
  periodicScheduler: T)(implicit executor: ExecutionContext) extends Stimulatable with Stoppable {

  private[this] val magnet = implicitly[PeriodicSchedulingMagnet[T]]

  private[this] val lastStimulation = new AtomicLong(System.nanoTime())

  private[this] val stopMe = magnet.schedule(periodicScheduler, minimumPatience, interval, () ⇒ {
    val currentNanoTime = System.nanoTime()
    val _lastStimulation = lastStimulation.get()
    val due = _lastStimulation + minimumPatience.toNanos

    if (currentNanoTime > due) {
      noMorePatienceAction(FiniteDuration(currentNanoTime - _lastStimulation, NANOSECONDS))
    }
  })

  override def stimulate() {
    val currentNanoTime = System.nanoTime()
    lastStimulation.set(currentNanoTime)
  }

  override def stop() {
    stopMe.stop()
  }
}