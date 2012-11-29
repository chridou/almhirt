package riftwarp.impl.rematerializers

import scala.util.parsing.json._
import scalaz._, Scalaz._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._

object FromJsonMapRematerializationArrayFuns {
  def getRematerializerFor[A](key: String)(implicit mA: Manifest[A]): AlmValidation[Any => AlmValidation[A]] = {
    if (mA.erasure.isAssignableFrom(classOf[String]))
      ((x: Any) => almCast[String](x)).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[Boolean]))
      ((x: Any) => almCast[Boolean](x)).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[Byte]))
      ((x: Any) => almCast[Double](x).map(_.toByte)).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[Int]))
      ((x: Any) => almCast[Double](x).map(_.toInt)).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[Long]))
      ((x: Any) => almCast[Double](x).map(_.toLong)).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[BigInt]))
      ((x: Any) => almCast[String](x).bind(parseBigIntAlm(_, key))).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[Float]))
      ((x: Any) => almCast[Double](x).map(_.toFloat)).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[Double]))
      ((x: Any) => almCast[Double](x)).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[BigDecimal]))
      ((x: Any) => almCast[String](x).bind(parseDecimalAlm(_, key))).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[org.joda.time.DateTime]))
      ((x: Any) => almCast[String](x).bind(parseDateTimeAlm(_, key))).asInstanceOf[Any => AlmValidation[A]].success
    else if (mA.erasure.isAssignableFrom(classOf[_root_.java.util.UUID]))
      ((x: Any) => almCast[String](x).bind(parseUuidAlm(_, key))).asInstanceOf[Any => AlmValidation[A]].success
    else
      UnspecifiedProblem("No primitive rematerializer found for '%s'".format(mA.erasure.getName())).failure
  }
}

class FromJsonMapRematerializationArray(jsonMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects) extends RematerializationArrayBasedOnOptionGetters {
  import FromJsonMapRematerializationArrayFuns._
  private def get(key: String): Option[Any] =
    jsonMap.get(key).flatMap(v => if (v == null) None else Some(v))

  def tryGetString(ident: String) = option.cata(get(ident))(almCast[String](_).map(Some(_)), None.success)

  def tryGetBoolean(ident: String) = option.cata(get(ident))(almCast[Boolean](_).map(Some(_)), None.success)

  def tryGetByte(ident: String) = option.cata(get(ident))(almCast[Double](_).map(x => Some(x.toByte)), None.success)
  def tryGetInt(ident: String) = option.cata(get(ident))(almCast[Double](_).map(x => Some(x.toInt)), None.success)
  def tryGetLong(ident: String) = option.cata(get(ident))(almCast[Double](_).map(x => Some(x.toLong)), None.success)
  def tryGetBigInt(ident: String) = option.cata(get(ident))(almCast[String](_).bind(parseBigIntAlm(_, ident)).map(Some(_)), None.success)

  def tryGetFloat(ident: String) = option.cata(get(ident))(almCast[Double](_).map(x => Some(x.toFloat)), None.success)
  def tryGetDouble(ident: String) = option.cata(get(ident))(almCast[Double](_).map(Some(_)), None.success)
  def tryGetBigDecimal(ident: String) = option.cata(get(ident))(almCast[String](_).bind(parseDecimalAlm(_, ident)).map(Some(_)), None.success)

  def tryGetByteArray(ident: String) =
    option.cata(get(ident))(almCast[List[Double]](_).map(x => Some(x.toArray.map(_.toByte))), None.success)
  def tryGetBlob(ident: String) =
    option.cata(get(ident))(almCast[String](_).bind(parseBase64Alm(_, ident).map(Some(_))), None.success)

  def tryGetDateTime(ident: String) = option.cata(get(ident))(almCast[String](_).bind(parseDateTimeAlm(_, ident)).map(Some(_)), None.success)

  def tryGetUuid(ident: String) = option.cata(get(ident))(almCast[String](_).bind(parseUuidAlm(_, ident)).map(Some(_)), None.success)

  def tryGetJson(ident: String) = option.cata(get(ident))(almCast[String](_).map(Some(_)), None.success)
  def tryGetXml(ident: String) = option.cata(get(ident))(almCast[String](_).bind(parseXmlAlm(_, ident)).map(Some(_)), None.success)

