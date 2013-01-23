package riftwarp.components

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import riftwarp._

trait HasRecomposersFuns {
  def getRawRecomposer(riftDescriptor: RiftDescriptor)(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    option.cata(hasRecomposers.tryGetRawRecomposer(riftDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No raw recomposer found for RiftDescriptor '%s')".format(riftDescriptor)).failure)

  def getRecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor)(implicit hasRecomposers: HasRecomposers, tag: ClassTag[T]): AlmValidation[Recomposer[T]] =
    option.cata(hasRecomposers.tryGetRecomposer[T](riftDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No recomposer found for RiftDescriptor '%s')".format(riftDescriptor)).failure)
  
  def lookUpFromRematerializer(remat: Rematerializer, backupDescriptor: Option[RiftDescriptor])(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    remat.tryGetRiftDescriptor.flatMap(tdOpt =>
      option.cata(tdOpt)(
        s => s.success,
        option.cata(backupDescriptor)(
          backDescr => backDescr.success,
          UnspecifiedProblem("Could not determine the required type").failure))).flatMap(td =>
      hasRecomposers.getRawRecomposer(td))

  def lookUpFromRematerializer(remat: Rematerializer, tBackup: Class[_])(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    remat.tryGetRiftDescriptor.map(tdOpt =>
      tdOpt.getOrElse(RiftDescriptor(tBackup))).flatMap(td =>
        hasRecomposers.getRawRecomposer(td))

  def lookUpFromRematerializer(remat: Rematerializer)(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    lookUpFromRematerializer(remat, None)

  def recomposeWithLookedUpRawRecomposerFromRiftDescriptor(descriptor: RiftDescriptor)(remat: Rematerializer)(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    getRawRecomposer(descriptor).flatMap(recomposer => recomposer.recomposeRaw(remat))
    
  def recomposeWithLookedUpRawRecomposerFromRematerializer(remat: Rematerializer, backupDescriptor: Option[RiftDescriptor])(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializer(remat, backupDescriptor).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def recomposeWithLookedUpRawRecomposerFromRematerializer(remat: Rematerializer, tBackup: Class[_])(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializer(remat, tBackup).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def recomposeWithLookedUpRawRecomposerFromRematerializer(remat: Rematerializer)(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializer(remat).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def recomposeWithLookUpFromRematerializer[T <: AnyRef](remat: Rematerializer)(implicit hasRecomposers: HasRecomposers, tag: ClassTag[T]): AlmValidation[T] =
    lookUpFromRematerializer(remat).flatMap(recomposer => recomposer.recomposeRaw(remat).flatMap(almCast[T](_)))
    
}