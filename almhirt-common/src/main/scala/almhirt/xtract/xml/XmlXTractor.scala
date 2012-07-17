package almhirt.xtract.xml

import scala.xml.Elem
import scalaz.syntax.validation._
import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._
import almhirt.xml.XmlPrimitives

import almhirt.xtract.XTractor

class XmlXTractor(elem: Elem) extends XTractor {
  type T = Elem
  def underlying() = elem
  def extractString(key: String) = XmlPrimitives.stringFromChild(elem, key)
  def extractInt(key: String) = XmlPrimitives.intFromChild(elem, key)
  def extractLong(key: String) = XmlPrimitives.longFromChild(elem, key)
  def extractDouble(key: String) = XmlPrimitives.doubleFromChild(elem, key)
  def extractOptString(key: String) = XmlPrimitives.stringOptionFromChild(elem, key)
  def extractOptInt(key: String) = XmlPrimitives.intOptionFromChild(elem, key)
  def extractOptLong(key: String) = XmlPrimitives.longOptionFromChild(elem, key)
  def extractOptDouble(key: String) = XmlPrimitives.doubleOptionFromChild(elem, key)
  def extractElement(key: String): AlmValidationSingleBadData[XTractor]
  def extractOptElement(key: String): AlmValidationSingleBadData[Option[XTractor]]
  def mapOpt[U](key: String, mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[Option[U]]
  def flatMapOpt[U](key: String, mapXtractor: XTractor => AlmValidationMultipleBadData[Option[U]]): AlmValidationMultipleBadData[Option[U]]
  def mapAll[U](mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[List[U]]
}

object XmlXTractor {
  implicit def elem2XmlXTracor(elem: Elem): ElemXtractorW = new ElemXtractorW(elem)
  final class ElemXtractorW(elem: Elem) {
    def xtractor(): XmlXTractor = new XmlXTractor(elem)
  }
}