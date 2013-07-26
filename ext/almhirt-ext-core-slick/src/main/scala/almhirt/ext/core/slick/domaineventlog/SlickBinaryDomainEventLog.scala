package almhirt.ext.core.slick.domaineventlog

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.domaineventlog.DomainEventLog
import almhirt.messaging.MessagePublisher
import scala.concurrent.ExecutionContext

class SlickBinaryDomainEventLog(
  override val messagePublisher: MessagePublisher,
  override val storeComponent: DomainEventLogStoreComponent[BinaryDomainEventLogRow],
  override val syncIoExecutionContext: ExecutionContext,
  serializer: DomainEventBinarySerializer,
  serializationChannel: String)
  extends SlickDomainEventLog with Actor with ActorLogging {
  type TRow = TextDomainEventLogRow

  def domainEventToRow(domainEvent: DomainEvent, channel: String): AlmValidation[BinaryDomainEventLogRow] = {
    for {
      serialized <- serializer.serialize(channel)(domainEvent, Map.empty)
    } yield BinaryDomainEventLogRow(domainEvent.id, domainEvent.aggId, domainEvent.aggVersion, channel, serialized._1)
  }

  def rowToDomainEvent(row: BinaryDomainEventLogRow): AlmValidation[DomainEvent] =
    serializer.deserialize(row.channel)(row.payload, Map.empty)
    
  def receive: Receive = receiveDomainEventLogMsg(serializationChannel)
    
}