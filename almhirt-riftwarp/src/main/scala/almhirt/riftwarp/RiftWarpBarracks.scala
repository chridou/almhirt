package almhirt.riftwarp

trait RiftWarpBarracks extends HasRecomposers with HasDecomposers

object RiftWarpBarracks {
  def apply(decomposers: HasDecomposers, recomposers: HasRecomposers): RiftWarpBarracks = {
    new RiftWarpBarracks {
      def tryGetRawDecomposer(typeDescriptor: TypeDescriptor) = decomposers.tryGetRawDecomposer(typeDescriptor)
      def tryGetDecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor) = decomposers.tryGetDecomposer[T](typeDescriptor)

      def addRawDecomposer(decomposer: RawDecomposer) { decomposers.addRawDecomposer(decomposer) }
      def addDecomposer(decomposer: Decomposer[_]) { decomposers.addDecomposer(decomposer) }

      def tryGetRawRecomposer(typeDescriptor: TypeDescriptor) = recomposers.tryGetRawRecomposer(typeDescriptor)
      def tryGetRecomposer[T <: AnyRef](typeDescriptor: TypeDescriptor) = recomposers.tryGetRecomposer[T](typeDescriptor)

      def addRawRecomposer(recomposer: RawRecomposer) { recomposers.addRawRecomposer(recomposer) }
      def addRecomposer(recomposer: Recomposer[_]) { recomposers.addRecomposer(recomposer) }
    
    }
  }

  def unsafe(): RiftWarpBarracks = apply(new impl.UnsafeDecomposerRegistry(), new impl.UnsafeRecomposerRegistry())
}