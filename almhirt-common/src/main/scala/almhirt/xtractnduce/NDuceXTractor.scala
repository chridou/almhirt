package almhirt.xtractnduce

import java.util.UUID
import org.joda.time.DateTime
import scalaz._
import Scalaz._
import almhirt._
import almvalidationinst._
import almhirt.syntax.almvalidation._

class NDuceXTractor(script: NDuceScript, val parent: Option[XTractor] = None) extends XTractor {
  private val opsByKeys = scala.collection.mutable.Map(script.ops.map(x => x.key -> x): _*)
  type T = NDuceScript
  def underlying() = script
  val key = script.name
  def keys() = script.ops.map(_.key)
  def tryGetString(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceString(_,v)) => Some(v).successSBD
      case Some(NDuceStringOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a String", aKey).failure[Option[String]]
    }
  
  def tryGetInt(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceInt(_,v)) => Some(v).successSBD
      case Some(NDuceIntOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not an Int", aKey).failure[Option[Int]]
    }
  
  def tryGetLong(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceLong(_,v)) => Some(v).successSBD
      case Some(NDuceLongOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a Long", aKey).failure[Option[Long]]
    }
  
  def tryGetDouble(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceDouble(_,v)) => Some(v).successSBD
      case Some(NDuceDoubleOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a Double", aKey).failure[Option[Double]]
    }

  def tryGetFloat(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceFloat(_,v)) => Some(v).successSBD
      case Some(NDuceFloatOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a Float", aKey).failure[Option[Float]]
    }

  def tryGetBoolean(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceBoolean(_,v)) => Some(v).successSBD
      case Some(NDuceBooleanOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a Boolean", aKey).failure[Option[Boolean]]
    }
    
  def tryGetDecimal(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceDecimal(_,v)) => Some(v).successSBD
      case Some(NDuceDecimalOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a BigDecimal", aKey).failure[Option[BigDecimal]]
    }
    
  def tryGetDateTime(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceDateTime(_,v)) => Some(v).successSBD
      case Some(NDuceDateTimeOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a String", aKey).failure[Option[DateTime]]
    }

  def tryGetUUID(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceUUID(_,v)) => Some(v).successSBD
      case Some(NDuceUUIDOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a String", aKey).failure[Option[UUID]]
    }
  
  def tryGetBytes(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceBytes(_,v)) => Some(v).successSBD
      case Some(NDuceBytesOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a String", aKey).failure[Option[Array[Byte]]]
    }
    
  def tryGetAsString(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(op) =>
        op match {
          case NDuceString(k,v) => v.notEmptyOrWhitespaceAlm(aKey).map(Some(_))
          case NDuceStringOpt(k,v) => v.map(_.notEmptyOrWhitespaceAlm(aKey)).validationOut
          case NDuceInt(k,v) => Some(v.toString).successSBD
	      case NDuceIntOpt(k,v) => v.map(_.toString).successSBD
	      case NDuceLong(k,v) => Some(v.toString).successSBD
	      case NDuceLongOpt(k,v) => v.map(_.toString).successSBD
	      case NDuceDouble(k,v) => Some(v.toString).successSBD
	      case NDuceDoubleOpt(k,v) => v.map(_.toString).successSBD
	      case NDuceFloat(k,v) => Some(v.toString).successSBD
	      case NDuceFloatOpt(k,v) => v.map(_.toString).successSBD
	      case NDuceBoolean(k,v) => Some(v.toString).successSBD
	      case NDuceBooleanOpt(k,v) => v.map(_.toString).successSBD
	      case NDuceDecimal(k,v) => Some(v.toString).successSBD
	      case NDuceDecimalOpt(k,v) => v.map(_.toString).successSBD
	      case NDuceDateTime(k,v) => Some(v.toString()).successSBD
	      case NDuceDateTimeOpt(k,v) => v.map(_.toString()).successSBD
	      case NDuceUUID(k,v) => Some(v.toString()).successSBD
	      case NDuceUUIDOpt(k,v) => v.map(_.toString()).successSBD
	      case _ => SingleBadDataProblem("Does not have a valid string representation", aKey).failure[Option[String]]
        }
      case None => None.successSBD
    }

  def isBooleanSetTrue(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceBoolean(_,v)) => v.successSBD
      case Some(NDuceBooleanOpt(_,Some(v))) => v.successSBD
      case Some(NDuceBooleanOpt(_,None)) => false.successSBD
      case None => false.successSBD
      case _ => SingleBadDataProblem("Not a Boolean", aKey).failure[Boolean]
    }
    
  def tryGetTypeInfo() = Some(key).successSBD
  
  def getXTractors(aKey: String): AlmValidationMBD[List[XTractor]] =
    opsByKeys.get(aKey) match {
      case Some(NDuceAggregate(name, ops, _)) => 
        ops
          .zipWithIndex
          .map { case (x, i) =>
            x match {
              case agg @ NDuceAggregate(_,_,_) => 
                new NDuceXTractor(agg, Some(this)).successMBD
              case x => 
                SingleBadDataProblem("%d cannot be an XTractor: %s".format(i, x.getClass.getName))
                  .toMBD.prefixWithPath(List(aKey))
                  .failure[NDuceXTractor]
            } }
          .toList
          .sequence
	  case _ => Nil.successMBD
    }
  
  def tryGetXTractor(aKey: String): AlmValidationSBD[Option[XTractor]] =
    opsByKeys.get(aKey) match {
      case Some(agg @ NDuceAggregate(_, _, _)) => Some(new NDuceXTractor(agg, Some(this))).successSBD
	  case Some(_) => SingleBadDataProblem("Cannot be an XTractor", aKey).failure[Option[XTractor]]
	  case None => None.successSBD
    }
    
  def tryGetAtomic(aKey: String): AlmValidationSBD[Option[XTractorAtomic]] =
    opsByKeys.get(aKey) match {
      case Some(op) =>
        op match {
          case NDuceString(k,v) =>Some(new XTractorAtomicAny(v, k, Some(this))).successSBD
          case NDuceStringOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).successSBD
          case NDuceInt(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).successSBD
	      case NDuceIntOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).successSBD
	      case NDuceLong(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).successSBD
	      case NDuceLongOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).successSBD
	      case NDuceDouble(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).successSBD
	      case NDuceDoubleOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).successSBD
	      case NDuceFloat(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).successSBD
	      case NDuceFloatOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).successSBD
	      case NDuceBoolean(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).successSBD
	      case NDuceBooleanOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).successSBD
	      case NDuceDecimal(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).successSBD
	      case NDuceDecimalOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).successSBD
	      case NDuceDateTime(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).successSBD
	      case NDuceDateTimeOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).successSBD
	      case NDuceUUID(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).successSBD
	      case NDuceUUIDOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).successSBD
	      case _ => SingleBadDataProblem("Does not have an atomic representation", aKey).failure[Option[XTractorAtomic]]
        }
      case None => None.successSBD
    }
    
  def getAtomics(aKey: String): AlmValidationMBD[List[XTractorAtomic]] =
    opsByKeys.get(aKey) match {
      case Some(op) =>
        op match {
          case NDucePrimitives(k,v) => 
          	v
          	 .zipWithIndex
          	 .map{case (x, i) => new XTractorAtomicAny(x, "[%d]".format(i), Some(this)).successMBD}
          	 .toList
          	 .sequence
	      case _ => 
	        SingleBadDataProblem("Does not have an atomic representation", aKey).toMBD.failure[List[XTractorAtomic]]
        }
      case None => Nil.successMBD
    }
}