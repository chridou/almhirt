package almhirt.almvalidation

import java.util.UUID
import scalaz.{Validation, Success, Failure, ValidationNEL}
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt._

/** Parsing operations that result in a validation 
 * 
 * All functions that start with try... result in Option[T]. None is returned if the String to parse is empty or only contains whitespaces.
 */
trait AlmValidationParseFunctions{
  import almvalidationfunctions._
  def parseIntAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Int] =
    try {
      toParse.toInt.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Int):%s".format(toParse), key).failure[Int]
    }

  def parseLongAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Long] =
    try {
      toParse.toLong.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Long): %s".format(toParse), key).failure[Long]
    }
  
  def parseDoubleAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Double] =
    try {
      toParse.toDouble.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Double): %s".format(toParse), key).failure[Double]
    }

  def parseFloatAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Float] =
    try {
      toParse.toFloat.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Float): %s".format(toParse), key).failure[Float]
    }

  def parseDecimalAlm(toParse: String, key: String = "some value"): AlmValidationSBD[BigDecimal] =
    try {
      BigDecimal(toParse).success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid number(BigDecimal): %s".format(toParse), key).failure[BigDecimal]
    }

  def parseDateTimeAlm(toParse: String, key: String = "some value"): AlmValidationSBD[DateTime] =
    try {
      new DateTime(toParse).success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid DateTime: %s".format(toParse), key).failure[DateTime]
    }

  def parseUUIDAlm(toParse: String, key: String = "some value"): AlmValidationSBD[UUID] =
    try {
      UUID.fromString(toParse).success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid UUID: %s".format(toParse), key).failure[UUID]
    }
     
     
  def parseBooleanAlm(toParse: String, key: String = "some value"): AlmValidationSBD[Boolean] =
    try {
      toParse.toBoolean.success[SingleBadDataProblem]
     } catch {
      case err => badData("Not a valid Boolean: %s".format(toParse), key).failure[Boolean]
    }

  def parseBase64Alm(toParse: String, key: String = "some value"): AlmValidationSBD[Array[Byte]] =
    try {
      org.apache.commons.codec.binary.Base64.decodeBase64(toParse).success
     } catch {
      case err => badData("Not a Base64 encoded String".format(toParse), key).failure[Array[Byte]]
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
    if(toTest.isEmpty) badData("must not be empty", key).failure[String] else toTest.success[SingleBadDataProblem]

  def notEmptyOrWhitespace(toTest: String, key: String = "some value"): AlmValidationSBD[String] =
    if(toTest.trim.isEmpty) 
      badData("must not be empty or whitespaces", key).failure[String] 
    else 
      toTest.success[SingleBadDataProblem]
  
  private def emptyStringIsNone[T](str: String, f: String => AlmValidationSBD[T]) =
    if(str.trim.isEmpty)
      None.success
    else
      f(str).map(Some(_))

}