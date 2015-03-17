package almhirt.akkax

import akka.actor.ActorLogging
import almhirt.common._

trait AlmActorLogging extends ActorLogging { me: AlmActor ⇒
  def logDebug(message: ⇒ String): Unit = {
    if (log.isDebugEnabled) {
      log.debug(message)
    }
    informNotWorthMentioning(message)
  }

  def logInfo(message: ⇒ String): Unit = {
    if (log.isInfoEnabled) {
      log.info(message)
    }
    informMentionable(message)
  }

  def logWarning(message: ⇒ String): Unit = {
    if (log.isWarningEnabled) {
      log.warning(message)
    }
    informImportant(message)
  }

  def logError(message: ⇒ String): Unit = {
    log.error(message)
    informVeryImportant(message)
  }

  def logError(message: String, throwable: Throwable): Unit = {
    log.error(throwable, message)
    informVeryImportant(s"$message\n${throwable.getMessage()}")
  }
  
  def logProblem(problem: ⇒ Problem): Unit = {
    log.warning(problem.toString())
    informImportant(problem.message)
  }
   
}