package almhirt.xtract.mongodb

import scalaz.{Success}
import scalaz.syntax.validation._
import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._
import almhirt.xtract.XTractor
import com.mongodb.casbah.Imports._

trait MongoXTractorKeyMapper extends Function[String, String] {
}

class MongoXTractor(val underlying: MongoDBObject, val key: String)(implicit mapKey: MongoXTractorKeyMapper) extends XTractor {
  type T = MongoDBObject
  
  def tryGetString(aKey: String) = 
    try {
       underlying.getAs[String](mapKey(aKey)) match {
        case Some(str) => if(str.trim.isEmpty) None.successSingleBadData  else Some(str).successSingleBadData
        case None => None.successSingleBadData
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[String]]  
    }
  
  def tryGetInt(aKey: String) = 
    try {
      underlying.getAs[Int](mapKey(aKey)).map(identity) match {
        case Some(v) => Some(v).successSingleBadData
        case None => None.successSingleBadData
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[Int]]  
    }
  
  def tryGetLong(aKey: String) =
    try {
      underlying.getAs[Long](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSingleBadData
        case None => None.successSingleBadData
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[Long]]  
    }
  
  def tryGetDouble(aKey: String) = 
    try {
      underlying.getAs[Double](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSingleBadData
        case None => None.successSingleBadData
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[Double]]  
    }
  
  def tryGetAsString(aKey: String) = 
    SingleBadDataProblem("not supported", key = aKey).fail[Option[String]]

  def getElements(aKey: String): AlmValidationMultipleBadData[List[XTractor]] =
    underlying.get(mapKey(aKey)) match {
      case Some(obj) => 
        obj.asInstanceOf[MongoDBList]
          .map(x => new MongoXTractor(x.asInstanceOf[MongoDBObject], aKey))
          .toList
          .successMultipleBadData  
      case None => Nil.successMultipleBadData  
    }
  
  def tryGetElement(aKey: String): AlmValidationSingleBadData[Option[XTractor]] =
    try {
      val theKey = mapKey(aKey)
      underlying.get(theKey) match {
        case Some(obj) => Some(new MongoXTractor(obj.asInstanceOf[MongoDBObject], aKey)).successSingleBadData
        case None => Success(None)
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[XTractor]]
    }
}

object MongoXTractor {
  def createKeyMapper(map: String => String): MongoXTractorKeyMapper =
    new MongoXTractorKeyMapper{ def apply(key: String) = map(key) }
  def createKeyMapper(idKey: String): MongoXTractorKeyMapper =
    createKeyMapper(key => if(key == idKey) "_id" else key)
  
  implicit val defaultMongoKeyMapper = createKeyMapper("id")
  implicit def mongoDBObject2MongoXTractorW(elem: MongoDBObject): MongoDBObjectMongoXTractorW = new MongoDBObjectMongoXTractorW(elem)
  final class MongoDBObjectMongoXTractorW(mongoObj: MongoDBObject) {
    def xtractor(aKey: String): MongoXTractor = new MongoXTractor(mongoObj, aKey)
  }
}