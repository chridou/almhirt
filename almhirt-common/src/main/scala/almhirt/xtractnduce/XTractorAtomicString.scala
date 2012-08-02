package almhirt.xtractnduce

import almhirt.validation.Problem._
import almhirt.validation.AlmValidation._
import almhirt.validation.AlmValidationSBD
import org.joda.time.DateTime
import scalaz.syntax.validation.ToValidationV
import scalaz.Success

class XTractorAtomicString(value: String, val key: String, val parent: Option[XTractor] = None) extends XTractorAtomic {
  type T = String
  val underlying = value
  def getString(): AlmValidationSBD[String] =
	value.notEmptyOrWhitespaceAlm(key).fail.map(_.prefixWithPath(path)).validation
	
  def getInt(): AlmValidationSBD[Int] =
	value.toIntAlm(key).fail.map(_.prefixWithPath(path)).validation
	
  def getLong(): AlmValidationSBD[Long] =
	value.toLongAlm(key).fail.map(_.prefixWithPath(path)).validation
	
  def getDouble(): AlmValidationSBD[Double] =
	value.toDoubleAlm(key).fail.map(_.prefixWithPath(path)).validation
	
  def getFloat(): AlmValidationSBD[Float] =
	value.toFloatAlm(key).fail.map(_.prefixWithPath(path)).validation
	
  def getBoolean(): AlmValidationSBD[Boolean] =
	value.toBooleanAlm(key).fail.map(_.prefixWithPath(path)).validation
	
  def getDecimal(): AlmValidationSBD[BigDecimal] =
	value.toDecimalAlm(key).fail.map(_.prefixWithPath(path)).validation
	
  def getDateTime(): AlmValidationSBD[DateTime] =
	value.toDateTimeAlm(key).fail.map(_.prefixWithPath(path)).validation
	
  def getBytes(): AlmValidationSBD[Array[Byte]] =
	value.toBytesFromBase64Alm(key).fail.map(_.prefixWithPath(path)).validation

  def tryGetString(): AlmValidationSBD[Option[String]] =
	  if(value.trim.isEmpty)
	    Success(None)
	  else
	    Success(Some(value))
  
  def tryGetInt(): AlmValidationSBD[Option[Int]] =
	onEmptyNoneElse(() => value.toIntAlm(key)).fail.map(_.prefixWithPath(path)).validation
  
  def tryGetLong(): AlmValidationSBD[Option[Long]] =
	onEmptyNoneElse(() => value.toLongAlm(key)).fail.map(_.prefixWithPath(path)).validation
  
  def tryGetDouble(): AlmValidationSBD[Option[Double]] =
	onEmptyNoneElse(() => value.toDoubleAlm(key)).fail.map(_.prefixWithPath(path)).validation

  def tryGetFloat(): AlmValidationSBD[Option[Float]] =
	onEmptyNoneElse(() => value.toFloatAlm(key)).fail.map(_.prefixWithPath(path)).validation

  def tryGetBoolean(): AlmValidationSBD[Option[Boolean]] = 
    onEmptyNoneElse(() => value.toBooleanAlm(key)).fail.map(_.prefixWithPath(path)).validation

  def tryGetDecimal(): AlmValidationSBD[Option[BigDecimal]] =
	onEmptyNoneElse(() => value.toDecimalAlm(key)).fail.map(_.prefixWithPath(path)).validation

  def tryGetDateTime(): AlmValidationSBD[Option[DateTime]] =
	onEmptyNoneElse(() => value.toDateTimeAlm(key)).fail.map(_.prefixWithPath(path)).validation

  def tryGetBytes(): AlmValidationSBD[Option[Array[Byte]]] =
	onEmptyNoneElse(() => value.toBytesFromBase64Alm(key)).fail.map(_.prefixWithPath(path)).validation

  def isBooleanSet(): AlmValidationSBD[Boolean] = 
    if(value.trim.isEmpty) 
      false.success[SingleBadDataProblem] 
    else 
      parseBooleanAlm(value, key).fail.map(_.prefixWithPath(path)).validation
  
  private def onEmptyNoneElse[U](f: () => AlmValidationSBD[U]): AlmValidationSBD[Option[U]] = {
    if(value.trim.isEmpty)
	  Success(None)
	else
	  f().map(Some(_))
  }

}