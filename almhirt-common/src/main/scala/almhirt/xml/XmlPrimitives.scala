package almhirt.xml

import scala.xml
import scala.xml.Node
import scalaz.syntax.validation._
import scalaz.{Validation, Success, Failure}
import almhirt.validation.Problem._
import almhirt.validation.AlmValidation
import almhirt.validation.AlmValidation._

object XmlPrimitives extends XmlPrimitivesImplicits {
  def intFromXmlNode(node: Node): Validation[BadDataProblem,Int] = {
    for{
      ne <- failIfEmptyOrWhitespace(node.text, node.label)
      res <- parseIntAlm(ne, node.label)
    } yield res
  }
    
  def longFromXmlNode(node: Node): Validation[BadDataProblem,Long] = {
    for{
      ne <- failIfEmptyOrWhitespace(node.text, node.label)
      res <- parseLongAlm(ne, node.label)
    } yield res
  }
  
  def doubleFromXmlNode(node: Node): Validation[BadDataProblem,Double] = {
    for{
      ne <- failIfEmptyOrWhitespace(node.text, node.label)
      res <- parseDoubleAlm(ne, node.label)
    } yield res
  }
  
  def optionalIntXmlNode(node: Node): Validation[BadDataProblem,Option[Int]] = {
    if(node.text.trim.isEmpty) 
      None.success[BadDataProblem]
    else 
      intFromXmlNode(node) match {
        case Success(v) => Some(v).success[BadDataProblem]
        case Failure(f) => f.fail[Option[Int]]
    }
  }
    
  def optionalLongXmlNode(node: Node): Validation[BadDataProblem,Option[Long]] = {
    if(node.text.trim.isEmpty) 
      None.success[BadDataProblem] 
    else 
      longFromXmlNode(node) match {
        case Success(v) => Some(v).success[BadDataProblem]
        case Failure(f) => f.fail[Option[Long]]
    }
  }
  
  def optionalDoubleXmlNode(node: Node): Validation[BadDataProblem,Option[Double]] = {
    if(node.text.trim.isEmpty) 
      None.success[BadDataProblem] 
    else 
      doubleFromXmlNode(node) match {
        case Success(v) => Some(v).success[BadDataProblem]
        case Failure(f) => f.fail[Option[Double]]
    }
  }
  
  def firstChildNodeMandatory(node: Node, label: String): Validation[BadDataProblem,Node] = {
    (node \ label).toList match {
      case Nil => BadDataProblem("Element '%s' not found.".format(label), node.label).fail[Node]
      case l :: ls => l.success[BadDataProblem]
    }
  }
}