package almhirt.messaging

import almhirt.common._
import akka.actor.ActorRef


trait MessageStream {
  type Message
  type Subscriber
  type Classifier
  
  
  def subscribe(subscriber: Subscriber, classifier: Classifier): AlmFuture[Subscription]
  def publish(message: Message)
}

trait ActorMessageStream extends MessageStream { 
  type Subscriber = ActorRef
  
}