package almhirt.environment

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration.doubleToDurationDouble
import akka.util.Duration
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import almhirt.common._
import almhirt.core._


/** Components and values needed to use Akka */
trait AlmhirtSystem extends Disposable {
  def config: Config
  def actorSystem: ActorSystem
  def futureDispatcher: MessageDispatcher
  def shortDuration: Duration
  def mediumDuration: Duration
  def longDuration: Duration
  def generateUuid: java.util.UUID
  def getDateTime: DateTime = new DateTime()
}

object AlmhirtSystem {
  def apply(config: Config): AlmValidation[AlmhirtSystem] = {
    import scalaz.syntax.validation._
    val uuidGen = new JavaUtilUuidGenerator()
    val ctx =
      new AlmhirtSystem {
        val config = ConfigFactory.load
        val actorSystem = ActorSystem(config.getString("almhirt.systemname"))
        val futureDispatcher = ConfigHelper.lookUpDispatcher(actorSystem)(ConfigHelper.tryGetDispatcherName(config)(ConfigPaths.futures))
        val shortDuration = config.getDouble("almhirt.durations.short") seconds
        val mediumDuration = config.getDouble("almhirt.durations.medium") seconds
        val longDuration = config.getDouble("almhirt.durations.long") seconds
        def generateUuid = uuidGen.generate
        def dispose = actorSystem.shutdown
      }
    ctx.success
  }
  def apply(): AlmValidation[AlmhirtSystem] = almhirt.almvalidation.funs.inTryCatch { ConfigFactory.load() }.bind(apply(_))
}
