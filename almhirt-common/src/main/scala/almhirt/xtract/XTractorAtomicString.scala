package almhirt.xtract

import scalaz.{Success}
import scalaz.syntax.validation._
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
	
  def tryGetString(): AlmValidationSingleBadData[Option[String]] =
	  if(value.trim.isEmpty)
	    Success(None)
	  else
	    Success(Some(value))
  
  def tryGetInt(): AlmValidationSingleBadData[Option[Int]] =
	  if(value.trim.isEmpty)
	    Success(None)
	  else
	    value.toIntAlm(key).map(Some(_))
  
  def tryGetLong(): AlmValidationSingleBadData[Option[Long]] =
	  if(value.trim.isEmpty)
	    Success(None)
	  else
	    value.toLongAlm(key).map(Some(_))
  
  def tryGetDouble(): AlmValidationSingleBadData[Option[Double]] =
	  if(value.trim.isEmpty)
	    Success(None)
	  else
	    value.toDoubleAlm(key).map(Some(_))
}