package almhirt.app

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.environment._
import almhirt.environment.configuration._
import com.typesafe.config._

object MainFuns {
  def createConfig(args: Array[String]): AlmValidation[Config] =
    inTryCatch(ConfigFactory.load())

  def initializeAlmhirt(config: Config): AlmValidation[(Almhirt, ShutDown)] =
    for {
      bootStrapper <- AlmhirtBootstrapper.createFromConfig(config)
      almhirtAndShutDown <- AlmhirtBootstrapper.runStartupSequence(bootStrapper)
    } yield almhirtAndShutDown

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