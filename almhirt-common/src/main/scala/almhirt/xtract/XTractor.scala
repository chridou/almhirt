package almhirt.xtract

import scalaz._
import Scalaz._
import almhirt.validation._
import AlmValidation._
import almhirt.validation.Problem._

trait XTractorAtomic {
  type T
  def underlying: T
  def extractString(): AlmValidationSingleBadData[String]
  def extractInt(): AlmValidationSingleBadData[Int]
  def extractLong(): AlmValidationSingleBadData[Long]
  def extractDouble(): AlmValidationSingleBadData[Double]
  def extractOptionalInt(): AlmValidationSingleBadData[Option[Int]]
  def extractOptionalLong(): AlmValidationSingleBadData[Option[Long]]
  def extractOptionalDouble(): AlmValidationSingleBadData[Option[Double]]
}

trait XTractor {
  type T
  def key: String
  def underlying: T
  def tryGetString(key: String): AlmValidationSingleBadData[Option[String]]
  def tryGetInt(key: String): AlmValidationSingleBadData[Option[Int]]
  def tryGetLong(key: String): AlmValidationSingleBadData[Option[Long]]
  def tryGetDouble(key: String): AlmValidationSingleBadData[Option[Double]]
  def tryGetAsString(key: String): AlmValidationSingleBadData[Option[String]]

  def getString(aKey: String): AlmValidationSingleBadData[String] = 
    tryGetString(aKey) match {
      case Success(opt) => 
        opt match {
          case Some(v) => v.successSingleBadData
          case None => Failure(SingleBadDataProblem("Value not found: %s".format(key), key = aKey))
        }
      case Failure(f) => Failure(f)
  }
  def getInt(aKey: String): AlmValidationSingleBadData[Int] = 
    tryGetInt(aKey) match {
      case Success(opt) => 
        opt match {
          case Some(v) => Success(v)
          case None => Failure(SingleBadDataProblem("Value not found: %s".format(key), key = aKey))
        }
      case Failure(f) => Failure(f)
  }
  def getLong(aKey: String): AlmValidationSingleBadData[Long] = 
    tryGetLong(aKey) match {
      case Success(opt) => 
        opt match {
          case Some(v) => v.successSingleBadData
          case None => Failure(SingleBadDataProblem("Value not found: %s".format(aKey), key = aKey))
        }
      case Failure(f) => Failure(f)
  }
  
  def getDouble(aKey: String): AlmValidationSingleBadData[Double] = 
    tryGetDouble(aKey) match {
      case Success(opt) => 
        opt match {
          case Some(v) => v.successSingleBadData
          case None => Failure(SingleBadDataProblem("Value not found: %s".format(key), key = aKey))
        }
      case Failure(f) => Failure(f)
  }
  
  def getAsString(aKey: String): AlmValidationSingleBadData[String] = 
    tryGetAsString(aKey) match {
      case Success(opt) => 
        opt match {
          case Some(v) => v.successSingleBadData
          case None => Failure(SingleBadDataProblem("Value not found: %s".format(key), key = aKey))
        }
      case Failure(f) => Failure(f)
  }
  
  def getElements(aKey: String): AlmValidationMultipleBadData[List[XTractor]]
  def tryGetElement(aKey: String): AlmValidationSingleBadData[Option[XTractor]]
  def getElement(aKey: String): AlmValidationSingleBadData[XTractor] =
    tryGetElement(key) match {
      case Success(opt) =>
        opt
	      .map(Success(_))
	      .getOrElse(Failure(SingleBadDataProblem("Value not found: %s".format(aKey), key = aKey)))
      case Failure(f) => f.fail[XTractor]
    }
    
  def tryMapElem[U](aKey: String, mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[Option[U]] =
    tryGetElement(key) match {
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
    tryGetElement(key) match {
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
}