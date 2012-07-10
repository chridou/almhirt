package almhirt.xml

import scala.xml.Node
import scalaz.Validation
import almhirt.validation.Problem._

trait XmlPrimitivesImplicits {
  implicit def nodeToXmlNodeW(node: Node) = new XmlNodeW(node)
  final class XmlNodeW(node: Node) {
    def extractInt(): Validation[BadDataProblem,Int] = XmlPrimitives.intFromXmlNode(node)
    def extractLong(): Validation[BadDataProblem,Long] = XmlPrimitives.longFromXmlNode(node)
    def extractDouble(): Validation[BadDataProblem,Double] = XmlPrimitives.doubleFromXmlNode(node)
    def extractOptionalInt(): Validation[BadDataProblem,Option[Int]] = XmlPrimitives.optionalIntXmlNode(node)
    def extractOptionalLong(): Validation[BadDataProblem,Option[Long]] = XmlPrimitives.optionalLongXmlNode(node)
    def extractOptionalDouble(): Validation[BadDataProblem,Option[Double]] = XmlPrimitives.optionalDoubleXmlNode(node)
  }
}