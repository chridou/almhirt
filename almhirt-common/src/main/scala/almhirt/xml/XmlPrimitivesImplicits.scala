package almhirt.xml

import scala.xml.Node
import scalaz.Validation
import almhirt.validation.Problem._

trait XmlPrimitivesImplicits {
  implicit def nodeToXmlNodeW(node: Node) = new XmlNodeW(node)
  final class XmlNodeW(node: Node) {
    def extractInt(): Validation[SingleBadDataProblem,Int] = XmlPrimitives.intFromXmlNode(node)
    def extractLong(): Validation[SingleBadDataProblem,Long] = XmlPrimitives.longFromXmlNode(node)
    def extractDouble(): Validation[SingleBadDataProblem,Double] = XmlPrimitives.doubleFromXmlNode(node)
    def extractOptionalInt(): Validation[SingleBadDataProblem,Option[Int]] = XmlPrimitives.optionalIntXmlNode(node)
    def extractOptionalLong(): Validation[SingleBadDataProblem,Option[Long]] = XmlPrimitives.optionalLongXmlNode(node)
    def extractOptionalDouble(): Validation[SingleBadDataProblem,Option[Double]] = XmlPrimitives.optionalDoubleXmlNode(node)
  }
}