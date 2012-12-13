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
package almhirt.almvalidation

import java.util.UUID
import scalaz.{Validation, Success, Failure, ValidationNEL}
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.common._

/** Parsing operations that result in a validation 
 * 
 * All functions that start with try... result in Option[T]. None is returned if the String to parse is empty or only contains whitespaces.
 */
trait AlmValidationParseFunctions{
  import almhirt.almvalidation.funs._
  import almhirt.problem.all._
  def parseIntAlm(toParse: String, key: String = ""): AlmValidation[Int] =
    try {
      toParse.toInt.success
    } catch {
      case err => BadDataProblem("Not a valid number(Int):%s".format(toParse)).withIdentifier(key).failure
    }

  def parseLongAlm(toParse: String, key: String = ""): AlmValidation[Long] =
    try {
      toParse.toLong.success
    } catch {
      case err => BadDataProblem("Not a valid number(Long): %s".format(toParse)).withIdentifier(key).failure
    }

  def parseBigIntAlm(toParse: String, key: String = ""): AlmValidation[BigInt] =
    try {
      BigInt.apply(toParse).success
    } catch {
      case err => BadDataProblem("Not a valid number(BigInt): %s".format(toParse)).withIdentifier(key).failure
    }
    
  def parseDoubleAlm(toParse: String, key: String = ""): AlmValidation[Double] =
    try {
      toParse.toDouble.success
    } catch {
      case err => BadDataProblem("Not a valid number(Double): %s".format(toParse)).withIdentifier(key).failure
    }

  def parseFloatAlm(toParse: String, key: String = ""): AlmValidation[Float] =
    try {
      toParse.toFloat.success
    } catch {
      case err => BadDataProblem("Not a valid number(Float): %s".format(toParse)).withIdentifier(key).failure[Float]
    }

  def parseDecimalAlm(toParse: String, key: String = ""): AlmValidation[BigDecimal] =
    try {
      BigDecimal(toParse).success
     } catch {
      case err => BadDataProblem("Not a valid number(BigDecimal): %s".format(toParse)).withIdentifier(key).failure
    }

  def parseDateTimeAlm(toParse: String, key: String = ""): AlmValidation[DateTime] =
    try {
      new DateTime(toParse).success
     } catch {
      case err => BadDataProblem("Not a valid DateTime: %s".format(toParse)).withIdentifier(key).failure
    }

  def parseUriAlm(toParse: String, key: String = ""): AlmValidation[java.net.URI] =
    try {
      java.net.URI.create(toParse).success
     } catch {
      case err => BadDataProblem("Not a valid URI: %s".format(toParse)).withIdentifier(key).failure
    }

  def parseUuidAlm(toParse: String, key: String = ""): AlmValidation[UUID] =
    try {
      UUID.fromString(toParse).success
     } catch {
      case err => BadDataProblem("Not a valid UUID: %s".format(toParse)).withIdentifier(key).failure[UUID]
    }
     
     
  def parseBooleanAlm(toParse: String, key: String = ""): AlmValidation[Boolean] =
    try {
      toParse.toBoolean.success
     } catch {
      case err => BadDataProblem("Not a valid Boolean: %s".format(toParse)).withIdentifier(key).failure[Boolean]
    }

  def parseBase64Alm(toParse: String, key: String = ""): AlmValidation[Array[Byte]] =
    try {
      org.apache.commons.codec.binary.Base64.decodeBase64(toParse).success
     } catch {
      case err => BadDataProblem("Not a Base64 encoded String".format(toParse)).withIdentifier(key).failure[Array[Byte]]
    }

     
  def parseXmlAlm(toParse: String, key: String = ""): AlmValidation[scala.xml.Node] =
    try {
      scala.xml.XML.loadString(toParse).success
     } catch {
      case err => BadDataProblem("No valid XML: %s".format(toParse)).failure[scala.xml.Node]
    }
     
  def tryParseIntAlm(toParse: String, key: String = ""): AlmValidation[Option[Int]] =
    emptyStringIsNone(toParse, x => parseIntAlm(x, key))
  
  def tryParseLongAlm(toParse: String, key: String = ""): AlmValidation[Option[Long]] =
    emptyStringIsNone(toParse, x => parseLongAlm(x, key))
 
  def tryParseDoubleAlm(toParse: String, key: String = ""): AlmValidation[Option[Double]] =
    emptyStringIsNone(toParse, x => parseDoubleAlm(x, key))
 
  def tryParseFloatAlm(toParse: String, key: String = ""): AlmValidation[Option[Float]] =
    emptyStringIsNone(toParse, x => parseFloatAlm(x, key))

  def tryParseDecimalAlm(toParse: String, key: String = ""): AlmValidation[Option[BigDecimal]] =
    emptyStringIsNone(toParse, x => parseDecimalAlm(x, key))

  def tryParseDateTimeAlm(toParse: String, key: String = ""): AlmValidation[Option[DateTime]] =
    emptyStringIsNone(toParse, x => parseDateTimeAlm(x, key))

  def tryParseUUIDAlm(toParse: String, key: String = ""): AlmValidation[Option[UUID]] =
    emptyStringIsNone(toParse, x => parseUuidAlm(x, key))

  def tryParseBooleanAlm(toParse: String, key: String = ""): AlmValidation[Option[Boolean]] =
    emptyStringIsNone(toParse, x => parseBooleanAlm(x, key))

  def tryParseBase64Alm(toParse: String, key: String = ""): AlmValidation[Option[Array[Byte]]] =
    emptyStringIsNone(toParse, x => parseBase64Alm(x, key))
    
  def notEmpty(toTest: String, key: String = ""): AlmValidation[String] =
    if(toTest.isEmpty) BadDataProblem("must not be empty").withIdentifier(key).failure else toTest.success

  def notEmptyOrWhitespace(toTest: String, key: String = ""): AlmValidation[String] =
    if(toTest.trim.isEmpty) 
      BadDataProblem("must not be empty or whitespaces").withIdentifier(key).failure 
    else 
      toTest.success
  
  private def emptyStringIsNone[T](str: String, f: String => AlmValidation[T]) =
    if(str.trim.isEmpty)
      None.success
    else
      f(str).map(Some(_))

}