package almhirt.xml
package syntax

import scala.xml.{Node, NodeSeq, Elem}
import scalaz.Validation
import scalaz.syntax.Ops
import almhirt.validation._

trait XmlOps0 extends Ops[Elem]{
  def extractInt(): AlmValidationSBD[Int] = 
    XmlFunctions.intFromXmlNode(self)
  def extractLong(): AlmValidationSBD[Long] = 
    XmlFunctions.longFromXmlNode(self)
  def extractDouble(): AlmValidationSBD[Double] = 
    XmlFunctions.doubleFromXmlNode(self)
  def extractOptionalInt(): AlmValidationSBD[Option[Int]] = 
    XmlFunctions.optionalIntXmlNode(self)
  def extractOptionalLong(): AlmValidationSBD[Option[Long]] = 
    XmlFunctions.optionalLongXmlNode(self)
  def extractOptionalDouble(): AlmValidationSBD[Option[Double]] = 
    XmlFunctions.optionalDoubleXmlNode(self)
  def extractStringFromChild(label: String): AlmValidationSBD[String] = 
    XmlFunctions.stringFromChild(self, label)
  def extractIntFromChild(label: String): AlmValidationSBD[Int] = 
    XmlFunctions.intFromChild(self, label)
  def extractLongFromChild(label: String): AlmValidationSBD[Long] = 
    XmlFunctions.longFromChild(self, label)
  def extractDoubleFromChild(label: String): AlmValidationSBD[Double] = 
    XmlFunctions.doubleFromChild(self, label)
  def extractOptionalStringFromChild(label: String): AlmValidationSBD[Option[String]] = 
    XmlFunctions.stringOptionFromChild(self, label)
  def extractOptionalIntFromChild(label: String): AlmValidationSBD[Option[Int]] = 
    XmlFunctions.intOptionFromChild(self, label)
  def extractOptionalLongFromChild(label: String): AlmValidationSBD[Option[Long]] = 
    XmlFunctions.longOptionFromChild(self, label)
  def extractOptionalDoubleFromChild(label: String): AlmValidationSBD[Option[Double]] =
    XmlFunctions.doubleOptionFromChild(self, label)
  def firstChildNode(label: String): AlmValidationSBD[Elem] = 
    XmlFunctions.firstChildNodeMandatory(self, label)
  def mapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
    XmlFunctions.mapOptionalFirstChild(self, label, compute)
  def flatMapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[Option[T]]): AlmValidationSBD[Option[T]] =
    XmlFunctions.flatMapOptionalFirstChild(self, label, compute)
  def mapOptionalFirstChildM[T](label: String, compute: Elem => AlmValidationMBD[T]): AlmValidationMBD[Option[T]] =
    XmlFunctions.mapOptionalFirstChildM(self, label, compute)
  def mapChildren[T](label: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[T]] =
    XmlFunctions.mapChildren(self, label, map)
  def mapChildrenWithAttribute[T](label: String, attName: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[(Option[String], T)]] =
    XmlFunctions.mapChildrenWithAttribute(self, label, attName, map)
  def \* = XmlFunctions.elems(self)
  def \#(label: String) = XmlFunctions.elems(self, label)
  def elems = XmlFunctions.elems(self)
}

trait ToXmlOps {
  implicit def FromElemToXmlOps0(a: Elem) = new XmlOps0{ def self = a }
}

object XmlOps extends ToXmlOps