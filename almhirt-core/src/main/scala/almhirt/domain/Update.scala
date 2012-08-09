package almhirt.domain

import scalaz._, Scalaz._
import almhirt.validation.Problem
import almhirt.validation.Problem._

trait Update[+Event <: EntityEvent, +ES <: Entity[_, _]] {
  def apply[EE >: Event](events: List[EE] = Nil): (List[EE], EntityValidation[ES])

  def map[B <: Entity[_, _]](f: ES => B) =
  	Update[Event, B] { events =>
  	  val (updatedEvents, validation) = this(events)
  	  validation fold (problem => (updatedEvents, problem.failure), ar => (updatedEvents, f(ar).success))}

  def flatMap[EE >: Event <: EntityEvent , B <: Entity[_, _]](f: ES => Update[EE, B]) =
  	Update[EE, B] { events =>
  	  val (updatedEvents, validation) = this(events)
  	  validation fold (problem => (updatedEvents, problem.failure), ar => (updatedEvents, UnspecifiedSystemProblem("not yet implemented").failure))}
  	  //validation fold (problem => (updatedEvents, problem.failure), ar => (updatedEvents, f(ar)(updatedEvents)))}

  def onSuccess(onSuccessAction: (List[Event], ES) => Unit = (e, r) => ()): EntityValidation[ES] = {
    val (events, validation) = apply()
    validation bind (result => { onSuccessAction(events, result); result.success })
  }

  def isAccepted() = apply()._2.isSuccess
  def isRejected() = apply()._2.isFailure
  
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
    Update[Event, ES](events => (events, result.success))

  def accept[Event <: EntityEvent, ES <: Entity[_, _]](event: Event, result: ES) =
    Update[Event, ES](events => (event :: events, result.success))

  def reject[Event <: EntityEvent, ES <: Entity[_, _]](errors: Problem) =
    Update[Event, ES](events => (events, errors.failure))

}