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

import java.util.{ UUID => JUUID }
import scalaz._, Scalaz._
import scalaz.std._
import org.joda.time.DateTime
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.problem.inst._
import almhirt.syntax.almvalidation._

trait XmlFunctions {
  import scala.xml.{ XML, Node, Elem, NodeSeq }
  import scala.xml.XML
  import almhirt.problem.all._

  def allElems(elem: Elem): Seq[Elem] =
    elem.child flatMap { (n: Node) =>
      n match {
        case e: Elem => Some(e)
        case _ => None
      }
    }

  def elems(elem: Elem)(label: String): Seq[Elem] = allElems(elem) filter (_.label == label)

  def tryGetChild(elem: Elem)(label: String): AlmValidation[Option[Elem]] =
    elems(elem)(label) match {
      case Seq() => None.success
      case Seq(x) => Some(x).success
      case _ => UnspecifiedProblem("More than one child element found for: %s".format(label)).failure
    }

  def getChild(elem: Elem)(label: String): AlmValidation[Elem] =
    tryGetChild(elem)(label).flatMap(childOpt =>
      option.cata(childOpt)(
        e => e.success,
        UnspecifiedProblem("The element did not contain a child labeled %s".format(label)).failure))

  def xmlFromString(xmlString: String): AlmValidation[Elem] = {
    try {
      XML.loadString(xmlString).success
    } catch {
      case err: Exception => BadDataProblem("Could not parse xml: %s".format(err.getMessage), cause = Some(err)).failure[Elem]
    }
  }

  def stringFromXmlNode(node: Elem): AlmValidation[String] =
    notEmptyOrWhitespace(node.text)

  def optionalStringFromXmlNode(node: Elem): Option[String] =
    notEmptyOrWhitespace(node.text).toOption

