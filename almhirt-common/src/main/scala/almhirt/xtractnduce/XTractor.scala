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
import almhirt.common._
import almhirt.almvalidation.kit._

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
  def getString(): AlmValidation[String]
  def getInt(): AlmValidation[Int]
  def getLong(): AlmValidation[Long]
  def getDouble(): AlmValidation[Double]
  def getFloat(): AlmValidation[Float]
  def getBoolean(): AlmValidation[Boolean]
  def getDecimal(): AlmValidation[BigDecimal]
  def getDateTime(): AlmValidation[DateTime]
  def getUUID(): AlmValidation[UUID]
  def getBytes(): AlmValidation[Array[Byte]]
  def tryGetString(): AlmValidation[Option[String]]
  def tryGetInt(): AlmValidation[Option[Int]]
  def tryGetLong(): AlmValidation[Option[Long]]
  def tryGetDouble(): AlmValidation[Option[Double]]
  def tryGetBoolean(): AlmValidation[Option[Boolean]]
  def tryGetFloat(): AlmValidation[Option[Float]]
  def tryGetDecimal(): AlmValidation[Option[BigDecimal]]
  def tryGetDateTime(): AlmValidation[Option[DateTime]]
  def tryGetBytes(): AlmValidation[Option[Array[Byte]]]
  def isBooleanSet(): AlmValidation[Boolean]
}

trait XTractor extends XTractorWithPathToRoot  {
  type T
  def keys: Seq[String]
  def tryGetString(aKey: String): AlmValidation[Option[String]]
  def tryGetInt(aKey: String): AlmValidation[Option[Int]]
  def tryGetLong(aKey: String): AlmValidation[Option[Long]]
  def tryGetDouble(aKey: String): AlmValidation[Option[Double]]
  def tryGetFloat(aKey: String): AlmValidation[Option[Float]]
  def tryGetBoolean(aKey: String): AlmValidation[Option[Boolean]]
  def tryGetDecimal(aKey: String): AlmValidation[Option[BigDecimal]]
  def tryGetDateTime(aKey: String): AlmValidation[Option[DateTime]]
  def tryGetUUID(aKey: String): AlmValidation[Option[UUID]]
  def tryGetAsString(aKey: String): AlmValidation[Option[String]]
  def tryGetBytes(aKey: String): AlmValidation[Option[Array[Byte]]]

  def getString(aKey: String): AlmValidation[String] = get(aKey, tryGetString)
  def getInt(aKey: String): AlmValidation[Int] = get(aKey, tryGetInt)
  def getLong(aKey: String): AlmValidation[Long] = get(aKey, tryGetLong)
  def getDouble(aKey: String): AlmValidation[Double] = get(aKey, tryGetDouble)
  def getFloat(aKey: String): AlmValidation[Float] = get(aKey, tryGetFloat)
  def getBoolean(aKey: String): AlmValidation[Boolean] = get(aKey, tryGetBoolean)
  def getDecimal(aKey: String): AlmValidation[BigDecimal] = get(aKey, tryGetDecimal)
  def getDateTime(aKey: String): AlmValidation[DateTime] = get(aKey, tryGetDateTime)
  def getUUID(aKey: String): AlmValidation[UUID] = get(aKey, tryGetUUID)
  def getBytes(aKey: String): AlmValidation[Array[Byte]] = get(aKey, tryGetBytes)
  def getAsString(aKey: String): AlmValidation[String] = get(aKey, tryGetAsString)

