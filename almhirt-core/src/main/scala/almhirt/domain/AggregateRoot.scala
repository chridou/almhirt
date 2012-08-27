package almhirt.domain

import java.util.UUID
import scalaz._, Scalaz._
import almhirt.validation._
import almhirt.validation.Problem
import almhirt.validation.Problem._


/** An aggregate root is the topmost entity of an aggregate. It aggregates those entities and value objects which cannot exist without the whole.
 * All entities within the aggregate should only be accessible via the aggregate root. Only an aggregate root justifies a repository.
 */
trait AggregateRoot[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends CanHandleDomainEvent[AR, Event]{
  /** The unique id that gives the aggregate its identity */
  def id: UUID
  /** The monotonically growing version which is increased by one with each event generated via mutation. 
   * The minimum value is 1L */
  def version: Long

  /** Applies the event by calling the default handler after validating the event. */
  def applyEvent = {event: Event => applyValidated(event, handlers)}
  
  /** A [[scala.PartialFunction]] that takes an event and returns a modified AR according to the event.
   * This should be the standard handler. You can also create specialized handlers and invoke them via update(event, handler)
   * The handler must increase the aggregate roots version
   */ 
  protected def handlers: PartialFunction[Event,AR] 
 
  /** Apply the event by calling the given handler which modifies the aggregate root based on the event
   * This method is usually used to call a specialized handler. 
   * 
   * @param event The event to apply the standard handler to
   */
  protected def update(event: Event, handler: Event => AR): UpdateRecorder[Event, AR] = {
    try {
      UpdateRecorder.accept(event, handler(event))
    } catch {
      case exn => UpdateRecorder.reject(defaultSystemProblem.withMessage("Could not execute an update").withException(exn))
    }
  }

  /** Apply the event by calling the default handler defined by the protected abstract method 'handlers' 
   * 
   * @param event The event to apply the standard handler to
   */
  protected def update(event: Event): UpdateRecorder[Event, AR] = update(event, handlers)
  
  
  /** Abort the update process
   * 
   * @param prob The reason for rejection as a problem
   * @return A failed [[almhirt.domain.UpdateRecorder]]
   */
  def reject(prob: ApplicationProblem): UpdateRecorder[Event, AR] = UpdateRecorder.reject(prob)

   /** Abort the update process. Returns the default application problem
   * 
   * @param msg The reason for rejection as a message
   * @return A failed [[almhirt.domain.UpdateRecorder]] with the [[almhirt.validation.Problem]] being the default application problem
   */
  def reject(msg: String): UpdateRecorder[Event, AR] = reject(defaultApplicationProblem.withMessage(msg))

   /** Abort the update process. Returns a  BusinessRuleViolatedProblem
   * 
   * @param msg The reason for rejection as a message
   * @param key A key for the operation/property mutation that failed
   * @param severity The severity of the failure. Default is [[almhirt.validation.NoProblem]]
   * @return A failed [[almhirt.domain.UpdateRecorder]] with the [[almhirt.validation.Problem.BusinessRuleViolatedProblem]] being the application problem
   */
  def rejectBusinessRuleViolated(msg: String, key: String, severity: Severity = NoProblem): UpdateRecorder[Event, AR] = reject(BusinessRuleViolatedProblem(msg, key, severity))

  /** Validates the event and then applies the handler
   * 
   * @param event The Event to validate and then apply
   * @param handler the handler to call with the event
   * @return The modified aggregate root or a failure
   */
  protected def applyValidated(event: Event, handler: PartialFunction[Event,AR]): DomainValidation[AR] = {
    validateEvent(event) bind ( validated =>
      	try {
      	  handler(validated).success
      	} catch {
      		case err: MatchError => UnhandledDomainEventProblem("Unhandled event: %s".format(event.getClass.getName), event).failure
      		case err => defaultSystemProblem.withMessage(err.getMessage()).failure
      	})
  }
  
  /** Check if the event targets this AR by comparing the ids and versions of this instance and the event
   * As this method is called before applying the event, the versions must have the same value.
   * 
   * @param event The Event to check against
   * @return The passed event wrapped in a success if it is valid otherwise a failure
   */
  protected def validateEvent(event: Event): AlmValidation[Event] = {
  	if (event.aggRootId != this.id)
  	  defaultApplicationProblem.withMessage("Ids do not match!").failure
  	else if(event.aggRootVersion != this.version)
  	  CollisionProblem("Conflict: Versions do not match. Targetted version is %d but the entity has version %d. The event was: %s".format(event.aggRootVersion, this.version, event.getClass().getName)).failure
  	else
  	  event.success
  }
}

