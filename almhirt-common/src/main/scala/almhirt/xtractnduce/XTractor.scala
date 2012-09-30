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
import scalaz._
import Scalaz._
import org.joda.time.DateTime
import almhirt._
import problem.inst._
import almhirt.syntax.almvalidation._

trait XTractorWithPathToRoot{  
  def parent: Option[XTractor]
  def key: String
  lazy val pathToRoot: List[String] =
    parent match {
      case Some(p) => this.key :: p.pathToRoot
      case None => List(this.key)
  }
  lazy val path = pathToRoot.reverse
  def pathAsString(sep: String = ".") = path.mkString(sep)
  protected def pathAsStringWithKey(key: String, sep: String = ".") =
    (key :: pathToRoot).reverse.mkString(sep)
}

trait XTractorAtomic extends XTractorWithPathToRoot {
  type T
  def underlying: T
  def getString(): AlmValidationSBD[String]
  def getInt(): AlmValidationSBD[Int]
  def getLong(): AlmValidationSBD[Long]
  def getDouble(): AlmValidationSBD[Double]
  def getFloat(): AlmValidationSBD[Float]
  def getBoolean(): AlmValidationSBD[Boolean]
  def getDecimal(): AlmValidationSBD[BigDecimal]
  def getDateTime(): AlmValidationSBD[DateTime]
  def getUUID(): AlmValidationSBD[UUID]
  def getBytes(): AlmValidationSBD[Array[Byte]]
  def tryGetString(): AlmValidationSBD[Option[String]]
  def tryGetInt(): AlmValidationSBD[Option[Int]]
  def tryGetLong(): AlmValidationSBD[Option[Long]]
  def tryGetDouble(): AlmValidationSBD[Option[Double]]
  def tryGetBoolean(): AlmValidationSBD[Option[Boolean]]
  def tryGetFloat(): AlmValidationSBD[Option[Float]]
  def tryGetDecimal(): AlmValidationSBD[Option[BigDecimal]]
  def tryGetDateTime(): AlmValidationSBD[Option[DateTime]]
  def tryGetBytes(): AlmValidationSBD[Option[Array[Byte]]]
  def isBooleanSet(): AlmValidationSBD[Boolean]
}

trait XTractor extends XTractorWithPathToRoot  {
  type T
  def keys: Seq[String]
  def tryGetString(aKey: String): AlmValidationSBD[Option[String]]
  def tryGetInt(aKey: String): AlmValidationSBD[Option[Int]]
  def tryGetLong(aKey: String): AlmValidationSBD[Option[Long]]
  def tryGetDouble(aKey: String): AlmValidationSBD[Option[Double]]
  def tryGetFloat(aKey: String): AlmValidationSBD[Option[Float]]
  def tryGetBoolean(aKey: String): AlmValidationSBD[Option[Boolean]]
  def tryGetDecimal(aKey: String): AlmValidationSBD[Option[BigDecimal]]
  def tryGetDateTime(aKey: String): AlmValidationSBD[Option[DateTime]]
  def tryGetUUID(aKey: String): AlmValidationSBD[Option[UUID]]
  def tryGetAsString(aKey: String): AlmValidationSBD[Option[String]]
  def tryGetBytes(aKey: String): AlmValidationSBD[Option[Array[Byte]]]

  def getString(aKey: String): AlmValidationSBD[String] = get(aKey, tryGetString)
  def getInt(aKey: String): AlmValidationSBD[Int] = get(aKey, tryGetInt)
  def getLong(aKey: String): AlmValidationSBD[Long] = get(aKey, tryGetLong)
  def getDouble(aKey: String): AlmValidationSBD[Double] = get(aKey, tryGetDouble)
  def getFloat(aKey: String): AlmValidationSBD[Float] = get(aKey, tryGetFloat)
  def getBoolean(aKey: String): AlmValidationSBD[Boolean] = get(aKey, tryGetBoolean)
  def getDecimal(aKey: String): AlmValidationSBD[BigDecimal] = get(aKey, tryGetDecimal)
  def getDateTime(aKey: String): AlmValidationSBD[DateTime] = get(aKey, tryGetDateTime)
  def getUUID(aKey: String): AlmValidationSBD[UUID] = get(aKey, tryGetUUID)
  def getBytes(aKey: String): AlmValidationSBD[Array[Byte]] = get(aKey, tryGetBytes)
  def getAsString(aKey: String): AlmValidationSBD[String] = get(aKey, tryGetAsString)

