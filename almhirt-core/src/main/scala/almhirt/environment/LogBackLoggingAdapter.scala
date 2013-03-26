package almhirt.environment

import almhirt.common._
import org.slf4j.LoggerFactory

class LogBackLoggingAdapter(logger: org.slf4j.Logger) extends akka.event.LoggingAdapter {
  val isErrorEnabled = true
  val isWarningEnabled = true
  val isInfoEnabled = true
  val isDebugEnabled = true

  protected override def notifyError(message: String) = logger.error(message)
  protected override def notifyError(cause: Throwable, message: String) = logger.error(message, cause)
  protected override def notifyWarning(message: String) = logger.warn(message)
  protected override def notifyInfo(message: String) = logger.info(message)
  protected override def notifyDebug(message: String) = logger.debug(message)
}

object LogBackLoggingAdapter {
  def apply(): akka.event.LoggingAdapter =
    new LogBackLoggingAdapter(LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))

  def apply(name: String): akka.event.LoggingAdapter =
    new LogBackLoggingAdapter(LoggerFactory.getLogger(name))
  
  def create(): AlmValidation[akka.event.LoggingAdapter] = {
    almhirt.almvalidation.funs.inTryCatch{LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)}.map(logger =>
      new LogBackLoggingAdapter(logger))
  }
  def create(name: String): AlmValidation[akka.event.LoggingAdapter] = {
    almhirt.almvalidation.funs.inTryCatch{LoggerFactory.getLogger(name)}.map(logger =>
      new LogBackLoggingAdapter(logger))
  }
  
}