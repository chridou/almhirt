package almhirt.xtract.xml

import scala.xml.Elem
import scalaz._
import Scalaz._
import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._
import almhirt.xml.XmlPrimitives
import almhirt.xtract.{XTractor, XTractorAtomic, XTractorAtomicString}

class XmlXTractor(elem: Elem) extends XTractor {
  type T = Elem
  def underlying() = elem
  val key = elem.label
  
  def tryGetString(aKey: String) = XmlPrimitives.stringOptionFromChild(elem, aKey)
  def tryGetInt(aKey: String) = XmlPrimitives.intOptionFromChild(elem, aKey)
  def tryGetLong(aKey: String) = XmlPrimitives.longOptionFromChild(elem, aKey)
  def tryGetDouble(aKey: String) = XmlPrimitives.doubleOptionFromChild(elem, aKey)
  def tryGetAsString(aKey: String) = SingleBadDataProblem("not supported", key = aKey).fail[Option[String]]

  def getElements(aKey: String): AlmValidationMultipleBadData[List[XTractor]] =
    XmlPrimitives.elems(elem, aKey)
      .map(elem => (new XmlXTractor(elem)).successMultipleBadData)
      .toList
      .sequence
  
  def tryGetElement(aKey: String): AlmValidationSingleBadData[Option[XTractor]] =
    XmlPrimitives.elems(elem, aKey)
      .filterNot(elem => XmlPrimitives.elems(elem).isEmpty)
      .headOption
      .map(new XmlXTractor(_)).successSingleBadData
    
  def getAtomics(aKey: String): AlmValidationMultipleBadData[List[XTractorAtomic]] =
    XmlPrimitives.elems(elem, aKey).headOption match {
      case Some(head) => 
        XmlPrimitives.elems(head)
        .zipWithIndex
        .map{case (elem, i) => 
          new XTractorAtomicString(elem.text, "[i]".format(i)).successMultipleBadData}
        .toList
  	    .sequence
      case None => Nil.successMultipleBadData
  }
}

object XmlXTractor {
  def apply(elem: Elem): AlmValidationSingleBadData[XTractor] =
    new XmlXTractor(elem).successSingleBadData
  def apply(xml: String): AlmValidationSingleBadData[XTractor] = 
    try {
      new XmlXTractor(scala.xml.XML.loadString(xml)).successSingleBadData
    } catch {
      case exn => 
        SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = "xml", exception= Some(exn)).fail[XTractor] 
    }
    
  
  implicit def elem2XmlXTracor(elem: Elem): ElemXtractorW = new ElemXtractorW(elem)
  final class ElemXtractorW(elem: Elem) {
    def xtractor(): XmlXTractor = new XmlXTractor(elem)
  }
}