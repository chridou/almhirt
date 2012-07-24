package almhirt.xtractnduce

import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._
import org.joda.time.DateTime
import scalaz._
import Scalaz._

trait XTractorAtomic {
  type T
  def key: String
  def underlying: T
  def getString(): AlmValidationSBD[String]
  def getInt(): AlmValidationSBD[Int]
  def getLong(): AlmValidationSBD[Long]
  def getDouble(): AlmValidationSBD[Double]
  def getFloat(): AlmValidationSBD[Float]
  def getBoolean(): AlmValidationSBD[Boolean]
  def getDecimal(): AlmValidationSBD[BigDecimal]
  def getDateTime(): AlmValidationSBD[DateTime]
  def getBytes(): AlmValidationSBD[Array[Byte]]
  def tryGetString(): AlmValidationSBD[Option[String]]
  def tryGetInt(): AlmValidationSBD[Option[Int]]
  def tryGetLong(): AlmValidationSBD[Option[Long]]
  def tryGetDouble(): AlmValidationSBD[Option[Double]]
  def tryGetBoolean(): AlmValidationSBD[Option[Boolean]]
  def tryGetFloat(): AlmValidationSBD[Option[Float]]
  def tryGetDecimal(): AlmValidationSBD[Option[BigDecimal]]
  def tryGetDateTime(): AlmValidationSBD[Option[DateTime]]
  def tryGetBytes(): AlmValidationSBD[Option[Array[Byte]]]
  def isBooleanSet(): AlmValidationSBD[Boolean]
}

trait XTractor {
  type T
  def key: String
  def underlying: T
  def tryGetString(aKey: String): AlmValidationSBD[Option[String]]
  def tryGetInt(aKey: String): AlmValidationSBD[Option[Int]]
  def tryGetLong(aKey: String): AlmValidationSBD[Option[Long]]
  def tryGetDouble(aKey: String): AlmValidationSBD[Option[Double]]
  def tryGetFloat(aKey: String): AlmValidationSBD[Option[Float]]
  def tryGetBoolean(aKey: String): AlmValidationSBD[Option[Boolean]]
  def tryGetDecimal(aKey: String): AlmValidationSBD[Option[BigDecimal]]
  def tryGetDateTime(aKey: String): AlmValidationSBD[Option[DateTime]]
  def tryGetAsString(aKey: String): AlmValidationSBD[Option[String]]
  def tryGetBytes(aKey: String): AlmValidationSBD[Option[Array[Byte]]]

  def getString(aKey: String): AlmValidationSBD[String] = get(aKey, tryGetString)
  def getInt(aKey: String): AlmValidationSBD[Int] = get(aKey, tryGetInt)
  def getLong(aKey: String): AlmValidationSBD[Long] = get(aKey, tryGetLong)
  def getDouble(aKey: String): AlmValidationSBD[Double] = get(aKey, tryGetDouble)
  def getFloat(aKey: String): AlmValidationSBD[Float] = get(aKey, tryGetFloat)
  def getBoolean(aKey: String): AlmValidationSBD[Boolean] = get(aKey, tryGetBoolean)
  def getDecimal(aKey: String): AlmValidationSBD[BigDecimal] = get(aKey, tryGetDecimal)
  def getDateTime(aKey: String): AlmValidationSBD[DateTime] = get(aKey, tryGetDateTime)
  def getBytes(aKey: String): AlmValidationSBD[Array[Byte]] = get(aKey, tryGetBytes)
  def getAsString(aKey: String): AlmValidationSBD[String] = get(aKey, tryGetAsString)

  def isBooleanSetTrue(aKey: String): AlmValidationSBD[Boolean]

  def tryGetTypeInfo(): AlmValidationSBD[Option[String]]
  def getTypeInfo(): AlmValidationSBD[String] = 
    tryGetTypeInfo() match {
      case Success(Some(ti)) => Success(ti)
      case Success(None) => Failure(SingleBadDataProblem("No type Info!", key = "typeInfo"))
      case Failure(f) => Failure(f)
    }
  
  def getXTractors(aKey: String): AlmValidationMBD[List[XTractor]]
  def tryGetXTractor(aKey: String): AlmValidationSBD[Option[XTractor]]
  def getXTractor(aKey: String): AlmValidationSBD[XTractor] =
    tryGetXTractor(aKey) match {
      case Success(opt) =>
        opt
	      .map(Success(_))
	      .getOrElse(Failure(SingleBadDataProblem("Value not found: %s".format(aKey), key = aKey)))
      case Failure(f) => f.fail[XTractor]
    }
    
  def tryMapXTractor[U](aKey: String, mapXtractor: XTractor => AlmValidationMBD[U]): AlmValidationMBD[Option[U]] =
    tryGetXTractor(aKey) match {
      case Success(opt) =>
        opt match {
          case Some(xtractor) => 
            mapXtractor(xtractor) match {
              case Success(s) => Success(Some(s))
              case Failure(f) => Failure(f.prefixWithPath(List(key)))
            }
          case None => Success(None)
        }
      case Failure(f) => f.toMBD.fail[Option[U]]
    }
  
  def mapXTractor[U](aKey: String, mapXtractor: XTractor => AlmValidationMBD[U]): AlmValidationMBD[U] =
    tryMapXTractor(aKey, mapXtractor) match {
      case Success(opt) =>
        opt
          .map {Success(_)} 
          .getOrElse (Failure(SingleBadDataProblem("Value not found: %s".format(key), key = aKey).toMBD))
      case Failure(f) => (f.prefixWithPath(List(key))).fail[U]
    }
  
  def tryFlatMapXTractor[U](aKey: String, mapXtractor: XTractor => AlmValidationMBD[Option[U]]): AlmValidationMBD[Option[U]] =
    tryGetXTractor(aKey) match {
      case Success(opt) =>
        opt match {
          case Some(xtractor) => 
            mapXtractor(xtractor) match {
              case Success(s) => Success(s)
              case Failure(f) => Failure(f.prefixWithPath(List(key)))
            }
          case None => Success(None)
        }
      case Failure(f) => f.toMBD.fail[Option[U]]
    }

  def mapXTractorsToList[U](aKey: String, mapXtractor: XTractor => AlmValidationMBD[U]): AlmValidationMBD[List[U]] =
    getXTractors(aKey) match {
      case Success(seq) => seq.map(mapXtractor).sequence
      case Failure(f) => f.fail[List[U]]
  }
  
  def tryGetAtomic(aKey: String): AlmValidationSBD[Option[XTractorAtomic]]

  def getAtomic(aKey: String): AlmValidationSBD[XTractorAtomic] = 
    tryGetAtomic(aKey) match {
      case Success(Some(v)) => v.successSBD 
      case Success(None) => Failure(SingleBadDataProblem("Value not found: %s".format(key), key = aKey)) 
      case Failure(f) => Failure(f) 
    }
  
  def getAtomics(aKey: String): AlmValidationMBD[List[XTractorAtomic]]
  
  def getAtomicsEvaluated[T](aKey: String, eval: XTractorAtomic => AlmValidationSBD[T]): AlmValidationMBD[List[T]] = {
    for {
      atomicXTractors <- getAtomics(aKey)
      results <- atomicXTractors.map {eval(_).toMBD} sequence
    } yield results
  }
  
  private def get[U](aKey: String, f: String => AlmValidationSBD[Option[U]]): AlmValidationSBD[U] = 
    f(aKey) match {
      case Success(opt) => 
        opt match {
          case Some(v) => v.successSBD
          case None => Failure(SingleBadDataProblem("Value not found: %s".format(key), key = aKey))
        }
      case Failure(f) => Failure(f)
  }
}