package almhirt.environment.configuration

import akka.event.LoggingAdapter

trait CleanUpAction extends Function0[Unit] {
  def name: Option[String]
}

object CleanUpAction {
  def apply(cleanUp: () => Unit): CleanUpAction = new CleanUpAction { def apply() { cleanUp() }; val name = None }
  def apply(cleanUp: () => Unit, theMessage: String): CleanUpAction = new CleanUpAction { def apply() { cleanUp() }; val name = Some(theMessage) }

  def runCleanUps(cleanUps: List[(String, List[CleanUpAction])], startUpLogger: LoggingAdapter) {
    startUpLogger.info("Clean-ups")
    cleanUps.foreach {
      case (msg, cleanups) =>
        startUpLogger.info(s"Executing cleanups from: $msg")
        cleanups.foreach { action =>
          action.name.foreach(msg => startUpLogger.info(s"""  Executing cleanup action "$msg""""))
          try {
          action()
          } catch {
            case exn: Exception =>
              startUpLogger.error(exn, "CleanUpAction threw an exception")
          }
        }
        startUpLogger.info(s"Executed cleanups from: $msg")
    }
  }
  
  
  import language.implicitConversions
  
  implicit def actionToCleanUpAction(cleanUp: () => Unit): CleanUpAction = apply(cleanUp)
}
