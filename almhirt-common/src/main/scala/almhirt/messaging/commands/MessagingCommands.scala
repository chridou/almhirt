package almhirt.messaging.commands

import almhirt.messaging._

// Request creation of a message stream Filter = None means: Subscribe only to those messages that don't have a topic att all
case class BroadcastMessageCommand(msg: Message[AnyRef], topicFilter: Option[String])

case class PublishMessageCommand(msg: Message[AnyRef])

case class RegisterWildCardMessageHandlerCommand(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean)
case class RegisterMessageHandlerOnTopicCommand(handler: Message[AnyRef] => Unit, topicfilter: Option[String])
case class RegisterMessageGlobalMessageHandlerCommand(handler: Message[AnyRef] => Unit)

