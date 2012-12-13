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
package almhirt.xml

import scalaz._, Scalaz._
import org.joda.time.DateTime
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.problem.inst._
import almhirt.syntax.almvalidation._

trait XmlFunctions {
  import scala.xml.{XML, Node, Elem, NodeSeq}
  import scala.xml.XML
  import almhirt.problem.all._
  
  def elems(elem: Elem): Seq[Elem] = 
    elem.child flatMap { (n: Node) => 
      n match {
        case e:Elem => Some(e)                                   
        case _ => None
      } }

  def elems(elem: Elem, label: String): Seq[Elem] = elems(elem) filter (_.label == label)
  
  def xmlFromString(xmlString: String, key: String = ""): AlmValidation[Elem] = {
    try {
      XML.loadString(xmlString).success
    } catch {
      case err => BadDataProblem("Could not parse xml: %s".format(err.getMessage), cause = Some(CauseIsThrowable(err))).failure[Elem]
    }
  }
  
  def intFromXmlNode(node: Elem): AlmValidation[Int] = 
    notEmptyOrWhitespace(node.text, node.label) bind (ne => parseIntAlm(ne, node.label))
    
  def longFromXmlNode(node: Elem): AlmValidation[Long] = 
    notEmptyOrWhitespace(node.text, node.label) bind (ne => parseLongAlm(ne, node.label))
  
  def doubleFromXmlNode(node: Elem): AlmValidation[Double] = 
    notEmptyOrWhitespace(node.text, node.label) bind (ne => parseDoubleAlm(ne, node.label))

  def floatFromXmlNode(node: Elem): AlmValidation[Float] = 
   notEmptyOrWhitespace(node.text, node.label) bind (ne => parseFloatAlm(ne, node.label))

  def decimalFromXmlNode(node: Elem): AlmValidation[BigDecimal] =
   notEmptyOrWhitespace(node.text, node.label) bind (ne => parseDecimalAlm(ne, node.label))

  def dateTimeFromXmlNode(node: Elem): AlmValidation[DateTime] = 
   notEmptyOrWhitespace(node.text, node.label) bind (ne => parseDateTimeAlm(ne, node.label))

  def optionalIntXmlNode(node: Elem): AlmValidation[Option[Int]] = 
    if(node.text.trim.isEmpty) 
      None.success[BadDataProblem]
    else 
      intFromXmlNode(node) fold(_.failure, Some(_).success)
    
  def optionalLongXmlNode(node: Elem): AlmValidation[Option[Long]] = 
    if(node.text.trim.isEmpty) 
      None.success[BadDataProblem] 
    else 
      longFromXmlNode(node) fold (_.failure, Some(_).success)
  
  def optionalDoubleXmlNode(node: Elem): AlmValidation[Option[Double]] = 
    if(node.text.trim.isEmpty) 
      None.success[BadDataProblem] 
    else 
      doubleFromXmlNode(node) fold (_.failure, Some(_).success)
  
  def optionalFloatXmlNode(node: Elem): AlmValidation[Option[Float]] = 
    if(node.text.trim.isEmpty) 
      None.success[BadDataProblem] 
    else 
      floatFromXmlNode(node) fold (_.failure, Some(_).success)
  
  def optionalDecimalXmlNode(node: Elem): AlmValidation[Option[BigDecimal]] = 
    if(node.text.trim.isEmpty) 
      None.success[BadDataProblem] 
    else 
      decimalFromXmlNode(node) fold (_.failure, Some(_).success)

  def optionalDateTimeXmlNode(node: Elem): AlmValidation[Option[DateTime]] = 
    if(node.text.trim.isEmpty) 
      None.success[BadDataProblem] 
    else 
      dateTimeFromXmlNode(node) fold (_.failure, Some(_).success)
  
  def isBooleanSetTrue(elem: Elem): AlmValidation[Boolean] = 
    if(elem.text.trim.isEmpty) 
      false.success[BadDataProblem] 
    else 
      parseBooleanAlm(elem.text, elem.label)
  
  def firstChildNodeMandatory(node: Elem, label: String): AlmValidation[Elem] = {
    elems(node, label).toList match {
      case Nil => 
        BadDataProblem("Element '%s' not found.".format(label)).withIdentifier(label).failure[Elem]
      case l :: ls => l.success
    }
  }
  
  def mapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidation[T]): AlmValidation[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t) map { r => Some(r) }
      case None => None.success
    }

  def flatMapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidation[Option[T]]): AlmValidation[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t)
      case None => None.success
    }

  def stringFromChild(node: Elem, label: String): AlmValidation[String] =
    firstChildNodeMandatory(node, label)
    .bind {node => notEmptyOrWhitespace(node.text, label)}

  def doubleFromChild(node: Elem, label: String): AlmValidation[Double] =
    firstChildNodeMandatory(node, label)
    .bind {node => parseDoubleAlm(node.text, label)}

  def intFromChild(node: Elem, label: String): AlmValidation[Int] =
    firstChildNodeMandatory(node, label)
    .bind {node => parseIntAlm(node.text, label)}

  def longFromChild(node: Elem, label: String): AlmValidation[Long] =
    firstChildNodeMandatory(node, label)
    .bind {node => parseLongAlm(node.text, label)}
  
  private def emptyStringIsNone[T](str: String, compute: String => AlmValidation[T]): AlmValidation[Option[T]] =
    if(str.trim().isEmpty)
      None.success
    else
      compute(str) fold(_.failure, Some(_).success)
  
  def stringOptionFromChild(node: Elem, label: String): AlmValidation[Option[String]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => s.success))

  def doubleOptionFromChild(node: Elem, label: String): AlmValidation[Option[Double]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseDoubleAlm(s, label)))

  def intOptionFromChild(node: Elem, label: String): AlmValidation[Option[Int]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseIntAlm(s, label)))

  def longOptionFromChild(node: Elem, label: String): AlmValidation[Option[Long]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseLongAlm(s, label)))
}

