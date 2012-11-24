package almhirt.riftwarp

trait RiftDimension {
  def rawManifestation: AnyRef
}

trait RiftTypedDimension[T <: AnyRef] extends RiftDimension {
  def manifestation: T
  def rawManifestation: AnyRef = manifestation
}

case class DimensionString(manifestation: String) extends RiftTypedDimension[String]
case class DimensionCord(manifestation: scalaz.Cord) extends RiftTypedDimension[scalaz.Cord]
case class DimensionBinary(manifestation: Array[Byte]) extends RiftTypedDimension[Array[Byte]]
case class DimensionRawMap(manifestation: Map[String, Any]) extends RiftTypedDimension[Map[String, Any]]
case class DimensionStdLibJsonMap(manifestation: Map[String, Any]) extends RiftTypedDimension[Map[String, Any]]
case class DimensionStdLibJsonList(manifestation: List[Any]) extends RiftTypedDimension[List[Any]]
