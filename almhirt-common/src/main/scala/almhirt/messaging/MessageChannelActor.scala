package almhirt.messaging

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import almhirt._

class MessageChannelActor extends Actor {
  private var almhirtsystem: Option[AlmhirtSystem] = None

  private var subscriptions = Vector.empty[(UUID, MessagingSubscription)]
  private var subChannels = Vector.empty[(UUID, ActorRef, Message[AnyRef] => Boolean)]

  def receive = {
    case PostMessage(message) =>
      subscriptions.filter(_._2.predicate(message)).foreach(_._2.handler(message))
      subChannels.filter(_._3(message)).foreach(_._2 ! PostMessage(message))
    case Subscribe(subscription) =>
      val registrationToken = UUID.randomUUID
      val registration = new RegistrationUUID { val ticket = registrationToken; def dispose { self ! Unsubscribe(ticket) } }
      subscriptions = subscriptions :+ (registrationToken, subscription)
      sender ! registration.success
    case Unsubscribe(token) =>
      subscriptions = subscriptions.filterNot(_._1 == token)
    case CreateSubChannel(name, predicate) =>
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
      almhirtsystem.foreach(sys => actor ! UseAlmhirtSystemMessage(sys))
      subChannels = subChannels :+ (registrationToken, actor, predicate)
      actor ! Subscription(registration.success)
      sender ! NewSubChannel(name, actor.success)
    case UnsubscribeSubChannel(token) =>
      subChannels = subChannels.filterNot(_._1 == token)
    case UseAlmhirtSystemMessage(system) =>
      almhirtsystem = Some(system)
      subChannels.foreach(_._2 ! UseAlmhirtSystemMessage(system))

  }

  private case class Unsubscribe(token: UUID)
  private case class UnsubscribeSubChannel(token: UUID)
}