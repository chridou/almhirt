package almhirt.xml

import scala.xml.{Node, NodeSeq, Elem}
import scalaz.Validation
import scalaz.syntax.Ops
import almhirt._
import almhirt.almvalidation._

trait XmlOps0 extends Ops[Elem]{
  def extractInt(): AlmValidationSBD[Int] = 
    xmlfunctions.intFromXmlNode(self)
  def extractLong(): AlmValidationSBD[Long] = 
    xmlfunctions.longFromXmlNode(self)
  def extractDouble(): AlmValidationSBD[Double] = 
    xmlfunctions.doubleFromXmlNode(self)
  def extractOptionalInt(): AlmValidationSBD[Option[Int]] = 
    xmlfunctions.optionalIntXmlNode(self)
  def extractOptionalLong(): AlmValidationSBD[Option[Long]] = 
    xmlfunctions.optionalLongXmlNode(self)
  def extractOptionalDouble(): AlmValidationSBD[Option[Double]] = 
    xmlfunctions.optionalDoubleXmlNode(self)
  def extractStringFromChild(label: String): AlmValidationSBD[String] = 
    xmlfunctions.stringFromChild(self, label)
  def extractIntFromChild(label: String): AlmValidationSBD[Int] = 
    xmlfunctions.intFromChild(self, label)
  def extractLongFromChild(label: String): AlmValidationSBD[Long] = 
    xmlfunctions.longFromChild(self, label)
  def extractDoubleFromChild(label: String): AlmValidationSBD[Double] = 
    xmlfunctions.doubleFromChild(self, label)
  def extractOptionalStringFromChild(label: String): AlmValidationSBD[Option[String]] = 
    xmlfunctions.stringOptionFromChild(self, label)
  def extractOptionalIntFromChild(label: String): AlmValidationSBD[Option[Int]] = 
    xmlfunctions.intOptionFromChild(self, label)
  def extractOptionalLongFromChild(label: String): AlmValidationSBD[Option[Long]] = 
    xmlfunctions.longOptionFromChild(self, label)
  def extractOptionalDoubleFromChild(label: String): AlmValidationSBD[Option[Double]] =
    xmlfunctions.doubleOptionFromChild(self, label)
  def firstChildNode(label: String): AlmValidationSBD[Elem] = 
    xmlfunctions.firstChildNodeMandatory(self, label)
  def mapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
    xmlfunctions.mapOptionalFirstChild(self, label, compute)
  def flatMapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[Option[T]]): AlmValidationSBD[Option[T]] =
    xmlfunctions.flatMapOptionalFirstChild(self, label, compute)
  def mapOptionalFirstChildM[T](label: String, compute: Elem => AlmValidationMBD[T]): AlmValidationMBD[Option[T]] =
    xmlfunctions.mapOptionalFirstChildM(self, label, compute)
  def mapChildren[T](label: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[T]] =
    xmlfunctions.mapChildren(self, label, map)
  def mapChildrenWithAttribute[T](label: String, attName: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[(Option[String], T)]] =
    xmlfunctions.mapChildrenWithAttribute(self, label, attName, map)
  def \* = xmlfunctions.elems(self)
  def \#(label: String) = xmlfunctions.elems(self, label)
  def elems = xmlfunctions.elems(self)
}

trait ToXmlOps {
  implicit def FromElemToXmlOps0(a: Elem) = new XmlOps0{ def self = a }
}
