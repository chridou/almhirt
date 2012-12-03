package riftwarp.impl.dematerializers

import org.joda.time.DateTime
import scalaz._, Scalaz._
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.almvalidation.kit._
import almhirt.common._
import riftwarp._
import riftwarp.ma._
import riftwarp.TypeHelpers

object ToJsonCordDematerializerFuns {
  def launderString(str: String): Cord = Cord(str)

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
    val t = m.erasure
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
    DimensionCord((Cord("{\"k\":") ++ kv._1 :- ':') ++ kv._2 :- '}')
  }

  def foldKeyValuePairs(items: scala.collection.immutable.Iterable[(Cord, Cord)])(implicit functionObjects: HasFunctionObjects): AlmValidation[DimensionCord] =
    functionObjects.getChannelFolder[DimensionCord, DimensionCord](RiftJson()).bind(folder =>
      functionObjects.getMAFunctions[scala.collection.immutable.Iterable].bind(fo =>
        folder.fold(items.map(x => createKeyValuePair(x)).seq)(fo)))
}

class ToJsonCordDematerializer(state: Cord)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends ToCordDematerializer(RiftJson(), ToolGroup.StdLib) {
  import ToJsonCordDematerializerFuns._

  def dematerialize = DimensionCord(('{' -: state :- '}')).success

  def addPart(ident: String, part: Cord): AlmValidation[ToJsonCordDematerializer] = {
    val fieldCord = '\"' + ident + "\":"
    val completeCord = fieldCord ++ part
    if (state.length == 0)
      ToJsonCordDematerializer(completeCord).success
    else
      ToJsonCordDematerializer((state :- ',') ++ completeCord).success
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
  def addBlob(ident: String, aValue: Array[Byte]) = {
    val theBlob = org.apache.commons.codec.binary.Base64.encodeBase64String(aValue)
    addPart(ident, mapString(theBlob))
  }
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addBlob)

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addDateTimePart(ident, aValue)
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = ifNoneAddNull(ident: String, anOptionalValue, addDateTime)

