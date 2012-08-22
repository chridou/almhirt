package almhirt.domain

import java.util.UUID
import scalaz._, Scalaz._
import almhirt.validation.Problem
import almhirt.validation.Problem._



trait AggregateRoot[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends CanHandleDomainEvent[AR, Event]{
  def id: UUID
  def version: Long

  def applyEvent = {event: Event =>
    validateEvent(event) bind ( validated =>
      	try {
      	  handlers(validated).success
      	} catch {
      		case err: MatchError => UnhandledDomainEventProblem("Unhandled event: %s".format(event.getClass.getName), event).failure
      		case err => defaultSystemProblem.withMessage(err.getMessage()).failure
      	})
  }
  
  protected def handlers: PartialFunction[Event,AR] 
 
  def update(event: Event, handler: Event => AR): UpdateRecorder[Event, AR] = {
    try {
      UpdateRecorder.accept(event, handler(event))
    } catch {
      case exn => UpdateRecorder.reject(defaultSystemProblem.withMessage("Could not execute an update").withException(exn))
    }
  }

  def update(event: Event): UpdateRecorder[Event, AR]  = update(event, handlers)
  
  def reject(msg: String) =	UpdateRecorder.reject(defaultApplicationProblem.withMessage(msg))
  	
  protected def validateEvent(event: Event): Validation[Problem, Event] = {
  	if (event.aggRootId != this.id)
  	  defaultApplicationProblem.withMessage("Ids do not match!").failure
  	else if(event.aggRootVersion != this.version)
  	  CollisionProblem("Conflict: Versions do not match. Targetted version is %d but the entity has version %d. The event was: %s".format(event.aggRootVersion, this.version, event.getClass().getName)).failure
  	else
  	  event.success
  }
}

