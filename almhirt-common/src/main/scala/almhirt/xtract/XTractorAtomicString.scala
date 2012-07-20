package almhirt.xtract

import scalaz.{Success}
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._

class XTractorAtomicString(value: String, val key: String) extends XTractorAtomic {
  type T = String
  val underlying = value
  def getString(): AlmValidationSingleBadData[String] =
	value.notEmptyOrWhitespaceAlm(key)
	
  def getInt(): AlmValidationSingleBadData[Int] =
	value.toIntAlm(key)
	
  def getLong(): AlmValidationSingleBadData[Long] =
	value.toLongAlm(key)
	
  def getDouble(): AlmValidationSingleBadData[Double] =
	value.toDoubleAlm(key)
	
  def getFloat(): AlmValidationSingleBadData[Float] =
	value.toFloatAlm(key)
	
  def getDecimal(): AlmValidationSingleBadData[BigDecimal] =
	value.toDecimalAlm(key)
	
  def getDateTime(): AlmValidationSingleBadData[DateTime] =
	value.toDateTimeAlm(key)
	
  def tryGetString(): AlmValidationSingleBadData[Option[String]] =
	  if(value.trim.isEmpty)
	    Success(None)
	  else
	    Success(Some(value))
  
  def tryGetInt(): AlmValidationSingleBadData[Option[Int]] =
	  onEmptyNoneElse(() => value.toIntAlm(key))
  
  def tryGetLong(): AlmValidationSingleBadData[Option[Long]] =
	  onEmptyNoneElse(() => value.toLongAlm(key))
  
  def tryGetDouble(): AlmValidationSingleBadData[Option[Double]] =
	  onEmptyNoneElse(() => value.toDoubleAlm(key))

  def tryGetFloat(): AlmValidationSingleBadData[Option[Float]] =
	  onEmptyNoneElse(() => value.toFloatAlm(key))

  def tryGetDecimal(): AlmValidationSingleBadData[Option[BigDecimal]] =
	  onEmptyNoneElse(() => value.toDecimalAlm(key))

  def tryGetDateTime(): AlmValidationSingleBadData[Option[DateTime]] =
	  onEmptyNoneElse(() => value.toDateTimeAlm(key))

  def isBooleanSet(): AlmValidationSingleBadData[Boolean] = 
    if(value.trim.isEmpty) 
      false.success[SingleBadDataProblem] 
    else 
      parseBooleanAlm(value, key)
  
  private def onEmptyNoneElse[U](f: () => AlmValidationSingleBadData[U]): AlmValidationSingleBadData[Option[U]] = {
    if(value.trim.isEmpty)
	  Success(None)
	else
	  f().map(Some(_))
  }

}