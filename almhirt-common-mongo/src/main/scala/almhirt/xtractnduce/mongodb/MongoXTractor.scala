package almhirt.xtractnduce.mongodb

import org.joda.time.DateTime
import scalaz._
import Scalaz._
import almhirt.validation._
import almhirt.validation.Problem._
import almhirt.validation.AlmValidation._
import almhirt.xtractnduce._

import com.mongodb.casbah.Imports._

class MongoXTractor(val underlying: MongoDBObject, val key: String)(implicit mapKey: MongoKeyMapper) extends XTractor {
  type T = MongoDBObject
  
  def tryGetString(aKey: String) = 
    try {
       underlying.getAs[String](mapKey(aKey)) match {
        case Some(str) => if(str.trim.isEmpty) None.successSBD  else Some(str).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[String]]  
    }
  
  def tryGetInt(aKey: String) = 
    try {
      underlying.getAs[Int](mapKey(aKey)).map(identity) match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[Int]]  
    }
  
  def tryGetLong(aKey: String) =
    try {
      underlying.getAs[Long](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[Long]]  
    }
  
  def tryGetDouble(aKey: String) = 
    try {
      underlying.getAs[Double](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[Double]]  
    }

  def tryGetFloat(aKey: String) = 
    try {
      underlying.getAs[Float](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[Float]]  
    }
    
  def tryGetBoolean(aKey: String) = 
    try {
      underlying.getAs[Boolean](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[Boolean]]  
    }
    
  def tryGetDecimal(aKey: String) = 
    try {
      underlying.getAs[BigDecimal](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[BigDecimal]]  
    }
    
  def tryGetDateTime(aKey: String) = 
    try {
      underlying.getAs[DateTime](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[DateTime]]  
    }

  def tryGetBytes(aKey: String) = 
    try {
      underlying.getAs[Array[Byte]](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[Array[Byte]]]  
    }
    
  def tryGetAsString(aKey: String) = 
    SingleBadDataProblem("not supported", key = aKey).fail[Option[String]]

  def isBooleanSetTrue(aKey: String) =
   	tryGetBoolean(aKey).map{_.getOrElse(false)}
  
  def getElements(aKey: String): AlmValidationMBD[List[XTractor]] =
    try {
      val theKey = mapKey(aKey)
      underlying.getAs[MongoDBList](theKey).map{identity} match {
        case Some(obj) => 
          obj
            .toList
            .map(x => new MongoXTractor(x.asInstanceOf[BasicDBObject], aKey))
            .successMBD  
        case None => Nil.successMBD  
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).toMultipleBadData.fail[List[XTractor]]
    }
  
  def tryGetElement(aKey: String): AlmValidationSBD[Option[XTractor]] =
    try {
      val theKey = mapKey(aKey)
      underlying.getAs[BasicDBObject](theKey).map{identity} match {
        case Some(obj) => 
          val mongoExtr = new MongoXTractor(obj, aKey)
          Some(mongoExtr).successSBD
        case None => 
          Success(None)
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn)).fail[Option[XTractor]]
    }

  def tryGetAtomic(aKey: String): AlmValidationSBD[Option[XTractorAtomic]] =
    try {
      val theKey = mapKey(aKey)
      underlying.get(theKey) match {
        case Some(null) => 
          SingleBadDataProblem("Null is not allowed!", key = aKey).fail[Option[XTractorAtomic]]
        case Some(v) => 
          new XTractorAtomicAny(v, aKey).successSBD.map(Some(_))
        case None => 
          None.successSBD
      }
    } catch {
      case exn => 
        SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn))
        .fail[Option[XTractorAtomic]]
    }
    
    
  def getAtomics(aKey: String): AlmValidationMBD[List[XTractorAtomic]] =
    try {
      val theKey = mapKey(aKey)
      underlying.getAs[MongoDBList](theKey).map{identity} match {
        case Some(dbList) => 
          dbList.toList
            .zipWithIndex
            .map{ case(o, i) => 
              o match {
                case null => 
                  Failure(
                    SingleBadDataProblem("Null is not allowed!", key = aKey)
                      .toMultipleBadData)
                case _ : BasicDBObject => 
                  Failure(
                    SingleBadDataProblem("Not allowed as an atomic: MongoDBObject", key = aKey)
                      .toMultipleBadData)
                case _ : BasicDBList => 
                  Failure(
                    SingleBadDataProblem("Not allowed as an atomic: BasicDBList", key = aKey)
                      .toMultipleBadData)
                case x => (new XTractorAtomicAny(x, "[%d]".format(i))).successMBD
              } }
            .toList
            .sequence
        case None => 
          Success(Nil)
      }
    } catch {
      case exn => 
        SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = aKey, exception= Some(exn))
          .toMultipleBadData
          .fail[List[XTractorAtomicAny]]
    }
}

object MongoXTractor {
  implicit def mongoDBObject2MongoXTractorW(elem: MongoDBObject): MongoDBObjectMongoXTractorW = new MongoDBObjectMongoXTractorW(elem)
  final class MongoDBObjectMongoXTractorW(mongoObj: MongoDBObject) {
    def xtractor(aKey: String): MongoXTractor = new MongoXTractor(mongoObj, aKey)
  }
}
