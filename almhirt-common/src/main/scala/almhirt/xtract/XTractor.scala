package almhirt.xtract

import scalaz._
import Scalaz._
import org.joda.time.DateTime
import almhirt.validation._
import AlmValidation._
import almhirt.validation.Problem._

trait XTractorAtomic {
  type T
  def key: String
  def underlying: T
  def getString(): AlmValidationSingleBadData[String]
  def getInt(): AlmValidationSingleBadData[Int]
  def getLong(): AlmValidationSingleBadData[Long]
  def getDouble(): AlmValidationSingleBadData[Double]
  def getFloat(): AlmValidationSingleBadData[Float]
  def getDecimal(): AlmValidationSingleBadData[BigDecimal]
  def getDateTime(): AlmValidationSingleBadData[DateTime]
  def tryGetString(): AlmValidationSingleBadData[Option[String]]
  def tryGetInt(): AlmValidationSingleBadData[Option[Int]]
  def tryGetLong(): AlmValidationSingleBadData[Option[Long]]
  def tryGetDouble(): AlmValidationSingleBadData[Option[Double]]
  def tryGetFloat(): AlmValidationSingleBadData[Option[Float]]
  def tryGetDecimal(): AlmValidationSingleBadData[Option[BigDecimal]]
  def tryGetDateTime(): AlmValidationSingleBadData[Option[DateTime]]
  def isBooleanSet(): AlmValidationSingleBadData[Boolean]
}

trait XTractor {
  type T
  def key: String
  def underlying: T
  def tryGetString(key: String): AlmValidationSingleBadData[Option[String]]
  def tryGetInt(key: String): AlmValidationSingleBadData[Option[Int]]
  def tryGetLong(key: String): AlmValidationSingleBadData[Option[Long]]
  def tryGetDouble(key: String): AlmValidationSingleBadData[Option[Double]]
  def tryGetFloat(key: String): AlmValidationSingleBadData[Option[Float]]
  def tryGetDecimal(key: String): AlmValidationSingleBadData[Option[BigDecimal]]
  def tryGetDateTime(key: String): AlmValidationSingleBadData[Option[DateTime]]
  def tryGetAsString(key: String): AlmValidationSingleBadData[Option[String]]

  def getString(aKey: String): AlmValidationSingleBadData[String] = get(aKey, tryGetString)
  def getInt(aKey: String): AlmValidationSingleBadData[Int] = get(aKey, tryGetInt)
  def getLong(aKey: String): AlmValidationSingleBadData[Long] = get(aKey, tryGetLong)
  def getDouble(aKey: String): AlmValidationSingleBadData[Double] = get(aKey, tryGetDouble)
  def getFloat(aKey: String): AlmValidationSingleBadData[Float] = get(aKey, tryGetFloat)
  def getDecimal(aKey: String): AlmValidationSingleBadData[BigDecimal] = get(aKey, tryGetDecimal)
  def getDateTime(aKey: String): AlmValidationSingleBadData[DateTime] = get(aKey, tryGetDateTime)
  def getAsString(aKey: String): AlmValidationSingleBadData[String] = get(aKey, tryGetAsString)

  def isBooleanSetTrue(aKey: String): AlmValidationSingleBadData[Boolean]
  
  def getElements(aKey: String): AlmValidationMultipleBadData[List[XTractor]]
  def tryGetElement(aKey: String): AlmValidationSingleBadData[Option[XTractor]]
  def getElement(aKey: String): AlmValidationSingleBadData[XTractor] =
    tryGetElement(aKey) match {
      case Success(opt) =>
        opt
	      .map(Success(_))
	      .getOrElse(Failure(SingleBadDataProblem("Value not found: %s".format(aKey), key = aKey)))
      case Failure(f) => f.fail[XTractor]
    }
    
  def tryMapElem[U](aKey: String, mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[Option[U]] =
    tryGetElement(aKey) match {
      case Success(opt) =>
        opt match {
          case Some(xtractor) => 
            mapXtractor(xtractor) match {
              case Success(s) => Success(Some(s))
              case Failure(f) => Failure(f.prefixWithPath(List(key)))
            }
          case None => Success(None)
        }
      case Failure(f) => f.toMultipleBadData.fail[Option[U]]
    }
  
  def mapElem[U](aKey: String, mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[U] =
    tryMapElem(aKey, mapXtractor) match {
      case Success(opt) =>
        opt
          .map {Success(_)} 
          .getOrElse (Failure(SingleBadDataProblem("Value not found: %s".format(key), key = aKey).toMultipleBadData))
      case Failure(f) => (f.prefixWithPath(List(key))).fail[U]
    }
  
  def tryFlatMapElem[U](aKey: String, mapXtractor: XTractor => AlmValidationMultipleBadData[Option[U]]): AlmValidationMultipleBadData[Option[U]] =
    tryGetElement(aKey) match {
      case Success(opt) =>
        opt match {
          case Some(xtractor) => 
            mapXtractor(xtractor) match {
              case Success(s) => Success(s)
              case Failure(f) => Failure(f.prefixWithPath(List(key)))
            }
          case None => Success(None)
        }
      case Failure(f) => f.toMultipleBadData.fail[Option[U]]
    }

  def mapToList[U](aKey: String, mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[List[U]] =
    getElements(aKey) match {
      case Success(seq) => seq.map(mapXtractor).sequence
      case Failure(f) => f.fail[List[U]]
  }
  
  def getAtomics(aKey: String): AlmValidationMultipleBadData[List[XTractorAtomic]]
  
  def getAtomicsEvaluated[T](aKey: String, eval: XTractorAtomic => AlmValidationSingleBadData[T]): AlmValidationMultipleBadData[List[T]] = {
    for {
      atomicXTractors <- getAtomics(aKey)
      results <- atomicXTractors.map {eval(_).toMultipleBadData} sequence
    } yield results
  }
  
  private def get[U](aKey: String, f: String => AlmValidationSingleBadData[Option[U]]): AlmValidationSingleBadData[U] = 
    f(aKey) match {
      case Success(opt) => 
        opt match {
          case Some(v) => v.successSingleBadData
          case None => Failure(SingleBadDataProblem("Value not found: %s".format(key), key = aKey))
        }
      case Failure(f) => Failure(f)
  }
}