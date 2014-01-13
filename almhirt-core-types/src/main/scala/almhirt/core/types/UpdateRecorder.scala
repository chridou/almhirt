/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.core.types

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.syntax.almvalidation._

/**
 * Records the events for aggregate root updates.
 * Use flatMap to record events on successfully updated [[almhirt.domain.AggregateRoot]]s
 *
 */
sealed trait UpdateRecorder[+AR <: AggregateRoot[_, _], +Event <: DomainEvent] {
  def targetVersion: Long
  val requiredTargetVersionOnNextEvent: Long = events.lastOption.map(_.aggVersion + 1).getOrElse(targetVersion)

  /** Returns the recorded events that lead to the current result in chronological order */
  def events: IndexedSeq[Event]

  /**
   * The result of previous recordings
   * Returns the current aggregate root in a success or a failure
   */
  def ar: DomainValidation[AR]

  /**
   * Check whether the current AR state is a success
   *
   * @return True in case of a success
   */
  def isAccepted: Boolean = ar.isSuccess

  /**
   * Check whether the current AR state is a failure
   *
   * @return True in case of a failure
   */
  def isRejected: Boolean = !isAccepted

  /** Returns the AR and the events in chronological order. If the AR is in a Failure, the whole result is a Failure. */
  def result: AlmValidation[(AR, IndexedSeq[Event])] = {
    ar.map((_, events))
  }

  //  /** Apply the events to this Update and return a result
  //   * 
  //   * @param events The events to process by the application to create the result
  //   * @return The current events and the resulting AR 
  //   */
  //  def apply[EE >: Event](events: List[EE]): (List[EE], DomainValidation[AR])

  /**
   * Creates a new Update with an aggregate root transformed by f and the same events as written to this instance.
   * It does not execute f  if the current aggregate root is already a failure
   * Usually it makes no sense to call this method manually
   *
   * @param f Function that returns a new aggregate root. Usually a mutation of the one stored in this instance.
   * @return The [[almhirt.domain.UpdateRecorder]] with the old events and a mapped aggregate root
   */
  def map[AAR >: AR <: AggregateRoot[_, _], EEvent >: Event <: DomainEvent](f: AR => AAR): UpdateRecorder[AAR, EEvent] =
    ar fold (
      prob => this,
      succ => {
        val nextAr = f(succ)
        val ar =
          if (nextAr.id != succ.id) UnspecifiedProblem(s"Map failed in UpdateRecorder: You may not change the AR during a map operation. Original id is '${succ.id.toString()}', you returned an AR with id '${nextAr.id.toString()}'").withArg("AggregateRootId", succ.id).failure
          else if (nextAr.version != succ.version) UnspecifiedProblem(s"Map failed in UpdateRecorder: You may not change the AR's version during a map operation. Original version is '${succ.version.toString()}', you returned an AR with version '${nextAr.version.toString()}'").withArg("AggregateRootId", succ.id).failure
          else nextAr.success
        UpdateRecorder[AAR, EEvent](ar, events, targetVersion)
      })

  /**
   * Creates a new Update from the Update returned by f.
   * The new aggregate root and the new events are determined by calling apply with the current events on the Update returned by f
   * It does not execute f if the current aggregate root is already a failure
   *
   * @param f Function that returns an Update which will be used to create the new Update(Write operation)
   * @return The [[almhirt.domain.UpdateRecorder]] with eventually updated events and the new AR state
   */
  def flatMap[AAR >: AR <: AggregateRoot[_, _], EEvent >: Event <: DomainEvent](f: AR => UpdateRecorder[AAR, EEvent]): UpdateRecorder[AAR, EEvent] =
    ar fold (
      prob => this,
      currAr => f(currAr) foldInternal (
        (prob, newEvents, newTargetVersion) =>
          UpdateRecorder[AAR, EEvent](prob.failure, newEvents, newTargetVersion),
        (newAr, newEvents, newTargetVersion) =>
          if (newAr.id != currAr.id) {
            val prob = UnspecifiedProblem(s"FlatMap failed in UpdateRecorder: You may not change the AR during a flatMap operation. Original id is '${currAr.id.toString()}', you returned an AR with id '${newAr.id.toString()}'").withArg("AggregateRootId", currAr.id)
            UpdateRecorder[AAR, EEvent](prob.failure, events, targetVersion)
          } else if (requiredTargetVersionOnNextEvent != newTargetVersion) {
            val prob = UnspecifiedProblem(s"FlatMap failed in UpdateRecorder: The computed UpdateRecorder targets version $newTargetVersion but the current UpdateRecorder must be targetted at version $requiredTargetVersionOnNextEvent.").withArg("AggregateRootId", currAr.id)
            UpdateRecorder[AAR, EEvent](prob.failure, events, targetVersion)
          } else
            UpdateRecorder[AAR, EEvent](newAr.success, events ++ newEvents , targetVersion)))

