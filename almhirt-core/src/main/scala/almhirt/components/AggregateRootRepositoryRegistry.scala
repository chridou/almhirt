package almhirt.components

import scala.reflect.ClassTag
import akka.actor._
import almhirt.common._
import almhirt.domain.AggregateRoot

trait AggregateRootRepositoryRegistry {
  def register(arType: Class[_ <: AggregateRoot[_, _]], repository: ActorRef)
  def get(arType: Class[_ <: AggregateRoot[_, _]]): AlmValidation[ActorRef]
}

object AggregateRootRepositoryRegistry {
  def apply(): AggregateRootRepositoryRegistry =
    new impl.AggregateRootRepositoryRegistryImpl()

  def apply(repos: Map[Class[_ <: AggregateRoot[_, _]], ActorRef]): AggregateRootRepositoryRegistry = {
    val newReg = AggregateRootRepositoryRegistry()
    repos.toSeq.foreach(mapping => newReg.register(mapping._1, mapping._2))
    newReg
  }

  implicit class AggregateRootRepositoryRegistryOps(self: AggregateRootRepositoryRegistry) {
    def registerTyped[T <: AggregateRoot[_, _]](repository: ActorRef)(implicit tag: ClassTag[T]) {
      self.register(tag.runtimeClass.asInstanceOf[Class[_ <: AggregateRoot[_, _]]], repository)
    }
    def getTyped[T <: AggregateRoot[_, _]](implicit tag: ClassTag[T]): AlmValidation[ActorRef] = {
      self.get(tag.runtimeClass.asInstanceOf[Class[_ <: AggregateRoot[_, _]]])
    }
  }
}