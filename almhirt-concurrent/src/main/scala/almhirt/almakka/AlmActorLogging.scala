package almhirt.almakka

import scalaz.syntax.show._
import akka.event._
import almhirt._, almvalidation.kit._

/** Enables an [[akka.actor.Actor]] to log directly on [[almhirt.validation.Problem]]s 
 * 
 * Log by calling the implicit on a [[almhirt.validation.AlmValidation]]
 */
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
  /** Implicits to be used on a problem */
  final class ProblemLoggerW(prob: Problem) {
    /** Log a [[almhirt.validation.Problem]] based on its [[almhirt.validation.Severity]]
     * 
     * @param minSeverity The minimum [[almhirt.validation.Severity]] the [[almhirt.validation.Problem]] must have to be logged
     */
    def log(minSeverity: Severity) {
      logProblem(prob, minSeverity)
    }
    /** Log a [[almhirt.validation.Problem]] */
    def log() {
      logProblem(prob, NoProblem)
    }
  }
  
  /** Implicits to be used on a [[almhirt.validation.AlmValidation]] */
  implicit def almValidation2AlmValidationLoggingW[T](validation: AlmValidation[T]) = new AlmValidationLoggingW[T](validation)
  final class AlmValidationLoggingW[T](validation: AlmValidation[T]) {
    /** Log a [[almhirt.validation.Problem]] based on its [[almhirt.validation.Severity]] in case of a Failure
     * 
     * @param minSeverity The minimum [[almhirt.validation.Severity]] the [[almhirt.validation.Problem]] contained in a Failure must have to be logged
     */
    def logFailure(minSeverity: Severity): AlmValidation[T] = 
      validation fold (prob => {logProblem(prob, minSeverity); validation}, _ => validation )
    /** Log a [[almhirt.validation.Problem]] contained in a Failure */
    def logFailure(): AlmValidation[T] = logFailure(NoProblem)
  }
  
  implicit def almFuture2AlmValidationLoggingW[T](future: AlmFuture[T]): AlmFutureLoggingW[T]  = new AlmFutureLoggingW[T](future)
  /** Implicits to be used on a [[almhirt.concurrent.AlmFuture]] */
  final class AlmFutureLoggingW[T](future: AlmFuture[T]) {
    /** Log a [[almhirt.validation.Problem]] based on its [[almhirt.validation.Severity]] in case of a future Failure
     * 
     * @param minSeverity The minimum [[almhirt.validation.Severity]] the [[almhirt.validation.Problem]] contained in a Failure must have to be logged
     */
    def logFailure(minSeverity: Severity): AlmFuture[T] = {
       future.onFailure(logProblem(_, minSeverity))
    }
    /** Log a [[almhirt.validation.Problem]] contained in case of a Failure */
    def logFailure(): AlmFuture[T] = logFailure(NoProblem)
  }
  
}

trait AlmSystemLogging { 
//  val log = Logging(AlmAkka.actorSystem, this)
}