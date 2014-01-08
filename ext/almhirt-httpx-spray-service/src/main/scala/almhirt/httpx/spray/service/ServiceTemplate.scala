package almhirt.httpx.spray.service

import scalaz._, Scalaz._
import almhirt.common._
import spray.httpx.marshalling.Marshaller
import spray.routing.RequestContext
import scala.concurrent.ExecutionContext
import spray.http._

trait ServiceWorkflow {
  implicit def problemMarshaller: Marshaller[Problem]

  implicit class RequestContextOps(self: RequestContext) {
    def completeWith[T: Marshaller](responseFuture: AlmFuture[T], successCode: StatusCode)(implicit executionContext: ExecutionContext) {
      ServiceWorkflow.this.completeWith(self, responseFuture, successCode)
    }
    
    def completeWithOk[T: Marshaller](responseFuture: AlmFuture[T])(implicit executionContext: ExecutionContext) {
      ServiceWorkflow.this.completeWith(self, responseFuture, StatusCodes.OK)
    }
  }

  protected def completeWith[T: Marshaller](ctx: RequestContext, responseFuture: AlmFuture[T], successCode: StatusCode)(implicit executionContext: ExecutionContext) {
    responseFuture.onComplete(
      fail => {
        fail match {
          case NotFoundProblem(nfp) =>
            ctx.complete(StatusCodes.NotFound, nfp)
          case p =>
            val problem = UnspecifiedProblem("An error occured.", cause = Some(p))
            ctx.complete(StatusCodes.InternalServerError, problem)
        }
      },
      succ =>
        ctx.complete(successCode, succ))
  }

  protected def completeWithOk[T: Marshaller](ctx: RequestContext, responseFuture: AlmFuture[T])(implicit executionContext: ExecutionContext) {
    completeWith(ctx, responseFuture, StatusCodes.OK)
  }
}

  