package almhirt.corex.spray.marshalling

object DefaultCoreMarshallingInstances {
  val DomainEventMarshallingInst = DomainEventMarshalling
  implicit val ExecutionStateMarshallingInst = ExecutionStateMarshalling

  implicit val DomainEventsMarshallingInst = DomainEventsMarshalling
  implicit val DomainCommandsMarshallingInst = DomainCommandsMarshalling
  implicit val ExecutionStatesMarshallingInst = ExecutionStatesMarshalling
}