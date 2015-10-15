package riftwarpx.almhirt.serialization.core

import riftwarp.RiftWarp

object Serialization {
  def addToRiftwarp(rw: RiftWarp): RiftWarp = {
    rw.packers.addTyped(AppNameWarpPackaging)
    rw.unpackers.addTyped(AppNameWarpPackaging)
    rw.packers.addTyped(ComponentNameWarpPackaging)
    rw.unpackers.addTyped(ComponentNameWarpPackaging)
    rw.packers.addTyped(ComponentIdWarpPackaging)
    rw.unpackers.addTyped(ComponentIdWarpPackaging)
    rw.packers.addTyped(GlobalComponentIdWarpPackaging)
    rw.unpackers.addTyped(GlobalComponentIdWarpPackaging)

    rw.packers.addTyped(FailureReportedWarpPackaging)
    rw.unpackers.addTyped(FailureReportedWarpPackaging)
    rw.packers.addTyped(EventNotProcessedWarpPackaging)
    rw.unpackers.addTyped(EventNotProcessedWarpPackaging)
    rw.packers.addTyped(CommandRejectedWarpPackaging)
    rw.unpackers.addTyped(CommandRejectedWarpPackaging)
    rw.packers.addTyped(RuntimeStateRecordedPackaging)
    rw.unpackers.addTyped(RuntimeStateRecordedPackaging)
    rw
  }
}