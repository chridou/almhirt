package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._

trait RiftWarp {
  def barracks: RiftWarpBarracks
  def toolShed: RiftWarpToolShed
  def converters: HasDimensionConverters

  def lookUpDematerializerFactoryAndConverters[DimTarget <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mT: Manifest[DimTarget]): AlmValidation[(DematerializerFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    def findDematerializerFactory(converters: List[RawDimensionConverter]): AlmValidation[(DematerializerFactory[_ <: RiftDimension], RawDimensionConverter)] =
      converters match {
        case Nil => UnspecifiedProblem("No DematerializerFactory found matching with a target type matching %s.".format(converters.mkString(", "))).failure
        case x :: xs =>
          option.cata(toolShed.tryGetDematerializerFactoryByType(x.tSource)(channel, toolGroup))(
            factory => (factory, x).success,
            findDematerializerFactory(xs))
      }

    option.cata(toolShed.tryGetDematerializerFactory[DimTarget](channel))(
      some => (some, Nil).success,
      findDematerializerFactory(converters.getConvertersTo[DimTarget]).map(tuple => (tuple._1, List(tuple._2))))
  }

  def getDematerializationFun[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(divertBlobs: BlobDivert)(implicit mD: Manifest[TDimension]): AlmValidation[(AnyRef) => AlmValidation[TDimension]] =
    lookUpDematerializerFactoryAndConverters[TDimension](channel, toolGroup).map(factoryAndConverters =>
      (what: AnyRef) =>
        barracks.getDecomposerForAny(what).flatMap(decomposer =>
          factoryAndConverters._1.createDematerializer(divertBlobs)(barracks, toolShed).flatMap(demat =>
            decomposer.decomposeRaw(what)(demat).flatMap(demat =>
                factoryAndConverters._2.foldLeft(demat.dematerializeRaw.success[Problem])((acc, converter) =>
                  acc.fold(prob => prob.failure, dim => converter.convertRaw(dim))).map(_.asInstanceOf[TDimension])))))

  def prepareForWarp[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit m: Manifest[TDimension]): AlmValidation[TDimension] =
    getDematerializationFun[TDimension](channel, toolGroup)(NoDivertBlobDivert).flatMap(fun => fun(what))

  def prepareForWarpWithBlobs[TDimension <: RiftDimension](divertBlobs: BlobDivert)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit m: Manifest[TDimension]): AlmValidation[TDimension] =
    getDematerializationFun[TDimension](channel, toolGroup)(divertBlobs).flatMap(fun => fun(what))
    
  def lookUpRematerializerFactoryAndConverters[DimSource <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mS: Manifest[DimSource]): AlmValidation[(RematerializerFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    def findRematerializerFactory(converters: List[RawDimensionConverter]): AlmValidation[(RematerializerFactory[_ <: RiftDimension], RawDimensionConverter)] =
      converters match {
        case Nil => UnspecifiedProblem("No RematerializerFactory or converter found").failure
        case x :: xs =>
          option.cata(toolShed.tryGetRematerializerFactoryByType(x.tTarget)(channel, toolGroup))(
            factory => (factory, x).success,
            findRematerializerFactory(xs))
      }

    option.cata(toolShed.tryGetRematerializerFactory[DimSource](channel, toolGroup))(
      some => (some, Nil).success,
      findRematerializerFactory(converters.getConvertersFrom[DimSource]).map(tuple => (tuple._1, List(tuple._2))))
  }

  def getRematerializationFun[TSource <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(blobFetch: BlobFetch)(implicit mtarget: Manifest[T], mD: Manifest[TSource]): AlmValidation[TSource => AlmValidation[T]] =
    lookUpRematerializerFactoryAndConverters[TSource](channel, toolGroup).map {
      case (arrayFactory, converters) =>
        (sourceDim: TSource) =>
          converters.foldLeft[AlmValidation[RiftDimension]](sourceDim.success[Problem])((acc, conv) => acc.fold(prob => prob.failure, succ => conv.convertRaw(succ))).flatMap(dimRematSource =>
            arrayFactory.createRematerializerRaw(dimRematSource, blobFetch)(barracks, toolShed).flatMap(remat =>
              remat.tryGetTypeDescriptor.flatMap(tdOpt => {
                val td = tdOpt.getOrElse(TypeDescriptor(mtarget.runtimeClass))
                barracks.getRawRecomposer(td).flatMap(recomp =>
                  recomp.recomposeRaw(remat).map(res =>
                    res.asInstanceOf[T]))
              })))
    }

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit mtarget: Manifest[T], mD: Manifest[TDimension]): AlmValidation[T] =
    getRematerializationFun[TDimension, T](channel, toolGroup)(NoFetchBlobFetch).flatMap(fun => fun(warpStream))

  def receiveFromWarpWithBlobs[TDimension <: RiftDimension, T <: AnyRef](blobFetch: BlobFetch)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit mtarget: Manifest[T], mD: Manifest[TDimension]): AlmValidation[T] =
    getRematerializationFun[TDimension, T](channel, toolGroup)(blobFetch).flatMap(fun => fun(warpStream))

}

object RiftWarp {
  def apply(theBarracks: RiftWarpBarracks, theToolShed: RiftWarpToolShed, theConverters: HasDimensionConverters): RiftWarp =
    new RiftWarp {
      val barracks = theBarracks
      val toolShed = theToolShed
      val converters = theConverters
    }

  def unsafe(): RiftWarp = apply(RiftWarpBarracks.unsafe, RiftWarpToolShed.unsafe, new impl.UnsafeDimensionConverterRegistry)
  def unsafeWithDefaults(): RiftWarp = {
    val riftWarp = unsafe()
    initializeWithDefaults(riftWarp)
    riftWarp
  }

  def concurrent(): RiftWarp = apply(RiftWarpBarracks.concurrent, RiftWarpToolShed.concurrent, new impl.ConcurrentDimensionConverterRegistry)
  def concurrentWithDefaults(): RiftWarp = {
    val riftWarp = concurrent()
    initializeWithDefaults(riftWarp)
    riftWarp
  }
  
  private def initializeWithDefaults(riftWarp: RiftWarp) {
    riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToMapDematerializer)
    riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToJsonCordDematerializer)
    riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToXmlElemDematerializer)

    riftWarp.toolShed.addRematerializerFactory(impl.rematerializers.FromMapRematerializer)
    riftWarp.toolShed.addRematerializerFactory(impl.rematerializers.FromJsonMapRematerializer)
    riftWarp.toolShed.addRematerializerFactory(impl.rematerializers.FromJsonStringRematerializer)
    riftWarp.toolShed.addRematerializerFactory(impl.rematerializers.FromJsonCordRematerializer)
    
    riftWarp.converters.addConverter(DimensionNiceStringToString)
    riftWarp.converters.addConverter(DimensionNiceCordToCord)
    riftWarp.converters.addConverter(DimensionConverterStringToCord)
    riftWarp.converters.addConverter(DimensionConverterCordToString)

    import riftwarp.ma._
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.listFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.vectorFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.setFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.iterableFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableFunctionObjects.treeFunctionObject)

    riftWarp.toolShed.addChannelFolder(JsonCordFolder)
    riftWarp.toolShed.addChannelFolder(XmlElemFolder)

    riftWarp.toolShed.addConvertsMAToNA(MAToNAConverters.listToIterableConverter)
    riftWarp.toolShed.addConvertsMAToNA(MAToNAConverters.listToSetConverter)
    riftWarp.toolShed.addConvertsMAToNA(MAToNAConverters.listToVectorConverter)
  }
}