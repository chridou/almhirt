package riftwarp


sealed trait RiftDimension {
  type Under
  def manifestation: Under
}

sealed trait RiftHttpDimension extends RiftDimension

sealed trait RiftStringBasedDimension extends RiftDimension {
  type Under = String
}

sealed trait CordBasedDimension extends RiftDimension {
  type Under = scalaz.Cord
}

sealed trait RiftByteArrayBasedDimension extends RiftDimension {
  type Under = Array[Byte]
}

case class DimensionString(manifestation: String) extends RiftStringBasedDimension with RiftHttpDimension
case class DimensionNiceString(manifestation: String) extends RiftStringBasedDimension with RiftHttpDimension
case class DimensionCord(manifestation: scalaz.Cord) extends CordBasedDimension
case class DimensionNiceCord(manifestation: scalaz.Cord) extends CordBasedDimension
case class DimensionBinary(manifestation: Array[Byte]) extends RiftByteArrayBasedDimension with RiftHttpDimension
case class DimensionRawMap(manifestation: Map[String, Any]) extends RiftDimension { type Under = Map[String, Any] }
case class DimensionStdLibJsonMap(manifestation: Map[String, Any]) extends RiftDimension{ type Under = Map[String, Any] }
case class DimensionStdLibJson(manifestation: Any) extends RiftDimension{ type Under = Any }
case class DimensionListAny(manifestation: List[Any]) extends RiftDimension{ type Under = List[Any] }
case class DimensionXmlElem(manifestation: scala.xml.Elem) extends RiftDimension{ type Under = scala.xml.Elem }
case class DimensionAny(manifestation: Any) extends RiftDimension{ type Under = Any }

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
        case x => UnspecifiedProblem(s"Not a valid HTTP-Dimension: ${x.getClass().getName()}").failure
      }
  }
  
}