  def tryGetComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]) =
    get(ident) match {
      case Some(elem) =>
        almCast[Map[String, Any]](elem).bind { elemAsMap =>
          FromJsonMapRematerializationArray.createRematerializationArray(DimensionStdLibJsonMap(elemAsMap)).bind(rematerializationArray =>
            recomposer.recompose(rematerializationArray))
        }.map(res =>
          Some(res))
      case None =>
        None.success
    }

  def tryGetComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]] = {
    get(ident) match {
      case Some(elem) =>
        almCast[Map[String, Any]](elem).bind { elemAsMap =>
          FromJsonMapRematerializationArray.createRematerializationArray(DimensionStdLibJsonMap(elemAsMap)).bind { rematerializationArray =>
            rematerializationArray.tryGetTypeDescriptor.map {
              case Some(td) => td
              case None => TypeDescriptor(m.erasure)
            }.bind(td =>
              hasRecomposers.tryGetRecomposer[T](td) match {
                case Some(recomposer) => recomposer.recompose(rematerializationArray)
                case None => UnspecifiedProblem("No recomposer found for ident '%s' and type descriptor '%s'".format(ident, td)).failure
              }).map(res => Some(res))
          }
        }
      case None =>
        None.success
    }
  }

  def tryGetPrimitiveMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    option.cata(get(ident))(
      elem =>
        boolean.fold(
          elem.isInstanceOf[List[_]],
          functionObjects.getConvertsMAToNA[List, M].bind(converterToN =>
            getRematerializerFor[A](ident).bind(rmForA =>
              almCast[List[Any]](elem).bind(lx =>
                computeSafely(lx.map(rmForA(_).toAgg).sequence).bind(la =>
                  converterToN.convert[A](la)).map(Some(_))))),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type List[_]".format(ident)).failure),
      None.success)

  def tryGetComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    option.cata(get(ident))(
      mx =>
        boolean.fold(
          mx.isInstanceOf[List[_]],
          functionObjects.getConvertsMAToNA[List, M].bind(converter =>
            computeSafely {
              val validations =
                mx.asInstanceOf[List[Any]].map(elem =>
                  FromJsonMapRematerializationArray.createRematerializationArray(DimensionStdLibJsonMap(elem.asInstanceOf[Map[String, Any]])).bind(remat =>
                    recomposer.recompose(remat))).map(_.toAgg)
              val sequenced = validations.sequence.bind(converter.convert(_))
              sequenced.map(x => Some(x))
            }),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type List[Any]".format(ident)).failure),
      None.success)

  def tryGetComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    hasRecomposers.getRecomposer[A](TypeDescriptor(mA.erasure)).bind(recomposer =>
      tryGetComplexMA[M, A](ident, recomposer))

  def tryGetComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    option.cata(get(ident))(
      mx =>
        boolean.fold(
          mx.isInstanceOf[List[_]],
          functionObjects.getConvertsMAToNA[List, M].bind(converter =>
            computeSafely {
              val validations =
                mx.asInstanceOf[List[Any]].map(elem =>
                  FromJsonMapRematerializationArray.createRematerializationArray(DimensionStdLibJsonMap(elem.asInstanceOf[Map[String, Any]])).bind(remat =>
                    remat.getTypeDescriptor.bind(typeDescriptor =>
                      hasRecomposers.getRawRecomposer(typeDescriptor).bind(recomposer =>
                        recomposer.recomposeRaw(remat).map(_.asInstanceOf[A]))))).map(_.toAgg)
              val sequenced = validations.sequence.bind(converter.convert(_))
              sequenced.map(x => Some(x))
            }),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type List[Any]".format(ident)).failure),
      None.success)

  def tryGetMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] = {
    def mapToA(what: Any): AlmValidation[A] =
      getRematerializerFor[A](ident).fold(
        prob =>
          if (classOf[Map[_, _]].isAssignableFrom(what.getClass))
            computeSafely {
            FromMapRematerializationArray.createRematerializationArray(DimensionRawMap(what.asInstanceOf[Map[String, Any]])).bind(remat =>
              remat.getTypeDescriptor.bind(typeDescriptor =>
                hasRecomposers.getRawRecomposer(typeDescriptor).bind(recomposer =>
                  recomposer.recomposeRaw(remat).map(_.asInstanceOf[A]))))
          }
          else
            UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is neither a primitive type nor a decomposer could be found. I was trying to decompose '%s'".format(ident, what.getClass.getName())).failure,
        rematPrimitive =>
          rematPrimitive(what))

    option.cata(get(ident))(
      mx =>
        boolean.fold(
          mM.erasure.isAssignableFrom(mx.getClass),
          functionObjects.getMAFunctions[M].bind(fo =>
            computeSafely {
              val validations = fo.map(mx.asInstanceOf[M[Map[String, Any]]])(mapToA)
              val sequenced = fo.sequenceValidations(fo.map(validations)(_.toAgg))
              sequenced.map(Some(_))
            }),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type M[_](%s[_]). It is of type '%s'".format(ident, mM.erasure.getName(), mx.getClass.getName())).failure),
      None.success)
  }

  def tryGetTypeDescriptor =
    option.cata(get(TypeDescriptor.defaultKey))(almCast[String](_).bind(TypeDescriptor.parse(_)).map(Some(_)), None.success)
}

object FromJsonMapRematerializationArray extends RematerializationArrayFactory[DimensionStdLibJsonMap] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionStdLibJsonMap].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(jsonMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): FromJsonMapRematerializationArray = new FromJsonMapRematerializationArray(jsonMap)
  def apply(jsonMap: DimensionStdLibJsonMap)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): FromJsonMapRematerializationArray = new FromJsonMapRematerializationArray(jsonMap.manifestation)
  def createRematerializationArray(from: DimensionStdLibJsonMap)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializationArray] = apply(from).success
}

object FromJsonStringRematerializationArray extends RematerializationArrayFactory[DimensionString] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionString].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(json: String)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializationArray] = {
    JSON.parseFull(json) match {
      case Some(map) =>
        almCast[Map[String, Any]](map).map(FromJsonMapRematerializationArray(_))
      case None =>
        ParsingProblem("Could not parse JSON", input = Some(json)).failure
    }
  }
  def apply(json: DimensionString)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializationArray] = apply(json.manifestation)
  def createRematerializationArray(from: DimensionString)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializationArray] =
    apply(from)
}

import scalaz.Cord
object FromJsonCordRematerializationArray extends RematerializationArrayFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(json: Cord)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializationArray] =
    FromJsonStringRematerializationArray.createRematerializationArray(DimensionString(json.toString))
  def apply(json: DimensionCord)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializationArray] =
    FromJsonStringRematerializationArray.createRematerializationArray(DimensionString(json.manifestation.toString))
  def createRematerializationArray(from: DimensionCord)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializationArray] =
    apply(from)
}