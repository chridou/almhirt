/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.xtractnduce.xml

import language.implicitConversions
import scala.xml.{Elem, Text}
import scalaz._
import Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.xml.funs._
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
        text => {
          if(text.trim.isEmpty) 
            None.success
          else
            Some(text).success })
  
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
    ??? //onSingleTextOnlyElem(aKey, tryParseBase64Alm)
    
  def tryGetAsString(aKey: String) = 
    ???//BadDataProblem("not supported", key = pathAsStringWithKey(aKey)).failure[Option[String]]

  def isBooleanSetTrue(aKey: String) =
   	getUniquePropertyElement(aKey) flatMap { x =>
   	  x match {
   	    case Some(e) =>
          if(e.text.trim.isEmpty) 
            false.success[BadDataProblem] 
          else 
            parseBooleanAlm(e.text)
   	    case None =>
   	      false.success[BadDataProblem] } }

  def tryGetTypeInfo() = Some(elem.label).success
  
  def getXTractors(aKey: String): AlmValidation[List[XTractor]] =
    (getUniquePropertyElement(aKey).toAgg) flatMap (x =>
      (x.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.success)).toAgg) flatMap (elems =>
      	elems
      	  .zipWithIndex
      	  .map{case(x, i) => onValidTypeContainerXTractor(x, Some("%s[%d]".format(aKey, i))).toAgg}.toList.sequence[AlmValidationAP, XTractor])
//      
//      elems <- 
//      	(propertyElement.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.success)).toAgg
//      xtractors <- 
//      	elems
//      	  .zipWithIndex
//      	  .map{case(x, i) => onValidTypeContainerXTractor(x, Some("%s[%d]".format(aKey, i))).toAgg}.toList.sequence
//    } yield xtractors
  
  def tryGetXTractor(aKey: String): AlmValidation[Option[XTractor]] = {
    getUniquePropertyElement(aKey) flatMap (propertyElement =>
      xtractorOnPropertyElem(propertyElement))
  }
    
  def tryGetAtomic(aKey: String): AlmValidation[Option[XTractorAtomic]] =
    getUniquePropertyElement(aKey) flatMap( propertyElement =>
      propertyElement
        .flatMap{e => onSingleTextOnlyTypeContainerGetText(e).optionOut }.validationOut) flatMap (text =>
        (text map {txt => new XTractorAtomicString(txt, aKey, Some(this))}).success)
    
  def getAtomics(aKey: String): AlmValidation[List[XTractorAtomic]] =
    (getUniquePropertyElement(aKey).toAgg) flatMap (propertyElement =>
      (propertyElement.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.success)).toAgg) flatMap { elems =>
      	val items =
      	  elems.zipWithIndex.map {case(x, i) => 
      	    onSingleTextOnlyTypeContainerGetText(x).flatMap { y =>
      	      val txt = y.getOrElse("")
      	      new XTractorAtomicString(txt, "%s[%d]".format(aKey, i), Some(this)).success}}
      	  .map{x => x.toAgg}
          .toList
        items.sequence[AlmValidationAP, XTractorAtomicString] }
  
  private def getUniquePropertyElement(aKey: String): AlmValidation[Option[Elem]] = {
    val propertyContainers = elems(elem)(aKey)
    propertyContainers match {
      case Seq(untrimmedPropertyContainer) => 
         scala.xml.Utility.trim(untrimmedPropertyContainer).asInstanceOf[Elem].success.map(Some(_))
      case Seq() =>
        None.success
      case _ =>
        BadDataProblem("Element contained more than once: %d times".format(propertyContainers.length)).failure[Option[Elem]] 
    }
  }
  
  private def xtractorOnPropertyElem(mayBeAnElem: Option[Elem]): AlmValidation[Option[XTractor]] = {
    mayBeAnElem match {
      case Some(elem) =>
        val aKey = elem.label
        elem match {
          case Elem(_, _, _, _, typeContainer @ Elem(_,_,_,_,_*)) => 
           onValidTypeContainerXTractor(typeContainer.asInstanceOf[Elem]).map(Some(_))
          case Elem(_, _, _, _) => 
            None.success
          case Elem(_, _, _, _, x) => 
            BadDataProblem("The only child is not an Element: %s".format(x)).failure[Option[XTractor]] 
          case Elem(_, _, _, _, _*) =>
            BadDataProblem("Element containes more than one child.").failure[Option[XTractor]] 
        }
      case None => None.success
    }
  }
  
  private def onAllChildrenAreElems(elem: Elem): AlmValidation[Seq[Elem]] =
    if(elem.child.forall(n => n.isInstanceOf[Elem]))
      allElems(elem).success
    else
      BadDataProblem("Not all children are Elems").failure[Seq[Elem]] 
    
  
  private def onSingleTextOnlyElem[U](aKey: String, f: String => AlmValidation[Option[U]]): AlmValidation[Option[U]] = {
    val theElems = elems(elem)(aKey)
    theElems match {
      case Seq() => 
        None.success
      case Seq(elem) => 
        scala.xml.Utility.trim(elem) match {
          case Elem(_, _, _, _, Text(text)) => 
            f(text)
          case Elem(_, _, _, _) =>
            None.success
          case _ =>
            BadDataProblem("Is not a text only node").failure[Option[U]] 
        }
      case _ => 
        BadDataProblem("More than one child: %d".format(theElems.length)).failure[Option[U]]
    }
  }

  private def onSingleTextOnlyTypeContainerGetText[U](elem: Elem): AlmValidation[Option[String]] =
    scala.xml.Utility.trim(elem) match {
      case Elem(_, _, _, _, Text(text)) => 
        (if(text.trim.isEmpty) None else Some(text)).success
      case Elem(_, _, _, _) =>
        None.success
      case _ =>
        BadDataProblem("Is not a text only node").failure[Option[String]] 
    }
  
  private def onValidTypeContainerXTractor(typeContainer: Elem, aKey: Option[String] = None): AlmValidation[XTractor] =
    onAllChildrenAreElems(typeContainer).map {_ => new XmlXTractor(typeContainer, aKey, Some(this))}
}

object XmlXTractor {
  def apply(elem: Elem): AlmValidation[XTractor] =
    new XmlXTractor(elem).success
  def apply(xml: String): AlmValidation[XTractor] = 
    try {
      new XmlXTractor(scala.xml.XML.loadString(xml)).success
    } catch {
      case exn: Exception => 
        BadDataProblem("An error occured: %s".format(exn.getMessage), cause = Some(exn)).failure[XTractor] 
    }
    
  
  implicit def elem2XmlXTracor(elem: Elem): ElemXtractorW = new ElemXtractorW(elem)
  final class ElemXtractorW(elem: Elem) {
    def xtractor(): XmlXTractor = new XmlXTractor(elem)
  }
}