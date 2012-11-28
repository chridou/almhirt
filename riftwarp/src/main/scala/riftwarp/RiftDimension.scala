package riftwarp

trait RiftDimension {
  def manifestation: Any
}

case class DimensionString(manifestation: String) extends RiftDimension
case class DimensionCord(manifestation: scalaz.Cord) extends RiftDimension
case class DimensionBinary(manifestation: Array[Byte]) extends RiftDimension
case class DimensionRawMap(manifestation: Map[String, Any]) extends RiftDimension
case class DimensionStdLibJsonMap(manifestation: Map[String, Any]) extends RiftDimension
case class DimensionListAny(manifestation: List[Any]) extends RiftDimension
case class DimensionAny(manifestation: Any) extends RiftDimension

object RiftDimension {
  def string(v: String) = DimensionString(v)
  def cord(v: scalaz.Cord) = DimensionCord(v)
  def binary(v: Array[Byte]) = DimensionBinary(v)
  def rawMap(v: Map[String, Any]) = DimensionRawMap(v)
  def stdLibJsonMap(v: Map[String, Any]) = DimensionStdLibJsonMap(v)
  def listAny(v: List[Any]) = DimensionListAny(v)
  def any(v: Any) = DimensionAny(v)
}
