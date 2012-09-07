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
        case p: NotFoundProblem => NotFound~> PlainTextContent ~> ResponseString(p.shows)
        case p: SingleBadDataProblem => BadRequest~> PlainTextContent ~> ResponseString("%s: %s".format(p.key, p.shows))
        case p: MultipleBadDataProblem => 
          val items = p.keysAndMessages.toSeq.map{case (key, msg) => "%s -> %s".format(key, msg)}
          BadRequest~> PlainTextContent ~> ResponseString("%s\n%s".format(p.message, items.mkString("\n")))
        case p: CollisionProblem => Conflict~> PlainTextContent ~> ResponseString("[%s]: %s".format(p.shows))
        case p: NotAuthorizedProblem => Unauthorized~> PlainTextContent ~> ResponseString(p.shows)
        case p: NotAuthenticatedProblem => Forbidden~> PlainTextContent ~> ResponseString(p.shows)
        case p: AlreadyExistsProblem => Conflict~> PlainTextContent ~> ResponseString(p.shows)
        case p: OperationCancelledProblem => InternalServerError~> PlainTextContent ~> ResponseString(p.shows)
        case p: BusinessRuleViolatedProblem => InternalServerError ~> PlainTextContent ~> ResponseString(p.shows)
        case p => InternalServerError ~> ResponseString(p.shows)
      }
  }
  
  def respondOnValidation[T](validation: AlmValidation[T], onSuccess: T => ResponseFunction[HttpResponse]) =
    validation fold (problemToResponse(_), onSuccess(_))
}
