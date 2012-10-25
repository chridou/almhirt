package almhirt.messaging.impl

import scalaz.syntax.validation._
import akka.util.Timeout
import akka.dispatch._
import almhirt._
import almhirt.messaging._

class DevNullMessageHub(implicit executionContext: ExecutionContext) extends MessageHub {
  def createMessageChannel[TPayload <: AnyRef](name: Option[String], topic: Option[String])(implicit m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] = 
     AlmPromise{ new DevNullMessageChannel[TPayload].success }
  def createGlobalMessageChannel[TPayload <: AnyRef](name: Option[String])(implicit m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] =
     AlmPromise{ new DevNullMessageChannel[TPayload].success }
  def broadcast(message: Message[AnyRef], topic: Option[String]){}
  def close(){}
}