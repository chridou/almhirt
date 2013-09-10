package almhirt.corex.slick.eventlog

import akka.actor._
import almhirt.almvalidation.kit._
import almhirt.testkit._
import almhirt.testkit.eventlog.EventLogSpecTemplate
import almhirt.core.HasAlmhirt
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config


object SlickTextEventLogSpecsConfig {
  def configStr = 
    """
almhirt {
		  text-event-log {
		  	profile = "h2"
		  	connection = "jdbc:h2:mem:almhirtslicktest;DB_CLOSE_DELAY=-1"
		  	table-name = ""
		    serialization-channel = "json"
		    create-schema = true
		  	drop-schema = true
            sync-io-dispatcher = "x"
		  	number-of-actors = 1
		  	properties {
		  		user = "testuser"
		  		password = "testuser"
		  	}
		  }
}
    """
    
    val baseConfig = ConfigFactory.parseString(configStr)
    
    def config(testId: Int) =
      ConfigFactory.parseString(s"""almhirt.text-event-log.table-name = "texteventlog_${testId}"""").withFallback(baseConfig)
}

trait CreatesSlickTextEventLog extends CreatesEventLog { self: HasAlmhirt =>
  def createEventLog(testId: Int): (ActorRef, () => Unit) = {
    import almhirt.testkit.AR1.Serialization._
    import almhirt.configuration._
    val theRiftwarp = almhirt.testkit.testevents.Serialization.addTestEventSerializers(riftwarp.RiftWarp())
    val serializer = riftwarp.util.RiftEventStringSerializer(theRiftwarp)
    val configSection = SlickTextEventLogSpecsConfig.config(testId).v[Config]("almhirt.text-event-log").resultOrEscalate
    val createParams = SlickTextEventLog.create(theAlmhirt, configSection, serializer).resultOrEscalate
    createParams.initAction().resultOrEscalate
    (theAlmhirt.actorSystem.actorOf(createParams.props, "texteeventlog_"+testId), () => createParams.closeAction().resultOrEscalate)
  }
}

class SlickTextEventLogSpecs
  extends EventLogSpecTemplate(ActorSystem("SlickTextEventLogSpecs", TestConfigs.default))
  with AlmhirtFromAkkaTestKitWithoutConfiguration
  with CreatesSlickTextEventLog{
  override val sleepMillisAfterWrite = Some(100)
}