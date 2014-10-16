package com.example.sillydemo

import scala.concurrent.duration._
import akka.actor.ActorSystem
import almhirt.common._

object Server extends App {
  val startupMessage =
    """   | 
		  |Silly Demo
		  |
		  |www.almhirt.org
		  |""".stripMargin

  println(startupMessage)
  val system = ActorSystem("silly-demo")
  // To register MediaTypes
  
  {
    import almhirt.httpx.spray._
    almhirt.http.AlmMediaTypes.iterator.filterNot(_.ianaRegistered).foreach(amt => _root_.spray.http.MediaTypes.register(amt.toSprayMediaType))
  }
  
  try {
    val startUpMaxDur = 20.seconds

    val (context, stop) = Boot.bootContext(system).awaitResultOrEscalate(startUpMaxDur)

    var exit = false
    while (!exit) {
      println("""Type "stop" to ...""")
      print("> ")
      val input = scala.io.StdIn.readLine
      if (input.toLowerCase() == "stop")
        exit = true
    }
    stop.stop()

  } finally {
    system.log.info("\n\nShutting down...\n\n")
    system.shutdown()
    val terminationDur: FiniteDuration = 30.seconds
    system.log.info(s"\n\nAwaiting termination(hard exit in ${terminationDur.defaultUnitString})...\n\n")
    system.awaitTermination(terminationDur)
  }
}