package almhirt.almakka

import scalaz.syntax.show._
import akka.event._
import almhirt.validation._
import almhirt.validation.Problem._
import almhirt.concurrent._

trait AlmActorLogging { self: akka.actor.Actor =>
  val log = Logging(context.system, this)
  
  private def logProblem(prob: Problem, minSeverity: Severity) {
    if(prob.severity >= minSeverity)
	  prob.severity match {
	    case NoProblem =>
	      log.debug(prob.shows)
	    case Minor =>
	      log.warning(prob.shows)
	    case Major =>
	      log.error(prob.shows)
	    case Critical =>
	      log.error(prob.shows)
	    }
  }
  
  
  implicit def problem2ProblemLoggerW(prob: Problem) = new ProblemLoggerW(prob)
  final class ProblemLoggerW(prob: Problem) {
    def log(minSeverity: Severity) {
      logProblem(prob, minSeverity)
    }
    def log() {
      logProblem(prob, NoProblem)
    }
  }
  
  implicit def almValidation2AlmValidationLoggingW[T](validation: AlmValidation[T]) = new AlmValidationLoggingW[T](validation)
  final class AlmValidationLoggingW[T](validation: AlmValidation[T]) {
    def logFailure(minSeverity: Severity): AlmValidation[T] = 
      validation fold (prob => {logProblem(prob, minSeverity); validation}, _ => validation )
    def logFailure(): AlmValidation[T] = logFailure(NoProblem)
  }
  
  implicit def almFuture2AlmValidationLoggingW[T](future: AlmFuture[T]): AlmFutureLoggingW[T]  = new AlmFutureLoggingW[T](future)
  final class AlmFutureLoggingW[T](future: AlmFuture[T]) {
    def logFailure(minSeverity: Severity): AlmFuture[T] = {
       future.onFailure(logProblem(_, minSeverity))
    }
    def logFailure(): AlmFuture[T] = logFailure(NoProblem)
  }
  
}

trait AlmSystemLogging { 
//  val log = Logging(AlmAkka.actorSystem, this)
}