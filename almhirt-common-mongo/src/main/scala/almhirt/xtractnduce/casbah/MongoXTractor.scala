package almhirt.xtractnduce.casbah

import java.util.UUID
import scalaz._
import Scalaz._
import org.joda.time.DateTime
import almhirt.validation._
import almhirt.validation.syntax._
import almhirt.validation.instances._
import almhirt.xtractnduce._
import com.mongodb.casbah.Imports._

class MongoXTractor(val underlying: MongoDBObject, val key: String, val parent: Option[XTractor] = None)(implicit mapKey: MongoKeyMapper) extends XTractor {
  type T = MongoDBObject
  def keys() = 
    underlying
      .keys
      .map(mapKey.inverse)
      .toSeq
  
  def tryGetString(aKey: String) = 
    try {
       underlying.getAs[String](mapKey(aKey)) match {
        case Some(str) => if(str.trim.isEmpty) None.successSBD  else Some(str).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => 
        SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn)))
          .failure[Option[String]]  
    }
  
  def tryGetInt(aKey: String) = 
    try {
      underlying.getAs[Int](mapKey(aKey)).map(identity) match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[Int]]  
    }
  
  def tryGetLong(aKey: String) =
    try {
      underlying.getAs[Long](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[Long]]  
    }
  
  def tryGetDouble(aKey: String) = 
    try {
      underlying.getAs[Double](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[Double]]  
    }

  def tryGetFloat(aKey: String) = 
    try {
      underlying.getAs[Float](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[Float]]  
    }
    
  def tryGetBoolean(aKey: String) = 
    try {
      underlying.getAs[Boolean](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[Boolean]]  
    }
    
  def tryGetDecimal(aKey: String) = 
    try {
      underlying.getAs[BigDecimal](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[BigDecimal]]  
    }
    
  def tryGetDateTime(aKey: String) = 
    try {
      underlying.getAs[DateTime](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[DateTime]]  
    }

  def tryGetUUID(aKey: String) = 
    try {
      underlying.getAs[UUID](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[UUID]]  
    }

  def tryGetBytes(aKey: String) = 
    try {
      underlying.getAs[Array[Byte]](mapKey(aKey)).map{identity} match {
        case Some(v) => Some(v).successSBD
        case None => None.successSBD
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[Array[Byte]]]  
    }
    
  def tryGetAsString(aKey: String) = 
    SingleBadDataProblem("not supported", key = pathAsStringWithKey(aKey)).failure[Option[String]]

  def isBooleanSetTrue(aKey: String) =
   	tryGetBoolean(aKey).map{_.getOrElse(false)}

  def tryGetTypeInfo() = tryGetString("typeInfo")
  
  def getXTractors(aKey: String): AlmValidationMBD[List[XTractor]] =
    try {
      val theKey = mapKey(aKey)
      underlying.getAs[BasicDBList](theKey).map{identity} match {
        case Some(obj) => 
          obj
            .zipWithIndex
            .toList
            .map{case (x,i) => new MongoXTractor(x.asInstanceOf[BasicDBObject], "%s[%d]".format(aKey, i), Some(this))(MongoKeyMapper.identityKeyMapper)}
            .successMBD  
        case None => Nil.successMBD  
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).toMBD.failure[List[XTractor]]
    }
  
  def tryGetXTractor(aKey: String): AlmValidationSBD[Option[XTractor]] =
    try {
      val theKey = mapKey(aKey)
      underlying.getAs[BasicDBObject](theKey).map{identity} match {
        case Some(obj) => 
          val mongoExtr = new MongoXTractor(obj, aKey, Some(this))(MongoKeyMapper.identityKeyMapper)
          Some(mongoExtr).successSBD
        case None => 
          None.success
      }
    } catch {
      case exn => SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).failure[Option[XTractor]]
    }

  def tryGetAtomic(aKey: String): AlmValidationSBD[Option[XTractorAtomic]] =
    try {
      val theKey = mapKey(aKey)
      underlying.get(theKey) match {
        case Some(null) => 
          SingleBadDataProblem("Null is not allowed!", key = pathAsStringWithKey(aKey)).failure[Option[XTractorAtomic]]
        case Some(v) => 
          new XTractorAtomicAny(v, aKey, Some(this)).successSBD.map(Some(_))
        case None => 
          None.successSBD
      }
    } catch {
      case exn => 
        SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn))).prefixWithPath(path)
        .failure[Option[XTractorAtomic]]
    }
    
    
  def getAtomics(aKey: String): AlmValidationMBD[List[XTractorAtomic]] =
    try {
      val theKey = mapKey(aKey)
      underlying.getAs[BasicDBList](theKey).map{identity} match {
        case Some(dbList) => 
          dbList.toList
            .zipWithIndex
            .map{ case(o, i) => 
              o match {
                case null => 
                  SingleBadDataProblem("Null is not allowed!", key = pathAsStringWithKey(aKey)).toMBD.failure
                case _ : BasicDBObject => 
                  SingleBadDataProblem("Not allowed as an atomic: BasicDBObject", key = pathAsStringWithKey(aKey)).toMBD.failure
                case _ : BasicDBList => 
                  SingleBadDataProblem("Not allowed as an atomic: BasicDBList", key = pathAsStringWithKey(aKey)).toMBD.failure
                case x => (new XTractorAtomicAny(x, "%s[%d]".format(aKey,i), Some(this))).successMBD
              } }
            .toList
            .sequence
        case None => 
          Nil.success
      }
    } catch {
      case exn => 
        SingleBadDataProblem("An error occured: %s".format(exn.getMessage), key = pathAsStringWithKey(aKey), cause = Some(CauseIsThrowable(exn)))
          .toMBD
          .failure[List[XTractorAtomicAny]]
    }
}

object MongoXTractor {
  def apply(mo: MongoDBObject, aKey: String)(implicit mapKey: MongoKeyMapper) =
	new MongoXTractor(mo, aKey)(mapKey)
  def apply(mo: MongoDBObject, aKey: String, idKey: String)(implicit mapKey: MongoKeyMapper) =
	new MongoXTractor(mo, aKey)(MongoKeyMapper.createKeyMapper(idKey))
  implicit def mongoDBObject2MongoXTractorW(elem: MongoDBObject): MongoDBObjectMongoXTractorW = 
    new MongoDBObjectMongoXTractorW(elem)
  final class MongoDBObjectMongoXTractorW(mongoObj: MongoDBObject) {
    def xtractor(aKey: String)(implicit mapKey: MongoKeyMapper): MongoXTractor = new MongoXTractor(mongoObj, aKey)(mapKey)
  }
}
