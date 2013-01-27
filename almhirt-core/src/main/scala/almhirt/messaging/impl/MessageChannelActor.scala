package almhirt.messaging.impl

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.messaging._
import almhirt.environment._
import almhirt.environment.configuration._
class MessageChannelActor(messageChannelDispatcherName: Option[String]) extends Actor {

  private var subscriptions = Vector.empty[(UUID, MessagingSubscription)]
  private var subChannels = Vector.empty[(UUID, ActorRef, Message[AnyRef] => Boolean)]

  def receive = {
    case PostMessageCmd(message) =>
      subscriptions.filter(_._2.predicate(message)).foreach(x => { x._2.handler(message) })
      subChannels.filter(_._3(message)).foreach(_._2 ! PostMessageCmd(message))
    case SubscribeQry(subscription) =>
      val registrationToken = UUID.randomUUID
      val registration = new RegistrationUUID { val ticket = registrationToken; def dispose { self ! Unsubscribe(ticket) } }
      subscriptions = subscriptions :+ (registrationToken, subscription)
      sender ! SubscriptionRsp(registration.success)
    case Unsubscribe(token) =>
      subscriptions = subscriptions.filterNot(_._1 == token)
    case CreateSubChannelQry(name, predicate) =>
      val registrationToken = UUID.randomUUID
      val registration = new RegistrationUUID { val ticket = registrationToken; def dispose { self ! UnsubscribeSubChannel(ticket) } }
      val actor =
        messageChannelDispatcherName match {
          case None => context.actorOf(Props(new MessageChannelActor(messageChannelDispatcherName)), name = name)
          case Some(dn) => context.actorOf(Props(new MessageChannelActor(messageChannelDispatcherName)).withDispatcher(dn), name = name)
        }
      subChannels = subChannels :+ (registrationToken, actor, predicate)
      actor ! SubscriptionRsp(registration.success)
      sender ! NewSubChannelRsp(name, actor.success)
    case UnsubscribeSubChannel(token) =>
      subChannels = subChannels.filterNot(_._1 == token)
  }

  private case class Unsubscribe(token: UUID)
  private case class UnsubscribeSubChannel(token: UUID)
}