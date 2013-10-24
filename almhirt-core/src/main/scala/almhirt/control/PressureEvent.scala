package almhirt.control

import almhirt.common._

case class PressureMeasuredEvent(
  header: EventHeader,
  system: String,
  subsystem: String,
  meter: Option[String],
  pressureLevel: BoilerPressure) extends ControlEvent {
  override def changeMetadata(newMetaData: Map[String, String]): PressureMeasuredEvent = copy(header = this.header.changeMetadata(newMetaData))
}
