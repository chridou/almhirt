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

  def createTuple[A, B](rematA: (JValue, String) => AlmValidation[A])(mapB: Any => AlmValidation[B])(kv: JObject)(implicit m: Manifest[A]): AlmValidation[(A, B)] = {
    val k = try { Some(kv \ "k") } catch { case exn => None }
    val v = try { Some(kv \ "v") } catch { case exn => None }
    (k, v) match {
      case (Some(k), Some(v)) => rematA(k, "key").bind(k => mapB(v).map(v => (k, v)))
      case (None, Some(_)) => KeyNotFoundProblem("Can not create key value tuple because the key entry is missing").failure
      case (Some(_), None) => KeyNotFoundProblem("Can not create key value tuple because the value entry is missing").failure
      case (None, None) => KeyNotFoundProblem("Can not create key value tuple because bothe the key entry and the value entry are missing").failure
    }
  }

  def createTuples[A, B](mapB: Any => AlmValidation[B])(kvPairs: List[JObject])(implicit m: Manifest[A]): AlmValidation[List[(A, B)]] =
    computeSafely {
      getRematerializerForPrimitive[A].bind(rematA =>
        kvPairs.map(x => createTuple(rematA)(mapB)(x))
          .map(_.toAgg)
          .sequence[AlmValidationAP, (A, B)])
    }
}

class FromLiftJsonObjectRematerializationArray(jsonObj: JObject)(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects) extends RematerializationArrayBasedOnOptionGetters {
  import LiftJsonRematerializationFuns._
  private def get(key: String): AlmValidation[Option[JValue]] =
    try {
      jsonObj \ key match {
        case JNothing => None.success
        case JNull => None.success
        case something => Some(something).success
      }
    } catch {
      case exn => None.success
    }

  private def extractPrimitive[A](ident: String, extract: (JValue, String) => AlmValidation[A]): AlmValidation[Option[A]] =
    get(ident).bind(jvalueOpt =>
      option.cata(jvalueOpt)(
        s => extract(s, ident).map(Some(_)),
        None.success))

  private def getComplexViaJObject[A <: AnyRef](ident: String)(mapToA: (JObject, String) => AlmValidation[A]): AlmValidation[Option[A]] =
    get(ident).bind(jvalueOpt =>
      option.cata(jvalueOpt)(
        v =>
          (v match {
            case v: JObject => mapToA(v, ident)
            case x => UnspecifiedProblem("'%s' is not a JObject. Cannot create a complex type".format(x)).failure
          }).map(Some(_)),
        None.success))

  private def getMAViaJArray[M[_], A](ident: String)(mapToMA: (JArray, String) => AlmValidation[M[A]]): AlmValidation[Option[M[A]]] =
    get(ident).bind(jvalueOpt =>
      option.cata(jvalueOpt)(
        v =>
          (v match {
            case v: JArray => mapToMA(v, ident)
            case x => UnspecifiedProblem("'%s' is not a JArray. Cannot create an M[A]".format(x)).failure
          }).map(Some(_)),
        None.success))

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
    get(ident).bind(jvalueOpt =>
      option.cata(jvalueOpt)(
        jvalue => jvalue match {
          case JArray(aList) =>
            aList.map(elem =>
              extractByte(elem, ident).toAgg).sequence.map(l => Some(l.toArray))
          case x =>
            UnspecifiedProblem("Not a JArray").failure
        },
        None.success))

  def tryGetBlob(ident: String) =
    extractPrimitive(ident, (jvalue, ident) => extractString(jvalue, ident).bind(str => parseBase64Alm(str, ident)))

  def tryGetDateTime(ident: String) = extractPrimitive(ident, extractDateTime)

  def tryGetUuid(ident: String) = extractPrimitive(ident, extractUuid)

  def tryGetJson(ident: String) =
    get(ident).map(jvalueOpt => jvalueOpt.map(jvalue => compact(render(jvalue))))

  def tryGetXml(ident: String) =
    extractPrimitive(ident, (jvalue, ident) => extractString(jvalue, ident).bind(str => parseXmlAlm(str, ident)))

  def tryGetComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]) =
    getComplexViaJObject(ident)((jobject, ident) =>
      recomposer.recompose(FromLiftJsonObjectRematerializationArray(jobject)))

  def tryGetComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]] =
    getComplexViaJObject(ident)((jobject, ident) => {
      val remat = FromLiftJsonObjectRematerializationArray(jobject)
      hasRecomposers.lookUpFromRematerializationArray(remat, m.erasure).bind(recomposer =>
        recomposer.recomposeRaw(remat).map(x => x.asInstanceOf[T]))
    })

  def tryGetComplexTypeFixed[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]] =
    getComplexViaJObject(ident)((jobject, ident) => {
      val remat = FromLiftJsonObjectRematerializationArray(jobject)
      hasRecomposers.getRawRecomposer(m.erasure).bind(recomposer =>
        recomposer.recomposeRaw(remat).map(x => x.asInstanceOf[T]))
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
              FromLiftJsonObjectRematerializationArray.fromJValue(elem).toAgg.bind(remat =>
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
              FromLiftJsonObjectRematerializationArray.fromJValue(elem).toAgg.bind(remat =>
                hasRecomposers.lookUpFromRematerializationArray(remat).toAgg.bind(recomposer =>
                  recomposer.recomposeRaw(remat).map(_.asInstanceOf[A]).toAgg))).sequence[AlmValidationAP, A].bind(la =>
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
        
  def tryGetTypeDescriptor =
    tryGetString(TypeDescriptor.defaultKey).bind(opt => opt.map(str => TypeDescriptor.parse(str)).validationOut)

  private def mapToAny[A](ident: String)(what: JValue)(implicit m: Manifest[A]): AlmValidation[A] =
    getRematerializerForPrimitive[A].fold(
      prob =>
        FromLiftJsonObjectRematerializationArray.fromJValue(what).bind(remat =>
          hasRecomposers.lookUpFromRematerializationArray(remat, m.erasure).bind(recomposer =>
            recomposer.recomposeRaw(remat).map(_.asInstanceOf[A]))),
      rematPrimitive =>
        rematPrimitive(what, ident))

}

object FromLiftJsonObjectRematerializationArray {
  def apply(jobject: JObject)(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects) = new FromLiftJsonObjectRematerializationArray(jobject)
  def fromJValue(jValue: JValue)(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects): AlmValidation[FromLiftJsonObjectRematerializationArray] =
    jValue match {
      case jo: JObject => apply(jo).success
      case x => UnspecifiedProblem("Can only create a FromLiftJsonObjectRematerializationArray from JObject").failure
    }
}