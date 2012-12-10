package riftwarp.ext.liftjson

import org.joda.time.DateTime
import scalaz._, Scalaz._
import scalaz.syntax.validation._
import scalaz.std._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._
import net.liftweb.json._

object LiftJsonAstDematerializerFuns {
  import net.liftweb.json.JsonDSL._

  val mapString = (value: String) => JString(value)
  val mapBoolean = (value: Boolean) => JBool(value)
  val mapInt = (value: Int) => JInt(value)
  val mapBigInt = (value: BigInt) => JString(value.toString)
  val mapFloatingPoint = (value: Double) => JDouble(value)
  val mapBigDecimal = (value: BigDecimal) => JString(value.toString)
  val mapDateTime = (value: DateTime) => JString(value.toString)
  val mapUuid = (value: _root_.java.util.UUID) => JString(value.toString)

  def primitiveMapperByType[A](implicit m: Manifest[A]): AlmValidation[A => JValue] = {
    val t = m.erasure
    if (t == classOf[String])
      (mapString).asInstanceOf[A => JValue].success
    else if (t == classOf[Boolean])
      (mapBoolean).asInstanceOf[A => JValue].success
    else if (t == classOf[Byte])
      ((x: Byte) => mapInt(x)).asInstanceOf[A => JValue].success
    else if (t == classOf[Int])
      mapInt.asInstanceOf[A => JValue].success
    else if (t == classOf[Long])
      ((x: Long) => mapInt(x.toInt)).asInstanceOf[A => JValue].success
    else if (t == classOf[BigInt])
      (mapBigInt).asInstanceOf[A => JValue].success
    else if (t == classOf[Float])
      ((x: Float) => mapFloatingPoint(x)).asInstanceOf[A => JValue].success
    else if (t == classOf[Double])
      (mapFloatingPoint).asInstanceOf[A => JValue].success
    else if (t == classOf[BigDecimal])
      (mapBigDecimal).asInstanceOf[A => JValue].success
    else if (t == classOf[DateTime])
      (mapDateTime).asInstanceOf[A => JValue].success
    else if (t == classOf[_root_.java.util.UUID])
      (mapUuid).asInstanceOf[A => JValue].success
    else
      UnspecifiedProblem("No mapper found for %s".format(t.getName())).failure
  }

  def mapperForAny(lookupFor: Any): AlmValidation[Any => JValue] = {
    if (lookupFor.isInstanceOf[String])
      primitiveMapperByType[String].map(mapper => (x: Any) => mapper(x.asInstanceOf[String]))
    else if (lookupFor.isInstanceOf[Boolean])
      primitiveMapperByType[Boolean].map(mapper => (x: Any) => mapper(x.asInstanceOf[Boolean]))
    else if (lookupFor.isInstanceOf[Byte])
      primitiveMapperByType[Byte].map(mapper => (x: Any) => mapper(x.asInstanceOf[Byte]))
    else if (lookupFor.isInstanceOf[Int])
      primitiveMapperByType[Int].map(mapper => (x: Any) => mapper(x.asInstanceOf[Int]))
    else if (lookupFor.isInstanceOf[Long])
      primitiveMapperByType[Long].map(mapper => (x: Any) => mapper(x.asInstanceOf[Long]))
    else if (lookupFor.isInstanceOf[BigInt])
      primitiveMapperByType[BigInt].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigInt]))
    else if (lookupFor.isInstanceOf[Float])
      primitiveMapperByType[Float].map(mapper => (x: Any) => mapper(x.asInstanceOf[Float]))
    else if (lookupFor.isInstanceOf[Double])
      primitiveMapperByType[Double].map(mapper => (x: Any) => mapper(x.asInstanceOf[Double]))
    else if (lookupFor.isInstanceOf[BigDecimal])
      primitiveMapperByType[BigDecimal].map(mapper => (x: Any) => mapper(x.asInstanceOf[BigDecimal]))
    else if (lookupFor.isInstanceOf[DateTime])
      primitiveMapperByType[DateTime].map(mapper => (x: Any) => mapper(x.asInstanceOf[DateTime]))
    else if (lookupFor.isInstanceOf[_root_.java.util.UUID])
      primitiveMapperByType[_root_.java.util.UUID].map(mapper => (x: Any) => mapper(x.asInstanceOf[_root_.java.util.UUID]))
    else
      UnspecifiedProblem("No mapper found for %s".format(lookupFor.getClass.getName())).failure
  }

  def createKeyValuePair(kv: (JValue, JValue)): JValue = {
    ("k" -> kv._1) ~ ("v" -> kv._2)
  }

  def foldKeyValuePairs(items: scala.collection.immutable.Iterable[(JValue, JValue)])(implicit functionObjects: HasFunctionObjects): AlmValidation[JArray] =
    functionObjects.getChannelFolder[JValue, JArray](RiftJson()).bind(folder =>
      functionObjects.getMAFunctions[scala.collection.immutable.Iterable].bind(fo =>
        folder.fold(items.map(x => createKeyValuePair(x)).seq)(fo)))
}

