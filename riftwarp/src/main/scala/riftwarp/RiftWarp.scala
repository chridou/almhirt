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
      RiftWarpFuns.getDematerializationFun[TDimension](channel, toolGroup)(BlobSeparationDisabled)(this, cDim).flatMap(fun =>
        fun(what, decomposer).map(_._1)))

  def prepareForWarpWithBlobs[TDimension <: RiftDimension](blobPolicy: BlobSerializationPolicy)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit cDim: ClassTag[TDimension]): AlmValidation[(TDimension, Vector[ExtractedBlobReference])] =
    barracks.getRawDecomposerFor(what).flatMap(decomposer =>
      RiftWarpFuns.getDematerializationFun[TDimension](channel, toolGroup)(blobPolicy)(this, cDim).flatMap(fun =>
        fun(what, decomposer)))

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit cDim: ClassTag[TDimension], cTarget: ClassTag[T]): AlmValidation[T] = {
    def findRecomposer(remat: Extractor) = barracks.lookUpFromRematerializer[T](remat, Some(RiftDescriptor(cTarget.runtimeClass)))
    for {
      recomposeFun <- RiftWarpFuns.getRecomposeFun[TDimension, T](channel, toolGroup)(findRecomposer)(BlobIntegrationDisabled)(cDim, cTarget, this)
      recomposed <- recomposeFun(warpStream)
    } yield recomposed
  }

  def receiveFromWarpWithBlobs[TDimension <: RiftDimension, T <: AnyRef](blobPolicy: BlobDeserializationPolicy)(channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit cDim: ClassTag[TDimension], cTarget: ClassTag[T]): AlmValidation[T] = {
    def findRecomposer(remat: Extractor) = barracks.lookUpFromRematerializer[T](remat, Some(RiftDescriptor(cTarget.runtimeClass)))
    for {
      recomposeFun <- RiftWarpFuns.getRecomposeFun[TDimension, T](channel, toolGroup)(findRecomposer)(blobPolicy)(cDim, cTarget, this)
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
    riftWarp.toolShed.addExtractorFactory(impl.rematerializers.FromStdLibXmlExtractor)
    riftWarp.toolShed.addExtractorFactory(impl.rematerializers.FromStdLibXmlStringExtractor)
    riftWarp.toolShed.addExtractorFactory(impl.rematerializers.FromStdLibXmlCordExtractor)

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

    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.BlobArrayValueDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.BlobArrayValueRecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.BlobValueDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.BlobValueRecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.BlobRefFilePathDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.BlobRefFilePathRecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.BlobRefByNameDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.BlobRefByNameRecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.BlobRefByUuidDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.BlobRefByUuidRecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.BlobRefByUriDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.BlobRefByUriRecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.BlobReferenceDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.BlobReferenceRecomposer)
    riftWarp.barracks.addDecomposer(riftwarp.serialization.common.BlobRepresentationDecomposer)
    riftWarp.barracks.addRecomposer(riftwarp.serialization.common.BlobRepresentationRecomposer)
    
    RiftChannel.register(riftWarp.channels)
  }
}