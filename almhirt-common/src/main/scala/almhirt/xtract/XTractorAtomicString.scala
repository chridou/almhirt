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
  def getString(): AlmValidationSBD[String] =
	value.notEmptyOrWhitespaceAlm(key)
	
  def getInt(): AlmValidationSBD[Int] =
	value.toIntAlm(key)
	
  def getLong(): AlmValidationSBD[Long] =
	value.toLongAlm(key)
	
  def getDouble(): AlmValidationSBD[Double] =
	value.toDoubleAlm(key)
	
  def getFloat(): AlmValidationSBD[Float] =
	value.toFloatAlm(key)
	
  def getBoolean(): AlmValidationSBD[Boolean] =
	value.toBooleanAlm(key)
	
  def getDecimal(): AlmValidationSBD[BigDecimal] =
	value.toDecimalAlm(key)
	
  def getDateTime(): AlmValidationSBD[DateTime] =
	value.toDateTimeAlm(key)
	
  def getBytes(): AlmValidationSBD[Array[Byte]] =
	value.toBytesFromBase64Alm(key)

  def tryGetString(): AlmValidationSBD[Option[String]] =
	  if(value.trim.isEmpty)
	    Success(None)
	  else
	    Success(Some(value))
  
  def tryGetInt(): AlmValidationSBD[Option[Int]] =
	onEmptyNoneElse(() => value.toIntAlm(key))
  
  def tryGetLong(): AlmValidationSBD[Option[Long]] =
	onEmptyNoneElse(() => value.toLongAlm(key))
  
  def tryGetDouble(): AlmValidationSBD[Option[Double]] =
	onEmptyNoneElse(() => value.toDoubleAlm(key))

  def tryGetFloat(): AlmValidationSBD[Option[Float]] =
	onEmptyNoneElse(() => value.toFloatAlm(key))

  def tryGetBoolean(): AlmValidationSBD[Option[Boolean]] = 
    onEmptyNoneElse(() => value.toBooleanAlm(key))

  def tryGetDecimal(): AlmValidationSBD[Option[BigDecimal]] =
	onEmptyNoneElse(() => value.toDecimalAlm(key))

  def tryGetDateTime(): AlmValidationSBD[Option[DateTime]] =
	onEmptyNoneElse(() => value.toDateTimeAlm(key))

  def tryGetBytes(): AlmValidationSBD[Option[Array[Byte]]] =
	onEmptyNoneElse(() => value.toBytesFromBase64Alm(key))

  def isBooleanSet(): AlmValidationSBD[Boolean] = 
    if(value.trim.isEmpty) 
      false.success[SingleBadDataProblem] 
    else 
      parseBooleanAlm(value, key)
  
  private def onEmptyNoneElse[U](f: () => AlmValidationSBD[U]): AlmValidationSBD[Option[U]] = {
    if(value.trim.isEmpty)
	  Success(None)
	else
	  f().map(Some(_))
  }

}