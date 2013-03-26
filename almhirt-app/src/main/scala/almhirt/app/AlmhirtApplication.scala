package almhirt.app

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.core.Almhirt
import almhirt.environment.ShutDown
import almhirt.environment.configuration.Bootstrapper

trait AlmhirtApplication extends SplashMessages {

  private var exit = false

  def run(bootStrapper: Bootstrapper, logger: akka.event.LoggingAdapter): Int = {
    println(startupMessage)
    println
    println("An application built on Almhirt. Check www.almhirt.org for more information")
    println
    logger.info("Initiating startup")
    val startupV = Bootstrapper.runBootstrapper(bootStrapper)(logger)
    startupV fold (
      prob => {
        onCrash(prob)
        -1
      },
      almhirtAndShutDown => {
        while (!exit) {
          println("""Type "stop" to ...""")
          print("> ")
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