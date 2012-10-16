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
import akka.dispatch.MessageDispatcher

trait MessageChannelActorHandler extends AlmActorLogging { actor: Actor => 
  implicit def timeout: Timeout
  implicit def futureDispatcher: ExecutionContext
  
  private var subscriptions: List[(UUID, Message[AnyRef] => Unit, Message[AnyRef] => Boolean)] = Nil
	  
  private def publishMessage(message: Message[AnyRef]) {
	subscriptions.view
	  .filter(_._3(message))
	  .foreach(_._2(message))
  }
	  
  private def addSubscription(id: UUID, handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean) {
	subscriptions = (id, handler, classifier) :: subscriptions
  }
	  	
  private def unsubscribeHandler(id: UUID) {
	subscriptions = subscriptions.filterNot(_._1 == id)
  }
  
  def receive: Receive = {
    case PublishMessageCommand(message) => 
  	  publishMessage(message)
  	case RegisterMessageHandlerCommand(handler, classifier) => {
   	  val subscriptionId = UUID.randomUUID()
 	  addSubscription(subscriptionId, handler, classifier)
  	  val subscription = 
  	    new Registration[UUID] {
  		  def dispose() = actor.self ! Unsubscribe(ticket)
  		  val ticket = subscriptionId
  		}
  	  sender ! (subscription.success[RegistrationProblem])
  	  }
  	case Unsubscribe(id) =>
     unsubscribeHandler(id)
  }

  private case class Unsubscribe(id: UUID)
	
  override def preStart() { log.info("ChannelDispatcher '%s' starting".format(actor.self.path)) } 
  override def postRestart(reason: Throwable) { log.info("ChannelDispatcher '%s' restarted".format(actor.self.path)) } 
  override def postStop() { log.info("ChannelDispatcher '%s' stopped".format(actor.self.path)) } 
}

object MessageChannelActorHandler {
  def apply(name: Option[String], actorSystem: ActorRefFactory, timeout: Timeout, futureDispatcher: ExecutionContext, actorDispatcherName: Option[String]): ActorRef = {
	(actorDispatcherName, name) match {
	  case (Some(dispatcherName), Some(name)) =>
	    actorSystem.actorOf(Props(new MessageChannelActorHandlerImpl(timeout, futureDispatcher)).withDispatcher(dispatcherName), name)
	  case (Some(dispatcherName), None) =>
	    actorSystem.actorOf(Props(new MessageChannelActorHandlerImpl(timeout, futureDispatcher)).withDispatcher(dispatcherName))
	  case (None, Some(name)) =>
	    actorSystem.actorOf(Props(new MessageChannelActorHandlerImpl(timeout, futureDispatcher)), name)
	  case (None, None) =>
	    actorSystem.actorOf(Props(new MessageChannelActorHandlerImpl(timeout, futureDispatcher)))
     }
  }

  def apply(name: Option[String], almAkkaContext: AlmAkkaContext): ActorRef =
    apply(name, almAkkaContext.actorSystem, almAkkaContext.mediumDuration, almAkkaContext.futureDispatcher, almAkkaContext.messageStreamDispatcherName)
  
  private class MessageChannelActorHandlerImpl(val timeout: Timeout, val futureDispatcher: ExecutionContext) extends Actor with MessageChannelActorHandler
}
