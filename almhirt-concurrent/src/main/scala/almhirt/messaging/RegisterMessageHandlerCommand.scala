package almhirt.messaging

case class RegisterMessageHandlerCommand(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean)