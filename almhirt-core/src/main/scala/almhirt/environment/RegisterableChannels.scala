package almhirt.environment

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import almhirt.messaging.MessageChannel
import almhirt.domain.DomainEvent
import almhirt.messaging.Message
import almhirt.commanding.CommandEnvelope
import almhirt.util.OperationState
import almhirt.common.Problem

trait CommandChannel extends MessageChannel[CommandEnvelope]
class CommandChannelWrapper(toWrap: MessageChannel[CommandEnvelope]) extends CommandChannel {
  def actor = toWrap.actor
  def <-* (handler: Message[CommandEnvelope] => Unit, classifier: Message[CommandEnvelope] => Boolean)(implicit atMost: FiniteDuration) = toWrap.<-*(handler)
  def post[U <: CommandEnvelope](message: Message[U]) = toWrap.post(message)
  def createSubChannel[U <: CommandEnvelope](name: String, classifier: Message[U] => Boolean)(implicit atMost: FiniteDuration, m: Manifest[U]) =
    toWrap.createSubChannel[U](name, classifier)
  def close = toWrap.close
}

trait DomainEventsChannel extends MessageChannel[DomainEvent]
class DomainEventsChannelWrapper(toWrap: MessageChannel[DomainEvent]) extends DomainEventsChannel {
  def actor = toWrap.actor
  def <-* (handler: Message[DomainEvent] => Unit, classifier: Message[DomainEvent] => Boolean)(implicit atMost: FiniteDuration) = toWrap.<-*(handler)
  def post[U <: DomainEvent](message: Message[U]) = toWrap.post(message)
  def createSubChannel[U <: DomainEvent](name: String, classifier: Message[U] => Boolean)(implicit atMost: FiniteDuration, m: Manifest[U]) =
    toWrap.createSubChannel[U](name, classifier)
  def close = toWrap.close
}

trait OperationStateChannel extends MessageChannel[OperationState]
class OperationStateChannelWrapper(toWrap: MessageChannel[OperationState]) extends OperationStateChannel {
  def actor = toWrap.actor
  def <-* (handler: Message[OperationState] => Unit, classifier: Message[OperationState] => Boolean)(implicit atMost: FiniteDuration) = toWrap.<-*(handler)
  def post[U <: OperationState](message: Message[U]) = toWrap.post(message)
  def createSubChannel[U <: OperationState](name: String, classifier: Message[U] => Boolean)(implicit atMost: FiniteDuration, m: Manifest[U]) =
    toWrap.createSubChannel[U](name, classifier)
  def close = toWrap.close
}

trait ProblemChannel extends MessageChannel[Problem]
class ProblemChannelWrapper(toWrap: MessageChannel[Problem]) extends ProblemChannel {
  def actor = toWrap.actor
  def <-* (handler: Message[Problem] => Unit, classifier: Message[Problem] => Boolean)(implicit atMost: FiniteDuration) = toWrap.<-*(handler)
  def post[U <: Problem](message: Message[U]) = toWrap.post(message)
  def createSubChannel[U <: Problem](name: String, classifier: Message[U] => Boolean)(implicit atMost: FiniteDuration, m: Manifest[U]) =
    toWrap.createSubChannel[U](name, classifier)
  def close = toWrap.close
}