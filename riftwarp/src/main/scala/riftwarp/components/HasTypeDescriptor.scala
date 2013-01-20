package riftwarp.components

import riftwarp._

trait HasRiftDescriptor {
  def riftDescriptor: RiftDescriptor
}

trait HasDefaultRiftDescriptor extends HasRiftDescriptor {
  val riftDescriptor = RiftDescriptor(this.getClass().getName())
}