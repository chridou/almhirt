package almhirt.xml

import scalaz._, Scalaz._
import org.joda.time.DateTime
import almhirt.validation._
import almhirt.validation.AlmValidationFunctions._
import almhirt.validation.ProblemInstances._
import almhirt.validation.syntax._

trait XmlFunctions {
  import scala.xml.{XML, Node, Elem, NodeSeq}
  import scala.xml.XML
  
  def elems(elem: Elem): Seq[Elem] = 
    elem.child flatMap { (n: Node) => 
      n match {
        case e:Elem => Some(e)                                   
        case _ => None
      } }

  def elems(elem: Elem, label: String): Seq[Elem] = elems(elem) filter (_.label == label)
  
  def xmlFromString(xmlString: String, key: String = "XML"): AlmValidationSBD[Elem] = {
    try {
      XML.loadString(xmlString).successSBD
    } catch {
      case err => SingleBadDataProblem("Could not parse xml: %s".format(err.getMessage), key = key, cause = Some(CauseIsThrowable(err))).failure[Elem]
    }
  }
  
  def intFromXmlNode(node: Elem): AlmValidationSBD[Int] = 
    notEmptyOrWhitespace(node.text, node.label) bind (ne => parseIntAlm(ne, node.label))
    
  def longFromXmlNode(node: Elem): AlmValidationSBD[Long] = 
    notEmptyOrWhitespace(node.text, node.label) bind (ne => parseLongAlm(ne, node.label))
  
  def doubleFromXmlNode(node: Elem): AlmValidationSBD[Double] = 
    notEmptyOrWhitespace(node.text, node.label) bind (ne => parseDoubleAlm(ne, node.label))

  def floatFromXmlNode(node: Elem): AlmValidationSBD[Float] = 
   notEmptyOrWhitespace(node.text, node.label) bind (ne => parseFloatAlm(ne, node.label))

  def decimalFromXmlNode(node: Elem): AlmValidationSBD[BigDecimal] =
   notEmptyOrWhitespace(node.text, node.label) bind (ne => parseDecimalAlm(ne, node.label))

  def dateTimeFromXmlNode(node: Elem): AlmValidationSBD[DateTime] = 
   notEmptyOrWhitespace(node.text, node.label) bind (ne => parseDateTimeAlm(ne, node.label))

  def optionalIntXmlNode(node: Elem): AlmValidationSBD[Option[Int]] = 
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem]
    else 
      intFromXmlNode(node) fold(_.failure, Some(_).successSBD)
    
  def optionalLongXmlNode(node: Elem): AlmValidationSBD[Option[Long]] = 
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      longFromXmlNode(node) fold (_.failure, Some(_).successSBD)
  
  def optionalDoubleXmlNode(node: Elem): AlmValidationSBD[Option[Double]] = 
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      doubleFromXmlNode(node) fold (_.failure, Some(_).successSBD)
  
  def optionalFloatXmlNode(node: Elem): AlmValidationSBD[Option[Float]] = 
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      floatFromXmlNode(node) fold (_.failure, Some(_).successSBD)
  
  def optionalDecimalXmlNode(node: Elem): AlmValidationSBD[Option[BigDecimal]] = 
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      decimalFromXmlNode(node) fold (_.failure, Some(_).successSBD)

  def optionalDateTimeXmlNode(node: Elem): AlmValidationSBD[Option[DateTime]] = 
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      dateTimeFromXmlNode(node) fold (_.failure, Some(_).successSBD)
  
  def isBooleanSetTrue(elem: Elem): AlmValidationSBD[Boolean] = 
    if(elem.text.trim.isEmpty) 
      false.success[SingleBadDataProblem] 
    else 
      parseBooleanAlm(elem.text, elem.label)
  
  def firstChildNodeMandatory(node: Elem, label: String): AlmValidationSBD[Elem] = {
    elems(node, label).toList match {
      case Nil => SingleBadDataProblem("Element '%s' not found.".format(label), node.label).failure[Elem]
      case l :: ls => l.successSBD
    }
  }
  
  def mapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t) map { r => Some(r) }
      case None => None.success
    }

  def flatMapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidationSBD[Option[T]]): AlmValidationSBD[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t)
      case None => None.success
    }

  def stringFromChild(node: Elem, label: String): AlmValidationSBD[String] =
    firstChildNodeMandatory(node, label)
    .bind {node => notEmptyOrWhitespace(node.text, label)}

  def doubleFromChild(node: Elem, label: String): AlmValidationSBD[Double] =
    firstChildNodeMandatory(node, label)
    .bind {node => parseDoubleAlm(node.text, label)}

  def intFromChild(node: Elem, label: String): AlmValidationSBD[Int] =
    firstChildNodeMandatory(node, label)
    .bind {node => parseIntAlm(node.text, label)}

  def longFromChild(node: Elem, label: String): AlmValidationSBD[Long] =
    firstChildNodeMandatory(node, label)
    .bind {node => parseLongAlm(node.text, label)}
  
  private def emptyStringIsNone[T](str: String, compute: String => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
    if(str.trim().isEmpty)
      None.success
    else
      compute(str) fold(_.failure, Some(_).success)
  
  def stringOptionFromChild(node: Elem, label: String): AlmValidationSBD[Option[String]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => s.success))

  def doubleOptionFromChild(node: Elem, label: String): AlmValidationSBD[Option[Double]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseDoubleAlm(s, label)))

  def intOptionFromChild(node: Elem, label: String): AlmValidationSBD[Option[Int]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseIntAlm(s, label)))

  def longOptionFromChild(node: Elem, label: String): AlmValidationSBD[Option[Long]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseLongAlm(s, label)))

  def mapOptionalFirstChildM[T](node: Elem, label: String, compute: Elem => AlmValidationMBD[T]): AlmValidationMBD[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t) map { r => Some(r) }
      case None => None.success
    }

  def mapChildren[T](node: Elem, label: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[T]] = {
    val validations: List[AlmValidationMBD[T]] =
      elems(node, label).toList map {node =>
        map(node) fold(_.prefixWithPath(List(label)).failure, _.success)}
    validations.sequence
  }
  
  def mapChildrenWithAttribute[T](node: Elem, label: String, attName: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[(Option[String], T)]] = {
    val validations: List[AlmValidationMBD[(Option[String], T)]] =
      elems(node, label).toList map {node =>
        val attValue = node.attribute(attName).headOption.map(_.text)
        map(node) fold (_.prefixWithPath(List(label)).failure, (attValue, _).success)}
    validations.sequence
  }
}

object XmlFunctions extends XmlFunctions