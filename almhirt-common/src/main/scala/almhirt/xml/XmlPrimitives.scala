package almhirt.xml

import scala.xml._
import scalaz.syntax.validation._
import scalaz.{Validation, Success, Failure}
import almhirt.validation.Problem._
import almhirt.validation.AlmValidation
import almhirt.validation.AlmValidation._

object XmlPrimitives extends XmlPrimitivesImplicits {
  def xmlFromString(xmlString: String, key: String = "XML"): Validation[SingleBadDataProblem,Node] = {
    try {
      XML.loadString(xmlString).success[SingleBadDataProblem]
    } catch {
      case err => SingleBadDataProblem("Could not parse xml: %s".format(err.getMessage), key = key, exception = Some(err)).fail[Node]
    }
  }
  
  def intFromXmlNode(node: Node): Validation[SingleBadDataProblem,Int] = {
    for{
      ne <- failIfEmptyOrWhitespace(node.text, node.label)
      res <- parseIntAlm(ne, node.label)
    } yield res
  }
    
  def longFromXmlNode(node: Node): Validation[SingleBadDataProblem,Long] = {
    for{
      ne <- failIfEmptyOrWhitespace(node.text, node.label)
      res <- parseLongAlm(ne, node.label)
    } yield res
  }
  
  def doubleFromXmlNode(node: Node): Validation[SingleBadDataProblem,Double] = {
    for{
      ne <- failIfEmptyOrWhitespace(node.text, node.label)
      res <- parseDoubleAlm(ne, node.label)
    } yield res
  }
  
  def optionalIntXmlNode(node: Node): Validation[SingleBadDataProblem,Option[Int]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem]
    else 
      intFromXmlNode(node) match {
        case Success(v) => Some(v).success[SingleBadDataProblem]
        case Failure(f) => f.fail[Option[Int]]
    }
  }
    
  def optionalLongXmlNode(node: Node): Validation[SingleBadDataProblem,Option[Long]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      longFromXmlNode(node) match {
        case Success(v) => Some(v).success[SingleBadDataProblem]
        case Failure(f) => f.fail[Option[Long]]
    }
  }
  
  def optionalDoubleXmlNode(node: Node): Validation[SingleBadDataProblem,Option[Double]] = {
    if(node.text.trim.isEmpty) 
      None.success[SingleBadDataProblem] 
    else 
      doubleFromXmlNode(node) match {
        case Success(v) => Some(v).success[SingleBadDataProblem]
        case Failure(f) => f.fail[Option[Double]]
    }
  }
  
  def firstChildNodeMandatory(node: Node, label: String): Validation[SingleBadDataProblem,Node] = {
    (node \ label).toList match {
      case Nil => SingleBadDataProblem("Element '%s' not found.".format(label), node.label).fail[Node]
      case l :: ls => l.success[SingleBadDataProblem]
    }
  }
}