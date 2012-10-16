package almhirt.messaging.impl

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.Timeout
import akka.dispatch.ExecutionContext
import almhirt._
import almhirt.messaging._
import almhirt.messaging.impl.commands._
import almhirt.almakka._
import almhirt.almfuture.all._

trait MessageHubActorHandler extends AlmActorLogging{ actor: Actor =>
  implicit def timeout: Timeout
  implicit def futureDispatcher: ExecutionContext

  private var subscriptions: List[(UUID, Message[AnyRef] => Unit, Option[String])] = Nil

  private def addSubscription(id: UUID, handler: Message[AnyRef] => Unit, topic: Option[String]) {
	subscriptions = (id, handler, topic) :: subscriptions
  }
  
  private def unsubscribeHandler(id: UUID) {
	subscriptions = subscriptions.filterNot(_._1 == id)
  }
  
  private def registerHandlerOnTopic(handler: Message[AnyRef] => Unit, topicFilter: Option[String]): AlmValidation[RegistrationHolder] = {
    val registration = 
	  new RegistrationUUID {
	    val ticket = UUID.randomUUID
	    def dispose() { actor.self ! Unregister(ticket)} }
      addSubscription(registration.ticket, handler, topicFilter)
    registration.success
  }

  private def registeredGlobalHandler(handler: Message[AnyRef] => Unit): AlmValidation[RegistrationHolder] = {
    val registration = 
	  new RegistrationUUID {
	    val ticket = UUID.randomUUID
	    def dispose() { actor.self ! Unregister(ticket)} }
      addSubscription(registration.ticket, handler, None)
    registration.success
  }

  private def broadcastMessage(message: Message[AnyRef], topic: Option[String]) {
	subscriptions.view
	  .foreach(_._2(message))
  }
  
  def receive: Receive = {
    case BroadcastMessageCommand(msg, pattern) => 
      broadcastMessage(msg, pattern)
    case RegisterMessageGlobalMessageHandlerCommand(handler) =>
      sender ! registeredGlobalHandler(handler)
    case RegisterMessageHandlerOnTopicCommand(handler, topicfilter) =>
      sender ! registerHandlerOnTopic(handler, topicfilter)
    case Unregister(id) =>
      unsubscribeHandler(id)
  }
  
  private case class Unregister(id: UUID)
  
  override def preStart() { log.info("MessageHub '%s' starting".format(actor.self.path)) } 
  override def postRestart(reason: Throwable) { log.info("MessageHub '%s' restarted".format(actor.self.path)) } 
  override def postStop() { log.info("MessageHub '%s' stopped".format(actor.self.path)) } 
}

object MessageHubActorHandler {
  def apply(name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext, actorDispatcherName: Option[String]): ActorRef = {
	(actorDispatcherName, name) match {
	  case (Some(dispatcherName), Some(name)) =>
	    actorSystem.actorOf(Props(new MessageHubActorHandlerImpl(timeout, futureDispatcher)).withDispatcher(dispatcherName), name)
	  case (Some(dispatcherName), None) =>
	    actorSystem.actorOf(Props(new MessageHubActorHandlerImpl(timeout, futureDispatcher)).withDispatcher(dispatcherName))
	  case (None, Some(name)) =>
	    actorSystem.actorOf(Props(new MessageHubActorHandlerImpl(timeout, futureDispatcher)), name)
	  case (None, None) =>
	    actorSystem.actorOf(Props(new MessageHubActorHandlerImpl(timeout, futureDispatcher)))
     }
  }

  def apply(name: Option[String], almAkkaContext: AlmAkkaContext): ActorRef =
    apply(name, almAkkaContext.actorSystem, almAkkaContext.mediumDuration, almAkkaContext.futureDispatcher, almAkkaContext.messageHubDispatcherName)
  
  private class MessageHubActorHandlerImpl(val timeout: Timeout, val futureDispatcher: ExecutionContext) extends Actor with MessageHubActorHandler
  
}