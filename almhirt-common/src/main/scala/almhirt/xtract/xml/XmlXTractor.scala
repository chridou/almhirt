package almhirt.xtract.xml

import scala.xml.{Elem, Text}
import scalaz._
import Scalaz._
import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._
import almhirt.xml.XmlPrimitives
import almhirt.xtract.{XTractor, XTractorAtomic, XTractorAtomicString}
import almhirt.xml.XmlPrimitives
import almhirt.xtract.XTractor

class XmlXTractor(elem: Elem) extends XTractor {
  type T = Elem
  def underlying() = elem
  val key = elem.label
  
  def tryGetString(aKey: String) = 
    onSingleTextOnlyElem(
        aKey, 
        (text, theKey) => {
          if(text.trim.isEmpty) 
            None.success[SingleBadDataProblem]
          else
            Some(text).success[SingleBadDataProblem] })
  
  def tryGetInt(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseIntAlm)
  
  def tryGetLong(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseLongAlm)
  
  def tryGetDouble(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseDoubleAlm)
  
  def tryGetAsString(aKey: String) = 
    SingleBadDataProblem("not supported", key = aKey).fail[Option[String]]

  def getElements(aKey: String): AlmValidationMultipleBadData[List[XTractor]] =
    for {
      propertyElement <-
      	getUniquePropertyElement(aKey).toMultipleBadData
      elems <- 
      	(propertyElement.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.successSingleBadData)).toMultipleBadData
      xtractors <- 
      	elems.map{x => onValidTypeContainerXTractor(x).toMultipleBadData}.toList.sequence
    } yield xtractors
  
  def tryGetElement(aKey: String): AlmValidationSingleBadData[Option[XTractor]] = {
    for {
      propertyElement <- getUniquePropertyElement(aKey)
      xtractor <- xtractorOnPropertyElem(propertyElement)
    } yield xtractor
  }
    
  def getAtomics(aKey: String): AlmValidationMultipleBadData[List[XTractorAtomic]] =
    for {
      propertyElement <-
      	getUniquePropertyElement(aKey).toMultipleBadData
      elems <- 
      	(propertyElement.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.successSingleBadData)).toMultipleBadData
      xtractors <- { 
      	val items =
      	  elems.zipWithIndex.map {case(x, i) => 
      	    onSingleTextOnlyTypeContainerGetText(x).flatMap { y =>
      	      val txt = y.getOrElse("")
      	      new XTractorAtomicString(txt, "[%d]".format(i)).successSingleBadData}}
      	  .map{x => x.toMultipleBadData}
          .toList
        items.sequence 
      }
    } yield xtractors
  
  private def getUniquePropertyElement(aKey: String): AlmValidationSingleBadData[Option[Elem]] = {
    val propertyContainers = XmlPrimitives.elems(elem, aKey)
    propertyContainers match {
      case Seq(untrimmedPropertyContainer) => 
         scala.xml.Utility.trim(untrimmedPropertyContainer).asInstanceOf[Elem].successSingleBadData.map(Some(_))
      case Seq() =>
        None.success
      case _ =>
        SingleBadDataProblem("Element contained more than once: %d".format(propertyContainers.length), key = aKey).fail[Option[Elem]] 
    }
  }
  
  private def xtractorOnPropertyElem(mayBeAnElem: Option[Elem]): AlmValidationSingleBadData[Option[XTractor]] = {
    mayBeAnElem match {
      case Some(elem) =>
        val aKey = elem.label
        elem match {
          case Elem(_, _, _, _, typeContainer @ Elem(_,_,_,_,_*)) => 
           onValidTypeContainerXTractor(typeContainer.asInstanceOf[Elem]).map(Some(_))
          case Elem(_, _, _, _) => 
            None.successSingleBadData
          case Elem(_, _, _, _, x) => 
            SingleBadDataProblem("The only child is not an Element: %s".format(x), key = aKey).fail[Option[XTractor]] 
          case Elem(_, _, _, _, _*) =>
            SingleBadDataProblem("Element containes more than one child.", key = aKey).fail[Option[XTractor]] 
        }
      case None => None.successSingleBadData
    }
  }
  
  private def onAllChildrenAreElems(elem: Elem): AlmValidationSingleBadData[Seq[Elem]] =
    if(elem.child.forall(n => n.isInstanceOf[Elem]))
      XmlPrimitives.elems(elem).successSingleBadData
    else
      SingleBadDataProblem("Not all children are Elems", key = elem.label).fail[Seq[Elem]] 
    
  
  private def onSingleTextOnlyElem[U](aKey: String, f: (String, String) => AlmValidationSingleBadData[Option[U]]): AlmValidationSingleBadData[Option[U]] = {
    val elems = XmlPrimitives.elems(elem, aKey)
    elems match {
      case Seq() => 
        None.successSingleBadData
      case Seq(elem) => 
        scala.xml.Utility.trim(elem) match {
          case Elem(_, _, _, _, Text(text)) => 
            f(text, aKey)
          case Elem(_, _, _, _) =>
            None.successSingleBadData
          case _ =>
            SingleBadDataProblem("Is not a text only node", key = elem.label).fail[Option[U]] 
        }
      case _ => 
        SingleBadDataProblem("More than one child: %d".format(elems.length), key = aKey).fail[Option[U]]
    }
  }

  private def onSingleTextOnlyTypeContainerGetText[U](elem: Elem): AlmValidationSingleBadData[Option[String]] =
    scala.xml.Utility.trim(elem) match {
      case Elem(_, _, _, _, Text(text)) => 
        (if(text.trim.isEmpty) None else Some(text)).successSingleBadData
      case Elem(_, _, _, _) =>
        None.successSingleBadData
      case _ =>
        SingleBadDataProblem("Is not a text only node", key = elem.label).fail[Option[String]] 
    }
  
  private def onValidTypeContainerXTractor(typeContainer: Elem): AlmValidationSingleBadData[XTractor] =
    onAllChildrenAreElems(typeContainer).map {_ => new XmlXTractor(typeContainer)}
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