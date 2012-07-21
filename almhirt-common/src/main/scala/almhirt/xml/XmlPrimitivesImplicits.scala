package almhirt.xml

import scala.xml.{Node, NodeSeq, Elem}
import scalaz.Validation
import almhirt.validation._
import almhirt.validation.Problem._

trait XmlPrimitivesImplicits {
  implicit def nodeToXmlNodeW(node: Elem) = new XmlNodeW(node)
  final class XmlNodeW(node: Elem) {
    def extractInt(): AlmValidationSBD[Int] = 
      XmlPrimitives.intFromXmlNode(node)
    def extractLong(): AlmValidationSBD[Long] = 
      XmlPrimitives.longFromXmlNode(node)
    def extractDouble(): AlmValidationSBD[Double] = 
      XmlPrimitives.doubleFromXmlNode(node)
    def extractOptionalInt(): AlmValidationSBD[Option[Int]] = 
      XmlPrimitives.optionalIntXmlNode(node)
    def extractOptionalLong(): AlmValidationSBD[Option[Long]] = 
      XmlPrimitives.optionalLongXmlNode(node)
    def extractOptionalDouble(): AlmValidationSBD[Option[Double]] = 
      XmlPrimitives.optionalDoubleXmlNode(node)
    def extractStringFromChild(label: String): AlmValidationSBD[String] = 
      XmlPrimitives.stringFromChild(node, label)
    def extractIntFromChild(label: String): AlmValidationSBD[Int] = 
      XmlPrimitives.intFromChild(node, label)
    def extractLongFromChild(label: String): AlmValidationSBD[Long] = 
      XmlPrimitives.longFromChild(node, label)
    def extractDoubleFromChild(label: String): AlmValidationSBD[Double] = 
      XmlPrimitives.doubleFromChild(node, label)
    def extractOptionalStringFromChild(label: String): AlmValidationSBD[Option[String]] = 
      XmlPrimitives.stringOptionFromChild(node, label)
    def extractOptionalIntFromChild(label: String): AlmValidationSBD[Option[Int]] = 
      XmlPrimitives.intOptionFromChild(node, label)
    def extractOptionalLongFromChild(label: String): AlmValidationSBD[Option[Long]] = 
      XmlPrimitives.longOptionFromChild(node, label)
    def extractOptionalDoubleFromChild(label: String): AlmValidationSBD[Option[Double]] =
      XmlPrimitives.doubleOptionFromChild(node, label)
    def firstChildNode(label: String): AlmValidationSBD[Elem] = 
      XmlPrimitives.firstChildNodeMandatory(node, label)
    def mapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
      XmlPrimitives.mapOptionalFirstChild(node, label, compute)
    def flatMapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[Option[T]]): AlmValidationSBD[Option[T]] =
      XmlPrimitives.flatMapOptionalFirstChild(node, label, compute)
    def mapOptionalFirstChildM[T](label: String, compute: Elem => AlmValidationMBD[T]): AlmValidationMBD[Option[T]] =
      XmlPrimitives.mapOptionalFirstChildM(node, label, compute)
    def mapChildren[T](label: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[T]] =
      XmlPrimitives.mapChildren(node, label, map)
    def mapChildrenWithAttribute[T](label: String, attName: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[(Option[String], T)]] =
      XmlPrimitives.mapChildrenWithAttribute(node, label, attName, map)
  }
  
  implicit def nodeSeq2NodeSeqW(elem: Elem) = new AlmElemW(elem)
  final class AlmElemW(elem: Elem) {
    def \* = XmlPrimitives.elems(elem)
    def \#(label: String) = XmlPrimitives.elems(elem, label)
    def elems = XmlPrimitives.elems(elem)
  }
}