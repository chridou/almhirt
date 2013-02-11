package almhirt.ext.core.riftwarp.serialization

object RiftWarpUtilityFuns {
  def addRiftWarpRegistrations(riftWarp: riftwarp.RiftWarp) {
    riftWarp.barracks.addDecomposer(MessageGroupingDecomposer)
    riftWarp.barracks.addDecomposer(MessageHeaderDecomposer)
    riftWarp.barracks.addDecomposer(MessageDecomposer)
    riftWarp.barracks.addDecomposer(CommandEnvelopeDecomposer)
    riftWarp.barracks.addDecomposer(AggregateRootRefDecomposer)
    riftWarp.barracks.addDecomposer(PerformedCreateActionDecomposer)
    riftWarp.barracks.addDecomposer(PerformedUpdateActionDecomposer)
    riftWarp.barracks.addDecomposer(PerformedUnspecifiedActionDecomposer)
    riftWarp.barracks.addDecomposer(PerformedActionDecomposer)
    riftWarp.barracks.addDecomposer(StringTrackingTicketDecomposer)
    riftWarp.barracks.addDecomposer(UuidTrackingTicketDecomposer)
    riftWarp.barracks.addDecomposer(TrackingTicketDecomposer)
    riftWarp.barracks.addDecomposer(InProcessDecomposer)
    riftWarp.barracks.addDecomposer(ExecutedDecomposer)
    riftWarp.barracks.addDecomposer(NotExecutedDecomposer)
    riftWarp.barracks.addDecomposer(OperationStateDecomposer)

    riftWarp.barracks.addRecomposer(MessageGroupingRecomposer)
    riftWarp.barracks.addRecomposer(MessageHeaderRecomposer)
    riftWarp.barracks.addRecomposer(MessageRecomposer)
    riftWarp.barracks.addRecomposer(CommandEnvelopeRecomposer)
    riftWarp.barracks.addRecomposer(AggregateRootRefRecomposer)
    riftWarp.barracks.addRecomposer(PerformedCreateActionRecomposer)
    riftWarp.barracks.addRecomposer(PerformedUpdateActionRecomposer)
    riftWarp.barracks.addRecomposer(PerformedUnspecifiedActionRecomposer)
    riftWarp.barracks.addRecomposer(PerformedActionRecomposer)
    riftWarp.barracks.addRecomposer(StringTrackingTicketRecomposer)
    riftWarp.barracks.addRecomposer(UuidTrackingTicketRecomposer)
    riftWarp.barracks.addRecomposer(TrackingTicketRecomposer)
    riftWarp.barracks.addRecomposer(InProcessRecomposer)
    riftWarp.barracks.addRecomposer(ExecutedRecomposer)
    riftWarp.barracks.addRecomposer(NotExecutedRecomposer)
    riftWarp.barracks.addRecomposer(OperationStateRecomposer)
  }
}