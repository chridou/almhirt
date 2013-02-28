package riftwarp.impl.dematerializers

import language.higherKinds
import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.collection.IterableLike
import org.joda.time.DateTime
import scalaz._, Scalaz._
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
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

  def mapperByType[A](implicit m: ClassTag[A]): AlmValidation[A => Cord] = {
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
      UnspecifiedProblem("No primitive mapper found for %s".format(lookupFor.getClass.getName())).failure
  }

  def mapperForWithTypeInfo(lookupFor: Any): AlmValidation[Any => Cord] = {
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
      UnspecifiedProblem("No primitive mapper found for %s".format(lookupFor.getClass.getName())).failure
  }
  
  @tailrec
  private def createInnerJson(rest: List[Cord], acc: Cord): Cord =
    rest match {
      case Nil => Cord("[]")
      case last :: Nil => '[' -: (acc ++ last) :- ']'
      case h :: t => createInnerJson(t, acc ++ h :- ',')
    }

  def foldParts(items: List[Cord]): Cord = createInnerJson(items, Cord.empty) 

  def createKeyValuePair(kv: (Cord, Cord)): DimensionCord = {
    DimensionCord((Cord("{\"k\":") ++ kv._1 ++ ",\"v\":" ++ kv._2 ++ "}"))
  }

  def foldKeyValuePairs(items: scala.Iterable[(Cord, Cord)])(implicit functionObjects: HasFunctionObjects): AlmValidation[DimensionCord] =
    functionObjects.getChannelFolder[DimensionCord, DimensionCord](RiftJson()).flatMap(folder =>
      functionObjects.getMAFunctions[scala.collection.Iterable].flatMap(fo =>
        folder.fold(items.map(x => createKeyValuePair(x)).seq)(fo)))
}

