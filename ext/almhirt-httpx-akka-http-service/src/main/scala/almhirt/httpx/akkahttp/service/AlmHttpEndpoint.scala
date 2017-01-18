package almhirt.httpx.akkahttp.service

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.tracking._
import almhirt.httpx.akkahttp.marshalling.HasProblemMarshaller
import scalaz.Validation.FlatMap._
import akka.http.scaladsl.marshalling._
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import scala.concurrent.Future

trait AlmHttpProblemTerminator {
  def terminateProblem(ctx: RequestContext, problem: Problem)(implicit problemMarshaller: ToEntityMarshaller[Problem]): Future[RouteResult]
}

trait AlmHttpEndpoint {

  sealed trait PostMappedResult[+T]
  case class SuccessContent[T](payload: T, status: StatusCode = StatusCodes.OK) extends PostMappedResult[T]
  case class NoContent(status: StatusCode = StatusCodes.Accepted) extends PostMappedResult[Nothing]
  case class FailureContent[T](payload: T, status: StatusCode = StatusCodes.InternalServerError) extends PostMappedResult[T]
  case class ProblemContent(p: Problem) extends PostMappedResult[Nothing]

  implicit protected class AlmContext1Ops(ctx: RequestContext) {
    def completeAlm[T: ToEntityMarshaller](successStatus: StatusCode, res: AlmValidation[T])(implicit problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      res.fold(
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ ctx.complete(successStatus, succ))

    def completeAlmOk[T: ToEntityMarshaller](res: AlmValidation[T])(implicit problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlm(StatusCodes.OK, res)

    def completeAlmAccepted[T: ToEntityMarshaller](res: AlmValidation[T])(implicit problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlm(StatusCodes.Accepted, res)

    def completeAlmF[T: ToEntityMarshaller](successStatus: StatusCode, res: AlmFuture[T])(implicit problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem], executionContext: ExecutionContext): Unit =
      res.onComplete(
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ ctx.complete(successStatus, succ))

    def completeAlmOkF[T: ToEntityMarshaller](res: AlmFuture[T])(implicit problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem], executionContext: ExecutionContext): Unit =
      ctx.completeAlmF(StatusCodes.OK, res)

    def completeAlmAcceptedF[T: ToEntityMarshaller](res: AlmFuture[T])(implicit problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem], executionContext: ExecutionContext): Unit =
      ctx.completeAlmF(StatusCodes.Accepted, res)
  }

