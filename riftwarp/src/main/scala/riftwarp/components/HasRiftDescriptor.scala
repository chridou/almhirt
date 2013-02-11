package riftwarp.components

import riftwarp._

/**
 * As the name says. If mixed into a Decomposer or Recomposer, its the RiftDescriptor used for lookup.
 * On a serializable class, it is the RiftDescriptor to look up a Decomposer.
 */
trait HasRiftDescriptor {
  def riftDescriptor: RiftDescriptor
}

trait HasAlternativeRiftDescriptors extends HasRiftDescriptor { def alternativeRiftDescriptors: List[RiftDescriptor] }
trait HasNoAlternativeRiftDescriptors extends HasAlternativeRiftDescriptors { override val alternativeRiftDescriptors = Nil }

trait HasDefaultRiftDescriptor extends HasRiftDescriptor {
  val riftDescriptor = RiftDescriptor(this.getClass().getName())
}