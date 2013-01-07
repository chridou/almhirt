package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.components._

trait RiftWarp {
  def barracks: RiftWarpBarracks
  def toolShed: RiftWarpToolShed
  def converters: HasDimensionConverters
  def channels: ChannelRegistry

  def prepareForWarp[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit m: Manifest[TDimension]): AlmValidation[TDimension] =
    barracks.getDecomposerForAny[AnyRef](what).flatMap(decomposer =>
      RiftWarpFuns.getDematerializationFun[TDimension, AnyRef](channel, toolGroup)(NoDivertBlobDivert)(this, m).flatMap(fun => 
        fun(what, decomposer)))

  def prepareForWarpWithBlobs[TDimension <: RiftDimension](divertBlobs: BlobDivert)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit m: Manifest[TDimension]): AlmValidation[TDimension] =
    barracks.getDecomposerForAny[AnyRef](what).flatMap(decomposer =>
      RiftWarpFuns.getDematerializationFun[TDimension, AnyRef](channel, toolGroup)(divertBlobs)(this, m).flatMap(fun => 
        fun(what, decomposer)))

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit mD: Manifest[TDimension], mTarget: Manifest[T]): AlmValidation[T] =
    for {
      recomposeFun <- RiftWarpFuns.getRecomposeFun[TDimension, T](channel, toolGroup)(barracks.lookUpFromRematerializer[T])(NoFetchBlobFetch)(mD, mTarget, this)
      recomposed <- recomposeFun(warpStream)
    } yield recomposed

  def receiveFromWarpWithBlobs[TDimension <: RiftDimension, T <: AnyRef](blobFetch: BlobFetch)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit mD: Manifest[TDimension], mTarget: Manifest[T]): AlmValidation[T] =
    for {
      recomposeFun <- RiftWarpFuns.getRecomposeFun[TDimension, T](channel, toolGroup)(barracks.lookUpFromRematerializer[T])(blobFetch)(mD, mTarget, this)
      recomposed <- recomposeFun(warpStream)
    } yield recomposed

}

object RiftWarp {
  def apply(theBarracks: RiftWarpBarracks, theToolShed: RiftWarpToolShed, theConverters: HasDimensionConverters, theChannels: ChannelRegistry): RiftWarp =
    new RiftWarp {
      val barracks = theBarracks
      val toolShed = theToolShed
      val converters = theConverters
      val channels = theChannels
    }

  def unsafe(): RiftWarp = apply(RiftWarpBarracks.unsafe, RiftWarpToolShed.unsafe, new impl.UnsafeDimensionConverterRegistry, impl.UnsafeChannelRegistry())
  def unsafeWithDefaults(): RiftWarp = {
    val riftWarp = unsafe()
    initializeWithDefaults(riftWarp)
    riftWarp
  }

  def concurrent(): RiftWarp = apply(RiftWarpBarracks.concurrent, RiftWarpToolShed.concurrent, new impl.ConcurrentDimensionConverterRegistry, impl.ConcurrentChannelRegistry())
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
    riftWarp.toolShed.addRematerializerFactory(impl.rematerializers.FromXmlElemRematerializer)
    
    riftWarp.converters.addConverter(DimensionNiceStringToString)
    riftWarp.converters.addConverter(DimensionNiceCordToCord)
    riftWarp.converters.addConverter(DimensionConverterStringToCord)
    riftWarp.converters.addConverter(DimensionConverterCordToString)
    riftWarp.converters.addConverter(DimensionConverterStringToXmlElem)
    riftWarp.converters.addConverter(DimensionConverterCordToXmlElem)
    riftWarp.converters.addConverter(DimensionConverterXmlElemToString)
    riftWarp.converters.addConverter(DimensionConverterXmlElemToCord)
    riftWarp.converters.addConverter(DimensionConverterXmlElemToNiceString)
    riftWarp.converters.addConverter(DimensionConverterXmlElemToNiceCord)

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
    
    serialization.common.Problems.registerAllCommonProblems(riftWarp)
    
    RiftChannel.register(riftWarp.channels)
  }
}