package almhirt.environment.configuration

import almhirt.common._
import almhirt.environment._
import com.typesafe.config.Config

trait AlmhirtBootstrapper {
  def createAlmhirtSystem(): AlmValidation[AlmhirtSystem]

  def createAlmhirtContext(system: AlmhirtSystem): AlmValidation[AlmhirtContext]

  def wireChannels(context: AlmhirtContext): AlmValidation[AlmhirtContext]

  def createAlmhirt(context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Almhirt]

  def registerChannels(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit]
  
  def registerComponents(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit]

  def registerServicesStage1(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit]

  def registerRepositories(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit]

  def registerCommandHandlers(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit]

  def registerServicesStage2(almhirt: Almhirt, context: AlmhirtContext, system: AlmhirtSystem): AlmValidation[Unit]

  def beforeClosing(): AlmValidation[Unit]

  def closing(): AlmValidation[Unit]

  def closed(): AlmValidation[Unit]

}

object AlmhirtBootstrapper {
  def createFromConfig(config: Config): AlmValidation[AlmhirtBootstrapper] = 
    SystemHelper.createBootstrapperFromConfig(config)

  def runStartupSequence(bootstrapper: AlmhirtBootstrapper): AlmValidation[Almhirt] =
    bootstrapper.createAlmhirtSystem().bind(system =>
      bootstrapper.createAlmhirtContext(system).bind(context =>
        bootstrapper.wireChannels(context).bind(context =>
          bootstrapper.createAlmhirt(context, system).bind(almhirt =>
          bootstrapper.registerChannels(almhirt, context, system).bind(_ =>
            bootstrapper.registerComponents(almhirt, context, system).bind(_ =>
              bootstrapper.registerServicesStage1(almhirt, context, system).bind(_ =>
                bootstrapper.registerRepositories(almhirt, context, system).bind(_ =>
                  bootstrapper.registerCommandHandlers(almhirt, context, system).bind(_ =>
                    bootstrapper.registerServicesStage2(almhirt, context, system).map(_ => almhirt))))))))))

  def runShutDownSequence(bootstrapper: AlmhirtBootstrapper): AlmValidation[Unit] =
    bootstrapper.beforeClosing().bind(_ =>
      bootstrapper.closing().bind(_ =>
        bootstrapper.closed()))
}