package almhirt.messaging

import java.util.UUID
import scala.reflect._
import scalaz.syntax.validation._
import akka.dispatch.Future
import akka.actor._
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.ask
import almhirt.validation.Problem
import almhirt.validation.AlmValidation
import almhirt.concurrent.AlmFuture
import almhirt.concurrent.AlmFuture._
import almhirt.almakka.AlmAkka

trait ChannelSubscription {
  /** Does not block. Will be unsubscribed some time in the future! **/
  def unsubscribe()
}

/** Someone who knows how to publish a message. */
trait CanPublish {
  def publish(message: Message[AnyRef])
}

/** Someone you can subscribe to for any Method. */
trait Subscribable {
  def subscribeAny(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean): AlmFuture[ChannelSubscription]

  def subscribeAny(handler: Message[AnyRef] => Unit): AlmFuture[ChannelSubscription] = 
  	subscribeAny(handler, (_: Message[AnyRef]) => true)

  def subscribe[TPayload <: AnyRef](handler: Message[TPayload] => Unit, classifier: Message[TPayload] => Boolean)(implicit m: Manifest[TPayload]): AlmFuture[ChannelSubscription] = {
    def wrappedHandler(message: Message[AnyRef]): Unit =
      handler(message.asInstanceOf[Message[TPayload]])
    def wrappedClassifier(message: Message[AnyRef]) = 
      if(m.erasure.isAssignableFrom(message.payload.getClass()))
      	classifier(message.asInstanceOf[Message[TPayload]])
      else
      	false
    subscribeAny(wrappedHandler, wrappedClassifier)
  }
	
  def subscribe[TPayload <: AnyRef](handler: Message[TPayload] => Unit)(implicit m: Manifest[TPayload]): AlmFuture[ChannelSubscription] = 
  	subscribe[TPayload](handler, (_: Message[TPayload]) => true)(m)

}


/** Publishes messages to its subscribers. 
 * This is the weakest contract a channel must fulfill:
 * A Channel does
 * * guarantee that all message have been published by someone
 * * not guarantee that messages arrive in the same order as they were published  
 * * not guarantee that all messages will be published
 * * not guarantee that all handlers will be called on the same thread
 * * not guarantee that handlers won't be called concurrently
 */
trait Channel extends CanPublish with Subscribable
case class SubscribeToChannel(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean)
case class Publish(msg: Message[AnyRef])

class ActorChannelDispatcher() extends Actor with ActorLogging {
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
  	  case Publish(message) => 
  		publishMessage(message)
  	  case SubscribeToChannel(handler, classifier) => {
   	    val subscriptionId = UUID.randomUUID()
 		addSubscription(subscriptionId, handler, classifier)
  	    val subscription = 
  	      new ChannelSubscription {
  		    def unsubscribe() = self ! Unsubscribe(subscriptionId)
  		  }
  		sender ! (subscription.success[Problem])
  		}
  	  case Unsubscribe(id) =>
  		unsubscribeHandler(id)
  	}

    private case class Unsubscribe(id: UUID)
	
    override def preStart() { log.info("Channel '%s' starting".format(self.path)) } 
    override def postRestart(reason: Throwable) { log.info("Channel '%s' restarted".format(self.path)) } 
    override def postStop() { log.info("Channel '%s'".format(self.path)) } 
  }


object Channel extends AlmAkka {
  def apply(name: String, createActor: (Props, String) => ActorRef): Channel = 
  	new ChannelImpl(createActor(Props(new ActorChannelDispatcher()), name))
	
  def apply(name: String)(implicit actorFactory: ActorRefFactory): Channel =
  	apply(name, (p: Props, n: String) => actorFactory.actorOf(p, n))
  	
  def apply(actorFactory: ActorRefFactory): Channel =
  	apply(java.util.UUID.randomUUID().toString)(actorFactory)

  def apply(aChannelDispatcher: ActorRef): Channel =
  	new ChannelImpl(aChannelDispatcher)
  
  def apply(): Channel =
  	new ChannelImpl(
  	    defaultActorSystem.actorOf(
  	        Props(new ActorChannelDispatcher()).withDispatcher("almhirt.almhirt-channel")
  	))

  private class ChannelImpl(dispatcher: ActorRef) extends Channel with AlmAkka{
	implicit val timeout = Timeout(defaultTimeout)
	
    def subscribeAny(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean): AlmFuture[ChannelSubscription] = {
	  ask(dispatcher, SubscribeToChannel(handler, classifier)).mapToAlm[ChannelSubscription]
    }
	
	def publish(message: Message[AnyRef]){ dispatcher ! Publish(message) }
  }	
}