package riftwarp.components

import language.higherKinds
import scala.reflect.ClassTag
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait RiftWarpToolShed extends HasWarpSequencers with HasExtractorFactories {
  def tryGetExtractor[TDimension <: RiftDimension](from: TDimension)(channel: RiftChannel)(implicit hasRecomposers: HasRecomposers, hasExtractorFactories: HasExtractorFactories, mD: ClassTag[TDimension]): AlmValidation[Option[Extractor]] =
    tryGetExtractorFactory[TDimension](channel).map(factory => factory.createExtractor(from)).validationOut

  def tryGetWarpSequencer[TDimension <: RiftDimension](to: TDimension)(channel: RiftChannel)(implicit hasDecomposers: HasDecomposers, mD: ClassTag[TDimension]): AlmValidation[Option[WarpSequencer[TDimension]]] =
    tryGetWarpSequencerFactory[TDimension](channel).map(factory => factory.createWarpSequencer).validationOut
}

object RiftWarpToolShed {
  def apply(hasWarpSequencers: HasWarpSequencers, hasExtractorFactories: HasExtractorFactories): RiftWarpToolShed = {
    new RiftWarpToolShed {
      def addWarpSequencerFactory(factory: WarpSequencerFactory[_ <: RiftDimension], asChannelDefault: Boolean) { hasWarpSequencers.addWarpSequencerFactory(factory, asChannelDefault) }
      def tryGetWarpSequencerFactoryByType(tDimemsion: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = hasWarpSequencers.tryGetWarpSequencerFactoryByType(tDimemsion)(channel, toolGroup)

      def addExtractorFactory(arrayFactory: ExtractorFactory[_ <: RiftDimension], isChannelDefault: Boolean = false) { hasExtractorFactories.addExtractorFactory(arrayFactory, isChannelDefault) }
      def tryGetExtractorFactoryByType(tDimension: Class[_ <: RiftDimension])(channel: RiftChannel, toolGroup: Option[ToolGroup] = None) = hasExtractorFactories.tryGetExtractorFactoryByType(tDimension)(channel, toolGroup)

    }
  }

  import riftwarp.impl._
  
  def unsafe(): RiftWarpToolShed = 
    apply(new UnsafeWarpSequencerRegistry, new UnsafeExtractorFactoryRegistry)
  
  def concurrent(): RiftWarpToolShed = 
    apply(new ConcurrentWarpSequencerRegistry, new ConcurrentExtractorFactoryRegistry)
  
}