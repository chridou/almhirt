package almhirt.environment

import akka.actor.ActorSystem
import akka.util.duration._
import almhirt.core.JavaUtilUuidGenerator
import com.typesafe.config._

trait StandardAlmhirtsystemComponent extends AlmhirtsystemComponent {
  val system: AlmhirtSystem = new AlmhirtsystemByConfig()

  private class AlmhirtsystemByConfig() extends AlmhirtSystem {
    private val uuidGen = new JavaUtilUuidGenerator()

    val config = ConfigFactory.load
    val actorSystem = ActorSystem(config.getString("almhirt.systemname"))
    val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.future-dispatcher")
    val messageStreamDispatcherName = Some("almhirt.messagestream-dispatcher")
    val messageHubDispatcherName = Some("almhirt.messagehub-dispatcher")
    val shortDuration = config.getDouble("almhirt.durations.short") seconds
    val mediumDuration = config.getDouble("almhirt.durations.medium") seconds
    val longDuration = config.getDouble("almhirt.durations.long") seconds
    def generateUuid = uuidGen.generate
    def dispose = actorSystem.shutdown
  }
}
