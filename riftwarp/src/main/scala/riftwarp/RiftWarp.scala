package riftwarp

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp.components._

trait RiftWarp {
  def barracks: RiftWarpBarracks
  def toolShed: RiftWarpToolShed
  def converters: HasDimensionConverters
  def channels: ChannelRegistry

  def prepareForWarp[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit cDim: ClassTag[TDimension]): AlmValidation[TDimension] =
    barracks.getRawDecomposerFor(what).flatMap(decomposer =>
      RiftWarpFuns.getDematerializationFun[TDimension](channel, toolGroup)(this, cDim).flatMap(fun =>
        fun(what, decomposer)))

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit cDim: ClassTag[TDimension], cTarget: ClassTag[T]): AlmValidation[T] = {
    def findRecomposer(remat: Extractor) = barracks.lookUpFromRematerializer[T](remat, Some(RiftDescriptor(cTarget.runtimeClass)))
    for {
      recomposeFun <- RiftWarpFuns.getRecomposeFun[TDimension, T](channel, toolGroup)(findRecomposer)(cDim, cTarget, this)
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
    riftWarp.toolShed.addWarpSequencerFactory(impl.dematerializers.ToJsonCordWarpSequencer)
    riftWarp.toolShed.addWarpSequencerFactory(impl.dematerializers.ToXmlElemWarpSequencer)

    riftWarp.toolShed.addExtractorFactory(impl.rematerializers.FromStdLibJsonExtractor)
    riftWarp.toolShed.addExtractorFactory(impl.rematerializers.FromStdLibJsonStringExtractor)
    riftWarp.toolShed.addExtractorFactory(impl.rematerializers.FromStdLibJsonCordExtractor)
    riftWarp.toolShed.addExtractorFactory(riftwarp.std.FromStdLibXmlExtractor)
    riftWarp.toolShed.addExtractorFactory(riftwarp.std.FromStdLibXmlStringExtractor)
    riftWarp.toolShed.addExtractorFactory(riftwarp.std.FromStdLibXmlCordExtractor)

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