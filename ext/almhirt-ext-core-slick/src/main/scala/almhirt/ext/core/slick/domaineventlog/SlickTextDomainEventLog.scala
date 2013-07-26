package almhirt.ext.core.slick.domaineventlog

import scala.concurrent.ExecutionContext
import scalaz.syntax.validation._
import akka.actor._
import almhirt.messaging.MessagePublisher
import almhirt.common._
import almhirt.domain._
import almhirt.serialization._
import almhirt.core.Almhirt
import com.typesafe.config.Config
import almhirt.ext.core.slick.SlickCreationParams

class SlickTextDomainEventLog private (
  override val messagePublisher: MessagePublisher,
  override val storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
  override val syncIoExecutionContext: ExecutionContext,
  serializer: DomainEventStringSerializer,
  serializationChannel: String)
  extends SlickDomainEventLog with Actor with ActorLogging {
  type TRow = TextDomainEventLogRow

  def domainEventToRow(domainEvent: DomainEvent, channel: String): AlmValidation[TextDomainEventLogRow] = {
    for {
      serialized <- serializer.serialize(channel)(domainEvent, Map.empty)
    } yield TextDomainEventLogRow(domainEvent.id, domainEvent.aggId, domainEvent.aggVersion, channel, serialized._1)
  }

  def rowToDomainEvent(row: TextDomainEventLogRow): AlmValidation[DomainEvent] =
    serializer.deserialize(row.channel)(row.payload, Map.empty)

  protected override def receiveDomainEventLogMsg: Receive = currentState(serializationChannel)

  def receive: Receive = receiveDomainEventLogMsg
}

object SlickTextDomainEventLog {
  import almhirt.configuration._
  import almhirt.almvalidation.kit._

  def props(
    messagePublisher: MessagePublisher,
    storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
    syncIoExecutionContext: ExecutionContext,
    serializer: DomainEventStringSerializer,
    serializationChannel: String): AlmValidation[Props] =
    Props(new SlickTextDomainEventLog(
      messagePublisher,
      storeComponent,
      syncIoExecutionContext,
      serializer,
      serializationChannel)).success

  def props(
    configSection: Config,
    messagePublisher: MessagePublisher,
    storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
    syncIoExecutionContext: ExecutionContext,
    serializer: DomainEventStringSerializer): AlmValidation[Props] =
    for {
      channel <- configSection.v[String]("serialization-channel").flatMap(_.notEmptyOrWhitespace)
      res <- props(messagePublisher, storeComponent, syncIoExecutionContext, serializer, channel)
    } yield res

  def props(
    theAlmhirt: Almhirt,
    configPath: String,
    storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
    serializer: DomainEventStringSerializer): AlmValidation[Props] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      channel <- configSection.v[String]("serialization-channel").flatMap(_.notEmptyOrWhitespace)
      res <- props(theAlmhirt.messageBus, storeComponent, theAlmhirt.syncIoWorker, serializer, channel)
    } yield res

  def props(
    theAlmhirt: Almhirt,
    configSection: Config,
    storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
    serializer: DomainEventStringSerializer): AlmValidation[Props] =
    for {
      channel <- configSection.v[String]("serialization-channel").flatMap(_.notEmptyOrWhitespace)
      res <- props(theAlmhirt.messageBus, storeComponent, theAlmhirt.syncIoWorker, serializer, channel)
    } yield res
    
  def create(theAlmhirt: Almhirt, configPath: String, serializer: DomainEventStringSerializer): AlmValidation[SlickCreationParams] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      storeComponent <- TextDomainEventLogDataAccess(configSection)
      createSchema <- configSection.opt[Boolean]("create-schema").map(_.getOrElse(false))
      dropSchema <- configSection.opt[Boolean]("drop-schema").map(_.getOrElse(false))
      theProps <- props(theAlmhirt, configPath, storeComponent, serializer)
    } yield new SlickCreationParams {
      val props = theProps
      val initAction = () => if (createSchema) storeComponent.create else ().success
      val closeAction = () => if (dropSchema) storeComponent.drop else ().success
    }
    
  def create(theAlmhirt: Almhirt, configPath: String, serializer: DomainEventStringSerializer, createAndDrop: Boolean): AlmValidation[SlickCreationParams] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      storeComponent <- TextDomainEventLogDataAccess(configSection)
      theProps <- props(theAlmhirt, configPath, storeComponent, serializer)
    } yield new SlickCreationParams {
      val props = theProps
      val initAction = () => if (createAndDrop) storeComponent.create else ().success
      val closeAction = () => if (createAndDrop) storeComponent.drop else ().success
    }

  def create(theAlmhirt: Almhirt, configSection: Config, serializer: DomainEventStringSerializer, createAndDrop: Boolean): AlmValidation[SlickCreationParams] =
    for {
      storeComponent <- TextDomainEventLogDataAccess(configSection)
      theProps <- props(theAlmhirt, configSection, storeComponent, serializer)
    } yield new SlickCreationParams {
      val props = theProps
      val initAction = () => if (createAndDrop) storeComponent.create else ().success
      val closeAction = () => if (createAndDrop) storeComponent.drop else ().success
    }
    
}

