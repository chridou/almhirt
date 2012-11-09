package almhirt.messaging

import akka.actor.ActorRef
import almhirt._

sealed trait MessagingMessage
case class BroadcastMessage(message: Message[AnyRef]) extends MessagingMessage
case class PostMessage(message: Message[AnyRef]) extends MessagingMessage
case class Subscribe(subscription: MessagingSubscription) extends MessagingMessage
case class Subscription(registration: AlmValidation[RegistrationHolder]) extends MessagingMessage
case class CreateSubChannel(name: String, predicate: Message[AnyRef] => Boolean) extends MessagingMessage
case class NewSubChannel(name: String, channel: AlmValidation[ActorRef]) extends MessagingMessage