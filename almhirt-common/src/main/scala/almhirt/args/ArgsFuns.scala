package almhirt.args

import scala.reflect.ClassTag
import scalaz.Validation.FlatMap._
import almhirt.common._

trait ArgsFuns {
  import scalaz.syntax.validation._
  import almhirt.almvalidation.kit._
  
  def getValue[T](key: String, theMap: Map[String, Any])(implicit tag: ClassTag[T]): AlmValidation[T] =
    (theMap >! key).flatMap(_.castTo[T])

  def getFromPathItems(path: scalaz.NonEmptyList[String], theMap: Map[String, Any]): AlmValidation[Any] = {
    def diveDeeper(rest: List[String], currLevel: Map[String, Any]): AlmValidation[Any] =
      rest match {
        case h :: Nil ⇒
          (currLevel >! h)
        case _ ⇒
          getValue[Map[String, Any]](rest.head, currLevel).flatMap(subArgs ⇒
            diveDeeper(rest.tail, subArgs))
      }
    diveDeeper(path.list, theMap)
  }

  def getFromPath(path: String, sep: Char, theMap: Map[String, Any]): AlmValidation[Any] =
    path.split(sep).toList match {
      case Nil ⇒ ArgumentProblem(s"""The path "$path" with separator "$sep" is not valid.""").failure
      case p ⇒ getFromPathItems(scalaz.NonEmptyList(p.head, p.tail: _*), theMap)
    }

  def getFromPropertyPath(path: String, theMap: Map[String, Any]): AlmValidation[Any] =
    getFromPath(path, '.', theMap)

  def getValueFromPropertyPath[T](path: String, theMap: Map[String, Any])(implicit tag: ClassTag[T]): AlmValidation[T] =
    getFromPropertyPath(path, theMap).flatMap(_.castTo[T])
    
  def isBooleanTrue(ident: String, theMap: Map[String, Any]): Boolean =
    theMap.get(ident) match {
    case Some(x) ⇒ 
      x.castTo[Boolean].explicitlyTrue
    case None ⇒ 
      false
  }

  def isBooleanFalse(ident: String, theMap: Map[String, Any]): Boolean =
    theMap.get(ident) match {
    case Some(x) ⇒ 
      x.castTo[Boolean].explicitlyFalse
    case None ⇒ 
      false
  }
  
  def isBooleanNotTrue(ident: String, theMap: Map[String, Any]): Boolean =
    theMap.get(ident) match {
    case Some(x) ⇒ 
      !(x.castTo[Boolean].explicitlyTrue)
    case None ⇒ 
      true
  }
  
def isBooleanNotFalse(ident: String, theMap: Map[String, Any]): Boolean =
    theMap.get(ident) match {
    case Some(x) ⇒ 
      !(x.castTo[Boolean].explicitlyFalse)
    case None ⇒ 
      true
  }  
  
  
}