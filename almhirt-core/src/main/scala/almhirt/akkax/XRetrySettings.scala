package almhirt.akkax

import almhirt.configuration._
import almhirt.common.Importance

object XRetrySettings {
  final case class NotifyingParams(importance: Importance, contextDescription: Option[String])
}

final case class XRetrySettings(
  numberOfRetries: NumberOfRetries,
  delay: RetryDelayMode,
  executorSelector: Option[ExtendedExecutionContextSelector],
  notifiyingParams: Option[XRetrySettings.NotifyingParams]) {

  def withNotifyingParams(params: XRetrySettings.NotifyingParams) = this.copy(notifiyingParams = Some(params))
  def inImportantContext(contextDescription: String) = this.withNotifyingParams(XRetrySettings.NotifyingParams(Importance.Important, Some(contextDescription)))
}