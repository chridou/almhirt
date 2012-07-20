package almhirt.xml

import scalaz._
import Scalaz._
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
  
  def xmlFromString(xmlString: String, key: String = "XML"): AlmValidationSingleBadData[Elem] = {
    try {
      XML.loadString(xmlString).successSingleBadData
    } catch {
      case err => SingleBadDataProblem("Could not parse xml: %s".format(err.getMessage), key = key, exception = Some(err)).fail[Elem]
    }
  }
  
  def intFromXmlNode(node: Elem): AlmValidationSingleBadData[Int] = {
    for{
      ne <- notEmptyOrWhitespace(node.text, node.label)
      res <- parseIntAlm(ne, node.label)
    } yield res
  }
    
  def longFromXmlNode(node: Elem): AlmValidationSingleBadData[Long] = {
    for{
      ne <- notEmptyOrWhitespace(node.text, node.label)
      res <- parseLongAlm(ne, node.label)
    } yield res
  }
  
  def doubleFromXmlNode(node: Elem): AlmValidationSingleBadData[Double] = {
    for{
      ne <- notEmptyOrWhitespace(node.text, node.label)
      res <- parseDoubleAlm(ne, node.label)
    } yield res
  }
  
  def optionalIntXmlNode(node: Elem): AlmValidationSingleBadData[Option[Int]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem]
    else 
      intFromXmlNode(node) match {
        case Success(v) => Some(v).successSingleBadData
        case Failure(f) => f.fail[Option[Int]]
    }
  }
    
  def optionalLongXmlNode(node: Elem): AlmValidationSingleBadData[Option[Long]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      longFromXmlNode(node) match {
        case Success(v) => Some(v).successSingleBadData
        case Failure(f) => f.fail[Option[Long]]
    }
  }
  
  def optionalDoubleXmlNode(node: Elem): AlmValidationSingleBadData[Option[Double]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      doubleFromXmlNode(node) match {
        case Success(v) => Some(v).successSingleBadData
        case Failure(f) => f.fail[Option[Double]]
    }
  }
  
  def isBooleanSetTrue(elem: Elem): AlmValidationSingleBadData[Boolean] = {
    elem.text.trim.toLowerCase match {
      case "" => false.successSingleBadData
      case "0" => false.successSingleBadData
      case "f" => false.successSingleBadData
      case "false" => false.successSingleBadData
      case "no" => false.successSingleBadData
      case "1" => true.successSingleBadData
      case "true" => true.successSingleBadData
      case "t" => true.successSingleBadData
      case "yes" => true.successSingleBadData
      case x => SingleBadDataProblem("Could not parse value to Boolean: %s".format(x), key = elem.label).fail[Boolean]
    }
  }
  
  def firstChildNodeMandatory(node: Elem, label: String): AlmValidationSingleBadData[Elem] = {
    elems(node, label).toList match {
      case Nil => SingleBadDataProblem("Element '%s' not found.".format(label), node.label).fail[Elem]
      case l :: ls => l.successSingleBadData
    }
  }
  
  def mapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidationSingleBadData[T]): AlmValidationSingleBadData[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t) map { r => Some(r) }
      case None => Success(None)
    }

  def flatMapOptionalFirstChild[T](node: Elem, label: String, compute: Elem => AlmValidationSingleBadData[Option[T]]): AlmValidationSingleBadData[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t)
      case None => Success(None)
    }

  def stringFromChild(node: Elem, label: String): AlmValidationSingleBadData[String] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => notEmptyOrWhitespace(node.text, label)}

  def doubleFromChild(node: Elem, label: String): AlmValidationSingleBadData[Double] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => parseDoubleAlm(node.text, label)}

  def intFromChild(node: Elem, label: String): AlmValidationSingleBadData[Int] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => parseIntAlm(node.text, label)}

  def longFromChild(node: Elem, label: String): AlmValidationSingleBadData[Long] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => parseLongAlm(node.text, label)}
  
  private def emptyStringIsNone[T](str: String, compute: String => AlmValidationSingleBadData[T]): AlmValidationSingleBadData[Option[T]] =
    if(str.trim().isEmpty)
      Success(None)
    else
      compute(str) match {
        case Success(r) => Success(Some(r))
        case Failure(f) => Failure(f)
    }
  
  def stringOptionFromChild(node: Elem, label: String): AlmValidationSingleBadData[Option[String]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => Success(s)))

  def doubleOptionFromChild(node: Elem, label: String): AlmValidationSingleBadData[Option[Double]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseDoubleAlm(s, label)))

  def intOptionFromChild(node: Elem, label: String): AlmValidationSingleBadData[Option[Int]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseIntAlm(s, label)))

  def longOptionFromChild(node: Elem, label: String): AlmValidationSingleBadData[Option[Long]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseLongAlm(s, label)))

  def mapOptionalFirstChildM[T](node: Elem, label: String, compute: Elem => AlmValidationMultipleBadData[T]): AlmValidationMultipleBadData[Option[T]] =
    elems(node, label).headOption match {
      case Some(t) => compute(t) map { r => Some(r) }
      case None => Success(None)
    }

  def mapChildren[T](node: Elem, label: String, map: Elem => AlmValidationMultipleBadData[T]): AlmValidationMultipleBadData[List[T]] = {
    val validations: List[AlmValidationMultipleBadData[T]] =
      elems(node, label).toList map {node =>
        map(node) match {
          case Success(r) => Success(r)
          case Failure(f) => Failure(f.prefixWithPath(List(label))) 
        }
      }
    validations.sequence
  }
  
  def mapChildrenWithAttribute[T](node: Elem, label: String, attName: String, map: Elem => AlmValidationMultipleBadData[T]): AlmValidationMultipleBadData[List[(Option[String], T)]] = {
    val validations: List[AlmValidationMultipleBadData[(Option[String], T)]] =
      elems(node, label).toList map {node =>
        val attValue = node.attribute(attName).headOption.map(_.text)
        map(node) match {
          case Success(r) => Success((attValue, r))
          case Failure(f) => Failure(f.prefixWithPath(List(label))) 
        }}
    validations.sequence
  }
}