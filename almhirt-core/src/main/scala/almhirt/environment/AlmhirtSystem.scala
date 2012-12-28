package almhirt.environment

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import org.joda.time.DateTime
import almhirt.common._
import almhirt.core._
import almhirt.environment.configuration._
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory


/** Components and values needed to use Akka */
trait AlmhirtSystem extends CanCreateUuidsAndDateTimes with HasExecutionContext with Disposable {
  def config: Config
  def actorSystem: ActorSystem
  def shortDuration: FiniteDuration
  def mediumDuration: FiniteDuration
  def longDuration: FiniteDuration
  def getUuid: java.util.UUID
  def getDateTime: DateTime = new DateTime()
}

object AlmhirtSystem {
  def apply(config: Config): AlmValidation[AlmhirtSystem] = {
    import scalaz.syntax.validation._
    val uuidGen = new JavaUtilUuidGenerator()
    for {
      short <- ConfigHelper.getDuration(config)("almhirt.durations.short")
      medium <- ConfigHelper.getDuration(config)("almhirt.durations.medium")
      long <- ConfigHelper.getDuration(config)("almhirt.durations.long")
    } yield
      new AlmhirtSystem {
        val config = ConfigFactory.load
        val actorSystem = ActorSystem(config.getString("almhirt.systemname"))
        val executionContext = ConfigHelper.lookUpDispatcher(actorSystem)(ConfigHelper.tryGetDispatcherName(config)(ConfigPaths.futures))
        val shortDuration = short
        val mediumDuration = medium
        val longDuration = long
        def getUuid = uuidGen.generate
        def dispose = actorSystem.shutdown
      }
  }
  def apply(): AlmValidation[AlmhirtSystem] = almhirt.almvalidation.funs.inTryCatch { ConfigFactory.load() }.flatMap(apply(_))
}
