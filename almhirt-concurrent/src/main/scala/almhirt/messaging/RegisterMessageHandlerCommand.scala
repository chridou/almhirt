package almhirt.messaging

case class RegisterWildCardMessageHandlerCommand(handler: Message[AnyRef] => Unit, classifier: Message[AnyRef] => Boolean)