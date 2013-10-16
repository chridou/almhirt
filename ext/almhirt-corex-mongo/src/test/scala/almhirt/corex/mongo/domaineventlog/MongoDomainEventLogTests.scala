package almhirt.corex.mongo.domaineventlog

import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.testkit._
import almhirt.testkit.domaineventlog.DomainEventLogSpecTemplate
import com.typesafe.config.ConfigFactory
import reactivemongo.api.MongoDriver
import almhirt.domain.DomainEvent
import reactivemongo.bson.BSONDocument
import riftwarpx.mongo.BsonDocumentSerializer
import com.typesafe.config.Config
import scala.concurrent.duration.FiniteDuration

class MongoDomainEventLogTests
  extends DomainEventLogSpecTemplate(ActorSystem("MongoDomainEventLogTests", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesDomainEventLog {

  override val defaultDuration = FiniteDuration(5, "s")
  override val sleepBeforeEachTest: Option[FiniteDuration] = Some(FiniteDuration(1500, "ms"))
  override val sleepAfterInsert: Option[FiniteDuration] = None

  val dbUuid = java.util.UUID.randomUUID()

  def configStr =
    """
almhirt {
		  mongo-domain-event-log {
	  	connections = ["localhost"]
	  	db-name = "mongo-domain-event-log-tests"
  		table-name = "domaineventlog"
  		write-warn-threshold-duration =  5 seconds
  		read-warn-threshold-duration =  5 seconds
  		use-number-cruncher-for-serialization = false
  		collect-statistics = false
		  }
}
    """

  val baseConfig = ConfigFactory.parseString(configStr)

  val fullConfig = baseConfig.withFallback(ConfigFactory.load())

  def config(testId: Int) =
    ConfigFactory.parseString(s"""	|almhirt.mongo-domain-event-log.table-name = "testdomaineventlog_${testId}"
    								|almhirt.mongo-domain-event-log.db-name = "testdomaineventlog_${dbUuid}"""".stripMargin).withFallback(fullConfig)

  var driver = new MongoDriver(theAlmhirt.actorSystem)

  override def createDomainEventLog(testId: Int): (ActorRef, () => Unit) = {
    import almhirt.testkit.AR1.Serialization._
    import almhirt.configuration._
    val theConfig = config(testId).v[Config]("almhirt.mongo-domain-event-log").resultOrEscalate
    val theRiftwarp = addAr1Serializers(riftwarp.RiftWarp())

    val serializer = BsonDocumentSerializer[DomainEvent](None, theRiftwarp)

    val props = MongoDomainEventLog.props(driver, serializer.toSerializationFunc, serializer.toDeserializationFunc, None, theConfig, theAlmhirt).resultOrEscalate

    (system.actorOf(props, "domaineventlog_" + testId.toString),
      () => ())
  }
}
