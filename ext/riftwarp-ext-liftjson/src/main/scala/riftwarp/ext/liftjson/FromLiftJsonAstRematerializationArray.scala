package riftwarp.ext.liftjson

import scalaz._, Scalaz._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._
import net.liftweb.json._
import _root_.java.lang.{ Integer => JavaInteger, Long => JavaLong, Short => JavaShort, Byte => JavaByte, Boolean => JavaBoolean, Double => JavaDouble, Float => JavaFloat }

object LiftJsonRematerializationFuns {
  implicit val formats = DefaultFormats

  val extractString = (jValue: JValue, key: String) => inTryCatch(jValue.extract[String])
  val extractBoolean = (jValue: JValue, key: String) => inTryCatch(jValue.extract[Boolean])
  val extractByte = (jValue: JValue, key: String) => inTryCatch(jValue.extract[Byte])
  val extractInt = (jValue: JValue, key: String) => inTryCatch(jValue.extract[Int])
  val extractLong = (jValue: JValue, key: String) => inTryCatch(jValue.extract[Long])
  val extractBigInt = (jValue: JValue, key: String) => inTryCatch(jValue.extract[String]).bind(x => parseBigIntAlm(x, key))
  val extractFloat = (jValue: JValue, key: String) => inTryCatch(jValue.extract[Float])
  val extractDouble = (jValue: JValue, key: String) => inTryCatch(jValue.extract[Double])
  val extractBigDecimal = (jValue: JValue, key: String) => inTryCatch(jValue.extract[String]).bind(x => parseDecimalAlm(x, key))
  val extractDateTime = (jValue: JValue, key: String) => inTryCatch(jValue.extract[String]).bind(x => parseDateTimeAlm(x, key))
  val extractUuid = (jValue: JValue, key: String) => inTryCatch(jValue.extract[String]).bind(x => parseUuidAlm(x, key))

  def getRematerializerForPrimitive[A](implicit mA: Manifest[A]): AlmValidation[(JValue, String) => AlmValidation[A]] = {
    if (mA.erasure == classOf[String])
      extractString.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[Symbol])
      extractString.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[Boolean])
      extractBoolean.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[JavaBoolean])
      extractBoolean.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[Byte])
      extractByte.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[JavaByte])
      extractByte.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[Int])
      extractInt.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[JavaInteger])
      extractInt.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[Long])
      extractLong.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[JavaLong])
      extractLong.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[BigInt])
      extractBigInt.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[Float])
      extractFloat.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[JavaFloat])
      extractFloat.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[Double])
      extractDouble.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[JavaDouble])
      extractDouble.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[BigDecimal])
      extractBigDecimal.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[org.joda.time.DateTime])
      extractDateTime.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else if (mA.erasure == classOf[_root_.java.util.UUID])
      extractUuid.asInstanceOf[(JValue, String) => AlmValidation[A]].success
    else
      UnspecifiedProblem("No primitive rematerializer found for '%s'".format(mA.erasure.getName())).failure
  }

  def createTuple[A, B](rematA: (JValue, String) => AlmValidation[A])(mapB: (JValue, String) => AlmValidation[B])(kv: JObject)(implicit m: Manifest[A]): AlmValidation[(A, B)] = {
    val k = try { Some(kv \ "k") } catch { case exn => None }
    val v = try { Some(kv \ "v") } catch { case exn => None }
    (k, v) match {
      case (Some(k), Some(v)) => rematA(k, "key").bind(k => mapB(v, "aKey").map(v => (k, v)))
      case (None, Some(_)) => KeyNotFoundProblem("Can not create key value tuple because the key entry is missing").failure
      case (Some(_), None) => KeyNotFoundProblem("Can not create key value tuple because the value entry is missing").failure
      case (None, None) => KeyNotFoundProblem("Can not create key value tuple because bothe the key entry and the value entry are missing").failure
    }
  }
}

