package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

trait HasRecomposers {
  def tryGetRawRecomposer(typeDescriptor: TypeDescriptor): Option[RawRecomposer]
  def tryGetRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): Option[Recomposer[T]]
  def getRawRecomposer(typeDescriptor: TypeDescriptor): AlmValidation[RawRecomposer] =
    option.cata(tryGetRawRecomposer(typeDescriptor))(
        recomposer => recomposer.success, 
        UnspecifiedProblem("No raw recomposer found for type descriptor '%s')".format(typeDescriptor)).failure)

  def getRawRecomposerForAnyRef(forWhat: AnyRef): AlmValidation[RawRecomposer] =
    forWhat match {
      case htd: HasTypeDescriptor => getRawRecomposer(htd.typeDescriptor)
      case x => getRawRecomposer(TypeDescriptor(x.getClass()))
  }

  def getRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): AlmValidation[Recomposer[T]] =
    option.cata(tryGetRecomposer[T](typeDescriptor))(
        recomposer => recomposer.success, 
        UnspecifiedProblem("No recomposer found for type descriptor '%s')".format(typeDescriptor)).failure)
 
  def addRawRecomposer(recomposer: RawRecomposer): Unit
  def addRecomposer(recomposer: Recomposer[_]): Unit
}

