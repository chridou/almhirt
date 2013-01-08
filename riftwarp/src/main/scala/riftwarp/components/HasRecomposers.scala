package riftwarp.components

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait HasRecomposers {
  def tryGetRawRecomposer(typeDescriptor: TypeDescriptor): Option[RawRecomposer]
  def tryGetRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): Option[Recomposer[T]]
  def getRawRecomposer(typeDescriptor: TypeDescriptor): AlmValidation[RawRecomposer] =
    option.cata(tryGetRawRecomposer(typeDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No raw recomposer found for type descriptor '%s')".format(typeDescriptor)).failure)

  def getRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): AlmValidation[Recomposer[T]] =
    option.cata(tryGetRecomposer[T](typeDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No recomposer found for type descriptor '%s')".format(typeDescriptor)).failure)

  def lookUpRawFromRematerializer(remat: Rematerializer, backupDescriptor: Option[TypeDescriptor]): AlmValidation[RawRecomposer] =
    remat.tryGetTypeDescriptor.flatMap(tdOpt =>
      option.cata(tdOpt)(
        s => s.success,
        option.cata(backupDescriptor)(
          backDescr => backDescr.success,
          UnspecifiedProblem("Could not determine the required type").failure))).flatMap(td =>
      getRawRecomposer(td))

  def lookUpRawFromRematerializer(remat: Rematerializer): AlmValidation[RawRecomposer] =
    lookUpRawFromRematerializer(remat, None)

  def decomposeRawWithLookedUpRawRecomposer(remat: Rematerializer): AlmValidation[AnyRef] =
    lookUpRawFromRematerializer(remat).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def lookUpFromRematerializer[T <: AnyRef](remat: Rematerializer, backupDescriptor: Option[TypeDescriptor])(implicit mTarget: Manifest[T]): AlmValidation[Recomposer[T]] =
    remat.tryGetTypeDescriptor.fold(
      prob => prob.failure,
      succ =>
        option.cata(succ)(
          td => getRecomposer[T](td),
          option.cata(backupDescriptor)(
            td => getRecomposer[T](td),
            UnspecifiedProblem("Could extract the require TypeDescriptor").failure)))
    
  def addRawRecomposer(recomposer: RawRecomposer): Unit
  def addRecomposer(recomposer: Recomposer[_]): Unit
}

