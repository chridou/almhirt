package almhirt.xtractnduce

import java.util.UUID
import scalaz.syntax.validation.ToValidationV
import scalaz.Success
import org.joda.time.DateTime
import almhirt._
import almhirt.syntax.almvalidation._

class XTractorAtomicAny(value: Any, val key: String, val parent: Option[XTractor] = None) extends XTractorAtomic {
  type T = Any
  val underlying = value
  def getString(): AlmValidationSBD[String] =
	try {
	  value.asInstanceOf[String].notEmptyOrWhitespaceAlm(key)
	} catch {
	  case exn => SingleBadDataProblem("Not a String: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[String]
	}
	
  def getInt(): AlmValidationSBD[Int] =
	try {
	  value.asInstanceOf[Int].success
	} catch {
	  case exn => SingleBadDataProblem("Not an Int: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[Int]
	}
	
  def getLong(): AlmValidationSBD[Long] =
	try {
	  value.asInstanceOf[Long].success
	} catch {
	  case exn => SingleBadDataProblem("Not a Long: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[Long]
	}
	
  def getDouble(): AlmValidationSBD[Double] =
	try {
	  value.asInstanceOf[Double].success
	} catch {
	  case exn => SingleBadDataProblem("Not a Double: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[Double]
	}

  def getFloat(): AlmValidationSBD[Float] =
	try {
	  value.asInstanceOf[Float].success
	} catch {
	  case exn => SingleBadDataProblem("Not a Float: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[Float]
	}

  def getBoolean(): AlmValidationSBD[Boolean] =
	try {
	  value.asInstanceOf[Boolean].success
	} catch {
	  case exn => SingleBadDataProblem("Not a Float: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[Boolean]
	}

  def getDecimal(): AlmValidationSBD[BigDecimal] =
	try {
	  value.asInstanceOf[BigDecimal].success
	} catch {
	  case exn => SingleBadDataProblem("Not a BigDecimal: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[BigDecimal]
	}

  def getDateTime(): AlmValidationSBD[DateTime] =
	try {
	  value.asInstanceOf[DateTime].success
	} catch {
	  case exn => SingleBadDataProblem("Not a DateTime: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[DateTime]
	}

  def getUUID(): AlmValidationSBD[UUID] =
	try {
	  value.asInstanceOf[UUID].success
	} catch {
	  case exn => SingleBadDataProblem("Not a UUID: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[UUID]
	}
	
  def getBytes(): AlmValidationSBD[Array[Byte]] =
	try {
	  value.asInstanceOf[Array[Byte]].success
	} catch {
	  case exn => SingleBadDataProblem("Not an Array[Byte]: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[Array[Byte]]
	}
	
  def tryGetString(): AlmValidationSBD[Option[String]] =
	try {
	  val str = value.asInstanceOf[String]
	  if(str.trim.isEmpty)
	    None.success
	  else
	    Some(str).success
	} catch {
	  case exn => SingleBadDataProblem("Not a String: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[Option[String]]
	}
  
  def tryGetInt(): AlmValidationSBD[Option[Int]] = 
    SingleBadDataProblem("Not supported: tryGetInt", key = pathAsString()).failure[Option[Int]]
  
  def tryGetLong(): AlmValidationSBD[Option[Long]] = 
    SingleBadDataProblem("Not supported: tryGetLong", key = pathAsString()).failure[Option[Long]]
  
  def tryGetDouble(): AlmValidationSBD[Option[Double]] = 
    SingleBadDataProblem("Not supported: tryGetDouble", key = pathAsString()).failure[Option[Double]]

  def tryGetFloat(): AlmValidationSBD[Option[Float]] = 
    SingleBadDataProblem("Not supported: tryGetFloat", key = pathAsString()).failure[Option[Float]]

  def tryGetBoolean(): AlmValidationSBD[Option[Boolean]] = 
    SingleBadDataProblem("Not supported: tryGetBoolean", key = pathAsString()).failure[Option[Boolean]]
  
  def tryGetDecimal(): AlmValidationSBD[Option[BigDecimal]] = 
    SingleBadDataProblem("Not supported: tryGetDecimal", key = pathAsString()).failure[Option[BigDecimal]]

  def tryGetDateTime(): AlmValidationSBD[Option[DateTime]] = 
    SingleBadDataProblem("Not supported: tryGetDateTime", key = pathAsString()).failure[Option[DateTime]]

  def tryGetUUID(): AlmValidationSBD[Option[UUID]] = 
    SingleBadDataProblem("Not supported: tryGetDateTime", key = pathAsString()).failure[Option[UUID]]
  
  def tryGetBytes(): AlmValidationSBD[Option[Array[Byte]]] = 
    SingleBadDataProblem("Not supported: tryGetBytes", key = pathAsString()).failure[Option[Array[Byte]]]

  def isBooleanSet(): AlmValidationSBD[Boolean] = 
	try {
	  value.asInstanceOf[Boolean].success
	} catch {
	  case exn => SingleBadDataProblem("Not a Boolean: %s".format(exn.getMessage), key = pathAsString(), cause = Some(CauseIsThrowable(exn))).failure[Boolean]
	}
  
}