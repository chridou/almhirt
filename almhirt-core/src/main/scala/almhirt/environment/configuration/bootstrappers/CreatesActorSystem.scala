package almhirt.environment.configuration.bootstrappers

import akka.event.LoggingAdapter
import akka.actor.ActorSystem
import almhirt.common.AlmValidation
import almhirt.almvalidation.kit._
import almhirt.core._
import almhirt.environment.configuration._

trait CreatesActorSystemFromConfig extends PreInitBootstrapperPhase with HasActorSystem { self: HasConfig =>
  override def actorSystem = myActorSystem
  private var myActorSystem: ActorSystem = null

  override def preInit(startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    super.preInit(startUpLogger).andThen(createActorSystem(startUpLogger))

  private def createActorSystem(startUpLogger: LoggingAdapter): BootstrapperPhaseResult =
    inTryCatch {
      val sysName = ConfigHelper.getString(config)("almhirt.systemname").getOrElse("almhirt")
      startUpLogger.info(s"Creating ActorSystem with name $sysName ...")
      myActorSystem = ActorSystem(sysName, config)
      BootstrapperPhaseSuccess(CleanUpAction(() => { myActorSystem.shutdown(); myActorSystem.awaitTermination() }, "ActorSystem"))
    }.toBootstrapperPhaseResult
}