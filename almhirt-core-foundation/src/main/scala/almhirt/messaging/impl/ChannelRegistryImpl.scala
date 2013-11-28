package almhirt.messaging.impl

import scala.language.existentials
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.Timeout

import almhirt.common._
import almhirt.messaging._

class ChannelRegistryImpl extends ChannelRegistry {
  private val channels = new java.util.concurrent.ConcurrentHashMap[Class[_], MessageChannel[_]](128)
  
  final override def getChannel[T](implicit tag: ClassTag[T]): AlmValidation[MessageChannel[T]] =
    channels.get(tag.runtimeClass) match {
      case null => NoSuchElementProblem(s"""No channel found for "${tag.runtimeClass.getName()}"""").failure
      case channel => channel.asInstanceOf[MessageChannel[T]].success
    }

  final override def addChannel[T](channel: MessageChannel[T])(implicit tag: ClassTag[T]) {
    channels.put(tag.runtimeClass, channel)
    
  }
}