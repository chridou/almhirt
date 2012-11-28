package almhirt.core.serialization

object RiftWarpUtilityFuns {
  def addRiftWarpRegistrations(riftWarp: riftwarp.RiftWarp) {
    riftWarp.barracks.addDecomposer(new MessageGroupingDecomposer())
    riftWarp.barracks.addDecomposer(new MessageHeaderDecomposer())
    riftWarp.barracks.addDecomposer(new MessageHeaderDecomposer())
    riftWarp.barracks.addDecomposer(new AggregateRootRefDecomposer())
    riftWarp.barracks.addDecomposer(new TrackingTicketDecomposer())
    riftWarp.barracks.addDecomposer(new CommandEnvelopeDecomposer())

    riftWarp.barracks.addRecomposer(new MessageGroupingRecomposer())
    riftWarp.barracks.addRecomposer(new MessageHeaderRecomposer())
    riftWarp.barracks.addRecomposer(new MessageRecomposer())
    riftWarp.barracks.addRecomposer(new AggregateRootRefRecomposer())
    riftWarp.barracks.addRecomposer(new TrackingTicketRecomposer())
    riftWarp.barracks.addRecomposer(new CommandEnvelopeRecomposer())

  }
}