  def record[AAR >: AR <: AggregateRoot[_, _], EEvent >: Event <: DomainEvent](newRecordings: UpdateRecorder[AAR, EEvent]): UpdateRecorder[AAR, EEvent] =
    this flatMap (_ => newRecordings)

  private def foldInternal[T](f: (Problem, IndexedSeq[Event], Long) => T, s: (AR, IndexedSeq[Event], Long) => T): T =
    ar fold (prob => f(prob, events, targetVersion), succ => s(succ, events, targetVersion))

  def fold[T](f: (Problem, IndexedSeq[Event]) => T, s: (AR, IndexedSeq[Event]) => T): T =
    result fold (prob => f(prob, events), succ => s(succ._1, succ._2))

  /**
   * Execute a side effect in case of a success
   *
   * @param onSuccessAction The action that triggers the side effect
   * @return This
   */
  //    def onSuccess(onSuccessAction: (List[Event], AR) => Unit): Unit = {
  //      result onSuccess (ar => { onSuccessAction(events, ar) })
  //    }

}

object UpdateRecorder {
  private def apply[AR <: AggregateRoot[_, _], Event <: DomainEvent](theAr: DomainValidation[AR], events: IndexedSeq[Event], theTargettedVersion: Long): UpdateRecorder[AR, Event] =
    new UpdateRecorderImpl[AR, Event](theAr, events, theTargettedVersion)

  /**
   * Creates a new update.
   * The function f will be applied when the new Update's apply method is triggered.
   *
   * @param f Function which takes a list of (previous) events and returns the new events with the result on the modified aggregate root
   */
  def apply[AR <: AggregateRoot[_, _], Event <: DomainEvent](theAr: AR): UpdateRecorder[AR, Event] =
    if (theAr.version != 0L)
      UpdateRecorder[AR, Event](theAr.success, IndexedSeq.empty, theAr.version)
    else
      UpdateRecorder[AR, Event](UnspecifiedProblem(s"You may start an UpdateRecorder only with an AggregateRoot which's version is greater than 0. The supplied AggregateRoot has a version of ${theAr.version}").withArg("AggregateRootId", theAr.id).failure, IndexedSeq.empty, theAr.version)

  /**
   * Starts a new recording with a fresh aggregate root and no previous events
   *
   * @param aggregate root The unmodified aggregate root
   */
  def startWith[AR <: AggregateRoot[_, _], Event <: DomainEvent](theAr: AR): UpdateRecorder[AR, Event] =
    apply[AR, Event](theAr)

  /**
   * Takes an event and the resulting Aggregate Root. The event is prepended to the previous events
   *
   * @param event The event resulting from an aggregate root operation
   * @param result The state of the aggregate root corresponding to the event
   */
  def accept[AR <: AggregateRoot[_, _], Event <: DomainEvent](event: Event, theAr: AR): UpdateRecorder[AR, Event] = {
    if (theAr.version - event.aggVersion != 1)
      reject(UnspecifiedProblem(s"The event's version(${event.aggVersion}) must be one less than the AR' version(${theAr.version}).").withArg("AggregateRootId", theAr.id))
    else if (theAr.id != event.aggId)
      reject(UnspecifiedProblem(s"The event's id(${event.aggId}) does not match the AR's id(${theAr.id}).").withArg("AggregateRootId", theAr.id))
    else
      UpdateRecorder(theAr.success, IndexedSeq(event), event.aggVersion)
  }

  /**
   * Takes an event and the resulting Aggregate Root. Previously written events are still contained
   *
   * @param error The problem causing this update to fail.
   */
  def reject[AR <: AggregateRoot[_, _], Event <: DomainEvent](error: Problem): UpdateRecorder[AR, Event] =
    UpdateRecorder[AR, Event](error.failure, IndexedSeq.empty, 0L)

  private class UpdateRecorderImpl[+AR <: AggregateRoot[_, _], +Event <: DomainEvent](val ar: DomainValidation[AR], val events: IndexedSeq[Event], val targetVersion: Long) extends UpdateRecorder[AR, Event]
}