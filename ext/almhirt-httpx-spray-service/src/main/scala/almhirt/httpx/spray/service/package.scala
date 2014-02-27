package almhirt.httpx.spray

import almhirt.common._
import spray.routing.RequestContext
import spray.http._
import spray.httpx.marshalling.Marshaller

package object service {
  implicit object DefaultAlmHttpProblemTerminator extends AlmHttpProblemTerminator {
    def terminateProblem(ctx: RequestContext, problem: Problem)(implicit problemMarshaller: Marshaller[Problem]) =
      problem match {
        case NotFoundProblem(p) => ctx.complete(StatusCodes.NotFound, p)
        case BadDataProblem(p) => ctx.complete(StatusCodes.BadRequest, p)
        case ConstraintViolatedProblem(p) => ctx.complete(StatusCodes.BadRequest, p)
        case BusinessRuleViolatedProblem(p) => ctx.complete(StatusCodes.BadRequest, p)
        case OperationTimedOutProblem(p) => ctx.complete(StatusCodes.InternalServerError, p)
        case p => ctx.complete(StatusCodes.InternalServerError, p)
      }
  }
}