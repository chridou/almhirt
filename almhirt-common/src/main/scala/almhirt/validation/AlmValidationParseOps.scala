package almhirt.validation

import scalaz.{Validation, Success, Failure, ValidationNEL}
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.validation.Problem._

trait AlmValidationParseOps {
  def parseIntAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Int] =
    try {
      toParse.toInt.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Int):%s".format(toParse), key).fail[Int]
    }

  def parseLongAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Long] =
    try {
      toParse.toLong.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Long)".format(toParse), key).fail[Long]
    }
  
  def parseDoubleAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Double] =
    try {
      toParse.toDouble.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Double)".format(toParse), key).fail[Double]
    }

  def parseFloatAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Float] =
    try {
      toParse.toFloat.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Float)".format(toParse), key).fail[Float]
    }

  def parseDecimalAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[BigDecimal] =
    try {
      BigDecimal(toParse).success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid number(BigDecimal)".format(toParse), key).fail[BigDecimal]
    }

  def parseDateTimeAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[DateTime] =
    try {
      new DateTime(toParse).success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid number(DateTime)".format(toParse), key).fail[DateTime]
    }

  def parseBooleanAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Boolean] =
    try {
      toParse.toBoolean.success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid Boolean".format(toParse), key).fail[Boolean]
    }
     
  def tryParseIntAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Option[Int]] =
    emptyStringIsNone(toParse, x => parseIntAlm(x, key))
  
  def tryParseLongAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Option[Long]] =
    emptyStringIsNone(toParse, x => parseLongAlm(x, key))
 
  def tryParseDoubleAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Option[Double]] =
    emptyStringIsNone(toParse, x => parseDoubleAlm(x, key))
 
  def tryParseFloatAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Option[Float]] =
    emptyStringIsNone(toParse, x => parseFloatAlm(x, key))

  def tryParseDecimalAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Option[BigDecimal]] =
    emptyStringIsNone(toParse, x => parseDecimalAlm(x, key))

  def tryParseDateTimeAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Option[DateTime]] =
    emptyStringIsNone(toParse, x => parseDateTimeAlm(x, key))
    
  def notEmpty(toTest: String, key: String = "some value"): AlmValidationSingleBadData[String] =
    if(toTest.isEmpty) badData("must not be empty", key).fail[String] else toTest.success[SingleBadDataProblem]

  def notEmptyOrWhitespace(toTest: String, key: String = "some value"): AlmValidationSingleBadData[String] =
    if(toTest.trim.isEmpty) 
      badData("must not be empty or whitespaces", key).fail[String] 
    else 
      toTest.success[SingleBadDataProblem]
  
  private def emptyStringIsNone[T](str: String, f: String => AlmValidationSingleBadData[T]) =
    if(str.trim.isEmpty)
      Success(None)
    else
      f(str).map(Some(_))

}