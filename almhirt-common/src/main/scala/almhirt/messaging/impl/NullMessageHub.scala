package almhirt.messaging.impl

import scalaz.syntax.validation._
import akka.util.Timeout
import akka.dispatch._
import almhirt._
import almhirt.messaging._

class NullMessageHub(implicit executionContext: ExecutionContext) extends MessageHub {
  def createMessageChannel[TPayload <: AnyRef](topic: Option[String])(implicit m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] = 
     AlmPromise{ new NullMessageChannel[TPayload].success }
  def createGlobalMessageChannel[TPayload <: AnyRef](implicit m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] =
     AlmPromise{ new NullMessageChannel[TPayload].success }
  def broadcast(message: Message[AnyRef], topic: Option[String]){}
  def close(){}
}