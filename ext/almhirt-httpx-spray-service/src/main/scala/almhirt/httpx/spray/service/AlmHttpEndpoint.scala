package almhirt.httpx.spray.service

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.httpx.spray.marshalling.HasProblemMarshaller
import spray.httpx.marshalling.Marshaller
import spray.routing.RequestContext
import scala.concurrent.ExecutionContext
import spray.http._

trait AlmHttpProblemTerminator {
  def terminateProblem(ctx: RequestContext, problem: Problem)(implicit problemMarshaller: Marshaller[Problem])
}

trait AlmHttpEndpoint { self: HasProblemMarshaller ⇒

  sealed trait PostMappedResult[+T]
  case class SuccessContent[T](payload: T, status: StatusCode = StatusCodes.OK) extends PostMappedResult[T]
  case class NoContent(status: StatusCode = StatusCodes.Accepted) extends PostMappedResult[Nothing]
  case class FailureContent[T](payload: T, status: StatusCode = StatusCodes.InternalServerError) extends PostMappedResult[T]
  case class ProblemContent(p: Problem) extends PostMappedResult[Nothing]

  implicit protected class AlmContext1Ops(ctx: RequestContext) {
    def completeAlm[T: Marshaller](successStatus: StatusCode, res: AlmValidation[T])(implicit problemTerminator: AlmHttpProblemTerminator): Unit =
      res.fold(
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ ctx.complete(successStatus, succ))

    def completeAlmOk[T: Marshaller](res: AlmValidation[T])(implicit problemTerminator: AlmHttpProblemTerminator): Unit =
      ctx.completeAlm(StatusCodes.OK, res)

    def completeAlmAccepted[T: Marshaller](res: AlmValidation[T])(implicit problemTerminator: AlmHttpProblemTerminator): Unit =
      ctx.completeAlm(StatusCodes.Accepted, res)

    def completeAlmF[T: Marshaller](successStatus: StatusCode, res: AlmFuture[T])(implicit problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      res.onComplete(
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ ctx.complete(successStatus, succ))

    def completeAlmOkF[T: Marshaller](res: AlmFuture[T])(implicit problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      ctx.completeAlmF(StatusCodes.OK, res)

    def completeAlmAcceptedF[T: Marshaller](res: AlmFuture[T])(implicit problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      ctx.completeAlmF(StatusCodes.Accepted, res)
  }

  implicit protected class AlmContext2Ops(ctx: RequestContext) {
    def completeAlmPF[T: Marshaller, U](successStatus: StatusCode, res: AlmValidation[U], pf: PartialFunction[U, AlmValidation[T]]): Unit =
      ctx.completeAlm(successStatus, res.flatMap(u ⇒ pf(u)))

    def completeAlmOkPF[T: Marshaller, U](res: AlmValidation[U], pf: PartialFunction[U, AlmValidation[T]]): Unit =
      ctx.completeAlmPF(StatusCodes.OK, res, pf)

    def completeAlmAcceptedPF[T: Marshaller, U](res: AlmValidation[U], pf: PartialFunction[U, AlmValidation[T]]): Unit =
      ctx.completeAlmPF(StatusCodes.Accepted, res, pf)

    def completeAlmFPF[T: Marshaller, U](successStatus: StatusCode, res: AlmFuture[U], pf: PartialFunction[U, AlmValidation[T]])(implicit executionContext: ExecutionContext): Unit =
      ctx.completeAlmF(successStatus, res.mapV(u ⇒ pf(u)))

    def completeAlmOkFPF[T: Marshaller, U](res: AlmFuture[U], pf: PartialFunction[U, AlmValidation[T]])(implicit executionContext: ExecutionContext): Unit =
      ctx.completeAlmFPF(StatusCodes.OK, res, pf)

    def completeAlmAcceptedFPF[T: Marshaller, U](res: AlmFuture[U], pf: PartialFunction[U, AlmValidation[T]])(implicit executionContext: ExecutionContext): Unit =
      ctx.completeAlmFPF(StatusCodes.Accepted, res, pf)
  }

  implicit protected class AlmContext3Ops(ctx: RequestContext) {
    @deprecated("Use completePostMapped", "0.7.0")
    def completeAlmPostMapped[T: Marshaller, U](res: AlmValidation[U], pf: PartialFunction[U, PostMappedResult[T]])(implicit problemTerminator: AlmHttpProblemTerminator): Unit =
      res fold (
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ pf(succ) match {
          case SuccessContent(payload, status) ⇒ ctx.complete(status, payload)
          case NoContent(status) ⇒ ctx.complete(status, "")
          case FailureContent(payload, status) ⇒ ctx.complete(status, payload)
          case ProblemContent(problem) ⇒ problemTerminator.terminateProblem(ctx, problem)
        })

    def completePostMapped[T: Marshaller, U](res: AlmValidation[U], pf: PartialFunction[U, PostMappedResult[T]])(implicit problemTerminator: AlmHttpProblemTerminator): Unit =
      res fold (
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ pf(succ) match {
          case SuccessContent(payload, status) ⇒ ctx.complete(status, payload)
          case NoContent(status) ⇒ ctx.complete(status, "")
          case FailureContent(payload, status) ⇒ ctx.complete(status, payload)
          case ProblemContent(problem) ⇒ problemTerminator.terminateProblem(ctx, problem)
        })

    @deprecated("Use completePostMappedF", "0.7.0")
    def completeAlmPostMappedF[T: Marshaller, U](res: AlmFuture[U], pf: PartialFunction[U, PostMappedResult[T]])(implicit problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      res fold (
        fail ⇒ problemTerminator.terminateProblem(ctx, fail),
        succ ⇒ pf(succ) match {
          case SuccessContent(payload, status) ⇒ ctx.complete(status, payload)
          case NoContent(status) ⇒ ctx.complete(status, "")
          case FailureContent(payload, status) ⇒ ctx.complete(status, payload)
          case ProblemContent(problem) ⇒ problemTerminator.terminateProblem(ctx, problem)
        })

    def completePostMappedF[T: Marshaller, U](res: AlmFuture[U], pf: PartialFunction[U, PostMappedResult[T]])(implicit problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
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
    def completeRequest(successStatus: StatusCode)(implicit entityMarshaller: Marshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator): Unit =
      ctx.completeAlm(successStatus, self)

    def completeRequestOk(implicit entityMarshaller: Marshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator): Unit =
      ctx.completeAlmOk(self)

    def completeRequestAccepted(implicit entityMarshaller: Marshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator): Unit =
      ctx.completeAlmAccepted(self)

    def completeRequestPF[U: Marshaller](pf: PartialFunction[T, AlmValidation[U]])(successStatus: StatusCode)(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator): Unit =
      ctx.completeAlmPF(successStatus, self, pf)

    def completeRequestOkPF[U: Marshaller](pf: PartialFunction[T, AlmValidation[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator): Unit =
      ctx.completeAlmOkPF(self, pf)

    def completeRequestAcceptedPF[U: Marshaller](pf: PartialFunction[T, AlmValidation[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator): Unit =
      ctx.completeAlmAcceptedPF(self, pf)

    def completeRequestPostMapped[U: Marshaller](pf: PartialFunction[T, PostMappedResult[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator): Unit =
      ctx.completeAlmPostMapped[U, T](self, pf)
  }

  implicit protected class AlmFutureOps[T](self: AlmFuture[T]) {
    def completeRequest(successStatus: StatusCode)(implicit entityMarshaller: Marshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      ctx.completeAlmF(successStatus, self)

    def completeRequestOk(implicit entityMarshaller: Marshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      ctx.completeAlmOkF(self)

    def completeRequestAccepted(implicit entityMarshaller: Marshaller[T], ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      ctx.completeAlmAcceptedF(self)

    def completeRequestPF[U: Marshaller](pf: PartialFunction[T, AlmValidation[U]])(successStatus: StatusCode)(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      ctx.completeAlmFPF(successStatus, self, pf)

    def completeRequestOkPF[U: Marshaller](pf: PartialFunction[T, AlmValidation[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      ctx.completeAlmOkFPF(self, pf)

    def completeRequestAcceptedPF[U: Marshaller](pf: PartialFunction[T, AlmValidation[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      ctx.completeAlmAcceptedFPF(self, pf)

    def completeRequestPostMapped[U: Marshaller](pf: PartialFunction[T, PostMappedResult[U]])(implicit ctx: RequestContext, problemTerminator: AlmHttpProblemTerminator, executionContext: ExecutionContext): Unit =
      ctx.completeAlmPostMappedF[U, T](self, pf)
  }

}

  
