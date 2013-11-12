package almhirt.almakka

import scala.language.implicitConversions
import almhirt.common._
import scala.concurrent.ExecutionContext
import akka.actor._

// Stolen from akka
trait AlmFuturePipeToSupport {
  final class PipeableAlmFuture[T](val future: AlmFuture[T])(implicit executionContext: ExecutionContext) {
    def pipeTo(recipient: ActorRef)(implicit sender: ActorRef = Actor.noSender): AlmFuture[T] = {
      future onComplete (
        problem => recipient ! Status.Failure(new EscalatedProblemException(problem)),
        succ =>
          recipient ! succ)
      future
    }
    //    def pipeToSelection(recipient: ActorSelection)(implicit sender: ActorRef = Actor.noSender): Future[T] = {
    //      future onComplete {
    //        case Success(r) ⇒ recipient ! r
    //        case Failure(f) ⇒ recipient ! Status.Failure(f)
    //      }
    //      future
    //    }
    //    def to(recipient: ActorRef): PipeableFuture[T] = to(recipient, Actor.noSender)
    //    def to(recipient: ActorRef, sender: ActorRef): PipeableFuture[T] = {
    //      pipeTo(recipient)(sender)
    //      this
    //    }
    //    def to(recipient: ActorSelection): PipeableFuture[T] = to(recipient, Actor.noSender)
    //    def to(recipient: ActorSelection, sender: ActorRef): PipeableFuture[T] = {
    //      pipeToSelection(recipient)(sender)
    //      this
    //    }
  }
  implicit def pipe[T](future: AlmFuture[T])(implicit executionContext: ExecutionContext): PipeableAlmFuture[T] = new PipeableAlmFuture(future)

}