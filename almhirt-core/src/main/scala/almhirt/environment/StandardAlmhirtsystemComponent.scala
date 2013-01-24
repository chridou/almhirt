package almhirt.environment

import akka.actor.ActorSystem
import almhirt.syntax.almvalidation._
import almhirt.core.JavaUtilUuidGenerator
import almhirt.environment.configuration.ConfigHelper
import com.typesafe.config._

trait StandardAlmhirtsystemComponent extends AlmhirtsystemComponent {
  val system: AlmhirtSystem = new AlmhirtsystemByConfig()

  private class AlmhirtsystemByConfig() extends AlmhirtSystem {
    private val uuidGen = new JavaUtilUuidGenerator()

    val config = ConfigFactory.load
    val actorSystem = ActorSystem(config.getString("almhirt.systemname"))
    val executionContext = actorSystem.dispatchers.lookup("almhirt.future-dispatcher")
    val messageStreamDispatcherName = Some("almhirt.messagestream-dispatcher")
    val messageHubDispatcherName = Some("almhirt.messagehub-dispatcher")
    val shortDuration = ConfigHelper.getDuration(config)("almhirt.durations.short").forceResult
    val mediumDuration = ConfigHelper.getDuration(config)("almhirt.durations.medium").forceResult
    val longDuration = ConfigHelper.getDuration(config)("almhirt.durations.long").forceResult
    def dispose = actorSystem.shutdown
  }
}
