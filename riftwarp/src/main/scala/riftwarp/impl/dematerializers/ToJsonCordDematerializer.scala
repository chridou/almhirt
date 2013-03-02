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

class ToJsonCordDematerializer(state: Cord, val path: List[String], protected val divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers) extends ToCordDematerializer(RiftJson(), ToolGroup.StdLib, hasDecomposers) with NoneIsHandledUnified[DimensionCord] {
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

  override def getStringRepr(aValue: String) = mapString(aValue)

  override def getBooleanRepr(aValue: Boolean) = mapBoolean(aValue)

  override def getByteRepr(aValue: Byte) = mapLong(aValue)
  override def getIntRepr(aValue: Int) = mapLong(aValue)
  override def getLongRepr(aValue: Long) = mapLong(aValue)
  override def getBigIntRepr(aValue: BigInt) = mapBigInt(aValue)

  override def getFloatRepr(aValue: Float) = mapFloatingPoint(aValue)
  override def getDoubleRepr(aValue: Double) = mapFloatingPoint(aValue)
  override def getBigDecimalRepr(aValue: BigDecimal) = mapBigDecimal(aValue)

  override def getByteArrayRepr(aValue: Array[Byte]) = '[' + aValue.mkString(",") + ']'
  override def getBase64EncodedByteArrayRepr(aValue: Array[Byte]) = {
    val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    mapString(base64)
  }
  override def getByteArrayBlobEncodedRepr(aValue: Array[Byte]) = {
    val theBlob = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    mapString(theBlob)
  }

  override def getDateTimeRepr(aValue: org.joda.time.DateTime) = mapDateTime(aValue)

  override def getUriRepr(aValue: _root_.java.net.URI) = mapString(aValue.toString())

  override def getUuidRepr(aValue: _root_.java.util.UUID) = mapUuid(aValue)


  override def addRiftDescriptor(descriptor: RiftDescriptor) = 
    addWith(RiftDescriptor.defaultKey, descriptor, riftwarp.serialization.common.RiftDescriptorDecomposer).forceResult
}

object ToJsonCordDematerializer extends DematerializerFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  def apply(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): ToJsonCordDematerializer = apply(Cord(""), divertBlob)
  def apply(state: Cord, divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): ToJsonCordDematerializer = apply(state, Nil, divertBlob)
  def apply(path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): ToJsonCordDematerializer = apply(Cord(""), path, divertBlob)
  def apply(state: Cord, path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): ToJsonCordDematerializer = new ToJsonCordDematerializer(state, path, divertBlob)
  def createDematerializer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers): AlmValidation[ToJsonCordDematerializer] =
    apply(divertBlob).success
}
