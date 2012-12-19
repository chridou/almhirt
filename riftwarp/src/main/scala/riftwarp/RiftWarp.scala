package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almvalidation.flatmap

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
        barracks.getDecomposerForAny(what).bind(decomposer =>
          factoryAndConverters._1.createDematerializer(divertBlobs)(barracks, toolShed).bind(demat =>
            decomposer.decomposeRaw(what)(demat).bind(demat =>
              demat.dematerializeRaw.bind(dimDemat =>
                factoryAndConverters._2.foldLeft(dimDemat.success[Problem])((acc, converter) =>
                  acc.fold(prob => prob.failure, dim => converter.convertRaw(dim))).map(_.asInstanceOf[TDimension]))))))

  def prepareForWarp[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit m: Manifest[TDimension]): AlmValidation[TDimension] =
    getDematerializationFun[TDimension](channel, toolGroup)(NoDivertBlobDivert).bind(fun => fun(what))

  def prepareForWarpWithBlobs[TDimension <: RiftDimension](divertBlobs: BlobDivert)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit m: Manifest[TDimension]): AlmValidation[TDimension] =
    getDematerializationFun[TDimension](channel, toolGroup)(divertBlobs).bind(fun => fun(what))
    
  def lookUpRematerializationArrayFactoryAndConverters[DimSource <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(implicit mS: Manifest[DimSource]): AlmValidation[(RematerializationArrayFactory[_ <: RiftDimension], List[RawDimensionConverter])] = {
    def findRematerializationArrayFactory(converters: List[RawDimensionConverter]): AlmValidation[(RematerializationArrayFactory[_ <: RiftDimension], RawDimensionConverter)] =
      converters match {
        case Nil => UnspecifiedProblem("No RematerializationArrayFactory or converter found").failure
        case x :: xs =>
          option.cata(toolShed.tryGetArrayFactoryByType(x.tTarget)(channel, toolGroup))(
            factory => (factory, x).success,
            findRematerializationArrayFactory(xs))
      }

    option.cata(toolShed.tryGetArrayFactory[DimSource](channel, toolGroup))(
      some => (some, Nil).success,
      findRematerializationArrayFactory(converters.getConvertersFrom[DimSource]).map(tuple => (tuple._1, List(tuple._2))))
  }

  def getRematerializationFun[TSource <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(blobFetch: BlobFetch)(implicit mtarget: Manifest[T], mD: Manifest[TSource]): AlmValidation[TSource => AlmValidation[T]] =
    lookUpRematerializationArrayFactoryAndConverters[TSource](channel, toolGroup).map {
      case (arrayFactory, converters) =>
        (sourceDim: TSource) =>
          converters.foldLeft[AlmValidation[RiftDimension]](sourceDim.success[Problem])((acc, conv) => acc.fold(prob => prob.failure, succ => conv.convertRaw(succ))).bind(dimRematSource =>
            arrayFactory.createRematerializationArrayRaw(dimRematSource, blobFetch)(barracks, toolShed).bind(remat =>
              remat.tryGetTypeDescriptor.bind(tdOpt => {
                val td = tdOpt.getOrElse(TypeDescriptor(mtarget.erasure))
                barracks.getRawRecomposer(td).bind(recomp =>
                  recomp.recomposeRaw(remat).map(res =>
                    res.asInstanceOf[T]))
              })))
    }

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit mtarget: Manifest[T], mD: Manifest[TDimension]): AlmValidation[T] =
    getRematerializationFun[TDimension, T](channel, toolGroup)(NoFetchBlobFetch).bind(fun => fun(warpStream))

  def receiveFromWarpWithBlobs[TDimension <: RiftDimension, T <: AnyRef](blobFetch: BlobFetch)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit mtarget: Manifest[T], mD: Manifest[TDimension]): AlmValidation[T] =
    getRematerializationFun[TDimension, T](channel, toolGroup)(blobFetch).bind(fun => fun(warpStream))

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

    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromMapRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonMapRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonStringRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonCordRematerializationArray)
    
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

    riftWarp.toolShed.addConvertsMAToNA(MAToNAConverters.listToIterableConverter)
    riftWarp.toolShed.addConvertsMAToNA(MAToNAConverters.listToSetConverter)
    riftWarp.toolShed.addConvertsMAToNA(MAToNAConverters.listToVectorConverter)
  }
}