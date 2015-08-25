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

import scala.language.higherKinds

import java.util.UUID
import scala.collection.generic.CanBuildFrom
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import scalaz.std._
import _root_.java.time.{ ZonedDateTime, LocalDateTime }
import almhirt.common._

/**
 * Parsing operations that result in a validation
 *
 * All functions that start with try... result in Option[T]. None is returned if the String to parse is empty or only contains whitespaces.
 */
trait AlmValidationParseFunctions {
  import almhirt.almvalidation.funs._
  import almhirt.problem.all._
  def parseBooleanAlm(toParse: String): AlmValidation[Boolean] =
    try {
      toParse.toBoolean.success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid Boolean: %s".format(toParse)).failure[Boolean]
    }

  def parseByteAlm(toParse: String): AlmValidation[Byte] =
    try {
      toParse.toByte.success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid number(Byte):%s".format(toParse)).failure
    }

  def parseShortAlm(toParse: String): AlmValidation[Short] =
    try {
      toParse.toShort.success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid number(Short):%s".format(toParse)).failure
    }

  def parseIntAlm(toParse: String): AlmValidation[Int] =
    try {
      toParse.toInt.success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid number(Int):%s".format(toParse)).failure
    }

  def parseLongAlm(toParse: String): AlmValidation[Long] =
    try {
      toParse.toLong.success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid number(Long): %s".format(toParse)).failure
    }

  def parseBigIntAlm(toParse: String): AlmValidation[BigInt] =
    try {
      BigInt.apply(toParse).success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid number(BigInt): %s".format(toParse)).failure
    }

  def parseDoubleAlm(toParse: String): AlmValidation[Double] =
    try {
      toParse.toDouble.success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid number(Double): %s".format(toParse)).failure
    }

  def parseFloatAlm(toParse: String): AlmValidation[Float] =
    try {
      toParse.toFloat.success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid number(Float): %s".format(toParse)).failure[Float]
    }

  def parseDecimalAlm(toParse: String): AlmValidation[BigDecimal] =
    try {
      BigDecimal(toParse).success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid number(BigDecimal): %s".format(toParse)).failure
    }

  def parseDateTimeAlm(toParse: String): AlmValidation[ZonedDateTime] =
    try {
      ZonedDateTime.parse(toParse).success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid DateTime: %s".format(toParse)).failure
    }

  def parseLocalDateTimeAlm(toParse: String): AlmValidation[LocalDateTime] =
    try {
      LocalDateTime.parse(toParse).success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid DateTime: %s".format(toParse)).failure
    }

  def parseDurationAlm(toParse: String): AlmValidation[scala.concurrent.duration.FiniteDuration] =
    try {
      val dur = scala.concurrent.duration.Duration(toParse)
      if (dur.isFinite)
        scala.concurrent.duration.Duration.fromNanos(dur.toNanos).success
      else
        BadDataProblem("Not a valid finite duration: %s".format(toParse)).failure
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid DateTime: %s".format(toParse)).failure
    }

  def parseUuidAlm(toParse: String): AlmValidation[UUID] =
    try {
      if (toParse.length() == 36)
        UUID.fromString(toParse).success
      else
        ParsingProblem(s"""Not a valid UUID: "$toParse". It must have 36 characters.""").failure
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid UUID: %s".format(toParse)).failure[UUID]
    }

  def parseUriAlm(toParse: String): AlmValidation[_root_.java.net.URI] =
    try {
      _root_.java.net.URI.create(toParse).success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not a valid URI: %s".format(toParse)).failure
    }

  def parseByteArrayAlm(toParse: String, sep: String): AlmValidation[Array[Byte]] =
    try {
      toParse.split(sep).map(_.toByte).success
    } catch {
      case err: Exception ⇒ ParsingProblem("Not all are valid bytes:".format(toParse)).failure
    }

  def parseXmlAlm(toParse: String): AlmValidation[scala.xml.Elem] =
    try {
      scala.xml.XML.loadString(toParse).success
    } catch {
      case err: Exception ⇒ ParsingProblem("No valid XML: %s".format(toParse)).failure[scala.xml.Elem]
    }

  def tryParseByteAlm(toParse: String): AlmValidation[Option[Byte]] =
    emptyStringIsNone(toParse, x ⇒ parseByteAlm(x))

  def tryParseIntAlm(toParse: String): AlmValidation[Option[Int]] =
    emptyStringIsNone(toParse, x ⇒ parseIntAlm(x))

  def tryParseLongAlm(toParse: String): AlmValidation[Option[Long]] =
    emptyStringIsNone(toParse, x ⇒ parseLongAlm(x))

  def tryParseDoubleAlm(toParse: String): AlmValidation[Option[Double]] =
    emptyStringIsNone(toParse, x ⇒ parseDoubleAlm(x))

  def tryParseFloatAlm(toParse: String): AlmValidation[Option[Float]] =
    emptyStringIsNone(toParse, x ⇒ parseFloatAlm(x))

  def tryParseDecimalAlm(toParse: String): AlmValidation[Option[BigDecimal]] =
    emptyStringIsNone(toParse, x ⇒ parseDecimalAlm(x))

  def tryParseDateTimeAlm(toParse: String): AlmValidation[Option[ZonedDateTime]] =
    emptyStringIsNone(toParse, x ⇒ parseDateTimeAlm(x))

  def tryParseUUIDAlm(toParse: String): AlmValidation[Option[UUID]] =
    emptyStringIsNone(toParse, x ⇒ parseUuidAlm(x))

  def tryParseBooleanAlm(toParse: String): AlmValidation[Option[Boolean]] =
    emptyStringIsNone(toParse, x ⇒ parseBooleanAlm(x))

  def emptyOrWhitespaceIsNone(toTest: String): Option[String] =
    if (toTest.trim.isEmpty)
      None
    else
      Some(toTest)

  /** Parses each item of the string toParse separated by sep with parser */
  def parseToManyAlm[A, M[_] <: Traversable[_]](toParse: String, parse: String ⇒ AlmValidation[A], sep: String = ";")(implicit cbf: CanBuildFrom[Seq[A], A, M[A]]): AlmValidation[M[A]] = {
    import almhirt.almvalidation.funs
    if (toParse.isEmpty()) {
      val bldr = cbf()
      bldr.result.success
    } else {
      inTryCatch(toParse.split(sep).map(x ⇒ parse(x))).flatMap(parsedItems ⇒
        funs.aggregateProblemsMN[A, Seq, M](parsedItems))
    }
  }

  /** Parses each item of the trimmed string toParse separated by sep with parser and applies each String to String.trim before parsing */
  def parseTrimmedToManyAlm[A, M[_] <: Traversable[_]](toParse: String, parse: String ⇒ AlmValidation[A], sep: String = ";")(implicit cbf: CanBuildFrom[Seq[A], A, M[A]]): AlmValidation[M[A]] = {
    parseToManyAlm(toParse.trim, str ⇒ parse(str.trim), sep)
  }

  private def emptyStringIsNone[T](str: String, f: String ⇒ AlmValidation[T]) =
    if (str.trim.isEmpty)
      None.success
    else
      f(str).map(Some(_))

}