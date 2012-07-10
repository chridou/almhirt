package almhirt.domain

import java.util.UUID
import scalaz.{NonEmptyList ,Validation, Success, Failure}
import scalaz.Validation._
import almhirt.validation.Problem
import almhirt.validation.Problem._


trait CanHandleEntityEvent[ES <: Entity[ES, Event], Event <: EntityEvent] {
  def applyEvent: Event => EntityValidation[ES]
}

trait Entity[ES <: Entity[ES, Event], Event <: EntityEvent] extends CanHandleEntityEvent[ES, Event]{
  def id: UUID
  def version: Long

  def applyEvent = {event: Event =>
    for {
      validated <- validateEvent(event)
      entity <- 
      	try {
      	  Success(handlers(validated))
      	} catch {
      		case err: MatchError => Failure(UnhandledEntityEventProblem("Unhandled event: %s".format(event.getClass.getName), event))
      		case err => Failure(defaultSystemProblem.withMessage(err.getMessage()))
      	}
    } yield entity
  }
  
  //private val unhandled: Event => EntityValidation[ES] = {event: Any => Failure(defaultSystemProblem.withMessage("Unhandled event")) }
  protected def handlers: PartialFunction[Event,ES] 
 
  def update(event: Event, handler: Event => ES): Update[Event, ES] = {
    try {
      Update.accept(event, handler(event))
    } catch {
      case exn => Update.reject(defaultSystemProblem.withMessage("Could not execute an update").withException(exn))
    }
  }

  def update(event: Event): Update[Event, ES]  = update(event, handlers)
  
  def reject(msg: String) =	Update.reject(defaultApplicationProblem.withMessage(msg))
  	
  protected def validateEvent(event: Event): Validation[Problem, Event] = {
  	if (event.entityId != this.id)
  	  Failure(defaultApplicationProblem.withMessage("Ids do not match!"))
  	else if(event.entityVersion != this.version)
  	  Failure(CollisionProblem("Conflict: Versions do not match. Targetted version is %d but the entity has version %d. The event was: %s".format(event.entityVersion, this.version, event.getClass().getName)))
  	else
  	  Success(event)
  }
}

trait CanCreateFromEntityEvents[ES <: Entity[ES, Event], Event <: EntityEvent] extends CanHandleEntityEvent[ES, Event] {
  def applyEvent = { event: Event =>
  	try { 
  	  Success(creationHandler(event)) 
  	} catch {
      case err: MatchError => Failure(defaultSystemProblem.withMessage("Unhandled creation event: %s".format(event.getClass.getName)))
      case err => Failure(defaultSystemProblem.withMessage(err.getMessage()))}
  }

  def rebuildFromHistory(history: NonEmptyList[Event]): EntityValidation[ES] = {
  	def buildEventSourced(es: ES, rest: List[Event]): EntityValidation[ES] = {
  	  rest match {
  	  	case Nil => Success(es)
  	  	case x :: xs =>
  	  	  es.applyEvent(x) match {
  	  	  	case Success(agg) => buildEventSourced(agg, xs)
  	  	  	case Failure(problem) => Failure(problem)
  	  	  }
  	  }
  	}
  	for {
  	  freshES <- applyEvent(history.head)
  	  built <- buildEventSourced(freshES, history.tail) 
  	} yield built
  }
  
  def create(event: Event): Update[Event,ES] =
  	applyEvent(event) match {
      case Success(es) => Update.accept(event, es) 
      case Failure(f) => Update.reject(f)
  }

    
  protected def creationHandler: PartialFunction[Event, ES]
}
