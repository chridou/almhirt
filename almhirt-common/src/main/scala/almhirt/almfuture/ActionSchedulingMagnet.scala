package almhirt.almfuture

trait ActionSchedulingMagnet[T] {
  def schedule(to: T, actionBlock: ⇒ Unit, in: scala.concurrent.duration.FiniteDuration, executor: scala.concurrent.ExecutionContext): Unit
}