package almhirt.messaging

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import almhirt._

sealed trait ChannelMessage
case class PostMessage(message: Message[AnyRef])
case class NewMessage(message: Message[AnyRef])
case class Subscribe(receiver: ActorRef, filter: Message[AnyRef] => Boolean)
case class Subscription(registration: AlmValidation[RegistrationHolder])
case class CreateSubChannel(name: String, filter: Message[AnyRef] => Boolean)
case class NewSubChannel(name: String, channel: ActorRef)

class MessageChannelActor extends Actor {
  private var almhirtsystem: Option[AlmhirtSystem] = None

  private var subscriptions = Vector.empty[(UUID, ActorRef, Message[AnyRef] => Boolean)]
  private var subChannels = Vector.empty[(UUID, ActorRef, Message[AnyRef] => Boolean)]

  def receive = {
    case PostMessage(message) =>
      subscriptions.filter(_._3(message)).foreach(_._2 ! NewMessage(message))
      subChannels.filter(_._3(message)).foreach(_._2 ! PostMessage(message))
    case Subscribe(subscriber, filter) =>
      val registrationToken = UUID.randomUUID
      val registration = new RegistrationUUID { val ticket = registrationToken; def dispose { self ! Unsubscribe(ticket) } }
      subscriptions = subscriptions :+ (registrationToken, subscriber, filter)
      sender ! Subscription(registration.success)
    case Unsubscribe(token) =>
      subscriptions = subscriptions.filterNot(_._1 == token)
    case UseAlmhirtSystemMessage(system) =>
      almhirtsystem = Some(system)
      subChannels.foreach(_._2 ! UseAlmhirtSystemMessage(system))
    case CreateSubChannel(name, filter) =>
      val registrationToken = UUID.randomUUID
      val registration = new RegistrationUUID { val ticket = registrationToken; def dispose { self ! UnsubscribeSubChannel(ticket) } }
      val actor =
        almhirtsystem match {
          case None => context.actorOf(Props[MessageChannelActor], name = name)
          case Some(sys) => 
            sys.messageStreamDispatcherName match {
              case None => context.actorOf(Props[MessageChannelActor], name = name)
              case Some(dn) => context.actorOf(Props[MessageChannelActor].withDispatcher(dn), name = name)
            }
        }
      almhirtsystem.foreach(sys => actor! UseAlmhirtSystemMessage(sys))
      subChannels = subChannels :+ (registrationToken, actor, filter)
      actor ! Subscription(registration.success)
      sender ! NewSubChannel(name, actor)
    case UnsubscribeSubChannel(token) =>
      subChannels = subChannels.filterNot(_._1 == token)
  }

  private case class Unsubscribe(token: UUID)
  private case class UnsubscribeSubChannel(token: UUID)
}