  def booleanFromXmlNode(node: Elem): AlmValidation[Boolean] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseBooleanAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalBooleanFromXmlNode(node: Elem): AlmValidation[Option[Boolean]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      booleanFromXmlNode(node) fold (_.failure, Some(_).success)

  def byteFromXmlNode(node: Elem): AlmValidation[Byte] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseByteAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalByteFromXmlNode(node: Elem): AlmValidation[Option[Byte]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      byteFromXmlNode(node) fold (_.failure, Some(_).success)
      
  def intFromXmlNode(node: Elem): AlmValidation[Int] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseIntAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalIntFromXmlNode(node: Elem): AlmValidation[Option[Int]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      intFromXmlNode(node) fold (_.failure, Some(_).success)

  def longFromXmlNode(node: Elem): AlmValidation[Long] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseLongAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalLongFromXmlNode(node: Elem): AlmValidation[Option[Long]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      longFromXmlNode(node) fold (_.failure, Some(_).success)

  def bigIntFromXmlNode(node: Elem): AlmValidation[BigInt] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseBigIntAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalBigIntFromXmlNode(node: Elem): AlmValidation[Option[BigInt]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      bigIntFromXmlNode(node) fold (_.failure, Some(_).success)

  def floatFromXmlNode(node: Elem): AlmValidation[Float] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseFloatAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalFloatFromXmlNode(node: Elem): AlmValidation[Option[Float]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      floatFromXmlNode(node) fold (_.failure, Some(_).success)

  def doubleFromXmlNode(node: Elem): AlmValidation[Double] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseDoubleAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalDoubleFromXmlNode(node: Elem): AlmValidation[Option[Double]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      doubleFromXmlNode(node) fold (_.failure, Some(_).success)

  def decimalFromXmlNode(node: Elem): AlmValidation[BigDecimal] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseDecimalAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalDecimalFromXmlNode(node: Elem): AlmValidation[Option[BigDecimal]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      decimalFromXmlNode(node) fold (_.failure, Some(_).success)

  def dateTimeFromXmlNode(node: Elem): AlmValidation[DateTime] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseDateTimeAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalDateTimeFromXmlNode(node: Elem): AlmValidation[Option[DateTime]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      dateTimeFromXmlNode(node) fold (_.failure, Some(_).success)

  def uuidFromXmlNode(node: Elem): AlmValidation[JUUID] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseUuidAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalUuidFromXmlNode(node: Elem): AlmValidation[Option[JUUID]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      uuidFromXmlNode(node) fold (_.failure, Some(_).success)

  def uriFromXmlNode(node: Elem): AlmValidation[_root_.java.net.URI] =
    notEmptyOrWhitespace(node.text) flatMap (ne => parseUriAlm(ne)) bimap (f => f.withIdentifier(node.label), s => s)

  def optionalUriFromXmlNode(node: Elem): AlmValidation[Option[_root_.java.net.URI]] =
    if (node.text.trim.isEmpty)
      None.success[BadDataProblem]
    else
      uriFromXmlNode(node) fold (_.failure, Some(_).success)

  def isBooleanSetTrue(elem: Elem): AlmValidation[Boolean] =
    if (elem.text.trim.isEmpty)
      false.success[BadDataProblem]
    else
      parseBooleanAlm(elem.text) bimap (f => f.withIdentifier(elem.label), s => s)

  def firstChildNodeMandatory(node: Elem, label: String): AlmValidation[Elem] = {
    elems(node)(label).toList match {
      case Nil =>
        BadDataProblem("Element '%s' not found.".format(label)).withIdentifier(label).failure[Elem]
      case l :: ls => l.success
    }
  }

  def mapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidation[T]): AlmValidation[Option[T]] =
    elems(node)(label).headOption match {
      case Some(t) => compute(t) map { r => Some(r) }
      case None => None.success
    }

  def flatMapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidation[Option[T]]): AlmValidation[Option[T]] =
    elems(node)(label).headOption match {
      case Some(t) => compute(t)
      case None => None.success
    }

  def stringFromChild(node: Elem, label: String): AlmValidation[String] =
    firstChildNodeMandatory(node, label)
      .flatMap { node => notEmptyOrWhitespace(node.text) } bimap (f => f.withIdentifier(label), s => s)

  def stringOptionFromChild(node: Elem, label: String): Option[String] =
    elems(node)(label).headOption.flatMap(optionalStringFromXmlNode)

  def intFromChild(node: Elem, label: String): AlmValidation[Int] =
    firstChildNodeMandatory(node, label)
      .flatMap { node => parseIntAlm(node.text) } bimap (f => f.withIdentifier(label), s => s)

  def intOptionFromChild(node: Elem, label: String): AlmValidation[Option[Int]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseIntAlm(s))) bimap (f => f.withIdentifier(label), s => s)

  def longFromChild(node: Elem, label: String): AlmValidation[Long] =
    firstChildNodeMandatory(node, label)
      .flatMap { node => parseLongAlm(node.text) } bimap (f => f.withIdentifier(label), s => s)

  def longOptionFromChild(node: Elem, label: String): AlmValidation[Option[Long]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseLongAlm(s))) bimap (f => f.withIdentifier(label), s => s)

  def doubleFromChild(node: Elem, label: String): AlmValidation[Double] =
    firstChildNodeMandatory(node, label)
      .flatMap { node => parseDoubleAlm(node.text) } bimap (f => f.withIdentifier(label), s => s)

  def doubleOptionFromChild(node: Elem, label: String): AlmValidation[Option[Double]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseDoubleAlm(s))) bimap (f => f.withIdentifier(label), s => s)

  def dateTimeFromChild(node: Elem, label: String): AlmValidation[DateTime] =
    firstChildNodeMandatory(node, label)
      .flatMap { node => parseDateTimeAlm(node.text) } bimap (f => f.withIdentifier(label), s => s)

  def dateTimeOptionFromChild(node: Elem, label: String): AlmValidation[Option[DateTime]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseDateTimeAlm(s))) bimap (f => f.withIdentifier(label), s => s)

  def uuidFromChild(node: Elem, label: String): AlmValidation[JUUID] =
    firstChildNodeMandatory(node, label)
      .flatMap { node => parseUuidAlm(node.text) } bimap (f => f.withIdentifier(label), s => s)

  def uuidOptionFromChild(node: Elem, label: String): AlmValidation[Option[JUUID]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseUuidAlm(s))) bimap (f => f.withIdentifier(label), s => s)

  private def emptyStringIsNone[T](str: String, compute: String => AlmValidation[T]): AlmValidation[Option[T]] =
    if (str.trim().isEmpty)
      None.success
    else
      compute(str) fold (_.failure, Some(_).success)

  def getAttributeValue(node: Elem, name: String): AlmValidation[String] =
    (node \ s"@$name") match {
      case NodeSeq.Empty => NoSuchElementProblem("Attribute not found").withIdentifier(name).failure
      case x => x.text.success
    }

  def getOptionalAttributeValue(node: Elem, name: String): Option[String] =
    getAttributeValue(node, name).toOption
}

