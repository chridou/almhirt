package almhirt.messaging.impl

import scala.language.existentials
import scala.reflect.ClassTag
import almhirt.messaging._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext

object ActorSystemEventStreamMessageBus {
  def apply(system: ActorSystem): AlmFuture[(MessageBus, CloseHandle)] = {
    implicit val executionContext = system.dispatcher
    val spawnTimeout = FiniteDuration(1, "s")
    val akkaEventStream = system.eventStream
    val supervisor = system.actorOf(Props[SupervisorActor], "MessageBusSupervisor")
    (supervisor ? supervisorMessages.CreateMessageDispatcherActor)(spawnTimeout).successfulAlmFuture[ActorRef].flatMap { dispatcherActor =>
      dispatcherActor ! messageDispatcherMessages.SubscribeToAkkaStream(akkaEventStream)
      val bus = new MessageBus {
        def subscribe(subscriber: ActorRef): AlmFuture[Subscription] = {
          (dispatcherActor ? messageDispatcherMessages.Subscribe(subscriber, Classifier.takeAll[AnyRef]))(spawnTimeout).successfulAlmFuture[Subscription]
        }

        def subscribe(subscriber: ActorRef, classifier: Classifier[AnyRef]): AlmFuture[Subscription] = {
          (dispatcherActor ? messageDispatcherMessages.Subscribe(subscriber, classifier))(spawnTimeout).successfulAlmFuture[Subscription]
        }

        def publishMessage(message: Message) { akkaEventStream.publish(message) }

        def channel[T <: AnyRef](implicit tag: ClassTag[T]): AlmFuture[MessageStream[T]] =
          (supervisor ? supervisorMessages.CreateActorStreamActor)(spawnTimeout).successfulAlmFuture[ActorRef].flatMap { streamActor =>
            (dispatcherActor ? messageDispatcherMessages.Subscribe(streamActor, Classifier.forClass(tag.runtimeClass)))(spawnTimeout).successfulAlmFuture[Subscription].map { subscription =>
              streamActor ! streamActorMessages.TakeSubscription(subscription)
              createMessageStream[T](streamActor, spawnTimeout)
            }
          }
        def channel[T <: AnyRef](classifier: Classifier[T])(implicit tag: ClassTag[T]): AlmFuture[MessageStream[T]] = {
          val preClassifier = Classifier.forClass(tag.runtimeClass)
          val composedClassifier =
            new Classifier[AnyRef] {
              def classify(header: MessageHeader, payload: AnyRef) = preClassifier.classify(header, payload) && classifier.classify(header, payload.asInstanceOf[T])
            }
          (supervisor ? supervisorMessages.CreateActorStreamActor)(spawnTimeout).successfulAlmFuture[ActorRef].flatMap { streamActor =>
            (dispatcherActor ? messageDispatcherMessages.Subscribe(streamActor, composedClassifier))(spawnTimeout).successfulAlmFuture[Subscription].map { subscription =>
              streamActor ! streamActorMessages.TakeSubscription(subscription)
              createMessageStream[T](streamActor, spawnTimeout)
            }
          }
        }
      }
      AlmFuture.successful(bus, new CloseHandle { def close() { supervisor ! supervisorMessages.CloseMessaging } })
    }
  }

  private object supervisorMessages {
    case object CreateMessageDispatcherActor
    case object CreateActorStreamActor
    case object CloseMessaging
  }
  private class SupervisorActor extends Actor {
    import supervisorMessages._
    override def receive: Receive = {
      case CreateActorStreamActor =>
        sender ! context.actorOf(Props(new StreamActor()))
      case CreateMessageDispatcherActor =>
        sender ! context.actorOf(Props[MessageDispatcherActor])
      case CloseMessaging =>
        context.stop(self)
    }
  }

  private object messageDispatcherMessages {
    case class SubscribeToAkkaStream(stream: akka.event.EventStream)
    case object UnsubscribeFromAkkaStream
    case class Subscribe(subscriber: ActorRef, classifier: Classifier[AnyRef])
    case class Unsubscribe(subscriber: ActorRef)
  }

  private class MessageDispatcherActor extends Actor {
    import messageDispatcherMessages._

    var subscribers = Vector.empty[(ActorRef, Classifier[AnyRef])]

    private def unsubscribed: Receive = {
      case SubscribeToAkkaStream(stream) =>
        stream.subscribe(self, classOf[Message])
        context.become(subscribed(stream))
    }

