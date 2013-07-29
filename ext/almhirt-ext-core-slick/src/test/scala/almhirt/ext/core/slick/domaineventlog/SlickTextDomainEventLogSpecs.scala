package almhirt.ext.core.slick.domaineventlog

import akka.actor._
import almhirt.almvalidation.kit._
import almhirt.testkit._
import almhirt.testkit.domaineventlog.DomainEventLogSpecTemplate
import almhirt.core.HasAlmhirt
import almhirt.domain.DomainEventStringSerializer
import almhirt.ext.core.riftwarp.serializers.RiftDomainEventStringSerializer
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config


object SlickTextDomainEventLogSpecsConfig {
  def configStr = 
    """
almhirt {
		  textdomaineventlog {
		  	profile = "h2"
		  	connection = "jdbc:h2:mem:almhirtslicktest;DB_CLOSE_DELAY=-1"
		  	table-name = ""
		    serialization-channel = "json"
		    create-schema = false
		  	drop-schema = false
		  	properties {
		  		user = "testuser"
		  		password = "testuser"
		  	}
		  }
}
    """
    
    val baseConfig = ConfigFactory.parseString(configStr)
    
    def config(testId: Int) =
      ConfigFactory.parseString(s"""almhirt.textdomaineventlog.table-name = "textdomaineventlog_${testId}"""").withFallback(baseConfig)
}

trait CreatesSlickTextDomainEventLog extends CreatesDomainEventLog { self: HasAlmhirt =>
  def createDomainEventLog(testId: Int): (ActorRef, () => Unit) = {
    import almhirt.testkit.AR1.Serialization._
    import almhirt.configuration._
    val theRiftwarp = addAr1Serializers(riftwarp.RiftWarp())
    val serializer = RiftDomainEventStringSerializer(theRiftwarp)
    val configSection = SlickTextDomainEventLogSpecsConfig.config(testId).v[Config]("almhirt.textdomaineventlog").resultOrEscalate
    val createParams = SlickTextDomainEventLog.create(theAlmhirt, configSection, serializer, true).resultOrEscalate
    createParams.initAction().resultOrEscalate
    (theAlmhirt.actorSystem.actorOf(createParams.props, "textdomaineventlog_"+testId), () => createParams.closeAction().resultOrEscalate)
  }
}

class SlickTextDomainEventLogSpecs
  extends DomainEventLogSpecTemplate(ActorSystem("SlickTextDomainEventLogSpecs", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesSlickTextDomainEventLog