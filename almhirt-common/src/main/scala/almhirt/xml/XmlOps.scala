package almhirt.xml

import scala.xml.{Node, NodeSeq, Elem}
import scalaz.Validation
import scalaz.syntax.Ops
import almhirt._
import almhirt.almvalidation._

trait XmlOps0 extends Ops[Elem]{
  def extractInt(): AlmValidationSBD[Int] = 
    xmlfuns.intFromXmlNode(self)
  def extractLong(): AlmValidationSBD[Long] = 
    xmlfuns.longFromXmlNode(self)
  def extractDouble(): AlmValidationSBD[Double] = 
    xmlfuns.doubleFromXmlNode(self)
  def extractOptionalInt(): AlmValidationSBD[Option[Int]] = 
    xmlfuns.optionalIntXmlNode(self)
  def extractOptionalLong(): AlmValidationSBD[Option[Long]] = 
    xmlfuns.optionalLongXmlNode(self)
  def extractOptionalDouble(): AlmValidationSBD[Option[Double]] = 
    xmlfuns.optionalDoubleXmlNode(self)
  def extractStringFromChild(label: String): AlmValidationSBD[String] = 
    xmlfuns.stringFromChild(self, label)
  def extractIntFromChild(label: String): AlmValidationSBD[Int] = 
    xmlfuns.intFromChild(self, label)
  def extractLongFromChild(label: String): AlmValidationSBD[Long] = 
    xmlfuns.longFromChild(self, label)
  def extractDoubleFromChild(label: String): AlmValidationSBD[Double] = 
    xmlfuns.doubleFromChild(self, label)
  def extractOptionalStringFromChild(label: String): AlmValidationSBD[Option[String]] = 
    xmlfuns.stringOptionFromChild(self, label)
  def extractOptionalIntFromChild(label: String): AlmValidationSBD[Option[Int]] = 
    xmlfuns.intOptionFromChild(self, label)
  def extractOptionalLongFromChild(label: String): AlmValidationSBD[Option[Long]] = 
    xmlfuns.longOptionFromChild(self, label)
  def extractOptionalDoubleFromChild(label: String): AlmValidationSBD[Option[Double]] =
    xmlfuns.doubleOptionFromChild(self, label)
  def firstChildNode(label: String): AlmValidationSBD[Elem] = 
    xmlfuns.firstChildNodeMandatory(self, label)
  def mapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
    xmlfuns.mapOptionalFirstChild(self, label, compute)
  def flatMapOptionalFirstChild[T](label: String, compute: Elem => AlmValidationSBD[Option[T]]): AlmValidationSBD[Option[T]] =
    xmlfuns.flatMapOptionalFirstChild(self, label, compute)
  def mapOptionalFirstChildM[T](label: String, compute: Elem => AlmValidationMBD[T]): AlmValidationMBD[Option[T]] =
    xmlfuns.mapOptionalFirstChildM(self, label, compute)
  def mapChildren[T](label: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[T]] =
    xmlfuns.mapChildren(self, label, map)
  def mapChildrenWithAttribute[T](label: String, attName: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[(Option[String], T)]] =
    xmlfuns.mapChildrenWithAttribute(self, label, attName, map)
  def \* = xmlfuns.elems(self)
  def \#(label: String) = xmlfuns.elems(self, label)
  def elems = xmlfuns.elems(self)
}

trait ToXmlOps {
  implicit def FromElemToXmlOps0(a: Elem) = new XmlOps0{ def self = a }
}
