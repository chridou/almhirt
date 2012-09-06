package almhirt.xtractnduce.xml

import scala.xml.{Elem, Text}
import scalaz._
import Scalaz._
import almhirt._
import almhirt.almvalidationimports._
import almhirt.xml._
import almhirt.syntax.xml._
import almhirt.xtractnduce.{XTractor, XTractorAtomic, XTractorAtomicString}

class XmlXTractor(elem: Elem, keyOverride: Option[String] = None, val parent: Option[XTractor] = None) extends XTractor with ScribbableXmlXTractor {
  type T = Elem
  def underlying() = elem
  val key = keyOverride getOrElse elem.label
  def keys() = elem.elems.map(_.label)
  
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

  def tryGetUUID(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseUUIDAlm)
    
  def tryGetBytes(aKey: String) = 
    onSingleTextOnlyElem(aKey, tryParseBase64Alm)
    
  def tryGetAsString(aKey: String) = 
    SingleBadDataProblem("not supported", key = pathAsStringWithKey(aKey)).failure[Option[String]]

  def isBooleanSetTrue(aKey: String) =
   	getUniquePropertyElement(aKey) bind { x =>
   	  x match {
   	    case Some(e) =>
          if(e.text.trim.isEmpty) 
            false.success[SingleBadDataProblem] 
          else 
            parseBooleanAlm(e.text, pathAsStringWithKey(aKey))
   	    case None =>
   	      false.success[SingleBadDataProblem] } }

  def tryGetTypeInfo() = Some(elem.label).success
  
  def getXTractors(aKey: String): AlmValidationMBD[List[XTractor]] =
    (getUniquePropertyElement(aKey).toMBD) bind (x =>
      (x.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.successSBD)).toMBD) bind (elems =>
      	elems
      	  .zipWithIndex
      	  .map{case(x, i) => onValidTypeContainerXTractor(x, Some("%s[%d]".format(aKey, i))).toMBD}.toList.sequence[AlmValidationMBD, XTractor])
//      
//      elems <- 
//      	(propertyElement.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.successSBD)).toMBD
//      xtractors <- 
//      	elems
//      	  .zipWithIndex
//      	  .map{case(x, i) => onValidTypeContainerXTractor(x, Some("%s[%d]".format(aKey, i))).toMBD}.toList.sequence
//    } yield xtractors
  
  def tryGetXTractor(aKey: String): AlmValidationSBD[Option[XTractor]] = {
    getUniquePropertyElement(aKey) bind (propertyElement =>
      xtractorOnPropertyElem(propertyElement))
  }
    
  def tryGetAtomic(aKey: String): AlmValidationSBD[Option[XTractorAtomic]] =
    getUniquePropertyElement(aKey) bind( propertyElement =>
      propertyElement
        .flatMap{e => onSingleTextOnlyTypeContainerGetText(e).optionOut }.validationOut) bind (text =>
        (text map {txt => new XTractorAtomicString(txt, aKey, Some(this))}).successSBD)
    
  def getAtomics(aKey: String): AlmValidationMBD[List[XTractorAtomic]] =
    (getUniquePropertyElement(aKey).toMBD) bind (propertyElement =>
      (propertyElement.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.successSBD)).toMBD) bind { elems =>
      	val items =
      	  elems.zipWithIndex.map {case(x, i) => 
      	    onSingleTextOnlyTypeContainerGetText(x).bind { y =>
      	      val txt = y.getOrElse("")
      	      new XTractorAtomicString(txt, "%s[%d]".format(aKey, i), Some(this)).successSBD}}
      	  .map{x => x.toMBD}
          .toList
        items.sequence[AlmValidationMBD, XTractorAtomicString] }
  
  private def getUniquePropertyElement(aKey: String): AlmValidationSBD[Option[Elem]] = {
    val propertyContainers = xmlfunctions.elems(elem, aKey)
    propertyContainers match {
      case Seq(untrimmedPropertyContainer) => 
         scala.xml.Utility.trim(untrimmedPropertyContainer).asInstanceOf[Elem].successSBD.map(Some(_))
      case Seq() =>
        None.success
      case _ =>
        SingleBadDataProblem("Element contained more than once: %d times".format(propertyContainers.length), key = pathAsStringWithKey(aKey)).failure[Option[Elem]] 
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
            SingleBadDataProblem("The only child is not an Element: %s".format(x), key = pathAsStringWithKey(aKey)).failure[Option[XTractor]] 
          case Elem(_, _, _, _, _*) =>
            SingleBadDataProblem("Element containes more than one child.", key = pathAsStringWithKey(aKey)).failure[Option[XTractor]] 
        }
      case None => None.successSBD
    }
  }
  
  private def onAllChildrenAreElems(elem: Elem): AlmValidationSBD[Seq[Elem]] =
    if(elem.child.forall(n => n.isInstanceOf[Elem]))
      xmlfunctions.elems(elem).successSBD
    else
      SingleBadDataProblem("Not all children are Elems", key = pathAsStringWithKey(elem.label)).failure[Seq[Elem]] 
    
  
  private def onSingleTextOnlyElem[U](aKey: String, f: (String, String) => AlmValidationSBD[Option[U]]): AlmValidationSBD[Option[U]] = {
    val elems = xmlfunctions.elems(elem, aKey)
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
            SingleBadDataProblem("Is not a text only node", key = pathAsStringWithKey(aKey)).failure[Option[U]] 
        }
      case _ => 
        SingleBadDataProblem("More than one child: %d".format(elems.length), key = pathAsStringWithKey(aKey)).failure[Option[U]]
    }
  }

  private def onSingleTextOnlyTypeContainerGetText[U](elem: Elem): AlmValidationSBD[Option[String]] =
    scala.xml.Utility.trim(elem) match {
      case Elem(_, _, _, _, Text(text)) => 
        (if(text.trim.isEmpty) None else Some(text)).successSBD
      case Elem(_, _, _, _) =>
        None.successSBD
      case _ =>
        SingleBadDataProblem("Is not a text only node", key = pathAsStringWithKey(elem.label)).failure[Option[String]] 
    }
  
  private def onValidTypeContainerXTractor(typeContainer: Elem, aKey: Option[String] = None): AlmValidationSBD[XTractor] =
    onAllChildrenAreElems(typeContainer).map {_ => new XmlXTractor(typeContainer, aKey, Some(this))}
}

object XmlXTractor {
  def apply(elem: Elem): AlmValidationSBD[XTractor] =
    new XmlXTractor(elem).successSBD
  def apply(xml: String): AlmValidationSBD[XTractor] = 
    try {
      new XmlXTractor(scala.xml.XML.loadString(xml)).successSBD
    } catch {
      case exn => 
        SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = "xml", cause = Some(CauseIsThrowable(exn))).failure[XTractor] 
    }
    
  
  implicit def elem2XmlXTracor(elem: Elem): ElemXtractorW = new ElemXtractorW(elem)
  final class ElemXtractorW(elem: Elem) {
    def xtractor(): XmlXTractor = new XmlXTractor(elem)
  }
}