package riftwarp.components

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait HasDecomposers {
  def tryGetRawDecomposer(typeDescriptor: TypeDescriptor): Option[RawDecomposer]
  def tryGetRawDecomposerForAny(what: AnyRef): Option[RawDecomposer] =
    what match {
      case htd: HasTypeDescriptor => tryGetRawDecomposer(htd.typeDescriptor)
      case x => tryGetRawDecomposer(TypeDescriptor(x.getClass()))
    }

  def tryGetDecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): Option[Decomposer[T]]
  def tryGetDecomposer[T <: AnyRef](implicit m: Manifest[T]): Option[Decomposer[T]] = tryGetDecomposer(m.runtimeClass)
  def tryGetDecomposerFor[T <: HasTypeDescriptor](what: T): Option[Decomposer[T]] = tryGetDecomposer[T](what.typeDescriptor)
  def tryGetDecomposerForAny[T <: AnyRef](what: T): Option[Decomposer[T]] =
    what match {
      case htd: HasTypeDescriptor => tryGetDecomposer(htd.typeDescriptor)
      case x => tryGetDecomposer(TypeDescriptor(x.getClass()))
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