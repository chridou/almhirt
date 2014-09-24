package almhirt.tracking

import almhirt.common.CanCreateUuid

final case class CorrelationId(value: String) extends AnyVal

object CorrelationId {
  def apply()(implicit ccuad: CanCreateUuid): CorrelationId =
    CorrelationId(ccuad.getUniqueString)
}