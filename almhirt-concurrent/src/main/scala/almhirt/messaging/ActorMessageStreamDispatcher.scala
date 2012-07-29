package almhirt.messaging

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor.Actor
import almhirt._
import validation.Problem._
import almakka._
import concurrent._
import AlmFuture._


class ActorMessageStreamDispatcher extends Actor with AlmActorLogging {
  
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

	def receive = {
  	  case PublishMessageCommand(message) => 
  		publishMessage(message)
  	  case RegisterMessageHandlerCommand(handler, classifier) => {
   	    val subscriptionId = UUID.randomUUID()
 		addSubscription(subscriptionId, handler, classifier)
  	    val subscription = 
  	      new Disposable {
  		    def dispose() = self ! Unsubscribe(subscriptionId)
  		  }
  		sender ! (subscription.success[RegistrationProblem])
  		}
  	  case Unsubscribe(id) =>
  		unsubscribeHandler(id)
  	}

    private case class Unsubscribe(id: UUID)
	
    override def preStart() { log.info("StreamDispatcher '%s' starting".format(self.path)) } 
    override def postRestart(reason: Throwable) { log.info("StreamDispatcher '%s' restarted".format(self.path)) } 
    override def postStop() { log.info("StreamDispatcher '%s' stopped".format(self.path)) } 
}
