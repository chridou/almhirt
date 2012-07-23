package almhirt.xtractnduce.xml

import scala.xml.{Elem, Text}
import scalaz._
import Scalaz._
import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._
import almhirt.xml.XmlPrimitives
import almhirt.xtractnduce.{XTractor, XTractorAtomic, XTractorAtomicString}

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

  def tryGetFloat(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseFloatAlm)

  def tryGetBoolean(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseBooleanAlm)
    
  def tryGetDecimal(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseDecimalAlm)
    
  def tryGetDateTime(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseDateTimeAlm)

  def tryGetBytes(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseBase64Alm)
    
  def tryGetAsString(aKey: String) = 
    SingleBadDataProblem("not supported", key = aKey).fail[Option[String]]

  def isBooleanSetTrue(aKey: String) =
   	getUniquePropertyElement(aKey).flatMap { x =>
   	  x match {
   	    case Some(e) =>
          if(e.text.trim.isEmpty) 
            false.success[SingleBadDataProblem] 
          else 
            parseBooleanAlm(e.text, aKey)
   	    case None =>
   	      false.success[SingleBadDataProblem] } }

  def tryGetTypeInfo() = Success(Some(elem.label))
  
  def getXTractors(aKey: String): AlmValidationMBD[List[XTractor]] =
    for {
      propertyElement <-
      	getUniquePropertyElement(aKey).toMultipleBadData
      elems <- 
      	(propertyElement.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.successSBD)).toMultipleBadData
      xtractors <- 
      	elems.map{x => onValidTypeContainerXTractor(x).toMultipleBadData}.toList.sequence
    } yield xtractors
  
  def tryGetXTractor(aKey: String): AlmValidationSBD[Option[XTractor]] = {
    for {
      propertyElement <- getUniquePropertyElement(aKey)
      xtractor <- xtractorOnPropertyElem(propertyElement)
    } yield xtractor
  }
    
  def tryGetAtomic(aKey: String): AlmValidationSBD[Option[XTractorAtomic]] =
    for {
      propertyElement <- 
        getUniquePropertyElement(aKey)
      text <- 
        propertyElement
          .flatMap{e => onSingleTextOnlyTypeContainerGetText(e).insideOut }
          .insideOut
      xtractor <- 
        (text map {txt => new XTractorAtomicString(txt, aKey)})
        .successSBD
    } yield xtractor
    
  def getAtomics(aKey: String): AlmValidationMBD[List[XTractorAtomic]] =
    for {
      propertyElement <-
      	getUniquePropertyElement(aKey).toMultipleBadData
      elems <- 
      	(propertyElement.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.successSBD)).toMultipleBadData
      xtractors <- { 
      	val items =
      	  elems.zipWithIndex.map {case(x, i) => 
      	    onSingleTextOnlyTypeContainerGetText(x).flatMap { y =>
      	      val txt = y.getOrElse("")
      	      new XTractorAtomicString(txt, "[%d]".format(i)).successSBD}}
      	  .map{x => x.toMultipleBadData}
          .toList
        items.sequence 
      }
    } yield xtractors
  
  private def getUniquePropertyElement(aKey: String): AlmValidationSBD[Option[Elem]] = {
    val propertyContainers = XmlPrimitives.elems(elem, aKey)
    propertyContainers match {
      case Seq(untrimmedPropertyContainer) => 
         scala.xml.Utility.trim(untrimmedPropertyContainer).asInstanceOf[Elem].successSBD.map(Some(_))
      case Seq() =>
        None.success
      case _ =>
        SingleBadDataProblem("Element contained more than once: %d".format(propertyContainers.length), key = aKey).fail[Option[Elem]] 
    }
  }
  
  private def xtractorOnPropertyElem(mayBeAnElem: Option[Elem]): AlmValidationSBD[Option[XTractor]] = {
    mayBeAnElem match {
      case Some(elem) =>
        val aKey = elem.label
        elem match {
          case Elem(_, _, _, _, typeContainer @ Elem(_,_,_,_,_*)) => 
           onValidTypeContainerXTractor(typeContainer.asInstanceOf[Elem]).map(Some(_))
          case Elem(_, _, _, _) => 
            None.successSBD
          case Elem(_, _, _, _, x) => 
            SingleBadDataProblem("The only child is not an Element: %s".format(x), key = aKey).fail[Option[XTractor]] 
          case Elem(_, _, _, _, _*) =>
            SingleBadDataProblem("Element containes more than one child.", key = aKey).fail[Option[XTractor]] 
        }
      case None => None.successSBD
    }
  }
  
  private def onAllChildrenAreElems(elem: Elem): AlmValidationSBD[Seq[Elem]] =
    if(elem.child.forall(n => n.isInstanceOf[Elem]))
      XmlPrimitives.elems(elem).successSBD
    else
      SingleBadDataProblem("Not all children are Elems", key = elem.label).fail[Seq[Elem]] 
    
  
  private def onSingleTextOnlyElem[U](aKey: String, f: (String, String) => AlmValidationSBD[Option[U]]): AlmValidationSBD[Option[U]] = {
    val elems = XmlPrimitives.elems(elem, aKey)
    elems match {
      case Seq() => 
        None.successSBD
      case Seq(elem) => 
        scala.xml.Utility.trim(elem) match {
          case Elem(_, _, _, _, Text(text)) => 
            f(text, aKey)
          case Elem(_, _, _, _) =>
            None.successSBD
          case _ =>
            SingleBadDataProblem("Is not a text only node", key = elem.label).fail[Option[U]] 
        }
      case _ => 
        SingleBadDataProblem("More than one child: %d".format(elems.length), key = aKey).fail[Option[U]]
    }
  }

  private def onSingleTextOnlyTypeContainerGetText[U](elem: Elem): AlmValidationSBD[Option[String]] =
    scala.xml.Utility.trim(elem) match {
      case Elem(_, _, _, _, Text(text)) => 
        (if(text.trim.isEmpty) None else Some(text)).successSBD
      case Elem(_, _, _, _) =>
        None.successSBD
      case _ =>
        SingleBadDataProblem("Is not a text only node", key = elem.label).fail[Option[String]] 
    }
  
  private def onValidTypeContainerXTractor(typeContainer: Elem): AlmValidationSBD[XTractor] =
    onAllChildrenAreElems(typeContainer).map {_ => new XmlXTractor(typeContainer)}
}

object XmlXTractor {
  def apply(elem: Elem): AlmValidationSBD[XTractor] =
    new XmlXTractor(elem).successSBD
  def apply(xml: String): AlmValidationSBD[XTractor] = 
    try {
      new XmlXTractor(scala.xml.XML.loadString(xml)).successSBD
    } catch {
      case exn => 
        SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = "xml", exception= Some(exn)).fail[XTractor] 
    }
    
  
  implicit def elem2XmlXTracor(elem: Elem): ElemXtractorW = new ElemXtractorW(elem)
  final class ElemXtractorW(elem: Elem) {
    def xtractor(): XmlXTractor = new XmlXTractor(elem)
  }
}