class FromLiftJsonObjectRematerializationArray(jsonObj: JObject, protected val fetchBlobData: BlobFetch)(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects) extends RematerializationArrayWithBlobBlobFetch with RematerializationArrayBasedOnOptionGetters {
  import LiftJsonRematerializationFuns._
  import RecomposerFuns._
  private def get(ident: String): Option[JValue] =
    try {
      jsonObj \ ident match {
        case JNothing => None
        case JNull => None
        case something => Some(something)
      }
    } catch {
      case exn => None
    }

  protected def trySpawnNew(ident: String): AlmValidation[Option[RematerializationArray]] =
    option.cata(get(ident))(
      s => s match {
        case jObject: JObject =>
          Some(spawn(jObject)).success
        case x =>
          UnspecifiedProblem("Found a '%s' for ident %s but a JObject was required".format(x, ident)).failure
      },
      None.success)

  protected def spawn(from: JObject): RematerializationArray =
    FromLiftJsonObjectRematerializationArray(from, fetchBlobData)(hasRecomposers, functionObjects)

  protected def spawnFromJValue(from: JValue): AlmValidation[RematerializationArray] =
    FromLiftJsonObjectRematerializationArray.fromJValue(from, fetchBlobData)(hasRecomposers, functionObjects)
    
  private def tryGetJArraysAndCreateMap[A, B](ident: String)(mapB: (JValue, String) => AlmValidation[B])(implicit mA: Manifest[A]): AlmValidation[Option[Map[A, B]]] =
    try {
      jsonObj \ ident match {
        case JArray(arr) =>
          getRematerializerForPrimitive[A].bind(rematA =>
            arr.map {
              case jo: JObject => createTuple(rematA)(mapB)(jo).toAgg
              case notAJObject => AggregateProblem("JObject required. Found a '%s'".format(notAJObject)).failure
            }.sequence[AlmValidationAP, (A, B)].map(lObj =>
              Some(lObj.toMap)))
        case something => UnspecifiedProblem("JArray required. Found a '%s'".format(something)).failure
      }
    } catch {
      case exn => None.success
    }

  private def extractPrimitive[A](ident: String, extract: (JValue, String) => AlmValidation[A]): AlmValidation[Option[A]] =
    option.cata(get(ident))(
      s => extract(s, ident).map(Some(_)),
      None.success)

  private def getComplexViaJObject[A <: AnyRef](ident: String)(mapToA: (JObject, String) => AlmValidation[A]): AlmValidation[Option[A]] =
    option.cata(get(ident))(
      v =>
        (v match {
          case v: JObject => mapToA(v, ident)
          case x => UnspecifiedProblem("'%s' is not a JObject. Cannot create a complex type".format(x)).failure
        }).map(Some(_)),
      None.success)

  private def getMAViaJArray[M[_], A](ident: String)(mapToMA: (JArray, String) => AlmValidation[M[A]]): AlmValidation[Option[M[A]]] =
    option.cata(get(ident))(
      v =>
        (v match {
          case v: JArray => mapToMA(v, ident)
          case x => UnspecifiedProblem("'%s' is not a JArray. Cannot create an M[A]".format(x)).failure
        }).map(Some(_)),
      None.success)

  def tryGetString(ident: String) = extractPrimitive(ident, extractString)

  def tryGetBoolean(ident: String) = extractPrimitive(ident, extractBoolean)

  def tryGetByte(ident: String) = extractPrimitive(ident, extractByte)
  def tryGetInt(ident: String) = extractPrimitive(ident, extractInt)
  def tryGetLong(ident: String) = extractPrimitive(ident, extractLong)
  def tryGetBigInt(ident: String) = extractPrimitive(ident, extractBigInt)

  def tryGetFloat(ident: String) = extractPrimitive(ident, extractFloat)
  def tryGetDouble(ident: String) = extractPrimitive(ident, extractDouble)
  def tryGetBigDecimal(ident: String) = extractPrimitive(ident, extractBigDecimal)

