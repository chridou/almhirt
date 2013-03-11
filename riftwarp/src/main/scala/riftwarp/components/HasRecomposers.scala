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
      UnspecifiedProblem("No raw recomposer found for RiftDescriptor '%s')".format(riftDescriptor)).failure)

  def getRecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor)(implicit tag: ClassTag[T]): AlmValidation[Recomposer[T]] =
    option.cata(tryGetRecomposer[T](riftDescriptor))(
      recomposer => recomposer.success,
      UnspecifiedProblem("No recomposer found for RiftDescriptor '%s')".format(riftDescriptor)).failure)

  def lookUpRawFromRematerializer(remat: Extractor, backupDescriptor: Option[RiftDescriptor]): AlmValidation[RawRecomposer] =
    remat.tryGetRiftDescriptor.flatMap(tdOpt =>
      option.cata(tdOpt)(
        s => s.success,
        option.cata(backupDescriptor)(
          backDescr => backDescr.success,
          UnspecifiedProblem("Could not determine the required type").failure))).flatMap(td =>
      getRawRecomposer(td))

  def lookUpRawFromRematerializer(remat: Extractor): AlmValidation[RawRecomposer] =
    lookUpRawFromRematerializer(remat, None)

  def decomposeRawWithLookedUpRawRecomposer(remat: Extractor): AlmValidation[AnyRef] =
    lookUpRawFromRematerializer(remat).flatMap(recomposer => recomposer.recomposeRaw(remat))

  def lookUpFromRematerializer[T <: AnyRef](remat: Extractor, backupDescriptor: Option[RiftDescriptor])(implicit mTarget: ClassTag[T]): AlmValidation[Recomposer[T]] =
    remat.tryGetRiftDescriptor.fold(
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

