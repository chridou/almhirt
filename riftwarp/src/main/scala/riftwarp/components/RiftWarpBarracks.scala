package riftwarp.components

import scala.reflect.ClassTag
import riftwarp._

trait RiftWarpBarracks extends HasRecomposers with HasDecomposers

object RiftWarpBarracks {
  def apply(decomposers: HasDecomposers, recomposers: HasRecomposers): RiftWarpBarracks = {
    new RiftWarpBarracks {
      def getRawDecomposer(riftDescriptor: RiftDescriptor) = decomposers.getRawDecomposer(riftDescriptor)
      def addDecomposer(decomposer: Decomposer[_ <: AnyRef]) { decomposers.addDecomposer(decomposer) }

      def tryGetRawRecomposer(riftDescriptor: RiftDescriptor) = recomposers.tryGetRawRecomposer(riftDescriptor)
      def tryGetRecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor)(implicit tag: ClassTag[T]) = recomposers.tryGetRecomposer[T](riftDescriptor)

      def addRawRecomposer(recomposer: RawRecomposer) { recomposers.addRawRecomposer(recomposer) }
      def addRecomposer(recomposer: Recomposer[_]) { recomposers.addRecomposer(recomposer) }
    }
  }

  import riftwarp.impl._
  
  def unsafe(): RiftWarpBarracks = apply(new UnsafeDecomposerRegistry(), new UnsafeRecomposerRegistry())
  def concurrent(): RiftWarpBarracks = apply(new ConcurrentDecomposerRegistry(), new ConcurrentRecomposerRegistry())
  
}