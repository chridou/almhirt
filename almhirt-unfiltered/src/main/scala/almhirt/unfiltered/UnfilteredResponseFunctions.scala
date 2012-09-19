package almhirt.ext.unfiltered

import scalaz.syntax.show._
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.response._
import almhirt._
import almhirt.almvalidation.kit._

trait UnfilteredResponseFunctions {
  def problemToResponse(problem: Problem): ResponseFunction[HttpResponse] = {
    if(problem.isSystemProblem)
      InternalServerError ~> PlainTextContent ~> ResponseString(problem.shows)
    else
      problem match {
        case p: NotFoundProblem => NotFound~> PlainTextContent ~> ResponseString(p.toString)
        case p: SingleBadDataProblem => BadRequest~> PlainTextContent ~> ResponseString(p.toString)
        case p: MultipleBadDataProblem => BadRequest~> PlainTextContent ~> ResponseString(p.toString)
        case p: CollisionProblem => Conflict~> PlainTextContent ~> ResponseString(p.toString)
        case p: NotAuthorizedProblem => Unauthorized~> PlainTextContent ~> ResponseString(p.toString)
        case p: NotAuthenticatedProblem => Forbidden~> PlainTextContent ~> ResponseString(p.toString)
        case p: AlreadyExistsProblem => Conflict~> PlainTextContent ~> ResponseString(p.toString)
        case p: OperationCancelledProblem => InternalServerError~> PlainTextContent ~> ResponseString(p.toString)
        case p: BusinessRuleViolatedProblem => InternalServerError ~> PlainTextContent ~> ResponseString(p.toString)
        case p: LocaleNotSupportedProblem => BadRequest ~> PlainTextContent ~> ResponseString(p.toString)
        case p => InternalServerError ~> ResponseString(p.toString)
      }
  }
  
  def respondOnValidation[T](validation: AlmValidation[T], onSuccess: T => ResponseFunction[HttpResponse]) =
    validation fold (problemToResponse(_), onSuccess(_))
}
