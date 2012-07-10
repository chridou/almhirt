package almhirt.domain

import scalaz.{Validation, Success, Failure}
import almhirt.validation.Problem

trait Update[+Event <: EntityEvent, +ES <: Entity[_, _]] {
  def apply[EE >: Event](events: List[EE] = Nil): (List[EE], EntityValidation[ES])

  def map[B <: Entity[_, _]](f: ES => B) =
  	Update[Event, B] { events =>
  	  this(events) match {
  	  	case (updatedEvents, Success(ar)) => (updatedEvents, Success(f(ar)))
  	  	case (updatedEvents, Failure(problem)) => (updatedEvents, Failure(problem))
  	  }
  }

  def flatMap[EE >: Event <: EntityEvent , B <: Entity[_, _]](f: ES => Update[EE, B]) =
  	Update[EE, B] { events =>
  	  this(events) match {
  	  	case (updatedEvents, Success(ar)) => f(ar)(updatedEvents)
  	  	case (updatedEvents, Failure(problem)) => (updatedEvents, Failure(problem))
  	  }
  }

  def onSuccess(onSuccessAction: (List[Event], ES) => Unit = (e, r) => ()): EntityValidation[ES] = {
    val (events, validation) = apply()
    validation match {
      case Success(result) => { onSuccessAction(events, result); Success(result) }
      case failure         => failure
    }
  }

  def isAccepted() = apply()._2.isSuccess
  def isRejected() = apply()._2.isFailure
  
  def forceValue() =
    result match {
      case Success(es) => es
      case Failure(f) => sys.error("You tried to force a value from a validation! Never do this in production code!")
    }
  
  def result(): EntityValidation[ES] = {
    val (_, validation) = apply()
    validation
  }

  def events(): List[Event] = {
    val (events, _) = apply()
    events
  }
}

object Update {
  def apply[Event <: EntityEvent, ES <: Entity[_, _]](f: List[Event] => (List[Event], EntityValidation[ES])) =
  	new Update[Event, ES] {
  	  def apply[EE >: Event](events: List[EE]) = f(events.asInstanceOf[List[Event]])
  }
  
  def startWith[Event <: EntityEvent, ES <: Entity[_, _]](result: ES) =
    Update[Event, ES](events => (events, Success(result)))

  def accept[Event <: EntityEvent, ES <: Entity[_, _]](event: Event, result: ES) =
    Update[Event, ES](events => (event :: events, Success(result)))

  def reject[Event <: EntityEvent, ES <: Entity[_, _]](errors: Problem) =
    Update[Event, ES](events => (events, Failure(errors)))

}