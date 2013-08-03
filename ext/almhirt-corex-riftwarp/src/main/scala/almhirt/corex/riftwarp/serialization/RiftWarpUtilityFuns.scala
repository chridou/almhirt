package almhirt.corex.riftwarp.serialization

object RiftWarpUtilityFuns {
  def addRiftWarpRegistrations(riftWarp: riftwarp.RiftWarp) {
    riftWarp.packers.addTyped(AggregateRootRefWarpPackaging)
    riftWarp.unpackers.addTyped(AggregateRootRefWarpPackaging)
    
    riftWarp.packers.addTyped(CommandExecutedWarpPackaging)
    riftWarp.unpackers.addTyped(CommandExecutedWarpPackaging)
    
    riftWarp.packers.addTyped(CommandNotExecutedWarpPackaging)
    riftWarp.unpackers.addTyped(CommandNotExecutedWarpPackaging)
    
    riftWarp.packers.addTyped(CommandHandledEventWarpPackaging)
    riftWarp.unpackers.addTyped(CommandHandledEventWarpPackaging)
    
    riftWarp.packers.addTyped(CommandReceivedWarpPackaging)
    riftWarp.unpackers.addTyped(CommandReceivedWarpPackaging)
    
    riftWarp.packers.addTyped(CommandReceivedAsHeaderWarpPackaging)
    riftWarp.unpackers.addTyped(CommandReceivedAsHeaderWarpPackaging)
    
    riftWarp.packers.addTyped(CommandReceivedEventWarpPackaging)
    riftWarp.unpackers.addTyped(CommandReceivedEventWarpPackaging)
    
    riftWarp.packers.addTyped(DomainCommandHeaderWarpPackaging)
    riftWarp.unpackers.addTyped(DomainCommandHeaderWarpPackaging)
    
    riftWarp.packers.addTyped(DomainEventHeaderWarpPackaging)
    riftWarp.unpackers.addTyped(DomainEventHeaderWarpPackaging)
    
    riftWarp.packers.addTyped(ExecutionStateChangedWarpPackaging)
    riftWarp.unpackers.addTyped(ExecutionStateChangedWarpPackaging)
    
    riftWarp.packers.addTyped(ExecutionStartedWarpPackaging)
    riftWarp.unpackers.addTyped(ExecutionStartedWarpPackaging)
    
    riftWarp.packers.addTyped(ExecutionInProcessWarpPackaging)
    riftWarp.unpackers.addTyped(ExecutionInProcessWarpPackaging)
    
    riftWarp.packers.addTyped(ExecutionSuccessfulWarpPackaging)
    riftWarp.packers.addTyped(ExecutionSuccessfulWarpPackaging)
    
    riftWarp.packers.addTyped(ExecutionFailedWarpPackaging)
    riftWarp.packers.addTyped(ExecutionFailedWarpPackaging)
    
    riftWarp.packers.addTyped(ExecutionFinishedStateWarpPackaging)
    riftWarp.packers.addTyped(ExecutionFinishedStateWarpPackaging)
    
    riftWarp.packers.addTyped(ExecutionStateWarpPackaging)
    riftWarp.packers.addTyped(ExecutionStateWarpPackaging)
  }
}