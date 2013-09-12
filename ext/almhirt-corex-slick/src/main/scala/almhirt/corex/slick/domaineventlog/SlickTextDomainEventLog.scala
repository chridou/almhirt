package almhirt.corex.slick.domaineventlog

import scala.concurrent.ExecutionContext
import scalaz.syntax.validation._
import akka.actor._
import akka.routing.RoundRobinRouter
import almhirt.messaging.MessagePublisher
import almhirt.common._
import almhirt.domain._
import almhirt.serialization._
import almhirt.core.Almhirt
import com.typesafe.config.Config
import almhirt.corex.slick.SlickCreationParams

class SlickTextDomainEventLog private (
  override val messagePublisher: MessagePublisher,
  override val storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
  serializer: DomainEventStringSerializer,
  serializationChannel: String,
  override val writeWarnThresholdMs: Long,
  override val readWarnThresholdMs: Long)
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
  
  override def postStop() {
    log.info(writeStatistics.toString)
    log.info(readStatistics.toString)
    log.info(s"""SerializationStatistics${serializationStatistics.tailString()}""")
    log.info(s"""DeserializationStatistics${deserializationStatistics.tailString()}""")
  }
}

object SlickTextDomainEventLog {
  import almhirt.configuration._
  import almhirt.almvalidation.kit._

  def propsRaw(
    messagePublisher: MessagePublisher,
    storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
    serializer: DomainEventStringSerializer,
    serializationChannel: String,
    writeWarnThresholdMs: Long,
    readWarnThresholdMs: Long): AlmValidation[Props] =
    Props(new SlickTextDomainEventLog(
      messagePublisher,
      storeComponent,
      serializer,
      serializationChannel,
      writeWarnThresholdMs,
      readWarnThresholdMs)).success

  def props(
    configSection: Config,
    messagePublisher: MessagePublisher,
    storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
    serializer: DomainEventStringSerializer): AlmValidation[Props] =
    for {
      channel <- configSection.v[String]("serialization-channel").flatMap(_.notEmptyOrWhitespace)
      dispatcher <- configSection.v[String]("sync-io-dispatcher").flatMap(_.notEmptyOrWhitespace)
      numActors <- configSection.v[Int]("number-of-actors")
      writeWarnThresholdMs <- configSection.v[scala.concurrent.duration.FiniteDuration]("write-warn-threshold-duration").map(_.toMillis)
      readWarnThresholdMs <- configSection.v[scala.concurrent.duration.FiniteDuration]("read-warn-threshold-duration").map(_.toMillis)
      resRaw <- propsRaw(messagePublisher, storeComponent, serializer, channel, writeWarnThresholdMs, readWarnThresholdMs)
      resDisp <- resRaw.withDispatcher(dispatcher).success
      res <- if(numActors > 1) {
        resDisp.withRouter(RoundRobinRouter(numActors)).success
      } else {
        resDisp.success
      }
    } yield res

  def props(
    theAlmhirt: Almhirt,
    configPath: String,
    storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
    serializer: DomainEventStringSerializer): AlmValidation[Props] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      res <- props(theAlmhirt, configSection, storeComponent, serializer)
    } yield res

  def props(
    theAlmhirt: Almhirt,
    configSection: Config,
    storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
    serializer: DomainEventStringSerializer): AlmValidation[Props] =
    for {
      res <- props(configSection, theAlmhirt.messageBus, storeComponent, serializer)
    } yield res

  def props(
    theAlmhirt: Almhirt,
    storeComponent: DomainEventLogStoreComponent[TextDomainEventLogRow],
    serializer: DomainEventStringSerializer): AlmValidation[Props] =
    for {
      configSection <- theAlmhirt.config.v[Config]("almhirt.domain-eventlog") 
      res <- props(theAlmhirt, configSection, storeComponent, serializer)
    } yield res
    
  def create(theAlmhirt: Almhirt, configSection: Config, serializer: DomainEventStringSerializer): AlmValidation[SlickCreationParams] =
    for {
      storeComponent <- TextDomainEventLogDataAccess(configSection)
      createSchema <- configSection.opt[Boolean]("create-schema").map(_.getOrElse(false))
      dropSchema <- configSection.opt[Boolean]("drop-schema").map(_.getOrElse(false))
      theProps <- props(theAlmhirt, configSection, storeComponent, serializer)
    } yield new SlickCreationParams {
      val props = theProps
      val initAction = () => if (createSchema) storeComponent.create else ().success
      val closeAction = () => if (dropSchema) storeComponent.drop else ().success
    }
    
  def create(theAlmhirt: Almhirt, configPath: String, serializer: DomainEventStringSerializer): AlmValidation[SlickCreationParams] =
    for {
      configSection <- theAlmhirt.config.v[Config](configPath)
      res <- create(theAlmhirt, configSection, serializer)
    } yield res

  def create(theAlmhirt: Almhirt, serializer: DomainEventStringSerializer): AlmValidation[SlickCreationParams] =
    for {
      configSection <- theAlmhirt.config.v[Config]("almhirt.domain-eventlog") 
      res <- create(theAlmhirt, configSection, serializer)
    } yield res
    
}

