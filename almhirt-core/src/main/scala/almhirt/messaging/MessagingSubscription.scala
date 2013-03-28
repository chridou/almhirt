package almhirt.messaging

import scala.reflect.ClassTag

trait MessagingSubscription {
  def predicate: Message[AnyRef] => Boolean
  def handler: Message[AnyRef] => Unit
}

object MessagingSubscription {
  import akka.actor.ActorRef
  def forActor[TPayload <: AnyRef](actor: ActorRef)(implicit m: ClassTag[TPayload]): MessagingSubscription =
    new MessagingSubscription {
      val predicate = MessagePredicate[TPayload]
      val handler = (message: Message[AnyRef]) => actor ! (message.payload)
    }

  def forActorMapped[TPayload <: AnyRef, U](actor: ActorRef, map: TPayload => U)(implicit m: ClassTag[TPayload]): MessagingSubscription =
    new MessagingSubscription {
      val predicate = MessagePredicate[TPayload]
      val handler = (message: Message[AnyRef]) => actor ! (map(message.payload.asInstanceOf[TPayload]))
    }
  
  def forActorWithFilter[TPayload <: AnyRef](actor: ActorRef, filter: TPayload => Boolean)(implicit m: ClassTag[TPayload]): MessagingSubscription =
    new MessagingSubscription {
      val predicate = MessagePredicate.onPayload[TPayload](filter)
      val handler = (message: Message[AnyRef]) => actor ! (message.payload)
    }

  def forActorMappedWithFilter[TPayload <: AnyRef,U](actor: ActorRef, map: TPayload => U, filter: TPayload => Boolean)(implicit m: ClassTag[TPayload]): MessagingSubscription =
    new MessagingSubscription {
      val predicate = MessagePredicate.onPayload[TPayload](filter)
      val handler = (message: Message[AnyRef]) => actor ! (map(message.payload.asInstanceOf[TPayload]))
    }

  def typeBasedHandler[TPayload <: AnyRef](aHandler: TPayload => Unit)(implicit m: ClassTag[TPayload]): MessagingSubscription =
    new MessagingSubscription {
      val predicate = MessagePredicate[TPayload]
      val handler = (message: Message[AnyRef]) => aHandler(message.payload.asInstanceOf[TPayload])
    }

}
