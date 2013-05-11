package almhirt.ext.core.riftwarp.serialization

object RiftWarpUtilityFuns {
  def addRiftWarpRegistrations(riftWarp: riftwarp.RiftWarp) {
    riftWarp.packers.addTyped(MessageGroupingWarpPacker)
    riftWarp.packers.addTyped(MessageHeaderWarpPacker)
    riftWarp.packers.addTyped(MessageWarpPacker)
    riftWarp.packers.addTyped(CommandEnvelopeWarpPacker)
    riftWarp.packers.addTyped(AggregateRootRefWarpPacker)
    riftWarp.packers.addTyped(PerformedCreateActionWarpPacker)
    riftWarp.packers.addTyped(PerformedUpdateActionWarpPacker)
    riftWarp.packers.addTyped(PerformedDeleteActionWarpPacker)
    riftWarp.packers.addTyped(PerformedNoActionWarpPacker)
    riftWarp.packers.addTyped(PerformedDomainActionWarpPacker)
    riftWarp.packers.addTyped(PerformedActionWarpPacker)
    riftWarp.packers.addTyped(FullComandInfoWarpPacker)
    riftWarp.packers.addTyped(HeadCommandInfoWarpPacker)
    riftWarp.packers.addTyped(CommandInfoWarpPacker)
    riftWarp.packers.addTyped(CommandWithMaxResponseDurationWarpPacker)
    riftWarp.packers.addTyped(StringTrackingTicketWarpPacker)
    riftWarp.packers.addTyped(UuidTrackingTicketWarpPacker)
    riftWarp.packers.addTyped(TrackingTicketWarpPacker)
    riftWarp.packers.addTyped(InProcessWarpPacker)
    riftWarp.packers.addTyped(ExecutedWarpPacker)
    riftWarp.packers.addTyped(NotExecutedWarpPacker)
    riftWarp.packers.addTyped(OperationStateWarpPacker)
    riftWarp.packers.addTyped(DomainEventHeaderWarpPacker)
    riftWarp.packers.addTyped(BasicEventHeaderWarpPacker)
    riftWarp.packers.addTyped(EventHeaderWarpPacker)
    riftWarp.packers.addTyped(ProblemEventWarpPacker)
    riftWarp.packers.addTyped(OperationStateEventWarpPacker)

    riftWarp.unpackers.addTyped(MessageGroupingWarpUnpacker)
    riftWarp.unpackers.addTyped(MessageHeaderWarpUnpacker)
    riftWarp.unpackers.addTyped(MessageWarpUnpacker)
    riftWarp.unpackers.addTyped(CommandEnvelopeWarpUnpacker)
    riftWarp.unpackers.addTyped(AggregateRootRefWarpUnpacker)
    riftWarp.unpackers.addTyped(PerformedCreateActionWarpUnpacker)
    riftWarp.unpackers.addTyped(PerformedUpdateActionWarpUnpacker)
    riftWarp.unpackers.addTyped(PerformedDeleteActionWarpUnpacker)
    riftWarp.unpackers.addTyped(PerformedNoActionWarpUnpacker)
    riftWarp.unpackers.addTyped(PerformedDomainActionWarpUnpacker)
    riftWarp.unpackers.addTyped(PerformedActionWarpUnpacker)
    riftWarp.unpackers.addTyped(FullComandInfoWarpUnpacker)
    riftWarp.unpackers.addTyped(HeadCommandInfoWarpUnpacker)
    riftWarp.unpackers.addTyped(CommandInfoWarpUnpacker)
    riftWarp.unpackers.addTyped(CommandWithMaxResponseDurationUnpacker)
    riftWarp.unpackers.addTyped(StringTrackingTicketWarpUnpacker)
    riftWarp.unpackers.addTyped(UuidTrackingTicketWarpUnpacker)
    riftWarp.unpackers.addTyped(TrackingTicketWarpUnpacker)
    riftWarp.unpackers.addTyped(InProcessWarpUnpacker)
    riftWarp.unpackers.addTyped(ExecutedWarpUnpacker)
    riftWarp.unpackers.addTyped(NotExecutedWarpUnpacker)
    riftWarp.unpackers.addTyped(OperationStateWarpUnpacker)
    riftWarp.unpackers.addTyped(DomainEventHeaderWarpUnpacker)
    riftWarp.unpackers.addTyped(BasicEventHeaderWarpUnpacker)
    riftWarp.unpackers.addTyped(EventHeaderWarpUnpacker)
    riftWarp.unpackers.addTyped(ProblemEventWarpUnpacker)
    riftWarp.unpackers.addTyped(OperationStateEventWarpUnpacker)
  }
}