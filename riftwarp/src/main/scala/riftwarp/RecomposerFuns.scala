package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._

object RecomposerFuns {
  def getRawRecomposer(typeDescriptor: TypeDescriptor)(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    option.cata(hasRecomposers.tryGetRawRecomposer(typeDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No raw recomposer found for type descriptor '%s')".format(typeDescriptor)).failure)

  def getRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor)(implicit hasRecomposers: HasRecomposers): AlmValidation[Recomposer[T]] =
    option.cata(hasRecomposers.tryGetRecomposer[T](typeDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No recomposer found for type descriptor '%s')".format(typeDescriptor)).failure)
  
  def lookUpFromRematerializationArray(remat: RematerializationArray, backupDescriptor: Option[TypeDescriptor])(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    remat.tryGetTypeDescriptor.flatMap(tdOpt =>
      option.cata(tdOpt)(
        s => s.success,
        option.cata(backupDescriptor)(
          backDescr => backDescr.success,
          UnspecifiedProblem("Could not determine the required type").failure))).flatMap(td =>
      hasRecomposers.getRawRecomposer(td))

  def lookUpFromRematerializationArray(remat: RematerializationArray, tBackup: Class[_])(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    remat.tryGetTypeDescriptor.map(tdOpt =>
      tdOpt.getOrElse(TypeDescriptor(tBackup))).flatMap(td =>
        hasRecomposers.getRawRecomposer(td))

  def lookUpFromRematerializationArray(remat: RematerializationArray)(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    lookUpFromRematerializationArray(remat, None)

  def recomposeWithLookedUpRawRecomposerFromTypeDescriptor(descriptor: TypeDescriptor)(remat: RematerializationArray)(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    getRawRecomposer(descriptor).flatMap(recomposer => recomposer.recomposeRaw(remat))
    
  def recomposeWithLookedUpRawRecomposerFromRematerializationArray(remat: RematerializationArray, backupDescriptor: Option[TypeDescriptor])(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializationArray(remat, backupDescriptor).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def recomposeWithLookedUpRawRecomposerFromRematerializationArray(remat: RematerializationArray, tBackup: Class[_])(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializationArray(remat, tBackup).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def recomposeWithLookedUpRawRecomposerFromRematerializationArray(remat: RematerializationArray)(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializationArray(remat).flatMap(recomposer => recomposer.recomposeRaw(remat))
    
}