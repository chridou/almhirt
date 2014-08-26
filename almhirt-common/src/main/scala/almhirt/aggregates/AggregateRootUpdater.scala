package almhirt.aggregates

import scalaz._, Scalaz._
import almhirt.common._

/**
 * Mix in this trait if you want to update your aggregate roots using an [[AggregateRootEventHandler]].
 *  This can be useful in a command handler.
 */
trait AggregateRootUpdater[T <: AggregateRoot, E <: AggregateEvent] { self: AggregateRootEventHandler[T, E] ⇒
  /** Use the event handler to update the existing aggregate root */
  def update(agg: T, event: E): (AggregateRootLifecycle[T], E) =
    (this.applyEvent(agg, event), event)

  protected implicit class ArOps(self: T) {
    /** Use the event handler to update the existing aggregate root. More convinient than calling [[AggregateRootUpdater#update]] */
    def update(event: E) = AggregateRootUpdater.this.update(self, event)

    /**
     * Use the event handler to create an update recorder representing the change on an existing aggregate root.
     *  Even more convinient than calling [[AggregateRootUpdater#update]]
     */
    def accept(event: E): UpdateRecorder[T, E] = UpdateRecorder.accept(AggregateRootUpdater.this.update(self, event))

    /**
     * Use the event handler to create an [[UpdateRecorder]] that accepts an event in case of a success.
     * Otherwise the [[UpdateRecorder]] will be rejected.
     * A successful result will apply the changes via the contained event.
     */
    def recordUpdate(eventOnSuccess: ⇒ AlmValidation[E]): UpdateRecorder[T, E] =
      eventOnSuccess.fold(
        fail ⇒ UpdateRecorder.reject(fail),
        event ⇒ self accept event)
  }

  /**
   * Contains functions to use a function that requires an existing aggregate root in the
   * environment of an [[UpdateRecorder]]. Hint: An [[UpdateRecorder]] contains an
   * [[AggregatRootLifecycle]] so most of it's operation act on that.
   * This is especially useful when you need to call [[UpdateRecorder#flatMap]].
   */
  protected implicit class Lifter(self: T ⇒ UpdateRecorder[T, E]) {
    def lift: AggregateRootLifecycle[T] ⇒ UpdateRecorder[T, E] =
      UpdateRecorder.ifVivus(self)

    def liftWith(state: AggregateRootLifecycle[T]): UpdateRecorder[T, E] =
      UpdateRecorder.ifVivus(self)(state)
  }

  /** Create an AR and record the event with it */
  def recordCreate(eventOnSuccess: ⇒ AlmValidation[E]): UpdateRecorder[T, E] =
    eventOnSuccess.fold(
      fail ⇒ UpdateRecorder.reject(fail),
      event ⇒ UpdateRecorder.accept(this.fromEvent(event), event))
}