  def getStringOrElse(aKey: String, compensation: => String): AlmValidation[String] = 
    tryGetString(aKey).map(_.getOrElse(compensation))
  def getIntOrElse(aKey: String, compensation: => Int): AlmValidation[Int] =
    tryGetInt(aKey).map(_.getOrElse(compensation))
  def getLongOrElse(aKey: String, compensation: => Long): AlmValidation[Long] =
    tryGetLong(aKey).map(_.getOrElse(compensation))
  def getDoubleOrElse(aKey: String, compensation: => Double): AlmValidation[Double] =
    tryGetDouble(aKey).map(_.getOrElse(compensation))
  def getFloatOrElse(aKey: String, compensation: => Float): AlmValidation[Float] =
    tryGetFloat(aKey).map(_.getOrElse(compensation))
  def getBooleanOrElse(aKey: String, compensation: => Boolean): AlmValidation[Boolean] =
    tryGetBoolean(aKey).map(_.getOrElse(compensation))
  def getDecimalOrElse(aKey: String, compensation: => BigDecimal): AlmValidation[BigDecimal] =
    tryGetDecimal(aKey).map(_.getOrElse(compensation))
  def getDateTimeOrElse(aKey: String, compensation: => DateTime): AlmValidation[DateTime] =
    tryGetDateTime(aKey).map(_.getOrElse(compensation))
  def getUUIDOrElse(aKey: String, compensation: => UUID): AlmValidation[UUID] =
    tryGetUUID(aKey).map(_.getOrElse(compensation))
  def getBytesOrElse(aKey: String, compensation: => Array[Byte]): AlmValidation[Array[Byte]] =
    tryGetBytes(aKey).map(_.getOrElse(compensation))
  
  def isBooleanSetTrue(aKey: String): AlmValidation[Boolean]

  def tryGetTypeInfo(): AlmValidation[Option[String]]
  def getTypeInfo(): AlmValidation[String] = 
    tryGetTypeInfo() fold(
        _.failure,
        _.map(_.success) getOrElse (BadDataProblem("No type Info!").failure))
  
  def getXTractors(aKey: String): AlmValidation[List[XTractor]]
  
  def tryGetXTractor(aKey: String): AlmValidation[Option[XTractor]]
  
  def getXTractor(aKey: String): AlmValidation[XTractor] =
    tryGetXTractor(aKey) fold (
        _.failure,
        _.map(_.success).getOrElse (BadDataProblem("Structured data not found.").failure))
    
  def tryMapXTractor[U](aKey: String, mapXtractor: XTractor => AlmValidation[U]): AlmValidation[Option[U]] =
    tryGetXTractor(aKey) fold (
        _.failure,
        opt => {
          opt match {
            case Some(xtractor) => 
              mapXtractor(xtractor) fold (
                _.failure,
                Some(_).success)
            case None => None.success
          }})
  
  def mapXTractor[U](aKey: String, mapXtractor: XTractor => AlmValidation[U]): AlmValidation[U] =
    tryMapXTractor(aKey, mapXtractor) fold (
        _.failure,
        _.map {_.success} 
         .getOrElse (BadDataProblem("Structured data not found.").failure))
  
  def tryFlatMapXTractor[U](aKey: String, mapXtractor: XTractor => AlmValidation[Option[U]]): AlmValidation[Option[U]] =
    tryGetXTractor(aKey) fold(
        _.failure,
        _ match {
          case Some(xtractor) => 
            mapXtractor(xtractor) fold (
              _.failure,
              _.success)
          case None => None.success
        })

  def mapXTractorsToList[U](aKey: String, mapXtractor: XTractor => AlmValidation[U]): AlmValidation[List[U]] =
    getXTractors(aKey) fold (_.failure, _.map(x => mapXtractor(x).toAgg).sequence)
  
  def tryGetAtomic(aKey: String): AlmValidation[Option[XTractorAtomic]]

  def getAtomic(aKey: String): AlmValidation[XTractorAtomic] = 
    tryGetAtomic(aKey) fold(
        _.failure,
        _ match {
          case Some(v) => v.success 
          case None => BadDataProblem("Atomic not found.").failure 
        })
  
  def getAtomics(aKey: String): AlmValidation[List[XTractorAtomic]]
  
  def getAtomicsEvaluated[T](aKey: String, eval: XTractorAtomic => AlmValidation[T]): AlmValidation[List[T]] = 
    getAtomics(aKey) flatMap (ls => ls.map(xtract => eval(xtract).toAgg).sequence[AlmValidationAP, T])
  
  private def get[U](aKey: String, f: String => AlmValidation[Option[U]]): AlmValidation[U] = 
    f(aKey) fold (
        _.failure,
        _ match {
          case Some(v) => v.success 
          case None => BadDataProblem("Key not found.").failure })
 }