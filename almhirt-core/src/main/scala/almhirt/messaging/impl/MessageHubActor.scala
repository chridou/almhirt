package almhirt.messaging.impl

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.messaging._
import almhirt.environment._
import almhirt.environment.configuration._

class MessageHubActor(messageChannelsDispatcherName: Option[String]) extends Actor {
  private var subChannels = Vector.empty[(UUID, ActorRef, Message[AnyRef] => Boolean)]

  def receive = {
    case PostMessageCmd(message) =>
      subChannels.filter(_._3(message)).foreach(_._2 ! PostMessageCmd(message))
    case BroadcastMessageCmd(message) =>
      subChannels.filter(_._3(message)).foreach(_._2 ! PostMessageCmd(message))
    case CreateSubChannelQry(name, predicate) =>
      val registrationToken = UUID.randomUUID
      val registration = new RegistrationUUID { val ticket = registrationToken; def dispose { self ! UnsubscribeSubChannel(ticket) } }
      val actor =
        messageChannelsDispatcherName match {
          case None => context.actorOf(Props(new MessageChannelActor(messageChannelsDispatcherName)), name = name)
          case Some(dn) => context.actorOf(Props(new MessageChannelActor(messageChannelsDispatcherName)).withDispatcher(dn), name = name)
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