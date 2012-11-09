package almhirt.messaging

import akka.actor.ActorRef
import almhirt._

sealed trait MessagingMsg

sealed trait MessagingCmd extends MessagingMsg

case class BroadcastMessageCmd(message: Message[AnyRef]) extends MessagingCmd
case class PostMessageCmd(message: Message[AnyRef]) extends MessagingCmd
case class SubscribeCmd(subscription: MessagingSubscription) extends MessagingCmd
case class CreateSubChannelCmd(name: String, predicate: Message[AnyRef] => Boolean) extends MessagingCmd

sealed trait MessagingRsp extends MessagingMsg

case class SubscriptionRsp(registration: AlmValidation[RegistrationHolder]) extends MessagingRsp
case class NewSubChannelRsp(name: String, channel: AlmValidation[ActorRef]) extends MessagingRsp