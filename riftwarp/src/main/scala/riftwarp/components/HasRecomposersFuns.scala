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
      UnspecifiedProblem("HasRecomposersFuns.getRawRecomposer: No raw recomposer found for RiftDescriptor '%s'".format(riftDescriptor)).failure)

  def getRecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor)(implicit hasRecomposers: HasRecomposers, tag: ClassTag[T]): AlmValidation[Recomposer[T]] =
    option.cata(hasRecomposers.tryGetRecomposer[T](riftDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("HasRecomposersFuns.getRecomposer: No recomposer found for RiftDescriptor '%s')".format(riftDescriptor)).failure)
  
  def lookUpFromRematerializer(extractor: Extractor, backupDescriptor: Option[RiftDescriptor])(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    extractor.tryGetRiftDescriptor.flatMap(tdOpt =>
      option.cata(tdOpt)(
        s => s.success,
        option.cata(backupDescriptor)(
          backDescr => backDescr.success,
          {
            val path = extractor.showPathFromRoot()
            UnspecifiedProblem(s"""HasRecomposersFuns.lookUpFromRematerializer: Could not determine the required RiftDescriptor. The Extractor's path is "$path"""").failure[RiftDescriptor]
          })).flatMap(td =>
          getRawRecomposer(td)))

  def lookUpFromRematerializer(extractor: Extractor, tBackup: Class[_])(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    extractor.tryGetRiftDescriptor.map(tdOpt =>
      tdOpt.getOrElse(RiftDescriptor(tBackup))).flatMap(td =>
        hasRecomposers.getRawRecomposer(td))

  def lookUpFromRematerializer(extractor: Extractor)(implicit hasRecomposers: HasRecomposers): AlmValidation[RawRecomposer] =
    lookUpFromRematerializer(extractor, None)

  def recomposeWithLookedUpRawRecomposerFromRiftDescriptor(descriptor: RiftDescriptor)(remat: Extractor)(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    getRawRecomposer(descriptor).flatMap(recomposer => recomposer.recomposeRaw(remat))
    
  def recomposeWithLookedUpRawRecomposerFromRematerializer(extractor: Extractor, backupDescriptor: Option[RiftDescriptor])(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializer(extractor, backupDescriptor).flatMap(recomposer => recomposer.recomposeRaw(extractor))

  def recomposeWithLookedUpRawRecomposerFromRematerializer(extractor: Extractor, tBackup: Class[_])(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializer(extractor, tBackup).flatMap(recomposer => recomposer.recomposeRaw(extractor))

  def recomposeWithLookedUpRawRecomposerFromRematerializer(extractor: Extractor)(implicit hasRecomposers: HasRecomposers): AlmValidation[AnyRef] =
    lookUpFromRematerializer(extractor).flatMap(recomposer => recomposer.recomposeRaw(extractor))

  def recomposeWithLookUpFromRematerializer[T <: AnyRef](extractor: Extractor)(implicit hasRecomposers: HasRecomposers, tag: ClassTag[T]): AlmValidation[T] =
    lookUpFromRematerializer(extractor).flatMap(recomposer => recomposer.recomposeRaw(extractor).flatMap(almCast[T](_)))
    
}