class ToLiftJsonAstDematerializer(val state: List[JField], val path: List[String], protected val divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects) extends BaseDematerializer[DimensionLiftJsonAst](classOf[DimensionLiftJsonAst]) {
  val channel = RiftJson()
  val toolGroup = ToolGroupLiftJson()

  import LiftJsonAstDematerializerFuns._

  protected def spawnNew(path: List[String]): AlmValidation[ToLiftJsonAstDematerializer] =
    ToLiftJsonAstDematerializer.apply(path, divertBlob).success
  
  def dematerialize: AlmValidation[DimensionLiftJsonAst] = DimensionLiftJsonAst(JObject(state.reverse)).success
  private def dematerializeInternal: JValue = JObject(state.reverse)
  private def getJValue(demat: Dematerializer[DimensionLiftJsonAst]) =
    demat.asInstanceOf[ToLiftJsonAstDematerializer].dematerializeInternal

  def addField(ident: String, value: JValue): ToLiftJsonAstDematerializer =
    ToLiftJsonAstDematerializer(JField(ident, value) :: state, path, divertBlob)

  def ifNoneAddNull[T](ident: String, valueOpt: Option[T], ifNotNull: (String, T) => AlmValidation[Dematerializer[DimensionLiftJsonAst]]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    option.cata(valueOpt)(ifNotNull(ident, _), addField(ident, JNull).success)

  def addString(ident: String, aValue: String) = addField(ident, mapString(aValue)).success
  def addOptionalString(ident: String, anOptionalValue: Option[String]) = ifNoneAddNull(ident: String, anOptionalValue, addString)

  def addBoolean(ident: String, aValue: Boolean) = addField(ident, mapBoolean(aValue)).success
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]) = ifNoneAddNull(ident: String, anOptionalValue, addBoolean)

  def addByte(ident: String, aValue: Byte) = addField(ident, mapInt(aValue)).success
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]) = ifNoneAddNull(ident: String, anOptionalValue, addByte)
  def addInt(ident: String, aValue: Int) = addField(ident, mapInt(aValue)).success
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]) = ifNoneAddNull(ident: String, anOptionalValue, addInt)
  def addLong(ident: String, aValue: Long) = addField(ident, mapInt(aValue.toInt)).success
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]) = ifNoneAddNull(ident: String, anOptionalValue, addLong)
  def addBigInt(ident: String, aValue: BigInt) = addField(ident, mapBigInt(aValue)).success
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]) = ifNoneAddNull(ident: String, anOptionalValue, addBigInt)

  def addFloat(ident: String, aValue: Float) = addField(ident, mapFloatingPoint(aValue)).success
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]) = ifNoneAddNull(ident: String, anOptionalValue, addFloat)
  def addDouble(ident: String, aValue: Double) = addField(ident, mapFloatingPoint(aValue)).success
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]) = ifNoneAddNull(ident: String, anOptionalValue, addDouble)
  def addBigDecimal(ident: String, aValue: BigDecimal) = addField(ident, mapBigDecimal(aValue)).success
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]) = ifNoneAddNull(ident: String, anOptionalValue, addBigDecimal)

  def addByteArray(ident: String, aValue: Array[Byte]) =
  	addField(ident, JArray(aValue.map(v => JInt(v.toInt)).toList)).success
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addByteArray)
  def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]) = 
    addString(ident, org.apache.commons.codec.binary.Base64.encodeBase64String(aValue))
  def addOptionalBase64EncodedByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addBase64EncodedByteArray)
  def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]) = addBase64EncodedByteArray(ident, aValue)
  def addOptionalByteArrayBlobEncoded(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addByteArrayBlobEncoded)
  
  def addBlob(ident: String, aValue: Array[Byte]) = 
    getDematerializedBlob(ident, aValue).bind(blobDemat =>
      blobDemat.dematerialize.bind(dim =>
        addField(ident, dim.manifestation).success))
        
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]) = ifNoneAddNull(ident: String, anOptionalValue, addBlob)

  def addDateTime(ident: String, aValue: org.joda.time.DateTime) = addField(ident, mapDateTime(aValue)).success
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = ifNoneAddNull(ident: String, anOptionalValue, addDateTime)

  def addUri(ident: String, aValue: _root_.java.net.URI) = addString(ident, aValue.toString())
  def addOptionalUri(ident: String, anOptionalValue: Option[_root_.java.net.URI]) = ifNoneAddNull(ident: String, anOptionalValue, addUri)
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID) = addField(ident, mapUuid(aValue)).success
  def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]) = ifNoneAddNull(ident: String, anOptionalValue, addUuid)

  def addJson(ident: String, aValue: String) =
    try {
      val parsed = parse(aValue)
      addField(ident, parsed).success
    } catch {
      case exn => ParsingProblem("Could not parse JSON for field '%s'".format(ident), input = Some(aValue), cause = Some(CauseIsThrowable(exn))).failure
    }

  def addOptionalJson(ident: String, anOptionalValue: Option[String]) = ifNoneAddNull(ident: String, anOptionalValue, addJson)

  def addXml(ident: String, aValue: scala.xml.Node) = addField(ident, mapString(aValue.toString)).success
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = ifNoneAddNull(ident: String, anOptionalValue, addXml)

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[ToLiftJsonAstDematerializer] =
    decomposeWithDecomposer(ident :: Nil)(decomposer)(aComplexType).map(jValue => addField(ident, jValue))

  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, anOptionalComplexType, addComplexType(decomposer))

  def addComplexTypeFixed[U <: AnyRef](ident: String, aComplexType: U)(implicit mU: Manifest[U]): AlmValidation[ToLiftJsonAstDematerializer] =
    hasDecomposers.getDecomposer[U].bind(decomposer => addComplexType(decomposer)(ident, aComplexType))

  def addOptionalComplexTypeFixed[U <: AnyRef](ident: String, anOptionalComplexType: Option[U])(implicit mU: Manifest[U]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, anOptionalComplexType, (x: String, y: U) => addComplexTypeFixed(x, y))
    
  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[ToLiftJsonAstDematerializer] =
    hasDecomposers.getDecomposerForAny(aComplexType).bind(decomposer =>
      addComplexType(decomposer)(ident, aComplexType))
      
  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, anOptionalComplexType, (x: String, y: U) => addComplexType(x, y))


  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToLiftJsonAstDematerializer] =
    primitiveMapperByType[A].bind(map =>
      MAFuncs.map(ma)(x => map(x)).bind(mcord =>
        MAFuncs.fold(this.channel)(mcord)(hasFunctionObjects, mM, manifest[JValue], manifest[JArray])).map(jarray =>
        addField(ident, jarray)))

  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addPrimitiveMA(x, y))

  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToLiftJsonAstDematerializer] =
    MAFuncs.mapiV(ma)((x, idx) => decomposeWithDecomposer(idx :: ident :: Nil)(decomposer)(x)).bind(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[JValue], manifest[JArray])).map(jarray =>
      addField(ident, jarray))

  def addOptionalComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addComplexMA(decomposer)(x, y))

  def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToLiftJsonAstDematerializer] =
    hasDecomposers.getDecomposer[A].bind(decomposer => addComplexMA(decomposer)(ident, ma))

  def addOptionalComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addComplexMAFixed(x, y))

  def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToLiftJsonAstDematerializer] =
    MAFuncs.mapiV(ma)((a, idx) => mapWithComplexDecomposerLookUp(idx, ident)(a)).bind(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[JValue], manifest[JArray])).map(jarray =>
      addField(ident, jarray))

  def addOptionalComplexMALoose[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addComplexMALoose(x, y))

  def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[ToLiftJsonAstDematerializer] =
    MAFuncs.mapiV(ma)((a, idx) => mapWithPrimitiveAndComplexDecomposerLookUp(idx, ident)(a)).bind(complex =>
      MAFuncs.fold(RiftJson())(complex)(hasFunctionObjects, mM, manifest[JValue], manifest[JArray])).map(jarray =>
      addField(ident, jarray))

  def addOptionalMA[M[_], A <: Any](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, ma, (x: String, y: M[A]) => addMA(x, y))

  def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToLiftJsonAstDematerializer] =
    (TypeHelpers.isPrimitiveType(mA.erasure), TypeHelpers.isPrimitiveType(mB.erasure)) match {
      case (true, true) =>
        primitiveMapperByType[A].bind(mapA =>
          primitiveMapperByType[B].map(mapB =>
            aMap.map {
              case (a, b) => (mapA(a), mapB(b))
            }).bind(items =>
            foldKeyValuePairs(items)).map(jarray =>
            addField(ident, jarray)))
      case (false, true) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not create primitive map for %s: B(%s) is not a primitive type".format(ident, mB.erasure.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not create primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.erasure.getName(), mB.erasure.getName())).failure
    }

  def addOptionalPrimitiveMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addMA(x, y))

  def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToLiftJsonAstDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      primitiveMapperByType[A].map(mapA =>
        aMap.map {
          case (a, b) =>
            decomposeWithDecomposer("[" + a.toString + "]" :: ident :: Nil)(decomposer)(b).map(jvalue =>
              (mapA(a), jvalue)).toAgg
        }).bind(x =>
        x.toList.sequence[AlmValidationAP, (JValue, JValue)]).bind(items =>
        foldKeyValuePairs(items)).map(jarray =>
        addField(ident, jarray)),
      UnspecifiedProblem("Could not create primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addOptionalComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addComplexMap(decomposer)(x, y))

  def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToLiftJsonAstDematerializer] =
    hasDecomposers.getDecomposer[B].bind(decomposer => addComplexMap[A, B](decomposer)(ident, aMap))

  def addOptionalComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addComplexMapFixed(x, y))

  def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToLiftJsonAstDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      primitiveMapperByType[A].bind(mapA =>
        aMap.toList.map {
          case (a, b) =>
            mapWithComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b => (mapA(a), b)).toAgg
        }.sequence.map(_.toMap).bind(items =>
          foldKeyValuePairs(items)).map(jarray =>
          addField(ident, jarray))),
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addOptionalComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addComplexMapLoose(x, y))

  def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[ToLiftJsonAstDematerializer] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      primitiveMapperByType[A].bind(mapA =>
        aMap.toList.map {
          case (a, b) =>
            mapWithPrimitiveAndComplexDecomposerLookUp("[" + a.toString + "]", ident)(b).map(b => (mapA(a), b)).toAgg
        }.sequence.map(_.toMap).bind(items =>
          foldKeyValuePairs(items)).map(jarray =>
          addField(ident, jarray))),
      UnspecifiedProblem("Could not create complex map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def addOptionalMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    ifNoneAddNull(ident: String, aMap, (x: String, y: Map[A, B]) => addMap(x, y))

  def addTypeDescriptor(descriptor: TypeDescriptor) = addString(TypeDescriptor.defaultKey, descriptor.toString)

  private def decomposeWithDecomposer[T <: AnyRef](idxIdent: List[String])(decomposer: Decomposer[T])(what: T): AlmValidation[JValue] =
    decomposer.decompose(what)(ToLiftJsonAstDematerializer(idxIdent ++ path, divertBlob)).map(demat => getJValue(demat))

  private def mapWithComplexDecomposerLookUp(idx : String, ident: String)(toDecompose: AnyRef): AlmValidation[JValue] =
    hasDecomposers.getRawDecomposerForAny(toDecompose).bind(decomposer =>
      spawnNew(idx :: ident :: path).bind(freshDemat =>
        decomposer.decomposeRaw(toDecompose)(freshDemat).map(x => JObject(x.asInstanceOf[ToLiftJsonAstDematerializer].state.reverse))))

  private def mapWithPrimitiveAndComplexDecomposerLookUp(idx : String, ident: String)(toDecompose: Any): AlmValidation[JValue] =
    boolean.fold(
      TypeHelpers.isPrimitiveValue(toDecompose),
      mapperForAny(toDecompose).map(mapper => mapper(toDecompose)),
      toDecompose match {
        case toDecomposeAsAnyRef: AnyRef =>
          mapWithComplexDecomposerLookUp(idx, ident)(toDecomposeAsAnyRef)
        case x =>
          UnspecifiedProblem("The type '%s' is not supported for dematerialization. The ident was '%s'".format(x.getClass.getName(), ident)).failure
      })
}

object ToLiftJsonAstDematerializer extends DematerializerFactory[DimensionLiftJsonAst] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionLiftJsonAst]
  val toolGroup = ToolGroupLiftJson()
  def apply(path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToLiftJsonAstDematerializer = apply(Nil, path, divertBlob)
  def apply(state: List[JField], path: List[String], divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): ToLiftJsonAstDematerializer = new ToLiftJsonAstDematerializer(state, path, divertBlob)
  def createDematerializer(divertBlob: BlobDivert)(implicit hasDecomposers: HasDecomposers, hasFunctionObjects: HasFunctionObjects): AlmValidation[Dematerializer[DimensionLiftJsonAst]] =
    apply(Nil, Nil, divertBlob).success
}