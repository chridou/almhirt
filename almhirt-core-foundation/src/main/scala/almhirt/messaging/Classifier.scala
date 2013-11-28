package almhirt.messaging

import almhirt.common._

trait Classifies {
  def classifyUnsafe(message: Message): Boolean
}

trait Classifier[T] extends Classifies with Function2[MessageHeader, T, Boolean] {
  final def apply(header: MessageHeader, payload: T): Boolean = classify(header, payload)
  def classify(header: MessageHeader, payload: T): Boolean
  final override def classifyUnsafe(message: Message): Boolean = apply(message.header, message.payload.asInstanceOf[T])
}

object Classifier {
  def takeAll[T](): Classifier[T] =
    new Classifier[T] { def classify(header: MessageHeader, payload: T) = true }

  def payloadPredicate[T](pred: T => Boolean): Classifier[T] =
    new Classifier[T] { def classify(header: MessageHeader, payload: T) = pred(payload) }
  
  def forClass(clazz: Class[_]): Classifier[AnyRef] =
    new Classifier[AnyRef] { def classify(header: MessageHeader, payload: AnyRef) = clazz isAssignableFrom (payload.getClass()) }
}
