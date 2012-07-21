package almhirt.xml

import scalaz._
import Scalaz._
import org.joda.time.DateTime
import almhirt.validation.Problem._
import almhirt.validation._
import almhirt.validation.AlmValidation._
import scala.xml.Elem

object XmlPrimitives extends XmlPrimitivesImplicits {
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
      case err => SingleBadDataProblem("Could not parse xml: %s".format(err.getMessage), key = key, exception = Some(err)).fail[Elem]
    }
  }
  
  def intFromXmlNode(node: Elem): AlmValidationSBD[Int] = {
    for{
      ne <- notEmptyOrWhitespace(node.text, node.label)
      res <- parseIntAlm(ne, node.label)
    } yield res
  }
    
  def longFromXmlNode(node: Elem): AlmValidationSBD[Long] = {
    for{
      ne <- notEmptyOrWhitespace(node.text, node.label)
      res <- parseLongAlm(ne, node.label)
    } yield res
  }
  
  def doubleFromXmlNode(node: Elem): AlmValidationSBD[Double] = {
    for{
      ne <- notEmptyOrWhitespace(node.text, node.label)
      res <- parseDoubleAlm(ne, node.label)
    } yield res
  }

  def floatFromXmlNode(node: Elem): AlmValidationSBD[Float] = {
    for{
      ne <- notEmptyOrWhitespace(node.text, node.label)
      res <- parseFloatAlm(ne, node.label)
    } yield res
  }

  def decimalFromXmlNode(node: Elem): AlmValidationSBD[BigDecimal] = {
    for{
      ne <- notEmptyOrWhitespace(node.text, node.label)
      res <- parseDecimalAlm(ne, node.label)
    } yield res
  }

  def dateTimeFromXmlNode(node: Elem): AlmValidationSBD[DateTime] = {
    for{
      ne <- notEmptyOrWhitespace(node.text, node.label)
      res <- parseDateTimeAlm(ne, node.label)
    } yield res
  }

  def optionalIntXmlNode(node: Elem): AlmValidationSBD[Option[Int]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem]
    else 
      intFromXmlNode(node) match {
        case Success(v) => Some(v).successSBD
        case Failure(f) => f.fail[Option[Int]]
    }
  }
    
  def optionalLongXmlNode(node: Elem): AlmValidationSBD[Option[Long]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      longFromXmlNode(node) match {
        case Success(v) => Some(v).successSBD
        case Failure(f) => f.fail[Option[Long]]
    }
  }
  
  def optionalDoubleXmlNode(node: Elem): AlmValidationSBD[Option[Double]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      doubleFromXmlNode(node) match {
        case Success(v) => Some(v).successSBD
        case Failure(f) => f.fail[Option[Double]]
    }
  }
  
  def optionalFloatXmlNode(node: Elem): AlmValidationSBD[Option[Float]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      floatFromXmlNode(node) match {
        case Success(v) => Some(v).successSBD
        case Failure(f) => f.fail[Option[Float]]
    }
  }
  
  def optionalDecimalXmlNode(node: Elem): AlmValidationSBD[Option[BigDecimal]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      decimalFromXmlNode(node) match {
        case Success(v) => Some(v).successSBD
        case Failure(f) => f.fail[Option[BigDecimal]]
    }
  }

  def optionalDateTimeXmlNode(node: Elem): AlmValidationSBD[Option[DateTime]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      dateTimeFromXmlNode(node) match {
        case Success(v) => Some(v).successSBD
        case Failure(f) => f.fail[Option[DateTime]]
    }
  }
  
  def isBooleanSetTrue(elem: Elem): AlmValidationSBD[Boolean] = 
    if(elem.text.trim.isEmpty) 
      false.success[SingleBadDataProblem] 
    else 
      parseBooleanAlm(elem.text, elem.label)
  
  def firstChildNodeMandatory(node: Elem, label: String): AlmValidationSBD[Elem] = {
    elems(node, label).toList match {
      case Nil => SingleBadDataProblem("Element '%s' not found.".format(label), node.label).fail[Elem]
      case l :: ls => l.successSBD
    }
  }
  
  def mapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t) map { r => Some(r) }
      case None => Success(None)
    }

  def flatMapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidationSBD[Option[T]]): AlmValidationSBD[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t)
      case None => Success(None)
    }

  def stringFromChild(node: Elem, label: String): AlmValidationSBD[String] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => notEmptyOrWhitespace(node.text, label)}

  def doubleFromChild(node: Elem, label: String): AlmValidationSBD[Double] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => parseDoubleAlm(node.text, label)}

  def intFromChild(node: Elem, label: String): AlmValidationSBD[Int] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => parseIntAlm(node.text, label)}

  def longFromChild(node: Elem, label: String): AlmValidationSBD[Long] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => parseLongAlm(node.text, label)}
  
  private def emptyStringIsNone[T](str: String, compute: String => AlmValidationSBD[T]): AlmValidationSBD[Option[T]] =
    if(str.trim().isEmpty)
      Success(None)
    else
      compute(str) match {
        case Success(r) => Success(Some(r))
        case Failure(f) => Failure(f)
    }
  
  def stringOptionFromChild(node: Elem, label: String): AlmValidationSBD[Option[String]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => Success(s)))

  def doubleOptionFromChild(node: Elem, label: String): AlmValidationSBD[Option[Double]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseDoubleAlm(s, label)))

  def intOptionFromChild(node: Elem, label: String): AlmValidationSBD[Option[Int]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseIntAlm(s, label)))

  def longOptionFromChild(node: Elem, label: String): AlmValidationSBD[Option[Long]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseLongAlm(s, label)))

  def mapOptionalFirstChildM[T](node: Elem, label: String, compute: Elem => AlmValidationMBD[T]): AlmValidationMBD[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t) map { r => Some(r) }
      case None => Success(None)
    }

  def mapChildren[T](node: Elem, label: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[T]] = {
    val validations: List[AlmValidationMBD[T]] =
      elems(node, label).toList map {node =>
        map(node) match {
          case Success(r) => Success(r)
          case Failure(f) => Failure(f.prefixWithPath(List(label))) 
        }
      }
    validations.sequence
  }
  
  def mapChildrenWithAttribute[T](node: Elem, label: String, attName: String, map: Elem => AlmValidationMBD[T]): AlmValidationMBD[List[(Option[String], T)]] = {
    val validations: List[AlmValidationMBD[(Option[String], T)]] =
      elems(node, label).toList map {node =>
        val attValue = node.attribute(attName).headOption.map(_.text)
        map(node) match {
          case Success(r) => Success((attValue, r))
          case Failure(f) => Failure(f.prefixWithPath(List(label))) 
        }}
    validations.sequence
  }
}