  def tryGetByteArray(ident: String) =
    option.cata(get(ident))(
      jvalue => jvalue match {
        case JArray(aList) =>
          aList.map(elem =>
            extractByte(elem, ident).toAgg).sequence.map(l => Some(l.toArray))
        case x =>
          UnspecifiedProblem("Not a JArray").failure
      },
      None.success)
  def tryGetByteArrayFromBase64Encoding(ident: String) =
     extractPrimitive(ident, extractString).bind(strOpt => strOpt.map(str =>parseBase64Alm(str, ident)).validationOut)
     
  def tryGetByteArrayFromBlobEncoding(ident: String) = tryGetByteArrayFromBase64Encoding(ident)
      
  def tryGetDateTime(ident: String) = extractPrimitive(ident, extractDateTime)

  def tryGetUri(ident: String) = 
    extractPrimitive(ident, extractString).bind(strOpt => strOpt.map(str =>parseUriAlm(str, ident)).validationOut)
  
  def tryGetUuid(ident: String) = extractPrimitive(ident, extractUuid)

  def tryGetJson(ident: String) =
    get(ident).map(jvalue => compact(render(jvalue))).success

  def tryGetXml(ident: String) =
    extractPrimitive(ident, (jvalue, ident) => extractString(jvalue, ident).bind(str => parseXmlAlm(str, ident)))

   def tryGetBlob(ident: String): AlmValidation[Option[Array[Byte]]] =
    tryGetRematerializedBlob(ident)
    
