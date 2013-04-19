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
import scalaz.syntax.validation.ToValidationV
import scalaz.Success
import org.joda.time.DateTime
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almvalidation.constraints._

class XTractorAtomicAny(value: Any, val key: String, val parent: Option[XTractor] = None) extends XTractorAtomic {
  type T = Any
  val underlying = value
  def getString(): AlmValidation[String] =
    try {
      value.asInstanceOf[String].notEmptyOrWhitespace
    } catch {
      case exn: Exception => BadDataProblem("Not a String: %s".format(exn.getMessage), cause = Some(exn)).failure[String]
    }

  def getInt(): AlmValidation[Int] =
    try {
      value.asInstanceOf[Int].success
    } catch {
      case exn: Exception => BadDataProblem("Not an Int: %s".format(exn.getMessage), cause = Some(exn)).failure[Int]
    }

  def getLong(): AlmValidation[Long] =
    try {
      value.asInstanceOf[Long].success
    } catch {
      case exn: Exception => BadDataProblem("Not a Long: %s".format(exn.getMessage), cause = Some(exn)).failure[Long]
    }

  def getDouble(): AlmValidation[Double] =
    try {
      value.asInstanceOf[Double].success
    } catch {
      case exn: Exception => BadDataProblem("Not a Double: %s".format(exn.getMessage), cause = Some(exn)).failure[Double]
    }

  def getFloat(): AlmValidation[Float] =
    try {
      value.asInstanceOf[Float].success
    } catch {
      case exn: Exception => BadDataProblem("Not a Float: %s".format(exn.getMessage), cause = Some(exn)).failure[Float]
    }

  def getBoolean(): AlmValidation[Boolean] =
    try {
      value.asInstanceOf[Boolean].success
    } catch {
      case exn: Exception => BadDataProblem("Not a Float: %s".format(exn.getMessage), cause = Some(exn)).failure[Boolean]
    }

  def getDecimal(): AlmValidation[BigDecimal] =
    try {
      value.asInstanceOf[BigDecimal].success
    } catch {
      case exn: Exception => BadDataProblem("Not a BigDecimal: %s".format(exn.getMessage), cause = Some(exn)).failure[BigDecimal]
    }

  def getDateTime(): AlmValidation[DateTime] =
    try {
      value.asInstanceOf[DateTime].success
    } catch {
      case exn: Exception => BadDataProblem("Not a DateTime: %s".format(exn.getMessage), cause = Some(exn)).failure[DateTime]
    }

  def getUUID(): AlmValidation[UUID] =
    try {
      value.asInstanceOf[UUID].success
    } catch {
      case exn: Exception => BadDataProblem("Not a UUID: %s".format(exn.getMessage), cause = Some(exn)).failure[UUID]
    }

  def getBytes(): AlmValidation[Array[Byte]] =
    try {
      value.asInstanceOf[Array[Byte]].success
    } catch {
      case exn: Exception => BadDataProblem("Not an Array[Byte]: %s".format(exn.getMessage), cause = Some(exn)).failure[Array[Byte]]
    }

  def tryGetString(): AlmValidation[Option[String]] =
    try {
      val str = value.asInstanceOf[String]
      if (str.trim.isEmpty)
        None.success
      else
        Some(str).success
    } catch {
      case exn: Exception => BadDataProblem("Not a String: %s".format(exn.getMessage), cause = Some(exn)).failure[Option[String]]
    }

  def tryGetInt(): AlmValidation[Option[Int]] =
    BadDataProblem("Not supported: tryGetInt").failure[Option[Int]]

  def tryGetLong(): AlmValidation[Option[Long]] =
    BadDataProblem("Not supported: tryGetLong").failure[Option[Long]]

  def tryGetDouble(): AlmValidation[Option[Double]] =
    BadDataProblem("Not supported: tryGetDouble").failure[Option[Double]]

  def tryGetFloat(): AlmValidation[Option[Float]] =
    BadDataProblem("Not supported: tryGetFloat").failure[Option[Float]]

  def tryGetBoolean(): AlmValidation[Option[Boolean]] =
    BadDataProblem("Not supported: tryGetBoolean").failure[Option[Boolean]]

  def tryGetDecimal(): AlmValidation[Option[BigDecimal]] =
    BadDataProblem("Not supported: tryGetDecimal").failure[Option[BigDecimal]]

  def tryGetDateTime(): AlmValidation[Option[DateTime]] =
    BadDataProblem("Not supported: tryGetDateTime").failure[Option[DateTime]]

  def tryGetUUID(): AlmValidation[Option[UUID]] =
    BadDataProblem("Not supported: tryGetDateTime").failure[Option[UUID]]

  def tryGetBytes(): AlmValidation[Option[Array[Byte]]] =
    BadDataProblem("Not supported: tryGetBytes").failure[Option[Array[Byte]]]

  def isBooleanSet(): AlmValidation[Boolean] =
    try {
      value.asInstanceOf[Boolean].success
    } catch {
      case exn: Exception => BadDataProblem("Not a Boolean: %s".format(exn.getMessage), cause = Some(exn)).failure[Boolean]
    }

}