package almhirt.tooling

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import almhirt.common.Stoppable

trait PeriodicSchedulingMagnet[T] {
  def schedule(to: T, initialDelay: FiniteDuration, interval: FiniteDuration, block: ⇒ Unit)(implicit executor: ExecutionContext): Stoppable
}

trait WantsToBeStimulated {
  def stimulate(): Unit
  def pause(): Unit
  def restart(): Unit
  def isPaused: Boolean
}

object ImpatientCrybaby {
  def apply[T: PeriodicSchedulingMagnet](
    checkStimualationEvery: FiniteDuration,
    startCryingNotBefore: FiniteDuration,
    noMorePatienceAction: FiniteDuration ⇒ Unit,
    periodicScheduler: T)(implicit executor: ExecutionContext): WantsToBeStimulated with Stoppable =
    new MyImpatientCrybaby(checkStimualationEvery, startCryingNotBefore, noMorePatienceAction, periodicScheduler)
}

private[tooling] class MyImpatientCrybaby[T: PeriodicSchedulingMagnet](
  checkStimualationEvery: FiniteDuration,
  startCryingNotBefore: FiniteDuration,
  noMorePatienceAction: FiniteDuration ⇒ Unit,
  periodicScheduler: T)(implicit executor: ExecutionContext) extends WantsToBeStimulated with Stoppable {

  private[this] val magnet = implicitly[PeriodicSchedulingMagnet[T]]

  private[this] val lastStimulation = new java.util.concurrent.atomic.AtomicLong(System.nanoTime())
  private[this] val paused = new java.util.concurrent.atomic.AtomicBoolean(false)

  private[this] val stopMe = magnet.schedule(periodicScheduler, startCryingNotBefore, checkStimualationEvery, {
    if (!paused.get) {
      val currentNanoTime = System.nanoTime()
      val _lastStimulation = lastStimulation.get()
      val due = _lastStimulation + startCryingNotBefore.toNanos

      if (currentNanoTime > due) {
        noMorePatienceAction(FiniteDuration(currentNanoTime - _lastStimulation, NANOSECONDS))
      }
    }
  })

  override def stimulate() {
    val currentNanoTime = System.nanoTime()
    lastStimulation.set(currentNanoTime)
  }

  def pause(): Unit = {
    paused.compareAndSet(false, true)
  }

  // Operations are independent from each other, so no lock required
  def restart(): Unit = {
    stimulate()
    paused.compareAndSet(true, false)
  }

  def isPaused: Boolean = {
    paused.get
  }

  override def stop() {
    stopMe.stop()
  }
}