  def addUuid(ident: String, aValue: _root_.java.util.UUID) = addUuidPart(ident, aValue)
  def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]) = ifNoneAddNull(ident: String, anOptionalValue, addUuid)

  def addJson(ident: String, aValue: String) = addJsonPart(ident, aValue)
  def addOptionalJson(ident: String, anOptionalValue: Option[String]) = ifNoneAddNull(ident: String, anOptionalValue, addJson)
  def addXml(ident: String, aValue: scala.xml.Node) = addXmlPart(ident, aValue)
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = ifNoneAddNull(ident: String, anOptionalValue, addXml)

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToJsonCordDematerializer] = {
    decomposer.decompose(aComplexType)(ToJsonCordDematerializer()).bind(toEmbed =>
      toEmbed.asInstanceOf[ToJsonCordDematerializer].dematerialize).bind(json =>
      addComplexPart(ident, json.manifestation))
  }
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
    hasDecomposers.getDecomposer[U].bind(decomposer => addComplexType(decomposer)(ident, aComplexType))
  
  def addOptionalComplexTypeFixed[U <: AnyRef](ident: String, anOptionalComplexType: Option[U])(implicit mU: Manifest[U]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, anOptionalComplexType, (x: String, y: U) => addComplexTypeFixed(x, y))
    
  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    mapperByType[A].bind(map =>
      MAFuncs.map(ma)(x => DimensionCord(map(x))).bind(mcord =>
        MAFuncs.fold(this.channel)(mcord)(hasFunctionObjects, mM, manifest[DimensionCord], manifest[DimensionCord])).bind(dimCord =>
        addPart(ident, dimCord.manifestation)))

  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addPrimitiveMA(x, y))

  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    MAFuncs.mapV(ma)(x => decomposer.decompose(x)(ToJsonCordDematerializer()).bind(_.dematerialize)).bind(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[DimensionCord], manifest[DimensionCord])).bind(dimCord =>
      addPart(ident, dimCord.manifestation))

  def addOptionalComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addComplexMA(decomposer)(x, y))

  def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    hasDecomposers.tryGetDecomposer[A] match {
      case Some(decomposer) => addComplexMA(decomposer)(ident, ma)
      case None => UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, mA.erasure.getName())).failure
    }

  def addOptionalComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addComplexMAFixed(x, y))

  def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] = {
    MAFuncs.mapV(ma)(mapWithComplexDecomposerLookUp(ident)).bind(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[DimensionCord], manifest[DimensionCord])).bind(dimCord =>
      addPart(ident, dimCord.manifestation))
  }

  def addOptionalComplexMALoose[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addComplexMALoose(x, y))

  def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] = {
    MAFuncs.mapV(ma)(mapWithPrimitiveAndDecomposerLookUp(ident)).bind(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[DimensionCord], manifest[DimensionCord])).bind(dimCord =>
      addPart(ident, dimCord.manifestation))
  }

  def addOptionalMA[M[_], A <: Any](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addMA(x, y))

  def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    (TypeHelpers.isPrimitiveType(mA.erasure), TypeHelpers.isPrimitiveType(mB.erasure)) match {
      case (true, true) =>
        mapperByType[A].bind(mapA =>
          mapperByType[B].map(mapB =>
            aMap.map {
              case (a, b) =>
                (mapA(a), mapB(b))
            }).bind(items =>
            foldKeyValuePairs(items)).bind(dimCord =>
            addPart(ident, dimCord.manifestation)))
      case (false, true) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not create primitive map for %s: B(%s) is not a primitive type".format(ident, mB.erasure.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.erasure.getName(), mB.erasure.getName())).failure
    }

  def addOptionalPrimitiveMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addMA(x, y))

  def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      mapperByType[A].bind(mapA =>
        aMap.map {
          case (a, b) =>
            decomposer.decompose(b)(ToJsonCordDematerializer()).bind(demat =>
              demat.dematerialize.map(b =>
                (mapA(a), b.manifestation)))
        }.map(_.toAgg).toList.sequence.bind(sequenced =>
          foldKeyValuePairs(sequenced).bind(pairs =>
            addPart(ident, pairs.manifestation)))),
      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addOptionalComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addComplexMap(decomposer)(x, y))

  def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    hasDecomposers.getDecomposer[B].bind(decomposer => addComplexMap[A, B](decomposer)(ident, aMap))

  def addOptionalComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addComplexMapFixed(x, y))

  def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      mapperByType[A].bind(mapA =>
        aMap.map {
          case (a, b) =>
            mapWithComplexDecomposerLookUp(ident)(b).map(b =>
              (mapA(a), b.manifestation))
        }.map(_.toAgg).toList.sequence.bind(sequenced =>
          foldKeyValuePairs(sequenced).bind(pairs =>
            addPart(ident, pairs.manifestation)))),
      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addOptionalComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addComplexMapLoose(x, y))

  def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      mapperByType[A].bind(mapA =>
        aMap.map {
          case (a, b) =>
            mapWithPrimitiveAndDecomposerLookUp(ident)(b).map(b =>
              (mapA(a), b.manifestation))
        }.map(_.toAgg).toList.sequence.bind(sequenced =>
          foldKeyValuePairs(sequenced).bind(pairs =>
            addPart(ident, pairs.manifestation)))),
      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addOptionalMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToJsonCordDematerializer] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addMap(x, y))

  def addTypeDescriptor(descriptor: TypeDescriptor) = addString(TypeDescriptor.defaultKey, descriptor.toString)

  private def mapWithComplexDecomposerLookUp(ident: String)(toDecompose: AnyRef): AlmValidation[DimensionCord] =
    hasDecomposers.tryGetRawDecomposer(toDecompose.getClass) match {
      case Some(decomposer) =>
        decomposer.decomposeRaw(toDecompose)(ToJsonCordDematerializer()).bind(_.dematerialize)
      case None =>
        UnspecifiedProblem("No decomposer found for ident '%s'. i was looking for a '%s'-Decomposer".format(ident, toDecompose.getClass.getName())).failure
    }

  def mapWithPrimitiveAndDecomposerLookUp(ident: String)(toDecompose: Any): AlmValidation[DimensionCord] =
    mapperForAny(toDecompose).fold(
      _ =>
        toDecompose match {
          case toDecomposeAsAnyRef: AnyRef =>
            option.cata(hasDecomposers.tryGetRawDecomposer(toDecomposeAsAnyRef.getClass))(
              decomposer => decomposer.decomposeRaw(toDecomposeAsAnyRef)(ToJsonCordDematerializer()).bind(_.dematerialize),
              UnspecifiedProblem("No decomposer or primitive mapper found for ident '%s'. i was trying to find a match for '%s'".format(ident, toDecompose.getClass.getName())).failure)
          case x =>
            UnspecifiedProblem("The type '%s' is not supported for dematerialization. The ident was '%s'".format(x.getClass.getName(), ident)).failure
        },
      mbt =>
        DimensionCord(mbt(toDecompose)).success)
}

object ToJsonCordDematerializer extends DematerializerFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  def apply()(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToJsonCordDematerializer = apply(Cord(""))
  def apply(state: Cord)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToJsonCordDematerializer = new ToJsonCordDematerializer(state)
  def createDematerializer(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): AlmValidation[Dematerializer[DimensionCord]] =
    apply().success
}
