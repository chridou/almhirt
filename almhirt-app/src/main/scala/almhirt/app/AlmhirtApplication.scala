package almhirt.app

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.environment.ShutDown
import almhirt.environment.configuration.AlmhirtBootstrapper

trait AlmhirtApplication {
  protected def startupMessage: String
  protected def goodByeMessage: String
  protected def crashMessage: String

  private var exit = false

  def run(bootStrapper: AlmhirtBootstrapper, logger: akka.event.LoggingAdapter): Int = {
    println(startupMessage)
    logger.info("Initiating startup")
    val startupV = AlmhirtBootstrapper.runStartupSequence(bootStrapper, logger)
    startupV fold (
      prob => {
        onCrash(prob)
        -1
      },
      almhirtAndShutDown => {
        while (!exit) {
          println("""Type "stop" to ...""")
          val input = readLine;
          if (input.toLowerCase() == "stop")
            exit = true;
        }
        println("Shutdown initiated")
        almhirtAndShutDown._2.shutDown
        println(goodByeMessage)
        0
      })
  }

  def createShutDown(shutDown: ShutDown) =
    (() => {
      println("Shutdown initiated")
      shutDown.shutDown
      println(goodByeMessage)
    }).success

  def onCrash(prob: Problem) {
    println(crashMessage)
    println(prob.toString())
  }
}