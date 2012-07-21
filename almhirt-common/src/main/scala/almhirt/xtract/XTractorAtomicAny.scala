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
  def getString(): AlmValidationSBD[String] =
	try {
	  value.asInstanceOf[String].notEmptyOrWhitespaceAlm(key)
	} catch {
	  case exn => SingleBadDataProblem("Not a String: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[String]
	}
	
  def getInt(): AlmValidationSBD[Int] =
	try {
	  value.asInstanceOf[Int].successSBD
	} catch {
	  case exn => SingleBadDataProblem("Not an Int: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Int]
	}
	
  def getLong(): AlmValidationSBD[Long] =
	try {
	  value.asInstanceOf[Long].successSBD
	} catch {
	  case exn => SingleBadDataProblem("Not a Long: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Long]
	}
	
  def getDouble(): AlmValidationSBD[Double] =
	try {
	  value.asInstanceOf[Double].successSBD
	} catch {
	  case exn => SingleBadDataProblem("Not a Double: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Double]
	}

  def getFloat(): AlmValidationSBD[Float] =
	try {
	  value.asInstanceOf[Float].successSBD
	} catch {
	  case exn => SingleBadDataProblem("Not a Float: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Float]
	}

  def getBoolean(): AlmValidationSBD[Boolean] =
	try {
	  value.asInstanceOf[Boolean].successSBD
	} catch {
	  case exn => SingleBadDataProblem("Not a Float: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Boolean]
	}

  def getDecimal(): AlmValidationSBD[BigDecimal] =
	try {
	  value.asInstanceOf[BigDecimal].successSBD
	} catch {
	  case exn => SingleBadDataProblem("Not a BigDecimal: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[BigDecimal]
	}

  def getDateTime(): AlmValidationSBD[DateTime] =
	try {
	  value.asInstanceOf[DateTime].successSBD
	} catch {
	  case exn => SingleBadDataProblem("Not a DateTime: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[DateTime]
	}

  def getBytes(): AlmValidationSBD[Array[Byte]] =
	try {
	  value.asInstanceOf[Array[Byte]].successSBD
	} catch {
	  case exn => SingleBadDataProblem("Not an Array[Byte]: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Array[Byte]]
	}
	
  def tryGetString(): AlmValidationSBD[Option[String]] =
	try {
	  val str = value.asInstanceOf[String]
	  if(str.trim.isEmpty)
	    Success(None)
	  else
	    Success(Some(str))
	} catch {
	  case exn => SingleBadDataProblem("Not a String: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Option[String]]
	}
  
  def tryGetInt(): AlmValidationSBD[Option[Int]] = 
    SingleBadDataProblem("Not supported: tryGetInt", key = key).fail[Option[Int]]
  
  def tryGetLong(): AlmValidationSBD[Option[Long]] = 
    SingleBadDataProblem("Not supported: tryGetLong", key = key).fail[Option[Long]]
  
  def tryGetDouble(): AlmValidationSBD[Option[Double]] = 
    SingleBadDataProblem("Not supported: tryGetDouble", key = key).fail[Option[Double]]

  def tryGetFloat(): AlmValidationSBD[Option[Float]] = 
    SingleBadDataProblem("Not supported: tryGetFloat", key = key).fail[Option[Float]]

  def tryGetBoolean(): AlmValidationSBD[Option[Boolean]] = 
    SingleBadDataProblem("Not supported: tryGetBoolean", key = key).fail[Option[Boolean]]
  
  def tryGetDecimal(): AlmValidationSBD[Option[BigDecimal]] = 
    SingleBadDataProblem("Not supported: tryGetDecimal", key = key).fail[Option[BigDecimal]]

  def tryGetDateTime(): AlmValidationSBD[Option[DateTime]] = 
    SingleBadDataProblem("Not supported: tryGetDateTime", key = key).fail[Option[DateTime]]

  def tryGetBytes(): AlmValidationSBD[Option[Array[Byte]]] = 
    SingleBadDataProblem("Not supported: tryGetBytes", key = key).fail[Option[Array[Byte]]]

  def isBooleanSet(): AlmValidationSBD[Boolean] = 
	try {
	  value.asInstanceOf[Boolean].successSBD
	} catch {
	  case exn => SingleBadDataProblem("Not a Boolean: %s".format(exn.getMessage), key = key, exception= Some(exn)).fail[Boolean]
	}
  
}