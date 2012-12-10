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

  def getRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor): AlmValidation[Recomposer[T]] =
    option.cata(tryGetRecomposer[T](typeDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No recomposer found for type descriptor '%s')".format(typeDescriptor)).failure)

  def lookUpFromRematerializationArray(remat: RematerializationArray, backupDescriptor: Option[TypeDescriptor]): AlmValidation[RawRecomposer] =
    remat.tryGetTypeDescriptor.bind(tdOpt =>
      option.cata(tdOpt)(
        s => s.success,
        option.cata(backupDescriptor)(
          backDescr => backDescr.success,
          UnspecifiedProblem("Could not determine the required type").failure))).bind(td =>
      getRawRecomposer(td))

  def lookUpFromRematerializationArray(remat: RematerializationArray, tBackup: Class[_]): AlmValidation[RawRecomposer] =
    remat.tryGetTypeDescriptor.map(tdOpt =>
      tdOpt.getOrElse(TypeDescriptor(tBackup))).bind(td =>
        getRawRecomposer(td))

  def lookUpFromRematerializationArray(remat: RematerializationArray): AlmValidation[RawRecomposer] =
    lookUpFromRematerializationArray(remat, None)
        
  def decomposeWithLookedUpRawRecomposer(remat: RematerializationArray): AlmValidation[AnyRef] =
    lookUpFromRematerializationArray(remat).bind(recomposer => recomposer.recomposeRaw(remat))
    
  def addRawRecomposer(recomposer: RawRecomposer): Unit
  def addRecomposer(recomposer: Recomposer[_]): Unit
}

