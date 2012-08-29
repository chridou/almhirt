package almhirt.xtractnduce

import java.util.UUID
import scalaz.syntax.validation.ToValidationV
import scalaz.Success
import org.joda.time.DateTime
import almhirt.validation._
import almhirt.validation.AlmValidationFunctions._
import almhirt.validation.syntax.AlmValidationOps._
import almhirt.validation.syntax.ProblemOps._

class XTractorAtomicString(value: String, val key: String, val parent: Option[XTractor] = None) extends XTractorAtomic {
  type T = String
  val underlying = value
  def getString(): AlmValidationSBD[String] =
	value.notEmptyOrWhitespaceAlm(pathAsString())
	
  def getInt(): AlmValidationSBD[Int] =
	value.toIntAlm(pathAsString())
	
  def getLong(): AlmValidationSBD[Long] =
	value.toLongAlm(pathAsString())
	
  def getDouble(): AlmValidationSBD[Double] =
	value.toDoubleAlm(pathAsString())
	
  def getFloat(): AlmValidationSBD[Float] =
	value.toFloatAlm(pathAsString())
	
  def getBoolean(): AlmValidationSBD[Boolean] =
	value.toBooleanAlm(pathAsString())
	
  def getDecimal(): AlmValidationSBD[BigDecimal] =
	value.toDecimalAlm(pathAsString())
	
  def getDateTime(): AlmValidationSBD[DateTime] =
	value.toDateTimeAlm(pathAsString())

  def getUUID(): AlmValidationSBD[UUID] =
	value.toUUIDAlm(pathAsString())

  def getBytes(): AlmValidationSBD[Array[Byte]] =
	value.toBytesFromBase64Alm(pathAsString())

  def tryGetString(): AlmValidationSBD[Option[String]] =
	  if(value.trim.isEmpty)
	    None.success
	  else
	    Some(value).success
  
  def tryGetInt(): AlmValidationSBD[Option[Int]] =
	onEmptyNoneElse(() => value.toIntAlm(pathAsString()))
  
  def tryGetLong(): AlmValidationSBD[Option[Long]] =
	onEmptyNoneElse(() => value.toLongAlm(pathAsString()))
  
  def tryGetDouble(): AlmValidationSBD[Option[Double]] =
	onEmptyNoneElse(() => value.toDoubleAlm(pathAsString()))

  def tryGetFloat(): AlmValidationSBD[Option[Float]] =
	onEmptyNoneElse(() => value.toFloatAlm(pathAsString()))

  def tryGetBoolean(): AlmValidationSBD[Option[Boolean]] = 
    onEmptyNoneElse(() => value.toBooleanAlm(pathAsString()))

  def tryGetDecimal(): AlmValidationSBD[Option[BigDecimal]] =
	onEmptyNoneElse(() => value.toDecimalAlm(pathAsString()))

  def tryGetDateTime(): AlmValidationSBD[Option[DateTime]] =
	onEmptyNoneElse(() => value.toDateTimeAlm(pathAsString()))

  def tryGetUUID(): AlmValidationSBD[Option[UUID]] =
	onEmptyNoneElse(() => value.toUUIDAlm(pathAsString()))
	
  def tryGetBytes(): AlmValidationSBD[Option[Array[Byte]]] =
	onEmptyNoneElse(() => value.toBytesFromBase64Alm(pathAsString()))

  def isBooleanSet(): AlmValidationSBD[Boolean] = 
    if(value.trim.isEmpty) 
      false.success[SingleBadDataProblem] 
    else 
      parseBooleanAlm(value, pathAsString())
  
  private def onEmptyNoneElse[U](f: () => AlmValidationSBD[U]): AlmValidationSBD[Option[U]] = {
    if(value.trim.isEmpty)
	  None.success
	else
	  f().map(Some(_))
  }

}