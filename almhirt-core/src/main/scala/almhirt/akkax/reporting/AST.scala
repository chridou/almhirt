package almhirt.akkax.reporting

import almhirt.akkax.ComponentState
import almhirt.common.CanCreateDateTime

object AST {
  sealed trait RValue
  sealed trait RBasicValue extends RValue

  final case class RComponentState(state: ComponentState) extends RBasicValue
  final case class RString(value: String) extends RBasicValue
  final case class RInteger(value: Long) extends RBasicValue
  final case class RFloat(value: Double) extends RBasicValue
  final case class RBool(value: Boolean) extends RBasicValue
  final case class RLocalDateTime(value: java.time.LocalDateTime) extends RBasicValue
  final case class RZonedDateTime(value: java.time.ZonedDateTime) extends RBasicValue
  final case class RError(message: String) extends RBasicValue
  case object RNotAvailable extends RBasicValue

  final case class RField(label: String, value: RValue)

  final case class RReport(fields: ReportFields) extends RValue

}



