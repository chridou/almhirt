package almhirt.unfiltered

import scalaz.{Success, Failure}
import org.jboss.netty.handler.codec.http.HttpResponse
import unfiltered.response._
import almhirt.validation._
import almhirt.validation.Problem._

object ProblemToUnfilteredResponse extends ProblemToUnfilteredResponseImplicits {
  def problemToResponse(problem: Problem): ResponseFunction[HttpResponse] = {
    problem match {
      case p: SystemProblem => InternalServerError ~> PlainTextContent ~> ResponseString(p.toInfoString)
      case p: ApplicationProblem => {
  		p match {
          case p: NotFoundProblem => unfiltered.response.NotFound~> PlainTextContent ~> ResponseString(p.toInfoString)
          case p: SingleBadDataProblem => unfiltered.response.BadRequest~> PlainTextContent ~> ResponseString("%s: %s".format(p.key, p.toInfoString))
          case p: MultipleBadDataProblem => 
            val items = p.keysAndMessages.toSeq.map{case (key, msg) => "%s -> %s".format(key, msg)}
            unfiltered.response.BadRequest~> PlainTextContent ~> ResponseString("%s\n%s".format(p.message, items.mkString("\n")))
          case p: CollisionProblem => unfiltered.response.Conflict~> PlainTextContent ~> ResponseString("[%s]: %s".format(p.toInfoString))
          case p: NotAuthorizedProblem => unfiltered.response.Unauthorized~> PlainTextContent ~> ResponseString(p.toInfoString)
          case p: NotAuthenticatedProblem => unfiltered.response.Forbidden~> PlainTextContent ~> ResponseString(p.toInfoString)
          case p: AlreadyExistsProblem => unfiltered.response.Conflict~> PlainTextContent ~> ResponseString(p.toInfoString)
          case p: OperationCancelledProblem => unfiltered.response.InternalServerError~> PlainTextContent ~> ResponseString(p.toInfoString)
          case p: BusinessRuleViolatedProblem => unfiltered.response.InternalServerError ~> PlainTextContent ~> ResponseString(p.toInfoString)
          case _ => InternalServerError ~> ResponseString(p.toInfoString)
  		}
      }
    }
  }
  
  def respondOnValidation[T](validation: AlmValidation[T], onSuccess: T => ResponseFunction[HttpResponse]) =
    validation fold (problemToResponse(_), onSuccess(_))
}


