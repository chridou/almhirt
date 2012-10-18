package almhirt.messaging.impl

import scalaz.syntax.validation._
import akka.util.Timeout
import akka.dispatch._
import almhirt._
import almhirt.messaging._

class DevNullMessageChannel[T <: AnyRef](implicit executionContext: ExecutionContext) extends MessageChannel[T] {
    def <-*(handler: Message[T] => Unit, classifier: Message[T] => Boolean): AlmFuture[RegistrationHolder] = AlmPromise{ new Registration[Int]{val ticket = 1; def dispose() = ()}.success }
    def createSubChannel[TPayload <: T](classifier: Message[TPayload] => Boolean)(implicit m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] = AlmPromise{new DevNullMessageChannel[TPayload]().success}
    def post[U <: T](message: Message[U]) = ()
    val topicPattern = None
    val registration = None
}