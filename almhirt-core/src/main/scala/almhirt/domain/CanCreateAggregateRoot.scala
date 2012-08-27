package almhirt.domain

import java.util.UUID
import scalaz._, Scalaz._
import almhirt.validation.Problem
import almhirt.validation.Problem._

/** Functionality to create a new aggregate root */
trait CanCreateAggragateRoot[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends CanHandleDomainEvent[AR, Event] {
  /** Applies the event and returns a new aggregate root from the event or a failure */
  def applyEvent = { event: Event =>
  	try { 
  	  creationHandler(event).success 
  	} catch {
      case err: MatchError =>defaultSystemProblem.withMessage("Unhandled creation event: %s".format(event.getClass.getName)).failure
      case err => defaultSystemProblem.withMessage(err.getMessage()).failure}
  }

  /** Creates a new aggregate root and applies all the events to it */
  def rebuildFromHistory(history: NonEmptyList[Event]): DomainValidation[AR] = {
  	def buildEventSourced(es: AR, rest: List[Event]): DomainValidation[AR] = {
  	  rest match {
  	  	case Nil => es.success
  	  	case x :: xs =>
  	  	  es.applyEvent(x) fold(_.failure, buildEventSourced(_, xs))
  	  }
  	}
    applyEvent(history.head) bind (freshES => buildEventSourced(freshES, history.tail))
  }
  
  /** Creates an UpdateRecorder from the creating event */
  def create(event: Event): UpdateRecorder[Event,AR] =
  	applyEvent(event) fold (UpdateRecorder.reject(_), UpdateRecorder.accept(event, _))

  /** The event passed to this handler must create a new aggregate root */
  protected def creationHandler: PartialFunction[Event, AR]
}
