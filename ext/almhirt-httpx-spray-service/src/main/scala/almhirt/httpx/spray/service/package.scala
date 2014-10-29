package almhirt.httpx.spray

import almhirt.common._
import spray.routing.RequestContext
import spray.http._
import spray.httpx.marshalling.Marshaller
import almhirt.problem.CauseIsProblem
import almhirt.problem.CauseIsThrowable
import almhirt.problem.HasAThrowable

package object service {
  def determineStatusCode(problem: Problem): StatusCode = {
    problem match {
      case NotFoundProblem(_) ⇒ StatusCodes.NotFound
      case ServiceNotFoundProblem(_) ⇒ StatusCodes.ServiceUnavailable
      case ServiceBrokenProblem(_) ⇒ StatusCodes.InternalServerError
      case ServiceShutDownProblem(_) ⇒ StatusCodes.ServiceUnavailable
      case ServiceNotAvailableProblem(_) ⇒ StatusCodes.ServiceUnavailable
      case ServiceBusyProblem(_) ⇒ StatusCodes.TooManyRequests
      case BadDataProblem(_) ⇒ StatusCodes.BadRequest
      case IllegalOperationProblem(_) ⇒ StatusCodes.BadRequest
      case ConstraintViolatedProblem(_) ⇒ StatusCodes.BadRequest
      case BusinessRuleViolatedProblem(_) ⇒ StatusCodes.BadRequest
      case OperationTimedOutProblem(_) ⇒ StatusCodes.InternalServerError
      case CircuitOpenProblem(_) ⇒ StatusCodes.ServiceUnavailable
      case ExceptionCaughtProblem(p) ⇒
        p.cause match {
          case Some(CauseIsThrowable(HasAThrowable(exn: EscalatedProblemException))) ⇒
            determineStatusCode(exn.escalatedProblem)
          case _ ⇒ StatusCodes.InternalServerError
        }
      case CommandExecutionFailedProblem(p) ⇒
        p.cause match {
          case Some(CauseIsProblem(innerProb)) ⇒ determineStatusCode(innerProb)
          case Some(CauseIsThrowable(HasAThrowable(exn: EscalatedProblemException))) ⇒
            determineStatusCode(exn.escalatedProblem)
          case _ ⇒ StatusCodes.InternalServerError
        }
      case _ ⇒ StatusCodes.InternalServerError
    }
  }

  implicit object DefaultAlmHttpProblemTerminator extends AlmHttpProblemTerminator {
    def terminateProblem(ctx: RequestContext, problem: Problem)(implicit problemMarshaller: Marshaller[Problem]) =
      ctx.complete(determineStatusCode(problem), problem)
  }
}