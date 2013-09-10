package almhirt.corex.slick.eventlog

import scala.concurrent.ExecutionContext
import scalaz.syntax.validation._
import akka.actor._
import akka.routing.RoundRobinRouter
import almhirt.messaging.MessagePublisher
import almhirt.common._
import almhirt.serialization._
import almhirt.core.Almhirt
import com.typesafe.config.Config
import almhirt.corex.slick.SlickCreationParams

class SlickTextEventLog private (
  override val messagePublisher: MessagePublisher,
  override val storeComponent: EventLogStoreComponent[TextEventLogRow],
  override val canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes,
  serializer: EventStringSerializer,
  serializationChannel: String)
  extends SlickEventLog with Actor with ActorLogging {
  type TRow = TextEventLogRow

  override def eventToRow(event: Event, channel: String): AlmValidation[TextEventLogRow] = {
   import almhirt.corex.slick.TypeConversion._
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

  def propsRaw(
    messagePublisher: MessagePublisher,
    storeComponent: EventLogStoreComponent[TextEventLogRow],
    canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes,
    serializer: EventStringSerializer,
    serializationChannel: String): AlmValidation[Props] =
    Props(new SlickTextEventLog(
      messagePublisher,
      storeComponent,
      canCreateUuidsAndDateTimes,
      serializer,
      serializationChannel)).success

  def props(
    configSection: Config,
    messagePublisher: MessagePublisher,
    storeComponent: EventLogStoreComponent[TextEventLogRow],
    canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes,
    serializer: EventStringSerializer): AlmValidation[Props] =
    for {
      channel <- configSection.v[String]("serialization-channel").flatMap(_.notEmptyOrWhitespace)
      dispatcher <- configSection.v[String]("sync-io-dispatcher").flatMap(_.notEmptyOrWhitespace)
      numActors <- configSection.v[Int]("number-of-actors")
      resRaw <- propsRaw(messagePublisher, storeComponent, canCreateUuidsAndDateTimes, serializer, channel)
      resDisp <- resRaw.withDispatcher(dispatcher).success
      res <- if(numActors > 1) {
        resDisp.withRouter(RoundRobinRouter(numActors)).success
      } else {
        resDisp.success
      }
    } yield resDisp

  def props(
    theAlmhirt: Almhirt,
    configPath: String,
    storeComponent: EventLogStoreComponent[TextEventLogRow],
    serializer: EventStringSerializer): AlmValidation[Props] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      res <- props(theAlmhirt, configPath, storeComponent, serializer)
    } yield res

  def props(
    theAlmhirt: Almhirt,
    configSection: Config,
    storeComponent: EventLogStoreComponent[TextEventLogRow],
    serializer: EventStringSerializer): AlmValidation[Props] =
    for {
      res <- props(configSection, theAlmhirt.messageBus, storeComponent, theAlmhirt, serializer)
    } yield res
    
  def props(
    theAlmhirt: Almhirt,
    storeComponent: EventLogStoreComponent[TextEventLogRow],
    serializer:EventStringSerializer): AlmValidation[Props] =
    for {
      configSection <- theAlmhirt.config.v[Config]("almhirt.eventlog") 
      res <- props(theAlmhirt, configSection, storeComponent, serializer)
    } yield res

  def create(theAlmhirt: Almhirt, configSection: Config, serializer: EventStringSerializer): AlmValidation[SlickCreationParams] =
    for {
      storeComponent <- TextEventLogDataAccess(configSection)
      createSchema <- configSection.opt[Boolean]("create-schema").map(_.getOrElse(false))
      dropSchema <- configSection.opt[Boolean]("drop-schema").map(_.getOrElse(false))
      theProps <- props(theAlmhirt, configSection, storeComponent, serializer)
    } yield new SlickCreationParams {
      val props = theProps
      val initAction = () => if (createSchema) storeComponent.create else ().success
      val closeAction = () => if (dropSchema) storeComponent.drop else ().success
    }
    
  def create(theAlmhirt: Almhirt, configPath: String, serializer: EventStringSerializer): AlmValidation[SlickCreationParams] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      res <- create(theAlmhirt, configSection, serializer)
    } yield res

  def create(theAlmhirt: Almhirt, serializer: EventStringSerializer): AlmValidation[SlickCreationParams] =
    for {
      configSection <- theAlmhirt.config.v[Config]("almhirt.eventlog") 
      res <- create(theAlmhirt, configSection, serializer)
    } yield res
    
}
