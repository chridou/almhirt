package almhirt.validation

import java.util.UUID
import scalaz.{Validation, Success, Failure, ValidationNEL}
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.validation.Problem._

trait AlmValidationParseOps {
  def parseIntAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Int] =
    try {
      toParse.toInt.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Int):%s".format(toParse), key).fail[Int]
    }

  def parseLongAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Long] =
    try {
      toParse.toLong.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Long)".format(toParse), key).fail[Long]
    }
  
  def parseDoubleAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Double] =
    try {
      toParse.toDouble.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Double)".format(toParse), key).fail[Double]
    }

  def parseFloatAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Float] =
    try {
      toParse.toFloat.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Float)".format(toParse), key).fail[Float]
    }

  def parseDecimalAlm(toParse: String, key: String = "some value"): AlmValidationSBD[BigDecimal] =
    try {
      BigDecimal(toParse).success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid number(BigDecimal)".format(toParse), key).fail[BigDecimal]
    }

  def parseDateTimeAlm(toParse: String, key: String = "some value"): AlmValidationSBD[DateTime] =
    try {
      new DateTime(toParse).success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid number(DateTime)".format(toParse), key).fail[DateTime]
    }

  def parseUUIDAlm(toParse: String, key: String = "some value"): AlmValidationSBD[UUID] =
    try {
      UUID.fromString(toParse).success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid number(DateTime)".format(toParse), key).fail[UUID]
    }
     
     
  def parseBooleanAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Boolean] =
    try {
      toParse.toBoolean.success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid Boolean".format(toParse), key).fail[Boolean]
    }

  def parseBase64Alm(toParse: String, key: String = "some value"): AlmValidationSBD[Array[Byte]] =
    try {
      Success(org.apache.commons.codec.binary.Base64.decodeBase64(toParse))
     } catch {
      case err => badData("Not a Base64 encoded String".format(toParse), key).fail[Array[Byte]]
    }
     
  def tryParseIntAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Option[Int]] =
    emptyStringIsNone(toParse, x => parseIntAlm(x, key))
  
  def tryParseLongAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Option[Long]] =
    emptyStringIsNone(toParse, x => parseLongAlm(x, key))
 
  def tryParseDoubleAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Option[Double]] =
    emptyStringIsNone(toParse, x => parseDoubleAlm(x, key))
 
  def tryParseFloatAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Option[Float]] =
    emptyStringIsNone(toParse, x => parseFloatAlm(x, key))

  def tryParseDecimalAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Option[BigDecimal]] =
    emptyStringIsNone(toParse, x => parseDecimalAlm(x, key))

  def tryParseDateTimeAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Option[DateTime]] =
    emptyStringIsNone(toParse, x => parseDateTimeAlm(x, key))

  def tryParseUUIDAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Option[UUID]] =
    emptyStringIsNone(toParse, x => parseUUIDAlm(x, key))

  def tryParseBooleanAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Option[Boolean]] =
    emptyStringIsNone(toParse, x => parseBooleanAlm(x, key))

  def tryParseBase64Alm(toParse: String, key: String = "some value"): AlmValidationSBD[Option[Array[Byte]]] =
    emptyStringIsNone(toParse, x => parseBase64Alm(x, key))
    
  def notEmpty(toTest: String, key: String = "some value"): AlmValidationSBD[String] =
    if(toTest.isEmpty) badData("must not be empty", key).fail[String] else toTest.success[SingleBadDataProblem]

  def notEmptyOrWhitespace(toTest: String, key: String = "some value"): AlmValidationSBD[String] =
    if(toTest.trim.isEmpty) 
      badData("must not be empty or whitespaces", key).fail[String] 
    else 
      toTest.success[SingleBadDataProblem]
  
  private def emptyStringIsNone[T](str: String, f: String => AlmValidationSBD[T]) =
    if(str.trim.isEmpty)
      Success(None)
    else
      f(str).map(Some(_))

}