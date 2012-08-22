package almhirt.domain

import java.util.UUID
import scalaz._, Scalaz._
import almhirt.validation.Problem
import almhirt.validation.Problem._

trait CanCreateAggragateRoot[AR <: AggregateRoot[AR, Event], Event <: DomainEvent] extends CanHandleDomainEvent[AR, Event] {
  def applyEvent = { event: Event =>
  	try { 
  	  creationHandler(event).success 
  	} catch {
      case err: MatchError =>defaultSystemProblem.withMessage("Unhandled creation event: %s".format(event.getClass.getName)).failure
      case err => defaultSystemProblem.withMessage(err.getMessage()).failure}
  }

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
  
  def create(event: Event): UpdateRecorder[Event,AR] =
  	applyEvent(event) fold (UpdateRecorder.reject(_), UpdateRecorder.accept(event, _))

    
  protected def creationHandler: PartialFunction[Event, AR]
}
