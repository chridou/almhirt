package almhirt.messaging

import scala.reflect.ClassTag

trait MessagePredicate extends Function1[Message[AnyRef], Boolean] {
  def apply(message: Message[AnyRef]): Boolean
}

object AlwaysTrueMessagePredicate extends MessagePredicate {
  def apply(message: Message[AnyRef]) = true
}

object MessagePredicate {
  def apply(isMatch: Message[AnyRef] => Boolean): MessagePredicate = new UntypedMatchingMessagePredicate(isMatch)
  def apply[TPayload <: AnyRef](implicit m: ClassTag[TPayload]): MessagePredicate = new TypeBasedMessagePredicate[TPayload]
  def apply[TPayload <: AnyRef](isMatch: Message[TPayload] => Boolean)(implicit m: ClassTag[TPayload]): MessagePredicate = new TypeBasedCustomMessagePredicate[TPayload](isMatch)
  def onPayload[TPayload <: AnyRef](isMatch: TPayload => Boolean)(implicit m: ClassTag[TPayload]): MessagePredicate = new TypeBasedPayloadCustomMessagePredicate[TPayload](isMatch)

  private class UntypedMatchingMessagePredicate(isMatch: Message[AnyRef] => Boolean) extends MessagePredicate {
    def apply(message: Message[AnyRef]) = isMatch(message)
  }

  private class TypeBasedMessagePredicate[TPayload <: AnyRef](implicit m: ClassTag[TPayload]) extends MessagePredicate {
    def apply(message: Message[AnyRef]) = {
      m.runtimeClass.isAssignableFrom(message.payload.getClass())
    }
  }

  private class TypeBasedCustomMessagePredicate[TPayload <: AnyRef](isMatch: Message[TPayload] => Boolean)(implicit m: ClassTag[TPayload]) extends MessagePredicate {
    def apply(message: Message[AnyRef]) = {
     if (m.runtimeClass.isAssignableFrom(message.payload.getClass()))
        isMatch(message.asInstanceOf[Message[TPayload]])
      else
        false
    }
  }

  private class TypeBasedPayloadCustomMessagePredicate[TPayload <: AnyRef](isMatch: TPayload => Boolean)(implicit m: ClassTag[TPayload]) extends MessagePredicate {
    def apply(message: Message[AnyRef]) = {
      if (m.runtimeClass.isAssignableFrom(message.payload.getClass()))
        isMatch(message.payload.asInstanceOf[TPayload])
      else
        false
    }
  }

}