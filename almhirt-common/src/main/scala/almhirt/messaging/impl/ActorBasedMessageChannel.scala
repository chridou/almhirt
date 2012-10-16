package almhirt.messaging.impl

import java.util.UUID
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import akka.dispatch._
import almhirt._
import almhirt.messaging._
import almhirt.messaging.impl.commands._
import almakka.{ AlmAkkaContext }
import almvalidation.kit._
import almfuture.all._

object ActorBasedMessageChannel {

  def apply[T <: AnyRef](name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext, actorDispatcherName: Option[String], registration: Option[RegistrationHolder], topicPattern: Option[String])(implicit m: Manifest[T]): MessageChannel[T] = {
    val newDispatcher = MessageChannelActorHandler(name, actorSystem, timeout, futureDispatcher, actorDispatcherName)
    new ActorBasedMessageChannelImpl[T](actorSystem, newDispatcher, timeout, futureDispatcher, actorDispatcherName, registration, topicPattern)
  }

  def apply[T <: AnyRef](name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext)(implicit m: Manifest[T]): MessageChannel[T] = {
    val newDispatcher = MessageChannelActorHandler(name, actorSystem, timeout, futureDispatcher, None)
    new ActorBasedMessageChannelImpl[T](actorSystem, newDispatcher, timeout, futureDispatcher, None, None, None)
  }

  def apply[T <: AnyRef](name: Option[String], almAkkaContext: AlmAkkaContext, registration: Option[RegistrationHolder], topicPattern: Option[String])(implicit m: Manifest[T]): MessageChannel[T] = {
    val newDispatcher = MessageChannelActorHandler(name, almAkkaContext)
    new ActorBasedMessageChannelImpl[T](
      almAkkaContext.actorSystem,
      newDispatcher,
      almAkkaContext.mediumDuration,
      almAkkaContext.futureDispatcher,
      almAkkaContext.messageStreamDispatcherName,
      registration,
      topicPattern)
  }

  def apply[T <: AnyRef](name: Option[String], almAkkaContext: AlmAkkaContext)(implicit m: Manifest[T]): MessageChannel[T] = {
    apply(name, almAkkaContext, None, None)
  }

  private[impl] def apply[T <: AnyRef](name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext, actorDispatcherName: Option[String], registration: Option[RegistrationHolder], topicPattern: Option[String], channelHandler: ActorRef)(implicit m: Manifest[T]): MessageChannel[T] = {
    new ActorBasedMessageChannelImpl[T](actorSystem, channelHandler, timeout, futureDispatcher, actorDispatcherName, registration, topicPattern)
  }

  class ActorBasedMessageChannelImpl[T <: AnyRef](actorSystem: ActorRefFactory, val dispatcher: ActorRef, implicit val timeout: Timeout, implicit val futureDispatcher: ExecutionContext, actorDispatcherName: Option[String], val registration: Option[RegistrationHolder], val topicPattern: Option[String])(implicit m: Manifest[T]) extends MessageChannel[T] {
    def <-*(handler: Message[T] => Unit, classifier: Message[T] => Boolean): AlmFuture[RegistrationHolder] = {
      def wrappedHandler(message: Message[AnyRef]): Unit =
        handler(message.asInstanceOf[Message[T]])
      def wrappedClassifier(message: Message[AnyRef]) =
        if (m.erasure.isAssignableFrom(message.payload.getClass()))
          classifier(message.asInstanceOf[Message[T]])
        else
          false
      (dispatcher ? RegisterMessageHandlerCommand(handler, classifier)).toAlmFuture[RegistrationHolder]
    }
    
    def createSubChannel[TPayload <: T](classifier: Message[TPayload] => Boolean)(implicit m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] = {
      def wrappedClassifier(message: Message[T]) = 
        if(m.erasure.isAssignableFrom(message.payload.getClass()))
       	  classifier(message.asInstanceOf[Message[TPayload]])
        else
      	  false
      val newDispatcher = MessageChannelActorHandler(None, actorSystem, timeout, futureDispatcher, actorDispatcherName)
      val subscription = this.<-*(msg => newDispatcher ! PublishMessageCommand(msg), wrappedClassifier)
      subscription.map { s => new ActorBasedMessageChannelImpl[TPayload](actorSystem, newDispatcher, timeout, futureDispatcher, actorDispatcherName, Some(s), topicPattern) }
    }

    def post[U <: T](message: Message[U]) = dispatcher ! PublishMessageCommand(message)

  }

  
}