package almhirt.aggregates

import scalaz._, Scalaz._
import almhirt.common._

trait AggregateRootUpdater[T <: AggregateRoot, E <: AggregateEvent] { self: AggregateRootEventHandler[T, E] =>
  def update(agg: T, event: E): (AggregateRootLifecycle[T], E) =
    (this.applyEvent(agg, event), event)

  protected implicit class ArOps(self: T) {
    def update(event: E) = AggregateRootUpdater.this.update(self, event)
    def accept(event: E) = UpdateRecorder.accept(AggregateRootUpdater.this.update(self, event))
    def recordUpdate(eventOnSuccess: => AlmValidation[E]): UpdateRecorder[T, E] =
      eventOnSuccess.fold(
        fail => UpdateRecorder.reject(fail),
        event => self accept event)
  }

  protected implicit class Lifter(self: T => UpdateRecorder[T, E]) {
    def lift: AggregateRootLifecycle[T] => UpdateRecorder[T, E] =
      UpdateRecorder.ifVivus(self)

    def liftWith(state: AggregateRootLifecycle[T]): UpdateRecorder[T, E] =
      UpdateRecorder.ifVivus(self)(state)
  }

  /** Create an AR and record the event with it */
  def recordCreate(eventOnSuccess: => AlmValidation[E]): UpdateRecorder[T, E] =
    eventOnSuccess.fold(
      fail => UpdateRecorder.reject(fail),
      event => UpdateRecorder.accept(this.fromEvent(event), event))

  object updaterimplicits {
    import scala.language.implicitConversions

    //    implicit def lifter(f: T => UpdateRecorder[T, E]): AggregateRootLifecycle[T] => UpdateRecorder[T, E] =
    //      ???
  }

}