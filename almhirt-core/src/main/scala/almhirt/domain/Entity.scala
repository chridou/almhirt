package almhirt.domain

import java.util.UUID
import scalaz._, Scalaz._
import almhirt.validation.Problem
import almhirt.validation.Problem._


trait CanHandleEntityEvent[ES <: Entity[ES, Event], Event <: EntityEvent] {
  def applyEvent: Event => EntityValidation[ES]
}

trait Entity[ES <: Entity[ES, Event], Event <: EntityEvent] extends CanHandleEntityEvent[ES, Event]{
  def id: UUID
  def version: Long

  def applyEvent = {event: Event =>
    validateEvent(event) bind ( validated =>
      	try {
      	  handlers(validated).success
      	} catch {
      		case err: MatchError => UnhandledEntityEventProblem("Unhandled event: %s".format(event.getClass.getName), event).failure
      		case err => defaultSystemProblem.withMessage(err.getMessage()).failure
      	})
  }
  
  //private val unhandled: Event => EntityValidation[ES] = {event: Any => Failure(defaultSystemProblem.withMessage("Unhandled event")) }
  protected def handlers: PartialFunction[Event,ES] 
 
  def update(event: Event, handler: Event => ES): UpdateRecorder[Event, ES] = {
    try {
      UpdateRecorder.accept(event, handler(event))
    } catch {
      case exn => UpdateRecorder.reject(defaultSystemProblem.withMessage("Could not execute an update").withException(exn))
    }
  }

  def update(event: Event): UpdateRecorder[Event, ES]  = update(event, handlers)
  
  def reject(msg: String) =	UpdateRecorder.reject(defaultApplicationProblem.withMessage(msg))
  	
  protected def validateEvent(event: Event): Validation[Problem, Event] = {
  	if (event.entityId != this.id)
  	  defaultApplicationProblem.withMessage("Ids do not match!").failure
  	else if(event.entityVersion != this.version)
  	  CollisionProblem("Conflict: Versions do not match. Targetted version is %d but the entity has version %d. The event was: %s".format(event.entityVersion, this.version, event.getClass().getName)).failure
  	else
  	  event.success
  }
}

trait CanCreateFromEntityEvents[ES <: Entity[ES, Event], Event <: EntityEvent] extends CanHandleEntityEvent[ES, Event] {
  def applyEvent = { event: Event =>
  	try { 
  	  creationHandler(event).success 
  	} catch {
      case err: MatchError =>defaultSystemProblem.withMessage("Unhandled creation event: %s".format(event.getClass.getName)).failure
      case err => defaultSystemProblem.withMessage(err.getMessage()).failure}
  }

  def rebuildFromHistory(history: NonEmptyList[Event]): EntityValidation[ES] = {
  	def buildEventSourced(es: ES, rest: List[Event]): EntityValidation[ES] = {
  	  rest match {
  	  	case Nil => es.success
  	  	case x :: xs =>
  	  	  es.applyEvent(x) fold(_.failure, buildEventSourced(_, xs))
  	  }
  	}
    applyEvent(history.head) bind (freshES => buildEventSourced(freshES, history.tail))
  }
  
  def create(event: Event): UpdateRecorder[Event,ES] =
  	applyEvent(event) fold (UpdateRecorder.reject(_), UpdateRecorder.accept(event, _))

    
  protected def creationHandler: PartialFunction[Event, ES]
}
