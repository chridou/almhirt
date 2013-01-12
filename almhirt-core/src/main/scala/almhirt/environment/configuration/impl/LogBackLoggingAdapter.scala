package almhirt.environment.configuration.impl

import almhirt.common._
import org.slf4j.LoggerFactory

class LogBackLoggingAdapter(logger: org.slf4j.Logger) extends akka.event.LoggingAdapter {
  val isErrorEnabled = true
  val isWarningEnabled = true
  val isInfoEnabled = true
  val isDebugEnabled = true

  protected def notifyError(message: String) = logger.error(message)
  protected def notifyError(cause: Throwable, message: String) = logger.error(message, cause)
  protected def notifyWarning(message: String) = logger.warn(message)
  protected def notifyInfo(message: String) = logger.info(message)
  protected def notifyDebug(message: String) = logger.debug(message)
}

object LogBackLoggingAdapter {
  def apply(): AlmValidation[akka.event.LoggingAdapter] = {
    almhirt.almvalidation.funs.inTryCatch{LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)}.map(logger =>
      new LogBackLoggingAdapter(logger))
  }
}