  implicit protected class AlmContext2Ops(ctx: RequestContext) {
    def completeAlmPF[T: ToEntityMarshaller, U](successStatus: StatusCode, res: AlmValidation[U], pf: PartialFunction[U, AlmValidation[T]])(implicit problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlm(successStatus, res.flatMap(u ⇒ pf(u)))

    def completeAlmOkPF[T: ToEntityMarshaller, U](res: AlmValidation[U], pf: PartialFunction[U, AlmValidation[T]])(implicit problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmPF(StatusCodes.OK, res, pf)

    def completeAlmAcceptedPF[T: ToEntityMarshaller, U](res: AlmValidation[U], pf: PartialFunction[U, AlmValidation[T]])(implicit problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmPF(StatusCodes.Accepted, res, pf)

    def completeAlmFPF[T: ToEntityMarshaller, U](successStatus: StatusCode, res: AlmFuture[U], pf: PartialFunction[U, AlmValidation[T]])(implicit executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmF(successStatus, res.mapV(u ⇒ pf(u)))

    def completeAlmOkFPF[T: ToEntityMarshaller, U](res: AlmFuture[U], pf: PartialFunction[U, AlmValidation[T]])(implicit executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmFPF(StatusCodes.OK, res, pf)

    def completeAlmAcceptedFPF[T: ToEntityMarshaller, U](res: AlmFuture[U], pf: PartialFunction[U, AlmValidation[T]])(implicit executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmFPF(StatusCodes.Accepted, res, pf)
  }

  implicit protected class AlmContext3Ops(ctx: RequestContext) {
    @deprecated("Use completePostMapped", "0.7.0")
    def completeAlmPostMapped[T: ToEntityMarshaller, U](res: AlmValidation[U], pf: PartialFunction[U, PostMappedResult[T]])(implicit problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      res fold (
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ pf(succ) match {
          case SuccessContent(payload, status) ⇒ ctx.complete(status, payload)
          case NoContent(status) ⇒ ctx.complete(status, "")
          case FailureContent(payload, status) ⇒ ctx.complete(status, payload)
          case ProblemContent(problem) ⇒ problemTerminator.terminateProblem(ctx, problem)
        })

    def completePostMapped[T: ToEntityMarshaller, U](res: AlmValidation[U], pf: PartialFunction[U, PostMappedResult[T]])(implicit problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      res fold (
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ pf(succ) match {
          case SuccessContent(payload, status) ⇒ ctx.complete(status, payload)
          case NoContent(status) ⇒ ctx.complete(status, "")
          case FailureContent(payload, status) ⇒ ctx.complete(status, payload)
          case ProblemContent(problem) ⇒ problemTerminator.terminateProblem(ctx, problem)
        })

    @deprecated("Use completePostMappedF", "0.7.0")
    def completeAlmPostMappedF[T: ToEntityMarshaller, U](res: AlmFuture[U], pf: PartialFunction[U, PostMappedResult[T]])(implicit problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      res fold (
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ pf(succ) match {
          case SuccessContent(payload, status) ⇒ ctx.complete(status, payload)
          case NoContent(status) ⇒ ctx.complete(status, "")
          case FailureContent(payload, status) ⇒ ctx.complete(status, payload)
          case ProblemContent(problem) ⇒ problemTerminator.terminateProblem(ctx, problem)
        })

    def completePostMappedF[T: ToEntityMarshaller, U](res: AlmFuture[U], pf: PartialFunction[U, PostMappedResult[T]])(implicit problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      res fold (
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ pf(succ) match {
          case SuccessContent(payload, status) ⇒ ctx.complete(status, payload)
          case NoContent(status) ⇒ ctx.complete(status, "")
          case FailureContent(payload, status) ⇒ ctx.complete(status, payload)
          case ProblemContent(problem) ⇒ problemTerminator.terminateProblem(ctx, problem)
        })
  }

  implicit protected class AlmValidationOps[T](self: AlmValidation[T]) {
    def completeRequest(successStatus: StatusCode)(implicit entityMarshaller: ToEntityMarshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlm(successStatus, self)

    def completeRequestOk(implicit entityMarshaller: ToEntityMarshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmOk(self)

    def completeRequestAccepted(implicit entityMarshaller: ToEntityMarshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmAccepted(self)

    def completeRequestPF[U: ToEntityMarshaller](pf: PartialFunction[T, AlmValidation[U]])(successStatus: StatusCode)(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmPF(successStatus, self, pf)

    def completeRequestOkPF[U: ToEntityMarshaller](pf: PartialFunction[T, AlmValidation[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmOkPF(self, pf)

    def completeRequestAcceptedPF[U: ToEntityMarshaller](pf: PartialFunction[T, AlmValidation[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmAcceptedPF(self, pf)

    def completeRequestPostMapped[U: ToEntityMarshaller](pf: PartialFunction[T, PostMappedResult[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completePostMapped[U, T](self, pf)
  }

  implicit protected class AlmFutureOps[T](self: AlmFuture[T]) {
    def completeRequest(successStatus: StatusCode)(implicit entityMarshaller: ToEntityMarshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmF(successStatus, self)

    def completeRequestOk(implicit entityMarshaller: ToEntityMarshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmOkF(self)

    def completeRequestAccepted(implicit entityMarshaller: ToEntityMarshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmAcceptedF(self)

    def completeRequestPF[U: ToEntityMarshaller](pf: PartialFunction[T, AlmValidation[U]])(successStatus: StatusCode)(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmFPF(successStatus, self, pf)

    def completeRequestOkPF[U: ToEntityMarshaller](pf: PartialFunction[T, AlmValidation[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmOkFPF(self, pf)

    def completeRequestAcceptedPF[U: ToEntityMarshaller](pf: PartialFunction[T, AlmValidation[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completeAlmAcceptedFPF(self, pf)

    def completeRequestPostMapped[U: ToEntityMarshaller](pf: PartialFunction[T, PostMappedResult[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]): Unit =
      ctx.completePostMappedF[U, T](self, pf)
  }

  implicit protected class AlmFutureCommandResponseOps(self: AlmFuture[CommandResponse]) {
    def completeWithFlattenedCommandResponse(implicit entityMarshaller: ToEntityMarshaller[String], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]) = {
      self.onComplete(
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        commandResponse ⇒ {
          commandResponse match {
            case CommandAccepted(id) ⇒
              ctx.complete(StatusCodes.Accepted, id.value)
            case CommandNotAccepted(id, problem) ⇒
              problemTerminator.terminateProblem(ctx, problem)
            case TrackedCommandResult(id, CommandStatus.Executed) ⇒
              ctx.complete(StatusCodes.OK, id.value)
            case TrackedCommandResult(id, CommandStatus.NotExecuted(cause)) ⇒
              problemTerminator.terminateProblem(ctx, CommandExecutionFailedProblem("A was not completed", args = Map("command-id" → id.value), cause = Some(cause)))
            case TrackingFailed(id, problem) ⇒
              problemTerminator.terminateProblem(ctx, problem)
          }
        })
    }

    def completeWithCommandResponse(implicit entityMarshaller: ToEntityMarshaller[CommandResponse], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]) {
      self.onComplete(
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        commandResponse ⇒ {
          commandResponse match {
            case r: CommandAccepted ⇒
              ctx.complete(StatusCodes.Accepted, r)
            case r: CommandNotAccepted ⇒
              ctx.complete(determineStatusCode(r.problem), r)
            case r @ TrackedCommandResult(_, CommandStatus.Executed) ⇒
              ctx.complete(StatusCodes.OK, r)
            case r @ TrackedCommandResult(id, CommandStatus.NotExecuted(cause)) ⇒
              ctx.complete(determineStatusCode(cause.toProblem), r)
            case r @ TrackingFailed(id, problem) ⇒
              ctx.complete(determineStatusCode(r.problem), r)
          }
        })
    }

    def completeCommandResponse(flattened: Boolean)(implicit entityMarshaller: ToEntityMarshaller[CommandResponse], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext, problemMarshaller: ToEntityMarshaller[Problem]) {
      if (flattened)
        self.completeWithFlattenedCommandResponse
      else
        self.completeWithCommandResponse
    }
  }
}

  
