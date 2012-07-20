package almhirt.xtract

import scalaz.{Success}
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._

class XTractorAtomicAny(value: Any, val key: String) extends XTractorAtomic {
  type T = Any
  val underlying = value
  def getString(): AlmValidationSingleBadData[String] =
	try {
	  value.asInstanceOf[String].notEmptyOrWhitespaceAlm(key)
	} catch {
	  case exn => SingleBadDataProblem("Not a String: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[String]
	}
	
  def getInt(): AlmValidationSingleBadData[Int] =
	try {
	  value.asInstanceOf[Int].successSingleBadData
	} catch {
	  case exn => SingleBadDataProblem("Not an Int: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Int]
	}
	
  def getLong(): AlmValidationSingleBadData[Long] =
	try {
	  value.asInstanceOf[Long].successSingleBadData
	} catch {
	  case exn => SingleBadDataProblem("Not a Long: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Long]
	}
	
  def getDouble(): AlmValidationSingleBadData[Double] =
	try {
	  value.asInstanceOf[Double].successSingleBadData
	} catch {
	  case exn => SingleBadDataProblem("Not a Double: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Double]
	}

  def getFloat(): AlmValidationSingleBadData[Float] =
	try {
	  value.asInstanceOf[Float].successSingleBadData
	} catch {
	  case exn => SingleBadDataProblem("Not a Float: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Float]
	}

  def getDecimal(): AlmValidationSingleBadData[BigDecimal] =
	try {
	  value.asInstanceOf[BigDecimal].successSingleBadData
	} catch {
	  case exn => SingleBadDataProblem("Not a BigDecimal: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[BigDecimal]
	}

  def getDateTime(): AlmValidationSingleBadData[DateTime] =
	try {
	  value.asInstanceOf[DateTime].successSingleBadData
	} catch {
	  case exn => SingleBadDataProblem("Not a DateTime: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[DateTime]
	}
	
  def tryGetString(): AlmValidationSingleBadData[Option[String]] =
	try {
	  val str = value.asInstanceOf[String]
	  if(str.trim.isEmpty)
	    Success(None)
	  else
	    Success(Some(str))
	} catch {
	  case exn => SingleBadDataProblem("Not a String: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Option[String]]
	}
  
  def tryGetInt(): AlmValidationSingleBadData[Option[Int]] = 
    SingleBadDataProblem("Not supported", key = key).fail[Option[Int]]
  
  def tryGetLong(): AlmValidationSingleBadData[Option[Long]] = 
    SingleBadDataProblem("Not supported", key = key).fail[Option[Long]]
  
  def tryGetDouble(): AlmValidationSingleBadData[Option[Double]] = 
    SingleBadDataProblem("Not supported", key = key).fail[Option[Double]]

  def tryGetFloat(): AlmValidationSingleBadData[Option[Float]] = 
    SingleBadDataProblem("Not supported", key = key).fail[Option[Float]]

  def tryGetDecimal(): AlmValidationSingleBadData[Option[BigDecimal]] = 
    SingleBadDataProblem("Not supported", key = key).fail[Option[BigDecimal]]

  def tryGetDateTime(): AlmValidationSingleBadData[Option[DateTime]] = 
    SingleBadDataProblem("Not supported", key = key).fail[Option[DateTime]]

  def isBooleanSet(): AlmValidationSingleBadData[Boolean] = 
	try {
	  value.asInstanceOf[Boolean].successSingleBadData
	} catch {
	  case exn => SingleBadDataProblem("Not a Boolean: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Boolean]
	}
  
}