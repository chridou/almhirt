package almhirt.messaging

trait MessageHandler extends Function1[Message[AnyRef],Unit]