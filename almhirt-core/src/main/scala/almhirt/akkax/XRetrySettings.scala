package almhirt.akkax

import almhirt.configuration._
import almhirt.common.Importance

object XRetrySettings {
  final case class NotifyingParams(importance: Importance, contextDescription: Option[String])
}

final case class XRetrySettings(
  numberOfRetries: NumberOfRetries,
  delay: RetryDelayMode,
  notifiyingParams: Option[XRetrySettings.NotifyingParams]) {
  
  def withNotifyingParams(params: XRetrySettings.NotifyingParams) = this.copy(notifiyingParams = Some(params))
}