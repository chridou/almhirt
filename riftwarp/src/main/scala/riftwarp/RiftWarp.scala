package riftwarp

import scala.reflect.ClassTag
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

  def prepareForWarp[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit cDim: ClassTag[TDimension]): AlmValidation[TDimension] =
    barracks.getRawDecomposerFor(what).flatMap(decomposer =>
      RiftWarpFuns.getDematerializationFun[TDimension](channel, toolGroup)(NoDivertBlobDivert)(this, cDim).flatMap(fun =>
        fun(what, decomposer)))

  def prepareForWarpWithBlobs[TDimension <: RiftDimension](divertBlobs: BlobDivert)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit cDim: ClassTag[TDimension]): AlmValidation[TDimension] =
    barracks.getRawDecomposerFor(what).flatMap(decomposer =>
      RiftWarpFuns.getDematerializationFun[TDimension](channel, toolGroup)(divertBlobs)(this, cDim).flatMap(fun =>
        fun(what, decomposer)))

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit cDim: ClassTag[TDimension], cTarget: ClassTag[T]): AlmValidation[T] = {
    def findRecomposer(remat: Rematerializer) = barracks.lookUpFromRematerializer[T](remat, Some(RiftDescriptor(cTarget.runtimeClass)))
    for {
      recomposeFun <- RiftWarpFuns.getRecomposeFun[TDimension, T](channel, toolGroup)(findRecomposer)(NoFetchBlobFetch)(cDim, cTarget, this)
      recomposed <- recomposeFun(warpStream)
    } yield recomposed
  }

  def receiveFromWarpWithBlobs[TDimension <: RiftDimension, T <: AnyRef](blobFetch: BlobFetch)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit cDim: ClassTag[TDimension], cTarget: ClassTag[T]): AlmValidation[T] = {
    def findRecomposer(remat: Rematerializer) = barracks.lookUpFromRematerializer[T](remat, Some(RiftDescriptor(cTarget.runtimeClass)))
    for {
      recomposeFun <- RiftWarpFuns.getRecomposeFun[TDimension, T](channel, toolGroup)(findRecomposer)(blobFetch)(cDim, cTarget, this)
      recomposed <- recomposeFun(warpStream)
    } yield recomposed
  }

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
    //riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToMapDematerializer)
    riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToJsonCordDematerializer)
    //riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToXmlElemDematerializer)

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

    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.RiftDescriptorDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.RiftDescriptorRecomposer)

    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.HasAThrowableDescribedDecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.HasAThrowableDecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.ThrowableRepresentationDecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.CauseIsThrowableDecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.CauseIsProblemDecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.ProblemCauseDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.HasAThrowableDescribedRecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.CauseIsProblemRecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.ProblemCauseRecomposer)

    RiftChannel.register(riftWarp.channels)
  }
}