package almhirt.messaging.impl.commands

import almhirt.messaging.Message

// Request creation of a message stream Filter = None means: Subscribe only to those messages that don't have a topic att all
case class BroadcastMessageCommand(msg: Message[AnyRef], topicFilter: Option[String])

case class PublishMessageCommand(msg: Message[AnyRef])

case class RegisterMessageHandlerCommand[T <: AnyRef](handler: Message[T] => Unit, classifier: Message[T] => Boolean)
case class RegisterMessageHandlerOnTopicCommand(handler: Message[AnyRef] => Unit, topicfilter: Option[String])
case class RegisterMessageGlobalMessageHandlerCommand(handler: Message[AnyRef] => Unit)

