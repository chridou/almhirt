package almhirt.domain

import java.util.UUID
import scalaz._, Scalaz._
import almhirt.core._
import almhirt.common._
import almhirt.almvalidation.kit._

trait UpdatesAggregateRoot[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] {
  /** Apply the event by calling the given handler which modifies the aggregate root based on the event
   * This method is usually used to call a specialized handler. 
   * 
   * @param event The event to apply the standard handler to
   */
  protected def update(event: Event, handler: Event => AR): UpdateRecorder[AR, Event] = {
    try {
      UpdateRecorder.accept(event, handler(event))
    } catch {
      case exn: Exception => UpdateRecorder.reject(ExceptionCaughtProblem(exn))
    }
  }

  /** Abort the update process
   * 
   * @param prob The reason for rejection as a problem
   * @return A failed [[almhirt.domain.UpdateRecorder]]
   */
  protected def reject(prob: Problem): UpdateRecorder[AR, Event] = UpdateRecorder.reject(prob)

   /** Abort the update process. Returns the default application problem
   * 
   * @param msg The reason for rejection as a message
   * @return A failed [[almhirt.domain.UpdateRecorder]] with the [[almhirt.validation.Problem]] being the default application problem
   */
  protected def reject(msg: String): UpdateRecorder[AR, Event] = reject(UnspecifiedProblem(msg))

   /** Abort the update process. Returns a  BusinessRuleViolatedProblem
   * 
   * @param msg The reason for rejection as a message
   * @param key A key for the operation/property mutation that failed
   * @param severity The severity of the failure. Default is [[almhirt.validation.NoProblem]]
   * @return A failed [[almhirt.domain.UpdateRecorder]] with the [[almhirt.validation.Problem.BusinessRuleViolatedProblem]] being the application problem
   */
  protected def rejectBusinessRuleViolated(msg: String, key: String): UpdateRecorder[AR, Event] = reject(BusinessRuleViolatedProblem(msg).withLabel(key))

}