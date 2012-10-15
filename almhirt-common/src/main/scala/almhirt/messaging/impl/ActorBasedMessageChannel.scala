package almhirt.messaging.impl

import java.util.UUID
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import akka.dispatch._
import almhirt._
import almhirt.messaging._
import almhirt.messaging.commands._
import almakka.{ AlmAkkaContext }
import almvalidation.kit._
import almfuture.all._

trait ActorBasedMessageChannel extends MessageChannel {
  implicit def timeout: Timeout
  implicit def futureDispatcher: ExecutionContext
  def dispatcher: ActorRef

  def <-*(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean): AlmFuture[RegistrationHolder] = {
    (dispatcher ? RegisterWildCardMessageHandlerCommand(handler, classifier)).toAlmFuture[RegistrationHolder]
  }

  def post(message: Message[AnyRef]) = dispatcher ! PublishMessageCommand(message)

}

object ActorBasedMessageChannel {

  def apply(name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext, actorDispatcherName: Option[String], registration: Option[RegistrationHolder], topicPattern: Option[String]): ActorBasedMessageChannel = {
    val newDispatcher = MessageChannelActorHandler(name, actorSystem, timeout, futureDispatcher, actorDispatcherName)
    new ActorBasedMessageChannelImpl(actorSystem, newDispatcher, timeout, futureDispatcher, actorDispatcherName, registration, topicPattern)
  }

  def apply(name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext): ActorBasedMessageChannel = {
    val newDispatcher = MessageChannelActorHandler(name, actorSystem, timeout, futureDispatcher, None)
    new ActorBasedMessageChannelImpl(actorSystem, newDispatcher, timeout, futureDispatcher, None, None, None)
  }

  def apply(name: Option[String], almAkkaContext: AlmAkkaContext, registration: Option[RegistrationHolder], topicPattern: Option[String]): ActorBasedMessageChannel = {
    val newDispatcher = MessageChannelActorHandler(name, almAkkaContext)
    new ActorBasedMessageChannelImpl(almAkkaContext.actorSystem, newDispatcher, almAkkaContext.mediumDuration, almAkkaContext.futureDispatcher, almAkkaContext.messageStreamDispatcherName, registration, topicPattern)
  }

  def apply(name: Option[String], almAkkaContext: AlmAkkaContext): ActorBasedMessageChannel = {
    apply(name, almAkkaContext, None, None)
  }

  private[impl] def apply(name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext, actorDispatcherName: Option[String], registration: Option[RegistrationHolder], topicPattern: Option[String], channelHandler: ActorRef): ActorBasedMessageChannel = {
    new ActorBasedMessageChannelImpl(actorSystem, channelHandler, timeout, futureDispatcher, actorDispatcherName, registration, topicPattern)
  }

  class ActorBasedMessageChannelImpl(actorSystem: ActorRefFactory, val dispatcher: ActorRef, val timeout: Timeout, val futureDispatcher: ExecutionContext, actorDispatcherName: Option[String], val registration: Option[RegistrationHolder], val topicPattern: Option[String]) extends ActorBasedMessageChannel {
    def createSubChannel(classifier: Message[AnyRef] => Boolean): AlmFuture[MessageChannel] = {
      val newDispatcher = MessageChannelActorHandler(None, actorSystem, timeout, futureDispatcher, actorDispatcherName)
      val subscription = this.<-*(msg => newDispatcher ! PublishMessageCommand(msg), classifier)
      subscription.map { s => new ActorBasedMessageChannelImpl(actorSystem, newDispatcher, timeout, futureDispatcher, actorDispatcherName, Some(s), topicPattern) }
    }
  }

}