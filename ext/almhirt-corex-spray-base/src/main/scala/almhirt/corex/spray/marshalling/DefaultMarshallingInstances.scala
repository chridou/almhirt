package almhirt.corex.spray.marshalling

object DefaultCoreMarshallingInstances {
  implicit val DomainEventMarshallingInst = DomainEventMarshalling
  implicit val ExecutionStateMarshallingInst = ExecutionStateMarshalling

  implicit val DomainEventsMarshallingInst = DomainEventsMarshalling
  implicit val ExecutionStatesMarshallingInst = ExecutionStatesMarshalling
}