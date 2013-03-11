package riftwarp.components

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait HasRecomposers {
  def tryGetRawRecomposer(riftDescriptor: RiftDescriptor): Option[RawRecomposer]
  def tryGetRecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor)(implicit tag: ClassTag[T]): Option[Recomposer[T]]
  def getRawRecomposer(riftDescriptor: RiftDescriptor): AlmValidation[RawRecomposer] =
    option.cata(tryGetRawRecomposer(riftDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("HasRecomposers.getRawRecomposer: No raw recomposer found for RiftDescriptor '%s')".format(riftDescriptor)).failure)

  def getRecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor)(implicit tag: ClassTag[T]): AlmValidation[Recomposer[T]] =
    option.cata(tryGetRecomposer[T](riftDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("HasRecomposers.getRecomposer: No recomposer found for RiftDescriptor '%s')".format(riftDescriptor)).failure)

  def lookUpRawFromRematerializer(extractor: Extractor, backupDescriptor: Option[RiftDescriptor]): AlmValidation[RawRecomposer] =
    extractor.tryGetRiftDescriptor.flatMap(tdOpt =>
      option.cata(tdOpt)(
        s => s.success,
        option.cata(backupDescriptor)(
          backDescr => backDescr.success,
          UnspecifiedProblem("HasRecomposers.lookUpRawFromRematerializer: Could not determine the required type").failure))).flatMap(td =>
      getRawRecomposer(td))

  def lookUpRawFromRematerializer(remat: Extractor): AlmValidation[RawRecomposer] =
    lookUpRawFromRematerializer(remat, None)

  def decomposeRawWithLookedUpRawRecomposer(extractor: Extractor): AlmValidation[AnyRef] =
    lookUpRawFromRematerializer(extractor).flatMap(recomposer => recomposer.recomposeRaw(extractor))

  def lookUpFromRematerializer[T <: AnyRef](extractor: Extractor, backupDescriptor: Option[RiftDescriptor])(implicit mTarget: ClassTag[T]): AlmValidation[Recomposer[T]] =
    extractor.tryGetRiftDescriptor.fold(
      prob =>
        prob.failure,
      succ =>
        option.cata(succ)(
          td => getRecomposer[T](td),
          option.cata(backupDescriptor)(
            td => getRecomposer[T](td),
            getRecomposer[T](RiftDescriptor(mTarget.runtimeClass)))))

  def addRawRecomposer(recomposer: RawRecomposer): Unit
  def addRecomposer(recomposer: Recomposer[_]): Unit
}

object HasRecomposers {
  implicit class HasRecomposersOps(self: HasRecomposers) {
    
  }
}

