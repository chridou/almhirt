package almhirt.xtract.mongodb

import scalaz.{Success}
import scalaz.syntax.validation._
import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._
import almhirt.xtract.XTractor
import com.mongodb.casbah.Imports._

class MongoXTractor(mongoObj: MongoDBObject, val key: String) extends XTractor {
  type T = MongoDBObject
  def underlying() = mongoObj
  
  def tryGetString(aKey: String) = 
    mongoObj.getAs[String](aKey) match {
      case Some(str) => if(str.trim.isEmpty) None.successSingleBadData  else Some(str).successSingleBadData
      case None => None.successSingleBadData
    }
  
  def tryGetInt(aKey: String) = 
    mongoObj.getAs[Int](aKey) match {
      case Some(v) => Some(v).successSingleBadData
      case None => None.successSingleBadData
    }
  
  def tryGetLong(aKey: String) = 
    mongoObj.getAs[Long](aKey) match {
      case Some(v) => Some(v).successSingleBadData
      case None => None.successSingleBadData
    }
  
  def tryGetDouble(aKey: String) = 
    mongoObj.getAs[Double](aKey) match {
      case Some(v) => Some(v).successSingleBadData
      case None => None.successSingleBadData
    }
  
  def tryGetAsString(aKey: String) = 
    SingleBadDataProblem("not supported", key = aKey).fail[Option[String]]
  

  def getElements(aKey: String): AlmValidationMultipleBadData[List[XTractor]] =
    mongoObj.get(aKey) match {
      case Some(obj) => 
        obj.asInstanceOf[MongoDBList]
          .map(x => new MongoXTractor(x.asInstanceOf[MongoDBObject], aKey))
          .toList
          .successMultipleBadData  
      case None => Nil.successMultipleBadData  
    }
  
  def tryGetElement(aKey: String): AlmValidationSingleBadData[Option[XTractor]] =
    mongoObj.get(aKey) match {
      case Some(obj) => Some(new MongoXTractor(obj.asInstanceOf[MongoDBObject], aKey)).successSingleBadData
      case None => Success(None)
    }
}

object MongoXTractor {
  implicit def mongoDBObject2MongoXTractorW(elem: MongoDBObject): MongoDBObjectMongoXTractorW = new MongoDBObjectMongoXTractorW(elem)
  final class MongoDBObjectMongoXTractorW(mongoObj: MongoDBObject) {
    def xtractor(aKey: String): MongoXTractor = new MongoXTractor(mongoObj, aKey)
  }
}