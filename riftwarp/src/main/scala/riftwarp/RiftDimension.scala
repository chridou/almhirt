package riftwarp


sealed trait RiftDimension {
  def manifestation: Any
}

sealed trait RiftHttpDimension extends RiftDimension

sealed trait RiftStringBasedDimension extends RiftDimension {
  def manifestation: String
}

sealed trait RiftByteArrayBasedDimension extends RiftDimension {
  def manifestation: Array[Byte]
}

case class DimensionString(manifestation: String) extends RiftStringBasedDimension with RiftHttpDimension
case class DimensionNiceString(manifestation: String) extends RiftStringBasedDimension with RiftHttpDimension
case class DimensionCord(manifestation: scalaz.Cord) extends RiftDimension
case class DimensionNiceCord(manifestation: scalaz.Cord) extends RiftDimension
case class DimensionBinary(manifestation: Array[Byte]) extends RiftByteArrayBasedDimension with RiftHttpDimension
case class DimensionRawMap(manifestation: Map[String, Any]) extends RiftDimension
case class DimensionStdLibJsonMap(manifestation: Map[String, Any]) extends RiftDimension
case class DimensionListAny(manifestation: List[Any]) extends RiftDimension
case class DimensionXmlElem(manifestation: scala.xml.Elem) extends RiftDimension
case class DimensionAny(manifestation: Any) extends RiftDimension

object RiftDimension {
  def string(v: String) = DimensionString(v)
  def niceString(v: String) = DimensionNiceString(v)
  def cord(v: scalaz.Cord) = DimensionCord(v)
  def niceCord(v: scalaz.Cord) = DimensionNiceCord(v)
  def binary(v: Array[Byte]) = DimensionBinary(v)
  def rawMap(v: Map[String, Any]) = DimensionRawMap(v)
  def stdLibJsonMap(v: Map[String, Any]) = DimensionStdLibJsonMap(v)
  def listAny(v: List[Any]) = DimensionListAny(v)
  def xmlElem(v: scala.xml.Elem) = DimensionXmlElem(v)
  def any(v: Any) = DimensionAny(v)

  
  implicit class RiftDimensionOps2(dim: RiftDimension) {
    import scalaz.syntax.validation._
    import almhirt.common._
    import riftwarp.http._
    def toHttpData(contentType: RiftHttpContentTypeWithChannel): AlmValidation[RiftHttpDataWithContent] =
      dim match {
        case stringBased: RiftStringBasedDimension => RiftHttpDataWithContent(contentType, RiftStringBody(stringBased.manifestation)).success
        case binaryBased: RiftByteArrayBasedDimension => RiftHttpDataWithContent(contentType, RiftBinaryBody(binaryBased.manifestation)).success
        case x => UnspecifiedProblem("Not a valid HTTP-Dimension: %s".format(x.getClass().getName())).failure
      }
  }
  
}