  def getStringOrElse(aKey: String, compensation: => String): AlmValidationSBD[String] = 
    tryGetString(aKey).map(_.getOrElse(compensation))
  def getIntOrElse(aKey: String, compensation: => Int): AlmValidationSBD[Int] =
    tryGetInt(aKey).map(_.getOrElse(compensation))
  def getLongOrElse(aKey: String, compensation: => Long): AlmValidationSBD[Long] =
    tryGetLong(aKey).map(_.getOrElse(compensation))
  def getDoubleOrElse(aKey: String, compensation: => Double): AlmValidationSBD[Double] =
    tryGetDouble(aKey).map(_.getOrElse(compensation))
  def getFloatOrElse(aKey: String, compensation: => Float): AlmValidationSBD[Float] =
    tryGetFloat(aKey).map(_.getOrElse(compensation))
  def getBooleanOrElse(aKey: String, compensation: => Boolean): AlmValidationSBD[Boolean] =
    tryGetBoolean(aKey).map(_.getOrElse(compensation))
  def getDecimalOrElse(aKey: String, compensation: => BigDecimal): AlmValidationSBD[BigDecimal] =
    tryGetDecimal(aKey).map(_.getOrElse(compensation))
  def getDateTimeOrElse(aKey: String, compensation: => DateTime): AlmValidationSBD[DateTime] =
    tryGetDateTime(aKey).map(_.getOrElse(compensation))
  def getUUIDOrElse(aKey: String, compensation: => UUID): AlmValidationSBD[UUID] =
    tryGetUUID(aKey).map(_.getOrElse(compensation))
  def getBytesOrElse(aKey: String, compensation: => Array[Byte]): AlmValidationSBD[Array[Byte]] =
    tryGetBytes(aKey).map(_.getOrElse(compensation))
  
  def isBooleanSetTrue(aKey: String): AlmValidationSBD[Boolean]

  def tryGetTypeInfo(): AlmValidationSBD[Option[String]]
  def getTypeInfo(): AlmValidationSBD[String] = 
    tryGetTypeInfo() fold(
        _.failure,
        _.map(_.success) getOrElse (SingleBadDataProblem("No type Info!", key = pathAsStringWithKey("<typeInfo>")).failure))
  
  def getXTractors(aKey: String): AlmValidationMBD[List[XTractor]]
  
  def tryGetXTractor(aKey: String): AlmValidationSBD[Option[XTractor]]
  
  def getXTractor(aKey: String): AlmValidationSBD[XTractor] =
    tryGetXTractor(aKey) fold (
        _.failure,
        _.map(_.success).getOrElse (SingleBadDataProblem("Structured data not found.", key = pathAsStringWithKey(aKey)).failure))
    
  def tryMapXTractor[U](aKey: String, mapXtractor: XTractor => AlmValidationMBD[U]): AlmValidationMBD[Option[U]] =
    tryGetXTractor(aKey) fold (
        _.toMBD.failure,
        opt => {
          opt match {
            case Some(xtractor) => 
              mapXtractor(xtractor) fold (
                _.failure,
                Some(_).success)
            case None => None.success
          }})
  
  def mapXTractor[U](aKey: String, mapXtractor: XTractor => AlmValidationMBD[U]): AlmValidationMBD[U] =
    tryMapXTractor(aKey, mapXtractor) fold (
        _.prefixWithPath(List(key)).failure,
        _.map {_.success} 
         .getOrElse (SingleBadDataProblem("Structured data not found.", key = pathAsStringWithKey(aKey)).failure.toMBD))
  
  def tryFlatMapXTractor[U](aKey: String, mapXtractor: XTractor => AlmValidationMBD[Option[U]]): AlmValidationMBD[Option[U]] =
    tryGetXTractor(aKey) fold(
        _.toMBD.failure,
        _ match {
          case Some(xtractor) => 
            mapXtractor(xtractor) fold (
              _.prefixWithPath(List(key)).failure,
              _.success)
          case None => None.success
        })

  def mapXTractorsToList[U](aKey: String, mapXtractor: XTractor => AlmValidationMBD[U]): AlmValidationMBD[List[U]] =
    getXTractors(aKey) fold (_.failure, _.map(mapXtractor).sequence)
  
  def tryGetAtomic(aKey: String): AlmValidationSBD[Option[XTractorAtomic]]

  def getAtomic(aKey: String): AlmValidationSBD[XTractorAtomic] = 
    tryGetAtomic(aKey) fold(
        _.failure,
        _ match {
          case Some(v) => v.successSBD 
          case None => SingleBadDataProblem("Atomic not found.", key = pathAsStringWithKey(aKey)).failure 
        })
  
  def getAtomics(aKey: String): AlmValidationMBD[List[XTractorAtomic]]
  
  def getAtomicsEvaluated[T](aKey: String, eval: XTractorAtomic => AlmValidationSBD[T]): AlmValidationMBD[List[T]] = 
    getAtomics(aKey) bind (ls => ls.map(xtract => eval(xtract).toMBD).sequence[AlmValidationMBD, T])
  
  private def get[U](aKey: String, f: String => AlmValidationSBD[Option[U]]): AlmValidationSBD[U] = 
    f(aKey) fold (
        _.failure,
        _ match {
          case Some(v) => v.successSBD 
          case None => SingleBadDataProblem("Key not found.", key = pathAsStringWithKey(aKey)).failure })
 }