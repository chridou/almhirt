package almhirt.almakka

import scalaz.{Success, Failure}
import akka.event._
import almhirt.validation._
import almhirt.concurrent._

trait AlmActorLogging { self: akka.actor.Actor =>
  val log = Logging(context.system, this)
  
  private def logProblem(prob: Problem, minSeverity: Severity) {
    if(prob.severity >= minSeverity)
	  prob.severity match {
	    case NoProblem =>
	      log.debug(prob.toString)
	    case Minor =>
	      log.warning(prob.toString)
	    case Major =>
	      log.error(prob.toString)
	    case Critical =>
	      log.error(prob.toString)
	    }
  }
  
  implicit def almValidation2AlmValidationLoggingW[T](validation: AlmValidation[T]) = new AlmValidationLoggingW[T](validation)
  final class AlmValidationLoggingW[T](validation: AlmValidation[T]) {
    def logFailure(minSeverity: Severity): AlmValidation[T] = {
      validation match {
        case Success(_) => 
          validation
        case Failure(problem) =>
          logProblem(problem, minSeverity)
          validation
      }
    }
    def logFailure(): AlmValidation[T] = logFailure(Minor)
  }
  
  implicit def almFuture2AlmValidationLoggingW[T](future: AlmFuture[T]) = new AlmFutureLoggingW[T](future)
  final class AlmFutureLoggingW[T](future: AlmFuture[T]) {
    def logFailure(minSeverity: Severity): AlmFuture[T] = {
       future.onFailure(logProblem(_, Minor))
    }
    def logFailure(): AlmFuture[T] = logFailure(Minor)
  }
  
}

trait AlmSystemLogging { 
//  val log = Logging(AlmAkka.actorSystem, this)
}