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

class XTractorAtomicString(value: String, val key: String, val parent: Option[XTractor] = None) extends XTractorAtomic {
  type T = String
  val underlying = value
  def getString(): AlmValidation[String] =
	value.notEmptyOrWhitespace
	
  def getInt(): AlmValidation[Int] =
	value.toIntAlm
	
  def getLong(): AlmValidation[Long] =
	value.toLongAlm
	
  def getDouble(): AlmValidation[Double] =
	value.toDoubleAlm
	
  def getFloat(): AlmValidation[Float] =
	value.toFloatAlm
	
  def getBoolean(): AlmValidation[Boolean] =
	value.toBooleanAlm
	
  def getDecimal(): AlmValidation[BigDecimal] =
	value.toDecimalAlm
	
  def getDateTime(): AlmValidation[DateTime] =
	value.toDateTimeAlm

  def getUUID(): AlmValidation[UUID] =
	value.toUuidAlm

  def getBytes(): AlmValidation[Array[Byte]] =
	??? //value.toBytesFromBase64Alm

  def tryGetString(): AlmValidation[Option[String]] =
	  if(value.trim.isEmpty)
	    None.success
	  else
	    Some(value).success
  
  def tryGetInt(): AlmValidation[Option[Int]] =
	onEmptyNoneElse(() => value.toIntAlm)
  
  def tryGetLong(): AlmValidation[Option[Long]] =
	onEmptyNoneElse(() => value.toLongAlm)
  
  def tryGetDouble(): AlmValidation[Option[Double]] =
	onEmptyNoneElse(() => value.toDoubleAlm)

  def tryGetFloat(): AlmValidation[Option[Float]] =
	onEmptyNoneElse(() => value.toFloatAlm)

  def tryGetBoolean(): AlmValidation[Option[Boolean]] = 
    onEmptyNoneElse(() => value.toBooleanAlm)

  def tryGetDecimal(): AlmValidation[Option[BigDecimal]] =
	onEmptyNoneElse(() => value.toDecimalAlm)

  def tryGetDateTime(): AlmValidation[Option[DateTime]] =
	onEmptyNoneElse(() => value.toDateTimeAlm)

  def tryGetUUID(): AlmValidation[Option[UUID]] =
	onEmptyNoneElse(() => value.toUuidAlm)
	
  def tryGetBytes(): AlmValidation[Option[Array[Byte]]] =
	??? //onEmptyNoneElse(() => value.toBytesFromBase64Alm)

  def isBooleanSet(): AlmValidation[Boolean] = 
    if(value.trim.isEmpty) 
      false.success[BadDataProblem] 
    else 
      parseBooleanAlm(value)
  
  private def onEmptyNoneElse[U](f: () => AlmValidation[U]): AlmValidation[Option[U]] = {
    if(value.trim.isEmpty)
	  None.success
	else
	  f().map(Some(_))
  }

}