package almhirt.environment

import scala.reflect.ClassTag
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import almhirt.common.Problem
import almhirt.core.Event
import almhirt.messaging.MessageChannel
import almhirt.domain.DomainEvent
import almhirt.messaging.Message
import almhirt.commanding.CommandEnvelope
import almhirt.util.OperationState

trait HasStandardChannels {
  def commandChannel: CommandChannel
  def eventsChannel: EventsChannel
  def domainEventsChannel: DomainEventsChannel
}

trait CommandChannel extends MessageChannel[CommandEnvelope]
class CommandChannelWrapper(toWrap: MessageChannel[CommandEnvelope]) extends CommandChannel {
  def actor = toWrap.actor
  def <-* (handler: Message[CommandEnvelope] => Unit, classifier: Message[CommandEnvelope] => Boolean)(implicit atMost: FiniteDuration) = toWrap.<-*(handler)
  def post[U <: CommandEnvelope](message: Message[U]) = { toWrap.post(message) }
  def createSubChannel[U <: CommandEnvelope](name: String, classifier: Message[U] => Boolean)(implicit atMost: FiniteDuration, m: ClassTag[U]) =
    toWrap.createSubChannel[U](name, classifier)
  def close = toWrap.close
}

trait EventsChannel extends MessageChannel[Event]
class EventsChannelWrapper(toWrap: MessageChannel[Event]) extends EventsChannel {
  def actor = toWrap.actor
  def <-* (handler: Message[Event] => Unit, classifier: Message[Event] => Boolean)(implicit atMost: FiniteDuration) = toWrap.<-*(handler)
  def post[U <: Event](message: Message[U]) = toWrap.post(message)
  def createSubChannel[U <: Event](name: String, classifier: Message[U] => Boolean)(implicit atMost: FiniteDuration, m: ClassTag[U]) =
    toWrap.createSubChannel[U](name, classifier)
  def close = toWrap.close
}

trait DomainEventsChannel extends MessageChannel[DomainEvent]
class DomainEventsChannelWrapper(toWrap: MessageChannel[DomainEvent]) extends DomainEventsChannel {
  def actor = toWrap.actor
  def <-* (handler: Message[DomainEvent] => Unit, classifier: Message[DomainEvent] => Boolean)(implicit atMost: FiniteDuration) = toWrap.<-*(handler)
  def post[U <: DomainEvent](message: Message[U]) = toWrap.post(message)
  def createSubChannel[U <: DomainEvent](name: String, classifier: Message[U] => Boolean)(implicit atMost: FiniteDuration, m: ClassTag[U]) =
    toWrap.createSubChannel[U](name, classifier)
  def close = toWrap.close
}
