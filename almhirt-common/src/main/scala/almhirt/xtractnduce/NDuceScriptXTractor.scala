package almhirt.xtractnduce

import org.joda.time.DateTime
import scalaz.syntax.validation._
import almhirt.validation.AlmValidation._
import almhirt.validation.Problem._

class NDuceScriptXTractor(script: NDuceScript) extends XTractor {
  private val opsByKeys = scala.collection.mutable.Map(script.ops.map(x => x.key -> x): _*)
  type T = NDuceScript
  def underlying() = script
  val key = script.name
  
  def tryGetString(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceString(_,v)) => Some(v).successSBD
      case Some(NDuceStringOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a String", aKey).fail[Option[String]]
    }
  
  def tryGetInt(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceInt(_,v)) => Some(v).successSBD
      case Some(NDuceIntOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not an Int", aKey).fail[Option[Int]]
    }
  
  def tryGetLong(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceLong(_,v)) => Some(v).successSBD
      case Some(NDuceLongOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a Long", aKey).fail[Option[Long]]
    }
  
  def tryGetDouble(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceDouble(_,v)) => Some(v).successSBD
      case Some(NDuceDoubleOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a Double", aKey).fail[Option[Double]]
    }

  def tryGetFloat(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceFloat(_,v)) => Some(v).successSBD
      case Some(NDuceFloatOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a Float", aKey).fail[Option[Float]]
    }

  def tryGetBoolean(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceBoolean(_,v)) => Some(v).successSBD
      case Some(NDuceBooleanOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a Boolean", aKey).fail[Option[Boolean]]
    }
    
  def tryGetDecimal(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceDecimal(_,v)) => Some(v).successSBD
      case Some(NDuceDecimalOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a BigDecimal", aKey).fail[Option[BigDecimal]]
    }
    
  def tryGetDateTime(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceDateTime(_,v)) => Some(v).successSBD
      case Some(NDuceDateTimeOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a String", aKey).fail[Option[DateTime]]
    }

  def tryGetBytes(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceBytes(_,v)) => Some(v).successSBD
      case Some(NDuceBytesOpt(_,v)) => v.successSBD
      case None => None.successSBD
      case _ => SingleBadDataProblem("Not a String", aKey).fail[Option[Array[Byte]]]
    }
    
  def tryGetAsString(aKey: String) = 
    opsByKeys.get(aKey) match {
      case NDuceString(k,v) => v.notEmptyOrWhitespaceAlm(aKey).map(Some(_))
      case NDuceStringOpt(k,v) => v.notEmptyOrWhitespaceAlm(aKey)
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
	  case NDuceDateTime(k,v) => Some(v.toString).successSBD
	  case NDuceDateTimeOpt(k,v) => v.map(_.toString).successSBD
	  case _ => SingleBadDataProblem("Does not have a valid string representation", aKey).fail[Option[String]]
    }

  def isBooleanSetTrue(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceBoolean(_,v)) => v.successSBD
      case Some(NDuceBooleanOpt(_,Some(v))) => v.successSBD
      case Some(NDuceBooleanOpt(_,None)) => false.successSBD
      case None => false.successSBD
      case _ => SingleBadDataProblem("Not a Boolean", aKey).fail[Option[Boolean]]
    }
    
  def tryGetTypeInfo() = Some(key).successSBD
  
  def getXTractors(aKey: String): AlmValidationMBD[List[XTractor]] =
    opsByKeys.get(aKey) match {
      case NDuceAggregate(name, ops, _) => NDuceScriptXTractor
	  case _ => SingleBadDataProblem("Cannot be an XTractor", aKey).fail[Option[String]]
  
  def tryGetXTractor(aKey: String): AlmValidationSBD[Option[XTractor]] = {
    opsByKeys.get(aKey) match {
      case Some(agg @ NDuceAggregate(_, _, _)) => new NDuceScriptXTractor(agg).successSBD
	  case Some(_) => SingleBadDataProblem("Cannot be an XTractor", aKey).fail[Option[XTractor]]
	  case None => None.successSBD
    
  def tryGetAtomic(aKey: String): AlmValidationSBD[Option[XTractorAtomic]] =
    opsByKeys.get(aKey) match {
      case NDuceString(k,v) => Some(new XTractorAtomicAny(v, k)).successSBD
      case NDuceStringOpt(k,v) => v.map(new XTractorAtomicAny(v, k)).successSBD
      case NDuceInt(k,v) => Some(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceIntOpt(k,v) => v.map(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceLong(k,v) => Some(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceLongOpt(k,v) => v.map(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceDouble(k,v) => Some(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceDoubleOpt(k,v) => v.map(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceFloat(k,v) => Some(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceFloatOpt(k,v) => v.map(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceBoolean(k,v) => Some(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceBooleanOpt(k,v) => v.map(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceDecimal(k,v) => Some(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceDecimalOpt(k,v) => v.map(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceDateTime(k,v) => Some(new XTractorAtomicAny(v, k)).successSBD
	  case NDuceDateTimeOpt(k,v) => v.map(new XTractorAtomicAny(v, k)).successSBD
	  case _ => SingleBadDataProblem("Does not have an atomic representation", aKey).fail[Option[XTractorAtomic]]
	}
    
  def getAtomics(aKey: String): AlmValidationMBD[List[XTractorAtomic]] =
    for {
      propertyElement <-
      	getUniquePropertyElement(aKey).toMBD
      elems <- 
      	(propertyElement.map(x => onAllChildrenAreElems(x)) getOrElse (Seq.empty.successSBD)).toMBD
      xtractors <- { 
      	val items =
      	  elems.zipWithIndex.map {case(x, i) => 
      	    onSingleTextOnlyTypeContainerGetText(x).flatMap { y =>
      	      val txt = y.getOrElse("")
      	      new XTractorAtomicString(txt, "[%d]".format(i)).successSBD}}
      	  .map{x => x.toMBD}
          .toList
        items.sequence 
      }
    } yield xtractors
}