  def tryGetComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]) =
    getComplexViaJObject(ident)((jobject, ident) =>
      recomposer.recompose(spawn(jobject)))

  def tryGetComplexTypeFixed[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]] =
    getComplexViaJObject(ident)((jobject, ident) => {
      recomposeWithLookedUpRawRecomposerFromTypeDescriptor(m.erasure)(spawn(jobject)).map(_.asInstanceOf[T])
    })

  def tryGetComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]] =
    getComplexViaJObject(ident)((jobject, ident) => {
      recomposeWithLookedUpRawRecomposerFromRematerializationArray(spawn(jobject), m.erasure).map(_.asInstanceOf[T])
    })

  def tryGetPrimitiveMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    functionObjects.getMAFunctions[M].bind(fo =>
      boolean.fold(
        fo.hasLinearCharacteristics,
        getMAViaJArray[M, A](ident)((jArray, ident) =>
          functionObjects.getConvertsMAToNA[List, M].bind(converterToN =>
            getRematerializerForPrimitive[A].bind(remat =>
              jArray.arr.map(elem => remat(elem, ident).toAgg).sequence.bind(la =>
                converterToN.convert(la))))),
        UnspecifiedProblem("Only linear M[A]s supported").failure))

  def tryGetComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    functionObjects.getMAFunctions[M].bind(fo =>
      boolean.fold(
        fo.hasLinearCharacteristics,
        getMAViaJArray[M, A](ident)((jArray, ident) =>
          functionObjects.getConvertsMAToNA[List, M].bind(converterToN =>
            jArray.arr.map(elem =>
              spawnFromJValue(elem).toAgg.bind(remat =>
                recomposer.recompose(remat).toAgg)).sequence[AlmValidationAP, A].bind(la =>
              converterToN.convert(la)))),
        UnspecifiedProblem("Only linear M[A]s supported").failure))

  def tryGetComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    hasRecomposers.getRecomposer[A](TypeDescriptor(mA.erasure)).bind(recomposer =>
      tryGetComplexMA[M, A](ident, recomposer))

  def tryGetComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]]): AlmValidation[Option[M[A]]] =
    functionObjects.getMAFunctions[M].bind(fo =>
      boolean.fold(
        fo.hasLinearCharacteristics,
        getMAViaJArray[M, A](ident)((jArray, ident) =>
          functionObjects.getConvertsMAToNA[List, M].bind(converterToN =>
            jArray.arr.map(elem =>
              spawnFromJValue(elem).bind(freshRemat => 
              recomposeWithLookedUpRawRecomposerFromRematerializationArray(freshRemat).map(_.asInstanceOf[A])).toAgg)
              .sequence[AlmValidationAP, A].bind(la =>
              converterToN.convert(la)))),
        UnspecifiedProblem("Only linear M[A]s supported").failure))

  def tryGetMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    functionObjects.getMAFunctions[M].bind(fo =>
      boolean.fold(
        fo.hasLinearCharacteristics,
        getMAViaJArray[M, A](ident)((jArray, ident) =>
          functionObjects.getConvertsMAToNA[List, M].bind(converterToN =>
            jArray.arr.map(elem =>
              mapToAny[A](ident)(elem).toAgg).sequence[AlmValidationAP, A].bind(la =>
              converterToN.convert(la)))),
        UnspecifiedProblem("Only linear M[A]s supported").failure))

  def tryGetPrimitiveMap[A, B](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Option[Map[A, B]]] =
    (TypeHelpers.isPrimitiveType(mA.erasure), TypeHelpers.isPrimitiveType(mB.erasure)) match {
      case (true, true) =>
        getRematerializerForPrimitive[B].bind(remat =>
          tryGetJArraysAndCreateMap[A, B](ident)(remat))
      case (false, true) => UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not rematerialize primitive map for %s: B(%s) is not a primitive type".format(ident, mB.erasure.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.erasure.getName(), mB.erasure.getName())).failure
    }

  def tryGetComplexMap[A, B <: AnyRef](ident: String, recomposer: Recomposer[B])(implicit mA: Manifest[A]): AlmValidation[Option[Map[A, B]]] =
    tryGetJArraysAndCreateMap[A, B](ident)((jValue, ident) =>
      spawnFromJValue(jValue).bind(remat =>
        recomposer.recompose(remat)))

  def tryGetComplexMapFixed[A, B <: AnyRef](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Option[Map[A, B]]] =
    hasRecomposers.getRecomposer[B](TypeDescriptor(mB.erasure)).bind(recomposer => tryGetComplexMap[A, B](ident, recomposer))

  def tryGetComplexMapLoose[A, B <: AnyRef](ident: String)(implicit mA: Manifest[A]): AlmValidation[Option[Map[A, B]]] =
    tryGetJArraysAndCreateMap[A, B](ident)((jValue, ident) =>
      spawnFromJValue(jValue).bind(remat =>
        recomposeWithLookedUpRawRecomposerFromRematerializationArray(remat).map(_.asInstanceOf[B])))

  def tryGetMap[A, B](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Option[Map[A, B]]] =
    tryGetJArraysAndCreateMap[A, B](ident)((jValue, ident) =>
      mapToAny[B](ident)(jValue))

  def tryGetTypeDescriptor =
    tryGetString(TypeDescriptor.defaultKey).bind(opt => opt.map(str => TypeDescriptor.parse(str)).validationOut)

  private def mapToAny[A](ident: String)(what: JValue)(implicit m: Manifest[A]): AlmValidation[A] =
    getRematerializerForPrimitive[A].fold(
      prob =>
        spawnFromJValue(what).bind(remat =>
          recomposeWithLookedUpRawRecomposerFromRematerializationArray(remat, m.erasure).map(_.asInstanceOf[A])),
      rematPrimitive =>
        rematPrimitive(what, ident))

}

object FromLiftJsonObjectRematerializationArray extends RematerializationArrayFactory[DimensionLiftJsonAst] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionLiftJsonAst]
  val toolGroup = ToolGroupLiftJson()
  def apply(jobject: JObject, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects) = new FromLiftJsonObjectRematerializationArray(jobject, fetchBlobs)
  def fromJValue(jValue: JValue, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects): AlmValidation[FromLiftJsonObjectRematerializationArray] =
    jValue match {
      case jo: JObject => apply(jo, fetchBlobs).success
      case x => UnspecifiedProblem("Can only create a FromLiftJsonObjectRematerializationArray from JObject").failure
    }
  def createRematerializationArray(from: DimensionLiftJsonAst, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromLiftJsonObjectRematerializationArray] =
    fromJValue(from.manifestation, fetchBlobs)

}