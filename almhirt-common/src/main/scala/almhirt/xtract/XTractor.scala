package almhirt.xtract

import almhirt.validation._
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
  def underlying: T
  def extractString(key: String): AlmValidationSingleBadData[String]
  def extractInt(key: String): AlmValidationSingleBadData[Int]
  def extractLong(key: String): AlmValidationSingleBadData[Long]
  def extractDouble(key: String): AlmValidationSingleBadData[Double]
  def extractOptString(key: String): AlmValidationSingleBadData[Option[String]]
  def extractOptInt(key: String): AlmValidationSingleBadData[Option[Int]]
  def extractOptLong(key: String): AlmValidationSingleBadData[Option[Long]]
  def extractOptDouble(key: String): AlmValidationSingleBadData[Option[Double]]
  def extractElement(key: String): AlmValidationSingleBadData[XTractor]
  def extractOptElement(key: String): AlmValidationSingleBadData[Option[XTractor]]
  def mapOpt[U](key: String, mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[Option[U]]
  def flatMapOpt[U](key: String, mapXtractor: XTractor => AlmValidationMultipleBadData[Option[U]]): AlmValidationMultipleBadData[Option[U]]
  def mapAll[U](mapXtractor: XTractor => AlmValidationMultipleBadData[U]): AlmValidationMultipleBadData[List[U]]
}