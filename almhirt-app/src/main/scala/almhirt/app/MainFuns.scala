package almhirt.app

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.environment.configuration._
import com.typesafe.config._
import almhirt.environment.configuration.impl.LogBackLoggingAdapter
import almhirt.environment.configuration.impl.AlmhirtBaseBootstrapper

object MainFuns {
  def createConfig(args: Array[String]): AlmValidation[Config] =
    inTryCatch(ConfigFactory.load())

  def initializeAlmhirt(bootStrapper: AlmhirtBootstrapper): AlmValidation[(Almhirt, ShutDown)] = {
    for {
      startUpLogger <- LogBackLoggingAdapter()
      almhirtAndShutDown <- AlmhirtBootstrapper.runStartupSequence(bootStrapper, startUpLogger)
    } yield almhirtAndShutDown
  }

  def createShutDown(shutDown: ShutDown) =
    (() => {
      println("Shutdown initiated")
      shutDown.shutDown
      println("")
      println(Splash.goodbye)
      println("")
    }).success

  def prepareShutDown(onShutDown: () => Unit) {
    println("Press CTRL-C key to exit...")
    sys.addShutdownHook(onShutDown)
  }

  def onCrash(prob: Problem) {
    println(Splash.crash)
    println("")
    println(prob.toString())
    System.exit(-1)
  }
}