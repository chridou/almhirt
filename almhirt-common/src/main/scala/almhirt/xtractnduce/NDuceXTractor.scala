/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.xtractnduce

import java.util.UUID
import org.joda.time.DateTime
import scalaz._
import Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almvalidation.constraints._

class NDuceXTractor(script: NDuceScript, val parent: Option[XTractor] = None) extends XTractor {
  private val opsByKeys = scala.collection.mutable.Map(script.ops.map(x => x.key -> x): _*)
  type T = NDuceScript
  def underlying() = script
  val key = script.name
  def keys() = script.ops.map(_.key)
  def tryGetString(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceString(_,v)) => Some(v).success
      case Some(NDuceStringOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not a String").failure[Option[String]]
    }
  
  def tryGetInt(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceInt(_,v)) => Some(v).success
      case Some(NDuceIntOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not an Int").failure[Option[Int]]
    }
  
  def tryGetLong(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceLong(_,v)) => Some(v).success
      case Some(NDuceLongOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not a Long").failure[Option[Long]]
    }
  
  def tryGetDouble(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceDouble(_,v)) => Some(v).success
      case Some(NDuceDoubleOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not a Double").failure[Option[Double]]
    }

  def tryGetFloat(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceFloat(_,v)) => Some(v).success
      case Some(NDuceFloatOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not a Float").failure[Option[Float]]
    }

  def tryGetBoolean(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceBoolean(_,v)) => Some(v).success
      case Some(NDuceBooleanOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not a Boolean").failure[Option[Boolean]]
    }
    
  def tryGetDecimal(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceDecimal(_,v)) => Some(v).success
      case Some(NDuceDecimalOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not a BigDecimal").failure[Option[BigDecimal]]
    }
    
  def tryGetDateTime(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceDateTime(_,v)) => Some(v).success
      case Some(NDuceDateTimeOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not a String").failure[Option[DateTime]]
    }

  def tryGetUUID(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceUUID(_,v)) => Some(v).success
      case Some(NDuceUUIDOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not a String").failure[Option[UUID]]
    }
  
  def tryGetBytes(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceBytes(_,v)) => Some(v).success
      case Some(NDuceBytesOpt(_,v)) => v.success
      case None => None.success
      case _ => BadDataProblem("Not a String").failure[Option[Array[Byte]]]
    }
    
  def tryGetAsString(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(op) =>
        op match {
          case NDuceString(k,v) => v.notEmptyOrWhitespace.map(Some(_))
          case NDuceStringOpt(k,v) => v.map(_.notEmptyOrWhitespace).validationOut
          case NDuceInt(k,v) => Some(v.toString).success
	      case NDuceIntOpt(k,v) => v.map(_.toString).success
	      case NDuceLong(k,v) => Some(v.toString).success
	      case NDuceLongOpt(k,v) => v.map(_.toString).success
	      case NDuceDouble(k,v) => Some(v.toString).success
	      case NDuceDoubleOpt(k,v) => v.map(_.toString).success
	      case NDuceFloat(k,v) => Some(v.toString).success
	      case NDuceFloatOpt(k,v) => v.map(_.toString).success
	      case NDuceBoolean(k,v) => Some(v.toString).success
	      case NDuceBooleanOpt(k,v) => v.map(_.toString).success
	      case NDuceDecimal(k,v) => Some(v.toString).success
	      case NDuceDecimalOpt(k,v) => v.map(_.toString).success
	      case NDuceDateTime(k,v) => Some(v.toString()).success
	      case NDuceDateTimeOpt(k,v) => v.map(_.toString()).success
	      case NDuceUUID(k,v) => Some(v.toString()).success
	      case NDuceUUIDOpt(k,v) => v.map(_.toString()).success
	      case _ => BadDataProblem("Does not have a valid string representation").failure[Option[String]]
        }
      case None => None.success
    }

  def isBooleanSetTrue(aKey: String) = 
    opsByKeys.get(aKey) match {
      case Some(NDuceBoolean(_,v)) => v.success
      case Some(NDuceBooleanOpt(_,Some(v))) => v.success
      case Some(NDuceBooleanOpt(_,None)) => false.success
      case None => false.success
      case _ => BadDataProblem("Not a Boolean").failure[Boolean]
    }
    
  def tryGetTypeInfo() = Some(key).success
  
  def getXTractors(aKey: String): AlmValidation[List[XTractor]] =
    opsByKeys.get(aKey) match {
      case Some(NDuceAggregate(name, ops, _)) => 
        ops
          .zipWithIndex
          .map { case (x, i) =>
            x match {
              case agg @ NDuceAggregate(_,_,_) => 
                new NDuceXTractor(agg, Some(this)).success
              case x => 
                BadDataProblem("%d cannot be an XTractor: %s".format(i, x.getClass.getName))
                  .failure[NDuceXTractor]
            } }
          .toList
          .map(x => x.toAgg)
          .sequence
	  case _ => Nil.success
    }
  
  def tryGetXTractor(aKey: String): AlmValidation[Option[XTractor]] =
    opsByKeys.get(aKey) match {
      case Some(agg @ NDuceAggregate(_, _, _)) => Some(new NDuceXTractor(agg, Some(this))).success
	  case Some(_) => BadDataProblem("Cannot be an XTractor").failure[Option[XTractor]]
	  case None => None.success
    }
    
  def tryGetAtomic(aKey: String): AlmValidation[Option[XTractorAtomic]] =
    opsByKeys.get(aKey) match {
      case Some(op) =>
        op match {
          case NDuceString(k,v) =>Some(new XTractorAtomicAny(v, k, Some(this))).success
          case NDuceStringOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).success
          case NDuceInt(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).success
	      case NDuceIntOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).success
	      case NDuceLong(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).success
	      case NDuceLongOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).success
	      case NDuceDouble(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).success
	      case NDuceDoubleOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).success
	      case NDuceFloat(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).success
	      case NDuceFloatOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).success
	      case NDuceBoolean(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).success
	      case NDuceBooleanOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).success
	      case NDuceDecimal(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).success
	      case NDuceDecimalOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).success
	      case NDuceDateTime(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).success
	      case NDuceDateTimeOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).success
	      case NDuceUUID(k,v) => Some(new XTractorAtomicAny(v, k, Some(this))).success
	      case NDuceUUIDOpt(k,v) => v.map(new XTractorAtomicAny(_, k, Some(this))).success
	      case _ => BadDataProblem("Does not have an atomic representation").failure[Option[XTractorAtomic]]
        }
      case None => None.success
    }
    
  def getAtomics(aKey: String): AlmValidation[List[XTractorAtomic]] =
    opsByKeys.get(aKey) match {
      case Some(op) =>
        op match {
          case NDucePrimitives(k,v) => 
          	v
          	 .zipWithIndex
          	 .map{case (x, i) => new XTractorAtomicAny(x, "[%d]".format(i), Some(this)).success}
          	 .toList
          	 .map(x => x.toAgg)
          	 .sequence
	      case _ => 
	        BadDataProblem("Does not have an atomic representation").failure[List[XTractorAtomic]]
        }
      case None => Nil.success
    }
}