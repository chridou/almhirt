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
    for {
      system <- bootstrapper.createAlmhirtSystem()
      context <- bootstrapper.createAlmhirtContext(system)
      context <- bootstrapper.wireChannels(context)
      almhirt <- bootstrapper.createAlmhirt(context, system)
      _ <- bootstrapper.registerChannels(almhirt, context, system)
      _ <- bootstrapper.registerComponents(almhirt, context, system)
      _ <- bootstrapper.registerServicesStage1(almhirt, context, system)
      _ <- bootstrapper.registerRepositories(almhirt, context, system)
      _ <- bootstrapper.registerCommandHandlers(almhirt, context, system)
      _ <- bootstrapper.registerServicesStage2(almhirt, context, system)
    } yield almhirt

  def runShutDownSequence(bootstrapper: AlmhirtBootstrapper): AlmValidation[Unit] =
    for {
      _ <- bootstrapper.beforeClosing()
      _ <- bootstrapper.closing()
      _ <- bootstrapper.closed()
    } yield ()
}