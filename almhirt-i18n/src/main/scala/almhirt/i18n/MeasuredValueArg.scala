package almhirt.i18n

trait MeasuredValueArg {
  def targetSystem: Option[UnitsOfMeasurementSystem]
}

private object MeasuredValueArg {
  final case class FullArg(measured: Measured, targetSystem: Option[UnitsOfMeasurementSystem]) extends MeasuredValueArg
  final case class SiArg(value: Double, targetSystem: Option[UnitsOfMeasurementSystem]) extends MeasuredValueArg
}