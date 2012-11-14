package almhirt.riftwarp

trait RiftWarp {
  def barracks: RiftWarpBarracks
  def toolShed: RiftWarpToolShed
}

object RiftWarp {
  def apply(theBarracks: RiftWarpBarracks, theToolShed: RiftWarpToolShed): RiftWarp =
    new RiftWarp {
      val barracks = theBarracks
      val toolShed = theToolShed
    }
  
  def unsafe(): RiftWarp = apply(RiftWarpBarracks.unsafe, RiftWarpToolShed.unsafe)
  def unsafeWithDefaults(): RiftWarp = {
    val riftWarp = apply(RiftWarpBarracks.unsafe, RiftWarpToolShed.unsafe)
    initializeWithDefaults(riftWarp)
    riftWarp
  }
  
  private def initializeWithDefaults(riftWarp: RiftWarp) {
    riftWarp.toolShed.addDematerializer(impl.dematerializers.ToMapDematerializer(Map.empty)(riftWarp.barracks))
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromMapRematerializationArray)
  }
}