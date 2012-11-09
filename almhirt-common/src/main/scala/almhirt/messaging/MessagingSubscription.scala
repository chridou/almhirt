package almhirt.messaging

trait MessagingSubscription {
  def predicate: Message[AnyRef] => Boolean
  def handler: Message[AnyRef] => Unit
}

object MessagingSubscription {
  import akka.actor.ActorRef
  def forActor[TPayload <: AnyRef](actor: ActorRef)(implicit m: Manifest[TPayload]): MessagingSubscription =
    new MessagingSubscription {
      val predicate = MessagePredicate[TPayload]
      val handler = (message: Message[AnyRef]) => actor ! (message.payload)
    }
}
