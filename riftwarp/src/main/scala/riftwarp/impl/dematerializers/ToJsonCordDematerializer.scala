package riftwarp.impl.dematerializers

import language.higherKinds

import org.joda.time.DateTime
import scalaz._, Scalaz._
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
import scalaz.syntax.validation
import almhirt.almvalidation.kit._
import almhirt.common._
import riftwarp._
import riftwarp.ma._
import riftwarp.components._

object ToJsonCordDematerializerFuns {
  /*
 * Parts of "launderString" are taken from Lift-JSON:
 * 
* Copyright 2009-2010 WorldWide Conferencing, LLC
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
  def launderString(str: String): Cord = {
    val buf = new StringBuilder
    for (i <- 0 until str.length) {
      val c = str.charAt(i)
      buf.append(c match {
        case '"' => "\\\""
        case '\\' => "\\\\"
        case '\b' => "\\b"
        case '\f' => "\\f"
        case '\n' => "\\n"
        case '\r' => "\\r"
        case '\t' => "\\t"
        case c if ((c >= '\u0000' && c < '\u001f') || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) => "\\u%04x".format(c: Int)
        case c => c
      })
    }
    buf.toString
  }

  def mapStringLike(part: Cord): Cord = '\"' -: part :- '\"'

  val mapString = (value: String) => Cord(mapStringLike(launderString(value)))
  val mapBoolean = (value: Boolean) => Cord(value.toString)
  val mapLong = (value: Long) => Cord(value.toString)
  val mapBigInt = (value: BigInt) => Cord(mapStringLike(value.toString))
  val mapFloatingPoint = (value: Double) => Cord(value.toString)
  val mapBigDecimal = (value: BigDecimal) => Cord(mapStringLike(value.toString))
  val mapDateTime = (value: DateTime) => Cord(mapStringLike(value.toString))
  val mapUuid = (value: _root_.java.util.UUID) => Cord(mapStringLike(value.toString))

  def mapperByType[A](implicit m: Manifest[A]): AlmValidation[A => Cord] = {
    val t = m.runtimeClass
    if (t == classOf[String])
      (mapString).asInstanceOf[A => Cord].success
    else if (t == classOf[Boolean])
      (mapBoolean).asInstanceOf[A => Cord].success
    else if (t == classOf[Byte])
      ((x: Byte) => mapLong(x)).asInstanceOf[A => Cord].success
    else if (t == classOf[Int])
      ((x: Int) => mapLong(x)).asInstanceOf[A => Cord].success
    else if (t == classOf[Long])
      (mapLong).asInstanceOf[A => Cord].success
    else if (t == classOf[BigInt])
      (mapBigInt).asInstanceOf[A => Cord].success
    else if (t == classOf[Float])
      ((x: Float) => mapFloatingPoint(x)).asInstanceOf[A => Cord].success
    else if (t == classOf[Double])
      (mapFloatingPoint).asInstanceOf[A => Cord].success
    else if (t == classOf[BigDecimal])
      (mapBigDecimal).asInstanceOf[A => Cord].success
    else if (t == classOf[DateTime])
      (mapDateTime).asInstanceOf[A => Cord].success
    else if (t == classOf[_root_.java.util.UUID])
      (mapUuid).asInstanceOf[A => Cord].success
    else
      UnspecifiedProblem("No mapper found for %s".format(t.getName())).failure
  }

  def mapperForAny(lookupFor: Any): AlmValidation[Any => Cord] = {
    if (lookupFor.isInstanceOf[String])
      mapperByType[String].map(mapper => (x: Any) => mapper(x.asInstanceOf[String]))
    else if (lookupFor.isInstanceOf[Boolean])
      mapperByType[Boolean].map(mapper => (x: Any) => mapper(x.asInstanceOf[Boolean]))
    else if (lookupFor.isInstanceOf[Byte])
      mapperByType[Byte].map(mapper => (x: Any) => mapper(x.asInstanceOf[Byte]))
    else if (lookupFor.isInstanceOf[Int])
      mapperByType[Int].map(mapper => (x: Any) => mapper(x.asInstanceOf[Int]))
    else if (lookupFor.isInstanceOf[Long])
      mapperByType[Long].map(mapper => (x: Any) => mapper(x.asInstanceOf[Long]))
    else if (lookupFor.isInstanceOf[BigInt])
      mapperByType[BigInt].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigInt]))
    else if (lookupFor.isInstanceOf[Float])
      mapperByType[Float].map(mapper => (x: Any) => mapper(x.asInstanceOf[Float]))
    else if (lookupFor.isInstanceOf[Double])
      mapperByType[Double].map(mapper => (x: Any) => mapper(x.asInstanceOf[Double]))
    else if (lookupFor.isInstanceOf[BigDecimal])
      mapperByType[BigDecimal].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigDecimal]))
    else if (lookupFor.isInstanceOf[DateTime])
      mapperByType[DateTime].map(mapper => (x: Any) => mapper(x.asInstanceOf[DateTime]))
    else if (lookupFor.isInstanceOf[_root_.java.util.UUID])
      mapperByType[_root_.java.util.UUID].map(mapper => (x: Any) => mapper(x.asInstanceOf[_root_.java.util.UUID]))
    else
      UnspecifiedProblem("No mapper found for %s".format(lookupFor.getClass.getName())).failure
  }

  def createKeyValuePair(kv: (Cord, Cord)): DimensionCord = {
    DimensionCord((Cord("{\"k\":") ++ kv._1 ++ ",\"v\":" ++ kv._2 ++ "}"))
  }

  def foldKeyValuePairs(items: scala.Iterable[(Cord, Cord)])(implicit functionObjects: HasFunctionObjects): AlmValidation[DimensionCord] =
    functionObjects.getChannelFolder[DimensionCord, DimensionCord](RiftJson()).flatMap(folder =>
      functionObjects.getMAFunctions[scala.collection.Iterable].flatMap(fo =>
        folder.fold(items.map(x => createKeyValuePair(x)).seq)(fo)))
}

class ToJsonCordDematerializer(state: Cord, val path: List[String], protected val divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends ToCordDematerializer(RiftJson(), ToolGroup.StdLib) {
  import ToJsonCordDematerializerFuns._
  protected def spawnNew(path: List[String]): AlmValidation[ToJsonCordDematerializer] =
    ToJsonCordDematerializer.apply(path, divertBlob).success

  def dematerialize = DimensionCord(('{' -: state :- '}'))

  def addPart(ident: String, part: Cord): AlmValidation[ToJsonCordDematerializer] = {
    val fieldCord = '\"' + ident + "\":"
    val completeCord = fieldCord ++ part
    if (state.length == 0)
      ToJsonCordDematerializer(completeCord, path, divertBlob).success
    else
      ToJsonCordDematerializer((state :- ',') ++ completeCord, path, divertBlob).success
  }

  private val nullCord = Cord("null")

  private def addNonePart(ident: String): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, nullCord)

  private def addStringPart(ident: String, value: String): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapString(value))

  private def addBooleanPart(ident: String, value: Boolean): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapBoolean(value))

  private def addLongPart(ident: String, value: Long): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapLong(value))

  private def addBigIntPart(ident: String, value: BigInt): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapBigInt(value))

  private def addFloatingPointPart(ident: String, value: Double): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapFloatingPoint(value))

  private def addBigDecimalPart(ident: String, value: BigDecimal): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapBigDecimal(value))

  private def addByteArrayPart(ident: String, value: Array[Byte]): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, '[' + value.mkString(",") + ']')

  private def addDateTimePart(ident: String, value: DateTime): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapDateTime(value))

  private def addUuidPart(ident: String, value: _root_.java.util.UUID): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapUuid(value))

  private def addJsonPart(ident: String, value: String): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapString(value))

  private def addXmlPart(ident: String, value: _root_.scala.xml.Node): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, mapString(value.toString()))

  private def addComplexPart(ident: String, value: Cord): AlmValidation[ToJsonCordDematerializer] =
    addPart(ident, value)

  def ifNoneAddNull[T](ident: String, valueOpt: Option[T], ifNotNull: (String, T) => AlmValidation[ToJsonCordDematerializer]): AlmValidation[ToJsonCordDematerializer] = {
    option.cata(valueOpt)(ifNotNull(ident, _), addNonePart(ident))
  }

  def addString(ident: String, aValue: String) = addStringPart(ident, aValue)
  def addOptionalString(ident: String, anOptionalValue: Option[String]) = ifNoneAddNull(ident: String, anOptionalValue, addString)

  def addBoolean(ident: String, aValue: Boolean) = addBooleanPart(ident, aValue)
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]) = ifNoneAddNull(ident: String, anOptionalValue, addBooleanPart)

  def addByte(ident: String, aValue: Byte) = addLongPart(ident, aValue)
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]) = ifNoneAddNull(ident: String, anOptionalValue, addByte)
  def addInt(ident: String, aValue: Int) = addLongPart(ident, aValue)
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]) = ifNoneAddNull(ident: String, anOptionalValue, addInt)
  def addLong(ident: String, aValue: Long) = addLongPart(ident, aValue)
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]) = ifNoneAddNull(ident: String, anOptionalValue, addLong)
  def addBigInt(ident: String, aValue: BigInt) = addBigIntPart(ident, aValue)
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]) = ifNoneAddNull(ident: String, anOptionalValue, addBigIntPart)

  def addFloat(ident: String, aValue: Float) = addFloatingPointPart(ident, aValue)
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]) = ifNoneAddNull(ident: String, anOptionalValue, addFloat)
  def addDouble(ident: String, aValue: Double) = addFloatingPointPart(ident, aValue)
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]) = ifNoneAddNull(ident: String, anOptionalValue, addDouble)
  def addBigDecimal(ident: String, aValue: BigDecimal) = addBigDecimalPart(ident, aValue)
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]) = ifNoneAddNull(ident: String, anOptionalValue, addBigDecimal)

  def addByteArray(ident: String, aValue: Array[Byte]) = addByteArrayPart(ident, aValue)
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addByteArray)
  def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]) = {
    val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    addPart(ident, mapString(base64))
  }
  def addOptionalBase64EncodedByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addBase64EncodedByteArray)
  def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]) = {
    val theBlob = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    addPart(ident, mapString(theBlob))
  }
  def addOptionalByteArrayBlobEncoded(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addByteArrayBlobEncoded)

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addDateTimePart(ident, aValue)
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = ifNoneAddNull(ident: String, anOptionalValue, addDateTime)

  def addUri(ident: String, aValue: _root_.java.net.URI) = addString(ident, aValue.toString())
  def addOptionalUri(ident: String, anOptionalValue: Option[_root_.java.net.URI]) = ifNoneAddNull(ident: String, anOptionalValue, addUri)

  def addUuid(ident: String, aValue: _root_.java.util.UUID) = addUuidPart(ident, aValue)
  def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]) = ifNoneAddNull(ident: String, anOptionalValue, addUuid)

  def addJson(ident: String, aValue: String) = addJsonPart(ident, aValue)
  def addOptionalJson(ident: String, anOptionalValue: Option[String]) = ifNoneAddNull(ident: String, anOptionalValue, addJson)
  def addXml(ident: String, aValue: scala.xml.Node) = addXmlPart(ident, aValue)
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = ifNoneAddNull(ident: String, anOptionalValue, addXml)

  def addBlob(ident: String, aValue: Array[Byte], blobIdentifier: RiftBlobIdentifier) =
    getDematerializedBlob(ident, aValue, blobIdentifier).flatMap(blobDemat =>
      addComplexPart(ident, blobDemat.dematerialize.manifestation))

  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]], blobIdentifier: RiftBlobIdentifier) =
    option.cata(anOptionalValue)(v => addBlob(ident, v, blobIdentifier), addNonePart(ident))

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToJsonCordDematerializer] =
    spawnNew(ident).flatMap(demat =>
      decomposer.decompose(aComplexType)(demat).flatMap(toEmbed =>
        addComplexPart(ident, toEmbed.asInstanceOf[ToJsonCordDematerializer].dematerialize.manifestation)))

  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, anOptionalComplexType, addComplexType(decomposer))

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[ToJsonCordDematerializer] = {
    hasDecomposers.tryGetDecomposerForAny(aComplexType) match {
      case Some(decomposer) => addComplexType(decomposer)(ident, aComplexType)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, aComplexType.getClass.getName())).failure
    }
  }

  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, anOptionalComplexType, (x: String, y: U) => addComplexType(x, y))

  def addComplexTypeFixed[U <: AnyRef](ident: String, aComplexType: U)(implicit mU: Manifest[U]): AlmValidation[ToJsonCordDematerializer] =
    hasDecomposers.getDecomposer[U].flatMap(decomposer => addComplexType(decomposer)(ident, aComplexType))

  def addOptionalComplexTypeFixed[U <: AnyRef](ident: String, anOptionalComplexType: Option[U])(implicit mU: Manifest[U]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, anOptionalComplexType, (x: String, y: U) => addComplexTypeFixed(x, y))

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    mapperByType[A].flatMap(map =>
      MAFuncs.map(ma)(x => DimensionCord(map(x))).flatMap(mcord =>
        MAFuncs.fold(this.channel)(mcord)(hasFunctionObjects, mM, manifest[DimensionCord], manifest[DimensionCord])).flatMap(dimCord =>
        addPart(ident, dimCord.manifestation)))

  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addPrimitiveMA(x, y))

  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    spawnNew(ident).flatMap(demat =>
      MAFuncs.mapiV(ma)((x, idx) => decomposer.decompose(x)(demat).map(_.dematerialize)).flatMap(complex =>
        MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[DimensionCord], manifest[DimensionCord])).flatMap(dimCord =>
        addPart(ident, dimCord.manifestation)))

  def addOptionalComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addComplexMA(decomposer)(x, y))

  def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    hasDecomposers.tryGetDecomposer[A] match {
      case Some(decomposer) => addComplexMA(decomposer)(ident, ma)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, mA.runtimeClass.getName())).failure
    }

  def addOptionalComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addComplexMAFixed(x, y))

  def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] = {
    MAFuncs.mapiV(ma)((a, idx) => mapWithComplexDecomposerLookUp(idx, ident)(a)).flatMap(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[DimensionCord], manifest[DimensionCord])).flatMap(dimCord =>
      addPart(ident, dimCord.manifestation))
  }

  def addOptionalComplexMALoose[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addComplexMALoose(x, y))

  def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] = {
    MAFuncs.mapiV(ma)((a, idx) => mapWithPrimitiveAndDecomposerLookUp(idx, ident)(a)).flatMap(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[DimensionCord], manifest[DimensionCord])).flatMap(dimCord =>
      addPart(ident, dimCord.manifestation))
  }

  def addOptionalMA[M[_], A <: Any](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addMA(x, y))

  def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    (TypeHelpers.isPrimitiveType(mA.runtimeClass), TypeHelpers.isPrimitiveType(mB.runtimeClass)) match {
      case (true, true) =>
        mapperByType[A].flatMap(mapA =>
          mapperByType[B].map(mapB =>
            aMap.map {
              case (a, b) =>
                (mapA(a), mapB(b))
            }).flatMap(items =>
            foldKeyValuePairs(items)).flatMap(dimCord =>
            addPart(ident, dimCord.manifestation)))
      case (false, true) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not create primitive map for %s: B(%s) is not a primitive type".format(ident, mB.runtimeClass.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.runtimeClass.getName(), mB.runtimeClass.getName())).failure
    }

  def addOptionalPrimitiveMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addMA(x, y))

  def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      mapperByType[A].flatMap(mapA =>
        aMap.map {
          case (a, b) =>
            spawnNew("[" + a.toString + "]" :: ident :: path).flatMap(freshDemat =>
              decomposer.decompose(b)(freshDemat).map(demat =>
                (mapA(a), demat.dematerialize.manifestation)))
        }.map(_.toAgg).toList.sequence.flatMap(sequenced =>
          foldKeyValuePairs(sequenced).flatMap(pairs =>
            addPart(ident, pairs.manifestation)))),
      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)

  def addOptionalComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addComplexMap(decomposer)(x, y))

  def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    hasDecomposers.getDecomposer[B].flatMap(decomposer => addComplexMap[A, B](decomposer)(ident, aMap))

  def addOptionalComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addComplexMapFixed(x, y))

  def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      mapperByType[A].flatMap(mapA =>
        aMap.map {
          case (a, b) =>
            mapWithComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b =>
              (mapA(a), b.manifestation))
        }.map(_.toAgg).toList.sequence.flatMap(sequenced =>
          foldKeyValuePairs(sequenced).flatMap(pairs =>
            addPart(ident, pairs.manifestation)))),
      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)

  def addOptionalComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addComplexMapLoose(x, y))

  def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      mapperByType[A].flatMap(mapA =>
        aMap.map {
          case (a, b) =>
            mapWithPrimitiveAndDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b =>
              (mapA(a), b.manifestation))
        }.map(_.toAgg).toList.sequence.flatMap(sequenced =>
          foldKeyValuePairs(sequenced).flatMap(pairs =>
            addPart(ident, pairs.manifestation)))),
      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)

  def addOptionalMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addMap(x, y))

  def addMapSkippingUnknownValues[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      mapperByType[A].flatMap(mapA =>
        aMap.map {
          case (a, b) =>
            mapForgivingWithPrimitiveAndDecomposerLookUp("[" + a.toString + "]", ident)(b)
              .map(b => (mapA(a), b))
        }.map(_.toAgg).toList.sequence.flatMap(sequenced =>
          foldKeyValuePairs(sequenced.filter(_._2.isDefined).map(x => (x._1, x._2.get.manifestation))).flatMap(pairs =>
            addPart(ident, pairs.manifestation)))),
      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)

  def addOptionalMapSkippingUnknownValues[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addMapSkippingUnknownValues(x, y))

  def addTypeDescriptor(descriptor: TypeDescriptor) = addString(TypeDescriptor.defaultKey, descriptor.toString)

  private def mapWithComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: AnyRef): AlmValidation[DimensionCord] =
    hasDecomposers.tryGetRawDecomposer(toDecompose.getClass) match {
      case Some(decomposer) =>
        spawnNew(idx :: ident :: path).flatMap(freshDemat =>
          decomposer.decomposeRaw(toDecompose)(freshDemat).map(_.dematerialize))
      case None =>
        UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, toDecompose.getClass.getName())).failure
    }

  def mapWithPrimitiveAndDecomposerLookUp(idx: String, ident: String)(toDecompose: Any): AlmValidation[DimensionCord] =
    mapperForAny(toDecompose).fold(
      _ =>
        toDecompose match {
          case toDecomposeAsAnyRef: AnyRef =>
            spawnNew(idx :: ident :: path).flatMap(freshDemat =>
              option.cata(hasDecomposers.tryGetRawDecomposer(toDecomposeAsAnyRef.getClass))(
                decomposer => decomposer.decomposeRaw(toDecomposeAsAnyRef)(freshDemat).map(_.dematerialize),
                UnspecifiedProblem("No decomposer or primitive mapper found for ident '%s'. i was trying to find a match for '%s'".format(ident, toDecompose.getClass.getName())).failure))
          case x =>
            UnspecifiedProblem("The type '%s' is not supported for dematerialization. The ident was '%s'".format(x.getClass.getName(), ident)).failure
        },
      mbt =>
        DimensionCord(mbt(toDecompose)).success)

  def mapForgivingWithPrimitiveAndDecomposerLookUp(idx: String, ident: String)(toDecompose: Any): AlmValidation[Option[DimensionCord]] =
    mapperForAny(toDecompose).fold(
      _ =>
        toDecompose match {
          case toDecomposeAsAnyRef: AnyRef =>
            spawnNew(idx :: ident :: path).flatMap(freshDemat =>
              option.cata(hasDecomposers.tryGetRawDecomposer(toDecomposeAsAnyRef.getClass))(
                decomposer => decomposer.decomposeRaw(toDecomposeAsAnyRef)(freshDemat).map(x => Some(x.dematerialize)),
                None.success))
          case x =>
            None.success
        },
      mbt =>
        Some(DimensionCord(mbt(toDecompose))).success)

}

object ToJsonCordDematerializer extends DematerializerFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  def apply(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToJsonCordDematerializer = apply(Cord(""), divertBlob)
  def apply(state: Cord, divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToJsonCordDematerializer = apply(state, Nil, divertBlob)
  def apply(path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToJsonCordDematerializer = apply(Cord(""), path, divertBlob)
  def apply(state: Cord, path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToJsonCordDematerializer = new ToJsonCordDematerializer(state, path, divertBlob)
  def createDematerializer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): AlmValidation[ToJsonCordDematerializer] =
    apply(divertBlob).success
}