    private def subscribed(subscribedTo: akka.event.EventStream): Receive = {
      case msg @ Message(header, payload) =>
        subscribers.filter(x => x._2.classify(header, payload)).map(_._1).foreach(_ ! msg)
      case UnsubscribeFromAkkaStream =>
        subscribedTo.unsubscribe(self)
        context.become(unsubscribed)
      case Subscribe(subscriber, classifier) =>
        subscribers = subscribers :+ (subscriber, classifier)
        sender ! (new Subscription { def cancel { self ! Unsubscribe(subscriber) } })
      case Unsubscribe(subscriber) =>
        subscribers = subscribers.filterNot(_._1 == subscriber)
    }

    override def receive: Receive = unsubscribed
  }

  private object streamActorMessages {
    case class TakeSubscription(subscription: Subscription)
    case class Subscribe(subscriber: ActorRef, classifier: Classifies)
    case class Unsubscribe(subscriber: ActorRef)
    case class CreateChannel(classifier: Classifies)
    case class UnsubscribeChannel(channel: ActorRef)
    case object UnsubscribeSelf
    case object CreateActorStreamActor
  }

  private object AlwaysTrueClassifier extends Classifies {
    def classifyUnsafe(what: Message) = true
  }

  private class StreamActor extends Actor {
    import streamActorMessages._
    var subscribers = Vector.empty[(ActorRef, Classifies)]
    var channels = Vector.empty[(ActorRef, Classifies)]

    private def unsubscribed: Receive = {
      case TakeSubscription(subscription) =>
        context.become(subscribed(subscription))
    }

    private def subscribed(subscription: Subscription): Receive = {
      case msg @ Message(header, payload) =>
        subscribers.filter(x => x._2.classifyUnsafe(msg)).map(_._1).foreach(_ ! msg.payload)
        channels.filter(x => x._2.classifyUnsafe(msg)).map(_._1).foreach(_ ! msg)
      case Subscribe(subscriber, classifier) =>
        subscribers = subscribers :+ (subscriber, classifier)
        sender ! (new Subscription { def cancel { self ! Unsubscribe(subscriber) } })
      case CreateChannel(classifier) =>
        val channel = context.actorOf(Props[StreamActor])
        channel ! TakeSubscription(new Subscription { def cancel { self ! UnsubscribeChannel(channel) } })
        sender ! channel
        channels = channels :+ (channel, classifier)
      case UnsubscribeChannel(channel) =>
        channels = channels.filterNot(_._1 == channel)
      case TakeSubscription(subscription) =>
        context.become(subscribed(subscription))
      case UnsubscribeSelf =>
        subscription.cancel
        context.become(unsubscribed)
      case CreateActorStreamActor =>
        sender ! context.actorOf(Props(new StreamActor()))
    }

    override def receive: Receive = unsubscribed
  }

  private def createMessageStream[T <: AnyRef](streamActor: ActorRef, spawnTimeout: FiniteDuration)(implicit executionContext: ExecutionContext): MessageStream[T] = {
    import streamActorMessages._
    new MessageStream[T] {
      def subscribe(subscriber: ActorRef): AlmFuture[Subscription] =
        (streamActor ? Subscribe(subscriber, AlwaysTrueClassifier))(spawnTimeout).successfulAlmFuture[Subscription]
      def subscribe(subscriber: ActorRef, classifier: Classifier[T]): AlmFuture[Subscription] =
        (streamActor ? Subscribe(subscriber, classifier))(spawnTimeout).successfulAlmFuture[Subscription]
      def channel[U <: T](implicit tag: ClassTag[U]): AlmFuture[MessageStream[U]] =
        (streamActor ? streamActorMessages.CreateChannel(Classifier.forClass(tag.runtimeClass)))(spawnTimeout).successfulAlmFuture[ActorRef].map { childStreamActor =>
          createMessageStream[U](childStreamActor, spawnTimeout)
        }
      def channel[U <: T](classifier: Classifier[U])(implicit tag: ClassTag[U]): AlmFuture[MessageStream[U]] = {
        val preClassifier = Classifier.forClass(tag.runtimeClass)
        val composedClassifier =
          new Classifier[T] {
            def classify(header: MessageHeader, payload: T) = preClassifier.classify(header, payload) && classifier.classify(header, payload.asInstanceOf[U])
          }
        (streamActor ? streamActorMessages.CreateActorStreamActor)(spawnTimeout).successfulAlmFuture[ActorRef].flatMap { childStreamActor =>
          (streamActor ? streamActorMessages.CreateChannel(composedClassifier))(spawnTimeout).successfulAlmFuture[ActorRef].map { childStreamActor =>
            createMessageStream[U](childStreamActor, spawnTimeout)
          }
        }
      }
    }
  }
}