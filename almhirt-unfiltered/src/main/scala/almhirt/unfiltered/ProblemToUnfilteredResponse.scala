package almhirt.unfiltered

import scalaz.{Success, Failure}
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.response._
import almhirt.validation._
import almhirt.validation.Problem._

object ProblemToUnfilteredResponse extends ProblemToUnfilteredResponseImplicits {
  def problemToResponse(problem: Problem): ResponseFunction[HttpResponse] = {
    problem match {
      case p: SystemProblem => InternalServerError ~> PlainTextContent ~> ResponseString(problem.message)
      case p: ApplicationProblem => {
  		p match {
          case p: NotFoundProblem => unfiltered.response.NotFound~> PlainTextContent ~> ResponseString(p.message)
          case p: SingleBadDataProblem => unfiltered.response.BadRequest~> PlainTextContent ~> ResponseString("%s: %s".format(p.key, p.message))
          case p: MultipleBadDataProblem => 
            val items = p.keysAndMessages.toSeq.map{case (key, msg) => "%s -> %s".format(key, msg)}
            unfiltered.response.BadRequest~> PlainTextContent ~> ResponseString("%s\n%s".format(p.message, items.mkString("\n")))
          case p: CollisionProblem => unfiltered.response.Conflict~> PlainTextContent ~> ResponseString("[%s]: %s".format(p.message))
          case p: NotAuthorizedProblem => unfiltered.response.Unauthorized~> PlainTextContent ~> ResponseString(p.message)
          case p: NotAuthenticatedProblem => unfiltered.response.Forbidden~> PlainTextContent ~> ResponseString(p.message)
          case p: AlreadyExistsProblem => unfiltered.response.Conflict~> PlainTextContent ~> ResponseString(p.message)
          case p: OperationCancelledProblem => unfiltered.response.InternalServerError~> PlainTextContent ~> ResponseString(p.message)
          case p: BusinessRuleViolatedProblem => unfiltered.response.InternalServerError ~> PlainTextContent ~> ResponseString(p.message)
          case _ => InternalServerError ~> ResponseString(p.message)
  		}
      }
    }
    Created
  }
  
  def respondOnValidation[T](validation: AlmValidation[T], onSuccess: T => ResponseFunction[HttpResponse]) = {
    validation match {
      case Success(r) => onSuccess(r)
      case Failure(problem) => problemToResponse(problem)
    }
  }
}

