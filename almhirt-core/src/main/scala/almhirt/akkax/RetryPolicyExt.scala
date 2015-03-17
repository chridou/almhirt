package almhirt.akkax

import almhirt.configuration._
import almhirt.common.Importance

object RetryPolicyExt {
  def apply(
    numberOfRetries: NumberOfRetries,
    delay: RetryDelayMode,
    executorSelector: Option[ExtendedExecutionContextSelector],
    notifiyingParams: Option[RetryPolicyExt.NotifyingParams]): RetryPolicyExt =
    RetryPolicyExt(RetryPolicy(numberOfRetries, delay), executorSelector, notifiyingParams)

  final case class NotifyingParams(importance: Importance, contextDescription: Option[String])
}

final case class RetryPolicyExt(
  policy: RetryPolicy,
  executorSelector: Option[ExtendedExecutionContextSelector],
  notifiyingParams: Option[RetryPolicyExt.NotifyingParams]) {

  def numberOfRetries: NumberOfRetries = policy.numberOfRetries
  def delay: RetryDelayMode = policy.delay

  def withNotifyingParams(params: RetryPolicyExt.NotifyingParams) = this.copy(notifiyingParams = Some(params))
  def inImportantContext(contextDescription: String) = this.withNotifyingParams(RetryPolicyExt.NotifyingParams(Importance.Important, Some(contextDescription)))
}