package almhirt.messaging.impl

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import akka.dispatch.ExecutionContext
import almhirt._
import almhirt.almakka._
import almhirt.almfuture.all._
import almhirt.messaging._
import almhirt.messaging.impl.commands._

trait ActorBasedMessageHub extends MessageHub {
  implicit def timeout: Timeout
  implicit def futureDispatcher: ExecutionContext
  def dispatcher: ActorRef

  def broadcast(message: Message[AnyRef], topic: Option[String]) {
    dispatcher ! BroadcastMessageCommand(message, topic)
  }


}

object ActorBasedMessageHub {
  def apply(name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext, actorDispatcherName: Option[String]): ActorBasedMessageHub = {
    val newDispatcher = MessageHubActorHandler(name, actorSystem, timeout, futureDispatcher, actorDispatcherName)
    new ActorBasedMessageHubImpl(actorSystem, newDispatcher, timeout, futureDispatcher, actorDispatcherName)
  }
  
  def apply(name: Option[String], almAkkaContext: AlmAkkaContext): ActorBasedMessageHub = {
    apply(name, almAkkaContext.actorSystem, almAkkaContext.mediumDuration, almAkkaContext.futureDispatcher, almAkkaContext.messageHubDispatcherName)
  }
  
  private class ActorBasedMessageHubImpl(actorSystem: ActorRefFactory, val dispatcher: ActorRef, implicit val timeout: Timeout, implicit val futureDispatcher: ExecutionContext, actorDispatcherName: Option[String]) extends ActorBasedMessageHub{
    def createMessageChannel[TPayload <: AnyRef](name: Option[String], topic: Option[String])(implicit m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] = {
      val newChannelDispatcher = MessageChannelActorHandler(None, actorSystem, timeout, futureDispatcher, actorDispatcherName)
      val handler = (msg: Message[AnyRef]) => if(m.erasure.isAssignableFrom(msg.payload.getClass())) newChannelDispatcher ! PublishMessageCommand(msg)
      val registration = (ask(dispatcher, RegisterMessageHandlerOnTopicCommand(handler, topic))).toAlmFuture[RegistrationHolder]
      registration.map(reg => 
        ActorBasedMessageChannel[TPayload](name, actorSystem, timeout, futureDispatcher, actorDispatcherName, Some(reg), topic, newChannelDispatcher))	  
 	}
    def createGlobalMessageChannel[TPayload <: AnyRef](name: Option[String])(implicit m: Manifest[TPayload]): AlmFuture[MessageChannel[TPayload]] = {
      val newChannelDispatcher = MessageChannelActorHandler(None, actorSystem, timeout, futureDispatcher, actorDispatcherName)
      val handler = (msg: Message[AnyRef]) => if(m.erasure.isAssignableFrom(msg.payload.getClass())) newChannelDispatcher ! PublishMessageCommand(msg)
      val registration = (ask(dispatcher, RegisterMessageGlobalMessageHandlerCommand(handler))).toAlmFuture[RegistrationHolder]
      registration.map(reg => 
        ActorBasedMessageChannel[TPayload](name, actorSystem, timeout, futureDispatcher, actorDispatcherName, Some(reg), None, newChannelDispatcher))	  
 	}
    def close() {}
  }
  
  
  
}
