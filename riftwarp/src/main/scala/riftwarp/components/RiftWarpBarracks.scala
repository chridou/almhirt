package riftwarp.components

import riftwarp._

trait RiftWarpBarracks extends HasRecomposers with HasDecomposers

object RiftWarpBarracks {
  def apply(decomposers: HasDecomposers, recomposers: HasRecomposers): RiftWarpBarracks = {
    new RiftWarpBarracks {
      def tryGetRawDecomposer(riftDescriptor: RiftDescriptor) = decomposers.tryGetRawDecomposer(riftDescriptor)
      def tryGetDecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor) = decomposers.tryGetDecomposer[T](riftDescriptor)

      def addRawDecomposer(decomposer: RawDecomposer) { decomposers.addRawDecomposer(decomposer) }
      def addDecomposer(decomposer: Decomposer[_]) { decomposers.addDecomposer(decomposer) }

      def tryGetRawRecomposer(riftDescriptor: RiftDescriptor) = recomposers.tryGetRawRecomposer(riftDescriptor)
      def tryGetRecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor) = recomposers.tryGetRecomposer[T](riftDescriptor)

      def addRawRecomposer(recomposer: RawRecomposer) { recomposers.addRawRecomposer(recomposer) }
      def addRecomposer(recomposer: Recomposer[_]) { recomposers.addRecomposer(recomposer) }
    
    }
  }

  import riftwarp.impl._
  
  def unsafe(): RiftWarpBarracks = apply(new UnsafeDecomposerRegistry(), new UnsafeRecomposerRegistry())
  def concurrent(): RiftWarpBarracks = apply(new ConcurrentDecomposerRegistry(), new ConcurrentRecomposerRegistry())
  
}