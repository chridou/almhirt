package riftwarp.components

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait HasDecomposers {
  def tryGetRawDecomposer(riftDescriptor: RiftDescriptor): Option[RawDecomposer]
  def tryGetRawDecomposerForAny(what: AnyRef): Option[RawDecomposer] =
    what match {
      case htd: HasRiftDescriptor => tryGetRawDecomposer(htd.riftDescriptor)
      case x => tryGetRawDecomposer(RiftDescriptor(x.getClass()))
    }

  def tryGetDecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor): Option[Decomposer[T]]
  def tryGetDecomposer[T <: AnyRef](implicit m: Manifest[T]): Option[Decomposer[T]] = tryGetDecomposer(m.runtimeClass)
  def tryGetDecomposerFor[T <: HasRiftDescriptor](what: T): Option[Decomposer[T]] = tryGetDecomposer[T](what.riftDescriptor)
  def tryGetDecomposerForAny[T <: AnyRef](what: T): Option[Decomposer[T]] =
    what match {
      case htd: HasRiftDescriptor => tryGetDecomposer(htd.riftDescriptor)
      case x => tryGetDecomposer(RiftDescriptor(x.getClass()))
    }

  def getDecomposer[T <: AnyRef](implicit m: Manifest[T]): AlmValidation[Decomposer[T]] =
    option.cata(tryGetDecomposer[T](m))(
      decomposer => decomposer.success,
      UnspecifiedProblem("No decomposer found for type '%s')".format(m.runtimeClass.getName())).failure)

  def getDecomposerForAny[T <: AnyRef](what: T): AlmValidation[Decomposer[T]] =
    option.cata(tryGetDecomposerForAny(what))(
      decomposer => decomposer.asInstanceOf[Decomposer[T]].success,
      UnspecifiedProblem("No decomposer found for type '%s')".format(what.getClass().getName())).failure)

  def getRawDecomposerForAny(what: AnyRef): AlmValidation[RawDecomposer] =
    option.cata(tryGetRawDecomposerForAny(what))(
      decomposer => decomposer.success,
      UnspecifiedProblem("No decomposer found for type '%s')".format(what.getClass().getName())).failure)

  def addRawDecomposer(decomposer: RawDecomposer): Unit
  def addDecomposer(decomposer: Decomposer[_]): Unit
}