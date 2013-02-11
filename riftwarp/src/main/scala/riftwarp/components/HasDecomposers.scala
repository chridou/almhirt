package riftwarp.components

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait HasDecomposers {
  def getRawDecomposer(riftDescriptor: RiftDescriptor): AlmValidation[RawDecomposer]

  def addDecomposer(decomposer: Decomposer[_ <: AnyRef]): Unit
}

object HasDecomposers {
  implicit class HasDecomposersOps(self: HasDecomposers) {
    def getRawDecomposerFor(what: AnyRef, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[RawDecomposer] = {
      val rd = what match {
        case htd: HasRiftDescriptor => htd.riftDescriptor
        case x => backupRiftDescriptor.getOrElse(RiftDescriptor(what.getClass()))
      }
      self.getRawDecomposer(rd)
    }

    def getRawDecomposerFor(what: AnyRef): AlmValidation[RawDecomposer] =
      getRawDecomposerFor(what, None)

    def getDecomposer[T <: AnyRef]()(implicit tag: ClassTag[T]) =
      self.getRawDecomposer(RiftDescriptor(tag.runtimeClass)).map(_.asInstanceOf[Decomposer[T]])
      
    def getDecomposerByDescriptor[T <: AnyRef](riftDescriptor: RiftDescriptor): AlmValidation[Decomposer[T]] =
      self.getRawDecomposer(riftDescriptor).map(_.asInstanceOf[Decomposer[T]])

    def getDecomposerFor[T <: AnyRef](what: T, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Decomposer[T]] =
      self.getRawDecomposerFor(what, backupRiftDescriptor).map(_.asInstanceOf[Decomposer[T]])

    def getDecomposerFor[T <: AnyRef](what: T): AlmValidation[Decomposer[T]] =
      self.getRawDecomposerFor(what, None).map(_.asInstanceOf[Decomposer[T]])

  }
}