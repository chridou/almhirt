package riftwarp.components

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait HasDecomposers {
  def getRawDecomposer(riftDescriptor: RiftDescriptor): AlmValidation[RawDecomposer]

  def addDecomposer(decomposer: Decomposer[_ <: AnyRef]): Unit
}

object HasDecomposers {
  implicit class HasDecomposersOps(self: HasDecomposers) {
    def getRawDecomposerFor(what: AnyRef, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[RawDecomposer] =
      what match {
        case htd: HasRiftDescriptor => self.getRawDecomposer(htd.riftDescriptor)
        case x =>
          self.getRawDecomposer(what.getClass()).fold(
            fail => option.cata(backupRiftDescriptor)(
              desc => self.getRawDecomposer(desc),
              UnspecifiedProblem(s"No decomposer found for '${what.getClass().getName()}'").failure),
            succ => succ.success)
      }

    def getRawDecomposerFor(what: AnyRef): AlmValidation[RawDecomposer] =
      getRawDecomposerFor(what, None)

    def getRawDecomposerByDescriptorAndThenByType(riftDescriptor: Option[RiftDescriptor], tpe: Class[_]): AlmValidation[RawDecomposer] =
      option.cata(riftDescriptor)(
        desc =>
          self.getRawDecomposer(desc).fold(
            fail => self.getRawDecomposer(tpe),
            succ => succ.success),
        self.getRawDecomposer(tpe))

    def getDecomposerByDescriptorAndThenByTag[T <: AnyRef](riftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[T]): AlmValidation[Decomposer[T]] =
      getRawDecomposerByDescriptorAndThenByType(riftDescriptor, tag.runtimeClass).flatMap(rd => rd.castTo[Decomposer[T]])

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