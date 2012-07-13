package almhirt.xml

import scalaz._
import Scalaz._
import almhirt.validation.Problem._
import almhirt.validation._
import almhirt.validation.AlmValidation._

object XmlPrimitives extends XmlPrimitivesImplicits {
  type XmlNode = scala.xml.Node
  import scala.xml.XML
  
  def xmlFromString(xmlString: String, key: String = "XML"): AlmValidationSingleBadData[XmlNode] = {
    try {
      XML.loadString(xmlString).successSingleBadData
    } catch {
      case err => SingleBadDataProblem("Could not parse xml: %s".format(err.getMessage), key = key, exception = Some(err)).fail[XmlNode]
    }
  }
  
  def intFromXmlNode(node: XmlNode): AlmValidationSingleBadData[Int] = {
    for{
      ne <- failIfEmptyOrWhitespace(node.text, node.label)
      res <- parseIntAlm(ne, node.label)
    } yield res
  }
    
  def longFromXmlNode(node: XmlNode): AlmValidationSingleBadData[Long] = {
    for{
      ne <- failIfEmptyOrWhitespace(node.text, node.label)
      res <- parseLongAlm(ne, node.label)
    } yield res
  }
  
  def doubleFromXmlNode(node: XmlNode): AlmValidationSingleBadData[Double] = {
    for{
      ne <- failIfEmptyOrWhitespace(node.text, node.label)
      res <- parseDoubleAlm(ne, node.label)
    } yield res
  }
  
  def optionalIntXmlNode(node: XmlNode): AlmValidationSingleBadData[Option[Int]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem]
    else 
      intFromXmlNode(node) match {
        case Success(v) => Some(v).successSingleBadData
        case Failure(f) => f.fail[Option[Int]]
    }
  }
    
  def optionalLongXmlNode(node: XmlNode): AlmValidationSingleBadData[Option[Long]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      longFromXmlNode(node) match {
        case Success(v) => Some(v).successSingleBadData
        case Failure(f) => f.fail[Option[Long]]
    }
  }
  
  def optionalDoubleXmlNode(node: XmlNode): AlmValidationSingleBadData[Option[Double]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      doubleFromXmlNode(node) match {
        case Success(v) => Some(v).successSingleBadData
        case Failure(f) => f.fail[Option[Double]]
    }
  }
  
  def firstChildNodeMandatory(node: XmlNode, label: String): AlmValidationSingleBadData[XmlNode] = {
    (node \ label).toList match {
      case Nil => SingleBadDataProblem("Element '%s' not found.".format(label), node.label).fail[XmlNode]
      case l :: ls => l.successSingleBadData
    }
  }
  
  def mapOptionalFirstChild[T](node: XmlNode, label: String, compute: XmlNode => AlmValidationSingleBadData[T]): AlmValidationSingleBadData[Option[T]] =
    (node \ label).headOption match {
      case Some(t) => compute(t) map { r => Some(r) }
      case None => Success(None)
    }

  def flatMapOptionalFirstChild[T](node: XmlNode, label: String, compute: XmlNode => AlmValidationSingleBadData[Option[T]]): AlmValidationSingleBadData[Option[T]] =
    (node \ label).headOption match {
      case Some(t) => compute(t)
      case None => Success(None)
    }
  
  def stringFromChild(node: XmlNode, label: String): AlmValidationSingleBadData[String] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => failIfEmptyOrWhitespace(node.text, label)}

  def doubleFromChild(node: XmlNode, label: String): AlmValidationSingleBadData[Double] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => parseDoubleAlm(node.text, label)}

  def intFromChild(node: XmlNode, label: String): AlmValidationSingleBadData[Int] =
    firstChildNodeMandatory(node, label)
    .flatMap {node => parseIntAlm(node.text, label)}

  def longFromChild(node: XmlNode, label: String): AlmValidationSingleBadData[Long] =
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
  
  def stringOptionFromChild(node: XmlNode, label: String): AlmValidationSingleBadData[Option[String]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => Success(s)))

  def doubleOptionFromChild(node: XmlNode, label: String): AlmValidationSingleBadData[Option[Double]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseDoubleAlm(s, label)))

  def intOptionFromChild(node: XmlNode, label: String): AlmValidationSingleBadData[Option[Int]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseIntAlm(s, label)))

  def longOptionFromChild(node: XmlNode, label: String): AlmValidationSingleBadData[Option[Long]] =
    flatMapOptionalFirstChild(node, label, n => emptyStringIsNone(n.text, s => parseLongAlm(s, label)))

  def mapChildrenWithAttribute[T](node: XmlNode, label: String, attName: String, map: XmlNode => AlmValidationMultipleBadData[T]): AlmValidationMultipleBadData[List[(Option[String], T)]] = {
    val validations: List[AlmValidationMultipleBadData[(Option[String], T)]] =
      (node \ label).toList map {node =>
        val attValue = node.attribute(attName).headOption.map(_.text)
        map(node) match {
          case Success(r) => Success((attValue, r))
          case Failure(f) => Failure(f.prefixWithPath(List(label))) 
        }}
    validations.sequence
  }
}