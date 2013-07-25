package almhirt.ext.core.slick.domaineventlog

import scala.concurrent.ExecutionContext
import akka.actor._
import almhirt.messaging.MessagePublisher
import almhirt.common._
import almhirt.domain.DomainEvent
import almhirt.serialization._

class SlickTextDomainEventLog(
  override val messagePublisher: MessagePublisher,
  override val storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
  override val syncIoExecutionContext: ExecutionContext,
  serializer: StringSerializingToFixedChannel[DomainEvent, DomainEvent])
  extends SlickDomainEventLog with Actor with ActorLogging {
  type TRow = TextDomainEventLogRow

  def domainEventToRow(domainEvent: DomainEvent): AlmValidation[TextDomainEventLogRow] = {
    for {
      serialized <- serializer.serialize(domainEvent, Map.empty)
    } yield TextDomainEventLogRow(domainEvent.id, domainEvent.aggId, domainEvent.aggVersion, serializer.channel, serialized._1)
  }

  def rowToDomainEvent(row: TextDomainEventLogRow): AlmValidation[DomainEvent] =
    serializer.deserialize(row.channel)(row.payload, Map.empty)
}

class SlickBinaryDomainEventLog(
  override val messagePublisher: MessagePublisher,
  override val storeComponent: DomainEventLogStoreComponent[BinaryDomainEventLogRow],
  override val syncIoExecutionContext: ExecutionContext,
  serializer: BinarySerializingToFixedChannel[DomainEvent, DomainEvent])
  extends SlickDomainEventLog with Actor with ActorLogging {
  type TRow = TextDomainEventLogRow

  def domainEventToRow(domainEvent: DomainEvent): AlmValidation[BinaryDomainEventLogRow] = {
    for {
      serialized <- serializer.serialize(domainEvent, Map.empty)
    } yield BinaryDomainEventLogRow(domainEvent.id, domainEvent.aggId, domainEvent.aggVersion, serializer.channel, serialized._1)
  }

  def rowToDomainEvent(row: BinaryDomainEventLogRow): AlmValidation[DomainEvent] =
    serializer.deserialize(row.channel)(row.payload, Map.empty)
}