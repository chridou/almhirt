package almhirt.riftwarp

trait RiftWarpBarracks extends HasRecomposers with HasDecomposers

object RiftWarpBarracks {
  def apply(decomposers: HasDecomposers, recomposers: HasRecomposers): RiftWarpBarracks = {
    new RiftWarpBarracks {
      def tryGetDecomposerByName(typeDescriptor: String) = decomposers.tryGetDecomposerByName(typeDescriptor)
      def tryGetRecomposerByName(typeDescriptor: String) = recomposers.tryGetRecomposerByName(typeDescriptor)

      def addDecomposerByName(typeDescriptor: String, decomposer: AnyRef) { decomposers.addDecomposerByName(typeDescriptor, decomposer) }
      def addRecomposerByName(typeDescriptor: String, recomposer: AnyRef) { recomposers.addRecomposerByName(typeDescriptor, recomposer) }
    }
  }
}