package almhirt.messaging

import akka.actor.ActorRefFactory
import scala.concurrent.ExecutionContext
import almhirt.common._
import scala.reflect.ClassTag

trait ChannelRegistry {
  final def apply[T: ClassTag](): AlmValidation[MessageChannel[T]] = getChannel[T] 
  def addChannel[T](channel: MessageChannel[T])(implicit tag: ClassTag[T])
  def getChannel[T](implicit tag: ClassTag[T]): AlmValidation[MessageChannel[T]]
}

object ChannelRegistry {
  def apply(): ChannelRegistry =
    new impl.ChannelRegistryImpl()
}