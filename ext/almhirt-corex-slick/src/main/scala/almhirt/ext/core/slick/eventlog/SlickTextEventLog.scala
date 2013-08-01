package almhirt.ext.core.slick.eventlog

import scala.concurrent.ExecutionContext
import scalaz.syntax.validation._
import akka.actor._
import almhirt.messaging.MessagePublisher
import almhirt.common._
import almhirt.serialization._
import almhirt.core.Almhirt
import com.typesafe.config.Config
import almhirt.ext.core.slick.SlickCreationParams

class SlickTextEventLog private (
  override val messagePublisher: MessagePublisher,
  override val storeComponent: EventLogStoreComponent[TextEventLogRow],
  override val syncIoExecutionContext: ExecutionContext,
  override val canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes,
  serializer: EventStringSerializer,
  serializationChannel: String)
  extends SlickEventLog with Actor with ActorLogging {
  type TRow = TextEventLogRow

  override def eventToRow(event: Event, channel: String): AlmValidation[TextEventLogRow] = {
   import almhirt.ext.core.slick.TypeConversion._
   for {
      serialized <- serializer.serialize(channel)(event, Map.empty)
    } yield TextEventLogRow(event.eventId, event.timestamp, channel, serialized._1)
  }

  override def rowToEvent(row: TextEventLogRow): AlmValidation[Event] =
    serializer.deserialize(row.channel)(row.payload, Map.empty)

  protected override def receiveEventLogMsg: Receive = currentState(serializationChannel)

  def receive: Receive = receiveEventLogMsg
}

object SlickTextEventLog {
  import almhirt.configuration._
  import almhirt.almvalidation.kit._

  def props(
    messagePublisher: MessagePublisher,
    storeComponent: EventLogStoreComponent[TextEventLogRow],
    syncIoExecutionContext: ExecutionContext,
    canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes,
    serializer: EventStringSerializer,
    serializationChannel: String): AlmValidation[Props] =
    Props(new SlickTextEventLog(
      messagePublisher,
      storeComponent,
      syncIoExecutionContext,
      canCreateUuidsAndDateTimes,
      serializer,
      serializationChannel)).success

  def props(
    configSection: Config,
    messagePublisher: MessagePublisher,
    storeComponent: EventLogStoreComponent[TextEventLogRow],
    syncIoExecutionContext: ExecutionContext,
    canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes,
    serializer: EventStringSerializer): AlmValidation[Props] =
    for {
      channel <- configSection.v[String]("serialization-channel").flatMap(_.notEmptyOrWhitespace)
      res <- props(messagePublisher, storeComponent, syncIoExecutionContext, canCreateUuidsAndDateTimes, serializer, channel)
    } yield res

  def props(
    theAlmhirt: Almhirt,
    configPath: String,
    storeComponent: EventLogStoreComponent[TextEventLogRow],
    serializer: EventStringSerializer): AlmValidation[Props] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      channel <- configSection.v[String]("serialization-channel").flatMap(_.notEmptyOrWhitespace)
      res <- props(theAlmhirt.messageBus, storeComponent, theAlmhirt.syncIoWorker, theAlmhirt, serializer, channel)
    } yield res

  def props(
    theAlmhirt: Almhirt,
    configSection: Config,
    storeComponent: EventLogStoreComponent[TextEventLogRow],
    serializer: EventStringSerializer): AlmValidation[Props] =
    for {
      channel <- configSection.v[String]("serialization-channel").flatMap(_.notEmptyOrWhitespace)
      res <- props(theAlmhirt.messageBus, storeComponent, theAlmhirt.syncIoWorker, theAlmhirt, serializer, channel)
    } yield res
    
  def create(theAlmhirt: Almhirt, configPath: String, serializer: EventStringSerializer): AlmValidation[SlickCreationParams] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      storeComponent <- TextEventLogDataAccess(configSection)
      createSchema <- configSection.opt[Boolean]("create-schema").map(_.getOrElse(false))
      dropSchema <- configSection.opt[Boolean]("drop-schema").map(_.getOrElse(false))
      theProps <- props(theAlmhirt, configPath, storeComponent, serializer)
    } yield new SlickCreationParams {
      val props = theProps
      val initAction = () => if (createSchema) storeComponent.create else ().success
      val closeAction = () => if (dropSchema) storeComponent.drop else ().success
    }
    
  def create(theAlmhirt: Almhirt, configPath: String, serializer: EventStringSerializer, createAndDrop: Boolean): AlmValidation[SlickCreationParams] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      storeComponent <- TextEventLogDataAccess(configSection)
      theProps <- props(theAlmhirt, configPath, storeComponent, serializer)
    } yield new SlickCreationParams {
      val props = theProps
      val initAction = () => if (createAndDrop) storeComponent.create else ().success
      val closeAction = () => if (createAndDrop) storeComponent.drop else ().success
    }

  def create(theAlmhirt: Almhirt, configSection: Config, serializer: EventStringSerializer, createAndDrop: Boolean): AlmValidation[SlickCreationParams] =
    for {
      storeComponent <- TextEventLogDataAccess(configSection)
      theProps <- props(theAlmhirt, configSection, storeComponent, serializer)
    } yield new SlickCreationParams {
      val props = theProps
      val initAction = () => if (createAndDrop) storeComponent.create else ().success
      val closeAction = () => if (createAndDrop) storeComponent.drop else ().success
    }
    
}