class ToJsonCordDematerializer(state: Cord, val path: List[String], protected val divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends ToCordDematerializer(RiftJson(), ToolGroup.StdLib, hasDecomposers, hasFunctionObjects) with NoneIsHandledUnified[DimensionCord] {
  import ToJsonCordDematerializerFuns._
  
  private val nullCord = Cord("null")
  override def dematerialize = DimensionCord(('{' -: state :- '}'))
  
  protected def noneHandler(ident: String): ToJsonCordDematerializer = addPart(ident, nullCord)

  protected def spawnNew(path: List[String]): ToJsonCordDematerializer =
    ToJsonCordDematerializer.apply(path, divertBlob)

  protected override def valueReprToDim(repr: ValueRepr): DimensionCord = DimensionCord(repr)
  protected override def dimToReprValue(dim: DimensionCord): ValueRepr = dim.manifestation
  protected override def addReprValue(ident: String, value: ValueRepr): Dematerializer[DimensionCord] = addPart(ident, value)
  protected override def foldReprs(elems: Iterable[ValueRepr]): ValueRepr = foldParts(elems.toList)
  protected override def getPrimitiveToRepr[A](implicit tag: ClassTag[A]): AlmValidation[(A => ValueRepr)] = mapperByType[A]
  protected override def getAnyPrimitiveToRepr(what: Any): AlmValidation[(Any => ValueRepr)] = mapperForAny(what)
  
  protected override def insertDematerializer(ident: String, dematerializer: Dematerializer[DimensionCord]) =
    addPart(ident, dematerializer.dematerialize.manifestation)

  def addPart(ident: String, part: Cord): ToJsonCordDematerializer = {
    val fieldCord = '\"' + ident + "\":"
    val completeCord = fieldCord ++ part
    if (state.length == 0)
      ToJsonCordDematerializer(completeCord, path, divertBlob)
    else
      ToJsonCordDematerializer((state :- ',') ++ completeCord, path, divertBlob)
  }

  private def addStringPart(ident: String, value: String): ToJsonCordDematerializer =
    addPart(ident, mapString(value))

  private def addBooleanPart(ident: String, value: Boolean): ToJsonCordDematerializer =
    addPart(ident, mapBoolean(value))

  private def addLongPart(ident: String, value: Long): ToJsonCordDematerializer =
    addPart(ident, mapLong(value))

  private def addBigIntPart(ident: String, value: BigInt): ToJsonCordDematerializer =
    addPart(ident, mapBigInt(value))

  private def addFloatingPointPart(ident: String, value: Double): ToJsonCordDematerializer =
    addPart(ident, mapFloatingPoint(value))

  private def addBigDecimalPart(ident: String, value: BigDecimal): ToJsonCordDematerializer =
    addPart(ident, mapBigDecimal(value))

  private def addByteArrayPart(ident: String, value: Array[Byte]): ToJsonCordDematerializer =
    addPart(ident, '[' + value.mkString(",") + ']')

  private def addDateTimePart(ident: String, value: DateTime): ToJsonCordDematerializer =
    addPart(ident, mapDateTime(value))

  private def addUuidPart(ident: String, value: _root_.java.util.UUID): ToJsonCordDematerializer =
    addPart(ident, mapUuid(value))

  private def addComplexPart(ident: String, value: Cord): ToJsonCordDematerializer =
    addPart(ident, value)

  override def addString(ident: String, aValue: String) = addStringPart(ident, aValue)

  override def addBoolean(ident: String, aValue: Boolean) = addBooleanPart(ident, aValue)

  override def addByte(ident: String, aValue: Byte) = addLongPart(ident, aValue)
  override def addInt(ident: String, aValue: Int) = addLongPart(ident, aValue)
  override def addLong(ident: String, aValue: Long) = addLongPart(ident, aValue)
  override def addBigInt(ident: String, aValue: BigInt) = addBigIntPart(ident, aValue)

  override def addFloat(ident: String, aValue: Float) = addFloatingPointPart(ident, aValue)
  override def addDouble(ident: String, aValue: Double) = addFloatingPointPart(ident, aValue)
  override def addBigDecimal(ident: String, aValue: BigDecimal) = addBigDecimalPart(ident, aValue)

  override def addByteArray(ident: String, aValue: Array[Byte]) = addByteArrayPart(ident, aValue)
  override def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]) = {
    val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    addPart(ident, mapString(base64))
  }
  override def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]) = {
    val theBlob = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    addPart(ident, mapString(theBlob))
  }

  override def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addDateTimePart(ident, aValue)

  override def addUri(ident: String, aValue: _root_.java.net.URI) = addString(ident, aValue.toString())

  override def addUuid(ident: String, aValue: _root_.java.util.UUID) = addUuidPart(ident, aValue)

  override def addBlob(ident: String, aValue: Array[Byte], blobIdentifier: RiftBlobIdentifier) =
    getDematerializedBlob(ident, aValue, blobIdentifier).map(blobDemat =>
      addComplexPart(ident, blobDemat.dematerialize.manifestation))

  override def addRiftDescriptor(descriptor: RiftDescriptor) = 
    addWith(RiftDescriptor.defaultKey, descriptor, riftwarp.serialization.common.RiftDescriptorDecomposer).forceResult

  private def mapWithComplexDecomposerLookUp(idx: String, ident: String)(toDecompose: AnyRef): AlmValidation[DimensionCord] =
    hasDecomposers.getRawDecomposer(toDecompose.getClass).toOption match {
      case Some(decomposer) =>
        decomposer.decomposeRaw(toDecompose, spawnNew(idx :: ident :: path)).map(_.dematerialize)
      case None =>
        UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, toDecompose.getClass.getName())).failure
    }

  def mapWithPrimitiveAndDecomposerLookUp(idx: String, ident: String)(toDecompose: Any): AlmValidation[DimensionCord] =
    mapperForAny(toDecompose).fold(
      _ =>
        toDecompose match {
          case toDecomposeAsAnyRef: AnyRef =>
            option.cata(hasDecomposers.getRawDecomposer(toDecomposeAsAnyRef.getClass).toOption)(
              decomposer => decomposer.decomposeRaw(toDecomposeAsAnyRef, spawnNew(idx :: ident :: path)).map(_.dematerialize),
              UnspecifiedProblem("No decomposer or primitive mapper found for ident '%s'. i was trying to find a match for '%s'".format(ident, toDecompose.getClass.getName())).failure)
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
            option.cata(hasDecomposers.getRawDecomposer(toDecomposeAsAnyRef.getClass).toOption)(
              decomposer => decomposer.decomposeRaw(toDecomposeAsAnyRef, spawnNew(idx :: ident :: path)).map(x => Some(x.dematerialize)),
              None.success)
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
