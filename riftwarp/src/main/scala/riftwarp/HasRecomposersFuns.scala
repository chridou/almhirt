package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._

trait HasRecomposersFuns {
  def getRawRecomposer(typeDescriptor: TypeDescriptor)(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    option.cata(hasRecomposers.tryGetRawRecomposer(typeDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No raw recomposer found for type descriptor '%s')".format(typeDescriptor)).failure)

  def getRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor)(implicit hasRecomposers: HasRecomposers): AlmValidation[Recomposer[T]] =
    option.cata(hasRecomposers.tryGetRecomposer[T](typeDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No recomposer found for type descriptor '%s')".format(typeDescriptor)).failure)
  
  def lookUpFromRematerializer(remat: Rematerializer, backupDescriptor: Option[TypeDescriptor])(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    remat.tryGetTypeDescriptor.flatMap(tdOpt =>
      option.cata(tdOpt)(
        s => s.success,
        option.cata(backupDescriptor)(
          backDescr => backDescr.success,
          UnspecifiedProblem("Could not determine the required type").failure))).flatMap(td =>
      hasRecomposers.getRawRecomposer(td))

  def lookUpFromRematerializer(remat: Rematerializer, tBackup: Class[_])(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    remat.tryGetTypeDescriptor.map(tdOpt =>
      tdOpt.getOrElse(TypeDescriptor(tBackup))).flatMap(td =>
        hasRecomposers.getRawRecomposer(td))

  def lookUpFromRematerializer(remat: Rematerializer)(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    lookUpFromRematerializer(remat, None)

  def recomposeWithLookedUpRawRecomposerFromTypeDescriptor(descriptor: TypeDescriptor)(remat: Rematerializer)(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    getRawRecomposer(descriptor).flatMap(recomposer => recomposer.recomposeRaw(remat))
    
  def recomposeWithLookedUpRawRecomposerFromRematerializer(remat: Rematerializer, backupDescriptor: Option[TypeDescriptor])(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializer(remat, backupDescriptor).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def recomposeWithLookedUpRawRecomposerFromRematerializer(remat: Rematerializer, tBackup: Class[_])(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializer(remat, tBackup).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def recomposeWithLookedUpRawRecomposerFromRematerializer(remat: Rematerializer)(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializer(remat).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def recomposeWithLookUpFromRematerializer[T <: AnyRef](remat: Rematerializer)(implicit hasRecomposers: HasRecomposers): AlmValidation[T] =
    lookUpFromRematerializer(remat).flatMap(recomposer => recomposer.recomposeRaw(remat).flatMap(almCast[T](_)))
    
}