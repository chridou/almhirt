package almhirt.control

import almhirt.common._

case class PressureMeasured(
  header: EventHeader,
  system: String,
  subsystem: String,
  meter: Option[String],
  pressureLevel: BoilerPressure,
  message: Option[String]) extends ControlEvent {
  override def changeMetadata(newMetaData: Map[String, String]): PressureMeasured = copy(header = this.header.changeMetadata(newMetaData))
}
