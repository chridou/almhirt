package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import riftwarp.components._

object RiftWarpFuns {
  def prepareForWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel)(what: T)(decomposer: Decomposer[T], dematerializer: Dematerializer[TDimension]): AlmValidation[TDimension] =
    decomposer.decompose(what)(dematerializer).map(demat =>
      demat.dematerialize)

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel)(warpStream: TDimension)(factory: RematerializerFactory[TDimension], recomposer: Recomposer[T])(implicit hasRecomposers: HasRecomposers, hasFunctionObject: ma.HasFunctionObjects): AlmValidation[T] = {
    factory.createRematerializer(warpStream).flatMap(array => recomposer.recompose(array))
  }

  private[riftwarp] def lookUpDematerializerFactoryAndConverters(channel: RiftChannel, tDimension: Class[_ <: RiftDimension], toolGroup: Option[ToolGroup] = None)(implicit riftwarp: RiftWarp): scalaz.Validation[RiftWarpProblem, (DematerializerFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    def findDematerializerFactory(converters: List[RawDimensionConverter]): scalaz.Validation[RiftWarpProblem, (DematerializerFactory[_ <: RiftDimension], RawDimensionConverter)] =
      converters match {
        case Nil => RiftWarpProblem("No DematerializerFactory found matching with a target type matching %s.".format(converters.mkString(", "))).failure
        case x :: xs =>
          option.cata(riftwarp.toolShed.tryGetDematerializerFactoryByType(x.tSource)(channel, toolGroup))(
            factory => (factory, x).success,
            findDematerializerFactory(xs))
      }

    option.cata(riftwarp.toolShed.tryGetDematerializerFactoryByType(tDimension)(channel))(
      some => (some, Nil).success,
      findDematerializerFactory(riftwarp.converters.getConvertersToByDimType(tDimension)).map(tuple => (tuple._1, List(tuple._2))))
  }

  private[riftwarp] def getDematerializationFun[T <: AnyRef](channel: RiftChannel, tDimension: Class[_ <: RiftDimension], toolGroup: Option[ToolGroup] = None)(divertBlobs: BlobDivert)(implicit riftwarp: RiftWarp): scalaz.Validation[RiftWarpProblem, (T, Decomposer[T]) => AlmValidation[RiftDimension]] =
    lookUpDematerializerFactoryAndConverters(channel, tDimension, toolGroup).map(factoryAndConverters =>
      (what: T, decomposer: Decomposer[T]) =>
        factoryAndConverters._1.createDematerializer(divertBlobs)(riftwarp.barracks, riftwarp.toolShed).flatMap(demat =>
          decomposer.decompose(what)(demat).flatMap(demat =>
            factoryAndConverters._2.foldLeft(demat.dematerializeRaw.success[Problem])((acc, converter) =>
              acc.fold(prob => prob.failure, dim => converter.convertRaw(dim))))))
  
  private[riftwarp] def getDematerializationFun[T <: AnyRef, TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(divertBlobs: BlobDivert)(implicit riftwarp: RiftWarp, m: Manifest[TDimension]): scalaz.Validation[RiftWarpProblem, (T, Decomposer[T]) => AlmValidation[TDimension]] =
  	getDematerializationFun[T](channel, m.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]], toolGroup)(divertBlobs).map(_.asInstanceOf[(T, Decomposer[T]) => AlmValidation[TDimension]])
              
  private[riftwarp] def lookUpRematerializerFactoryAndConverters(channel: RiftChannel, tDimension: Class[_ <: RiftDimension], toolGroup: Option[ToolGroup] = None)(implicit riftWarp: RiftWarp): AlmValidation[(RematerializerFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    def findRematerializerFactory(converters: List[RawDimensionConverter]): AlmValidation[(RematerializerFactory[_ <: RiftDimension], RawDimensionConverter)] =
      converters match {
        case Nil => UnspecifiedProblem("No RematerializerFactory or converter found for channel '%s' from source '%s'".format(channel, tDimension)).failure
        case x :: xs =>
          option.cata(riftWarp.toolShed.tryGetRematerializerFactoryByType(x.tTarget)(channel, toolGroup))(
            factory => (factory, x).success,
            findRematerializerFactory(xs))
      }

    option.cata(riftWarp.toolShed.tryGetRematerializerFactoryByType(tDimension)(channel, toolGroup))(
      some => (some, Nil).success,
      findRematerializerFactory(riftWarp.converters.getConvertersFromByDimType(tDimension)).map(tuple => (tuple._1, List(tuple._2))))
  }

  private[riftwarp] def lookUpRematerializerFactoryAndConverters[DimSource <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mS: Manifest[DimSource], riftWarp: RiftWarp): AlmValidation[(RematerializerFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    lookUpRematerializerFactoryAndConverters(channel,  mS.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]], toolGroup)
  }
  
  def getRecomposeFun[T <: AnyRef](channel: RiftChannel, tDimension: Class[_ <: RiftDimension], toolGroup: Option[ToolGroup] = None)(getRecomposer: (Rematerializer, Option[Class[_]]) => AlmValidation[Recomposer[T]])(blobFetch: BlobFetch)(implicit mTarget: Manifest[T], riftWarp: RiftWarp): AlmValidation[RiftDimension => AlmValidation[T]] = {
    lookUpRematerializerFactoryAndConverters(channel, tDimension, toolGroup).map {
      case (arrayFactory, converters) =>
        (sourceDim: RiftDimension) =>
          for {
            sourceDimForRematerializer <- converters.foldLeft[AlmValidation[RiftDimension]](sourceDim.success[Problem])((acc, conv) => acc.fold(prob => prob.failure, succ => conv.convertRaw(succ)))
            rematerializer <- arrayFactory.createRematerializerRaw(sourceDimForRematerializer, blobFetch)(riftWarp.barracks, riftWarp.toolShed)
            recomposer <- getRecomposer(rematerializer, Some(mTarget.runtimeClass))
            recomposed <- recomposer.recompose(rematerializer)
          } yield recomposed
    }
  }
  
  def getRecomposeFun[TSource <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(getRecomposer: (Rematerializer, Option[Class[_]]) => AlmValidation[Recomposer[T]])(blobFetch: BlobFetch)(implicit mD: Manifest[TSource], mTarget: Manifest[T], riftWarp: RiftWarp): AlmValidation[(TSource) => AlmValidation[T]] = {
    getRecomposeFun(channel, mD.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]], toolGroup)(getRecomposer)(blobFetch)
  }
  
}