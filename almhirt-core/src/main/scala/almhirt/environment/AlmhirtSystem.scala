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
trait AlmhirtSystem extends CanCreateUuidsAndDateTimes with HasExecutionContext  with HasDurations with Disposable {
  def config: Config
  def actorSystem: ActorSystem
  override def getUuid: java.util.UUID
  override def getDateTime: DateTime
}

object AlmhirtSystem {
  def apply(config: Config): AlmValidation[AlmhirtSystem] = {
    import scalaz.syntax.validation._
    for {
      short <- ConfigHelper.getDuration(config)("almhirt.durations.short")
      medium <- ConfigHelper.getDuration(config)("almhirt.durations.medium")
      long <- ConfigHelper.getDuration(config)("almhirt.durations.long")
      extraLong <- ConfigHelper.getDuration(config)("almhirt.durations.extraLong")
    } yield
      new AlmhirtSystem {
        override val config = ConfigFactory.load
        override val actorSystem = ActorSystem(config.getString("almhirt.systemname"))
        override val executionContext = ConfigHelper.lookUpDispatcher(actorSystem)(ConfigHelper.tryGetDispatcherNameFromRootConfig(config)(ConfigPaths.futures))
        override val durations = HasDurations(short, medium, long, extraLong)
        override def dispose = { actorSystem.shutdown; actorSystem.awaitTermination }
      }
  }
  def apply(): AlmValidation[AlmhirtSystem] = almhirt.almvalidation.funs.inTryCatch { ConfigFactory.load() }.flatMap(apply(_))
}
