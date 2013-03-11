package riftwarp

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import riftwarp.components._

object RiftWarpFuns {
  def prepareForWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel)(what: T)(decomposer: Decomposer[T], warpSequencer: WarpSequencer[TDimension]): AlmValidation[TDimension] =
    decomposer.decompose(what, warpSequencer).map(demat =>
      demat.dematerialize)

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel)(warpStream: TDimension)(factory: ExtractorFactory[TDimension], recomposer: Recomposer[T])(implicit hasRecomposers: HasRecomposers): AlmValidation[T] = {
    factory.createExtractor(warpStream).flatMap(array => recomposer.recompose(array))
  }

  private[riftwarp] def lookUpWarpSequencerFactoryAndConverters(channel: RiftChannel, tDimension: Class[_ <: RiftDimension], toolGroup: Option[ToolGroup] = None)(implicit riftwarp: RiftWarp): scalaz.Validation[RiftWarpProblem, (WarpSequencerFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    def findWarpSequencerFactory(converters: List[RawDimensionConverter]): scalaz.Validation[RiftWarpProblem, (WarpSequencerFactory[_ <: RiftDimension], RawDimensionConverter)] =
      converters match {
        case Nil => RiftWarpProblem(s"No WarpSequencerFactory found matching a target dimension '${tDimension.getName()}'. This has been found:${converters.mkString(", ")}.").failure
        case x :: xs =>
          option.cata(riftwarp.toolShed.tryGetWarpSequencerFactoryByType(x.tSource)(channel, toolGroup))(
            factory => (factory, x).success,
            findWarpSequencerFactory(xs))
      }

    option.cata(riftwarp.toolShed.tryGetWarpSequencerFactoryByType(tDimension)(channel))(
      some => (some, Nil).success,
      findWarpSequencerFactory(riftwarp.converters.getConvertersToByDimType(tDimension)).map(tuple => (tuple._1, List(tuple._2))))
  }

  private[riftwarp] def getDematerializationFun(channel: RiftChannel, tDimension: Class[_ <: RiftDimension], toolGroup: Option[ToolGroup] = None)(divertBlobs: BlobDivert)(implicit riftwarp: RiftWarp): scalaz.Validation[RiftWarpProblem, (AnyRef, RawDecomposer) => AlmValidation[RiftDimension]] =
    lookUpWarpSequencerFactoryAndConverters(channel, tDimension, toolGroup).map(factoryAndConverters =>
      (what: AnyRef, decomposer: RawDecomposer) =>
        factoryAndConverters._1.createWarpSequencer(divertBlobs)(riftwarp.barracks).flatMap(demat =>
          decomposer.decomposeRaw(what, demat).flatMap(demat =>
            factoryAndConverters._2.foldLeft(demat.dematerializeRaw.success[Problem])((acc, converter) =>
              acc.fold(prob => prob.failure, dim => converter.convertRaw(dim))))))
  
  private[riftwarp] def getDematerializationFun[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(divertBlobs: BlobDivert)(implicit riftwarp: RiftWarp, c: ClassTag[TDimension]): scalaz.Validation[RiftWarpProblem, (AnyRef, RawDecomposer) => AlmValidation[TDimension]] =
  	getDematerializationFun(channel, c.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]], toolGroup)(divertBlobs).map(_.asInstanceOf[(AnyRef, RawDecomposer) => AlmValidation[TDimension]])
              
  private[riftwarp] def lookUpRematerializerFactoryAndConverters(channel: RiftChannel, tDimension: Class[_ <: RiftDimension], toolGroup: Option[ToolGroup] = None)(implicit riftWarp: RiftWarp): AlmValidation[(ExtractorFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    def findRematerializerFactory(converters: List[RawDimensionConverter]): AlmValidation[(ExtractorFactory[_ <: RiftDimension], RawDimensionConverter)] =
      converters match {
        case Nil => UnspecifiedProblem("No RematerializerFactory or converter found for channel '%s' from source '%s'".format(channel, tDimension)).failure
        case x :: xs =>
          option.cata(riftWarp.toolShed.tryGetExtractorFactoryByType(x.tTarget)(channel, toolGroup))(
            factory => (factory, x).success,
            findRematerializerFactory(xs))
      }

    option.cata(riftWarp.toolShed.tryGetExtractorFactoryByType(tDimension)(channel, toolGroup))(
      some => (some, Nil).success,
      findRematerializerFactory(riftWarp.converters.getConvertersFromByDimType(tDimension)).map(tuple => (tuple._1, List(tuple._2))))
  }

  private[riftwarp] def lookUpRematerializerFactoryAndConverters[DimSource <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit cSourceDim: ClassTag[DimSource], riftWarp: RiftWarp): AlmValidation[(ExtractorFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    lookUpRematerializerFactoryAndConverters(channel,  cSourceDim.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]], toolGroup)
  }
  
  def getRecomposeFun[T <: AnyRef](channel: RiftChannel, tDimension: Class[_ <: RiftDimension], toolGroup: Option[ToolGroup] = None)(getRecomposer: (Extractor) => AlmValidation[Recomposer[T]])(blobFetch: BlobFetch)(implicit cTarget: ClassTag[T], riftWarp: RiftWarp): AlmValidation[RiftDimension => AlmValidation[T]] = {
//    lookUpRematerializerFactoryAndConverters(channel, tDimension, toolGroup).map {
//      case (arrayFactory, converters) =>
//        (sourceDim: RiftDimension) =>
//          for {
//            sourceDimForRematerializer <- converters.foldLeft[AlmValidation[RiftDimension]](sourceDim.success[Problem])((acc, conv) => acc.fold(prob => prob.failure, succ => conv.convertRaw(succ)))
//            extractor <- arrayFactory.createExtractorRaw(sourceDimForRematerializer, blobFetch)(riftWarp.barracks, riftWarp.toolShed)
//            recomposer <- getRecomposer(extractor)
//            recomposed <- recomposer.recompose(extractor)
//            casted <- almCast[T](recomposed)
//          } yield casted
//    }
    ???
  }
  
  def getRecomposeFun[TSource <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(getRecomposer: (Extractor) => AlmValidation[Recomposer[T]])(blobFetch: BlobFetch)(implicit cSourceDim: ClassTag[TSource], cTarget: ClassTag[T], riftWarp: RiftWarp): AlmValidation[(TSource) => AlmValidation[T]] = {
    getRecomposeFun(channel, cSourceDim.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]], toolGroup)(getRecomposer)(blobFetch)
  }
  
}