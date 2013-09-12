package almhirt.corex.slick.domaineventlog

import akka.actor._
import almhirt.almvalidation.kit._
import almhirt.testkit._
import almhirt.testkit.domaineventlog.DomainEventLogSpecTemplate
import almhirt.core.HasAlmhirt
import almhirt.domain.DomainEventStringSerializer
import almhirt.corex.riftwarp.serializers.RiftDomainEventStringSerializer
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import scala.concurrent.duration.FiniteDuration


object SlickTextDomainEventLogSpecsConfig {
  def configStr = 
    """
almhirt {
		  text-domain-event-log {
		  	profile = "h2"
		  	connection = "jdbc:h2:mem:almhirtslicktest;DB_CLOSE_DELAY=-1"
		  	table-name = ""
		    serialization-channel = "json"
		    create-schema = true
		  	drop-schema = true
            sync-io-dispatcher = "akka.actor.default-dispatcher"
		  	number-of-actors = 2
		  	write-warn-threshold-duration = 5 seconds
		  	properties {
		  		user = "testuser"
		  		password = "testuser"
		  	}
		  }
}
    """
    
    val baseConfig = ConfigFactory.parseString(configStr)
    
    val fullConfig = baseConfig.withFallback(ConfigFactory.load())
    
    def config(testId: Int) =
      ConfigFactory.parseString(s"""almhirt.text-domain-event-log.table-name = "textdomaineventlog_${testId}"""").withFallback(fullConfig)
}

trait CreatesSlickTextDomainEventLog extends CreatesDomainEventLog { self: HasAlmhirt =>
  def createDomainEventLog(testId: Int): (ActorRef, () => Unit) = {
    import almhirt.testkit.AR1.Serialization._
    import almhirt.configuration._
    val theRiftwarp = addAr1Serializers(riftwarp.RiftWarp())
    val serializer = RiftDomainEventStringSerializer(theRiftwarp)
    val configSection = SlickTextDomainEventLogSpecsConfig.config(testId).v[Config]("almhirt.text-domain-event-log").resultOrEscalate
    val createParams = SlickTextDomainEventLog.create(theAlmhirt, configSection, serializer).resultOrEscalate
    createParams.initAction().resultOrEscalate
    (theAlmhirt.actorSystem.actorOf(createParams.props, "textdomaineventlog_"+testId), () => createParams.closeAction().resultOrEscalate)
  }
}

class SlickTextDomainEventLogSpecs
  extends DomainEventLogSpecTemplate(ActorSystem("SlickTextDomainEventLogSpecs", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithConfiguration
  with CreatesSlickTextDomainEventLog {
  override def config = SlickTextDomainEventLogSpecsConfig.fullConfig
  override def defaultDuration = FiniteDuration(2, "s")
}