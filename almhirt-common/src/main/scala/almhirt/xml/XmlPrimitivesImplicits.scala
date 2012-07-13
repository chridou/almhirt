package almhirt.xml

import scala.xml.Node
import scalaz.Validation
import almhirt.validation._
import almhirt.validation.Problem._

trait XmlPrimitivesImplicits {
  implicit def nodeToXmlNodeW(node: Node) = new XmlNodeW(node)
  final class XmlNodeW(node: Node) {
    def extractInt(): AlmValidationSingleBadData[Int] = 
      XmlPrimitives.intFromXmlNode(node)
    def extractLong(): AlmValidationSingleBadData[Long] = 
      XmlPrimitives.longFromXmlNode(node)
    def extractDouble(): AlmValidationSingleBadData[Double] = 
      XmlPrimitives.doubleFromXmlNode(node)
    def extractOptionalInt(): AlmValidationSingleBadData[Option[Int]] = 
      XmlPrimitives.optionalIntXmlNode(node)
    def extractOptionalLong(): AlmValidationSingleBadData[Option[Long]] = 
      XmlPrimitives.optionalLongXmlNode(node)
    def extractOptionalDouble(): AlmValidationSingleBadData[Option[Double]] = 
      XmlPrimitives.optionalDoubleXmlNode(node)
    def extractStringFromChild(label: String): AlmValidationSingleBadData[String] = 
      XmlPrimitives.stringFromChild(node, label)
    def extractIntFromChild(label: String): AlmValidationSingleBadData[Int] = 
      XmlPrimitives.intFromChild(node, label)
    def extractLongFromChild(label: String): AlmValidationSingleBadData[Long] = 
      XmlPrimitives.longFromChild(node, label)
    def extractDoubleFromChild(label: String): AlmValidationSingleBadData[Double] = 
      XmlPrimitives.doubleFromChild(node, label)
    def extractOptionalStringFromChild(label: String): AlmValidationSingleBadData[Option[String]] = 
      XmlPrimitives.stringOptionFromChild(node, label)
    def extractOptionalIntFromChild(label: String): AlmValidationSingleBadData[Option[Int]] = 
      XmlPrimitives.intOptionFromChild(node, label)
    def extractOptionalLongFromChild(label: String): AlmValidationSingleBadData[Option[Long]] = 
      XmlPrimitives.longOptionFromChild(node, label)
    def extractOptionalDoubleFromChild(label: String): AlmValidationSingleBadData[Option[Double]] =
      XmlPrimitives.doubleOptionFromChild(node, label)
    def firstChildNode(label: String): AlmValidationSingleBadData[Node] = 
      XmlPrimitives.firstChildNodeMandatory(node, label)
    def mapOptionalFirstChild[T](label: String, compute: Node => AlmValidationSingleBadData[T]): AlmValidationSingleBadData[Option[T]] =
      XmlPrimitives.mapOptionalFirstChild(node, label, compute)
    def flatMapOptionalFirstChild[T](label: String, compute: Node => AlmValidationSingleBadData[Option[T]]): AlmValidationSingleBadData[Option[T]] =
      XmlPrimitives.flatMapOptionalFirstChild(node, label, compute)
    def mapOptionalFirstChildM[T](label: String, compute: Node => AlmValidationMultipleBadData[T]): AlmValidationMultipleBadData[Option[T]] =
      XmlPrimitives.mapOptionalFirstChildM(node, label, compute)
    def mapChildren[T](label: String, map: Node => AlmValidationMultipleBadData[T]): AlmValidationMultipleBadData[List[T]] =
      XmlPrimitives.mapChildren(node, label, map)
    def mapChildrenWithAttribute[T](label: String, attName: String, map: Node => AlmValidationMultipleBadData[T]): AlmValidationMultipleBadData[List[(Option[String], T)]] =
      XmlPrimitives.mapChildrenWithAttribute(node, label, attName, map)
  }
}