package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

trait HasRecomposers {
  def tryGetRecomposerByName(typeDescriptor: String): Option[AnyRef]
  
  def tryGetRecomposerForType(targetType: Class[_]): Option[AnyRef] = tryGetRecomposerByName(targetType.getName())
  def tryGetRecomposer[T <: AnyRef](implicit m: Manifest[T]): Option[Recomposer[T]] =
    tryGetRecomposerForType(m.erasure).map(_.asInstanceOf[Recomposer[T]])
  def getRecomposerByName(typeDescriptor: String): AlmValidation[AnyRef] =
    option.cata(tryGetRecomposerByName(typeDescriptor))(_.success, KeyNotFoundProblem("No recomposer found for '%s'".format(typeDescriptor)).failure)
  def getRecomposerForType(targetType: Class[_]): AlmValidation[AnyRef] =
    option.cata(tryGetRecomposerForType(targetType))(_.success, KeyNotFoundProblem("No recomposer found for '%s'".format(targetType.getName())).failure)
  def getRecomposer[T <: AnyRef](implicit m: Manifest[T]): AlmValidation[Recomposer[T]] =
    getRecomposerForType(m.erasure).map(anyRef => anyRef.asInstanceOf[Recomposer[T]])

  def addRecomposerByName(typeDescriptor: String, recomposer: AnyRef): Unit
  def addRecomposerForType(targetType: Class[_], recomposer: AnyRef) { addRecomposerByName(targetType.getName(), recomposer) }
  def addRecomposer[T <: Recomposer[_]](recomposer: T)(implicit m: Manifest[T]) {
    addRecomposerForType(m.erasure, recomposer)
  }
}

