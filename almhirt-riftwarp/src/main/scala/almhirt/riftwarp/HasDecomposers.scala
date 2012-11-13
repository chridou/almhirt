package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

trait HasDecomposers {
  def tryGetDecomposerByName(typeDescriptor: String): Option[AnyRef]
  def tryGetDecomposerForType(targetType: Class[_]): Option[AnyRef] = tryGetDecomposerByName(targetType.getName())
  def tryGetDecomposer[T <: AnyRef](implicit m: Manifest[T]): Option[Decomposer[T]] =
    tryGetDecomposerForType(m.erasure).map(_.asInstanceOf[Decomposer[T]])

  def getDecomposerByName(typeDescriptor: String): AlmValidation[AnyRef] =
    option.cata(tryGetDecomposerByName(typeDescriptor))(_.success, KeyNotFoundProblem("No decomposer found for '%s'".format(typeDescriptor)).failure)
  def getDecomposerForType(targetType: Class[_]): AlmValidation[AnyRef] =
    option.cata(tryGetDecomposerForType(targetType))(_.success, KeyNotFoundProblem("No decomposer found for '%s'".format(targetType.getName())).failure)
  def getDecomposer[T <: AnyRef](implicit m: Manifest[T]): AlmValidation[Decomposer[T]] =
    getDecomposerForType(m.erasure).map(anyRef => anyRef.asInstanceOf[Decomposer[T]])

  def addDecomposerByName(typeDescriptor: String, decomposer: AnyRef): Unit
  def addDecomposerForType(targetType: Class[_], decomposer: AnyRef) { addDecomposerByName(targetType.getName(), decomposer) }
  def addDecomposer[T <: Decomposer[_]](decomposer: T)(implicit m: Manifest[T]) {
    addDecomposerForType(m.erasure, decomposer)
  }
}