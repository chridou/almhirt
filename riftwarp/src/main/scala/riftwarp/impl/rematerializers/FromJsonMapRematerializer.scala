package riftwarp.impl.rematerializers

import language.higherKinds

import scala.reflect.ClassTag
import scala.util.parsing.json._
import scalaz._, Scalaz._
import scalaz.std._
import scalaz.syntax.validation
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._
import riftwarp.components._

private[rematerializers] object FromJsonMapRematerializerFuns {
  def getPrimitiveRematerializer[A](key: String, clazz: Class[_]): AlmValidation[Any => AlmValidation[A]] = {
    if (clazz == classOf[String])
      Success(((x: Any) => almCast[String](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.String])
      Success(((x: Any) => almCast[_root_.java.lang.String](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Boolean])
      Success(((x: Any) => almCast[Boolean](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Boolean])
      Success(((x: Any) => almCast[_root_.java.lang.Boolean](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Byte])
      Success(((x: Any) => almCast[Double](x).map(_.toByte)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Byte])
      Success(((x: Any) => almCast[Double](x).map(_.toByte)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Int])
      Success(((x: Any) => almCast[Double](x).map(_.toInt)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Integer])
      Success(((x: Any) => almCast[Double](x).map(_.toInt)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Long])
      Success(((x: Any) => almCast[Double](x).map(_.toLong)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Long])
      Success(((x: Any) => almCast[Double](x).map(_.toLong)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[BigInt])
      Success(((x: Any) => almCast[String](x).flatMap(parseBigIntAlm(_).withIdentifierOnFailure(key))).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Float])
      Success(((x: Any) => almCast[Double](x).map(_.toFloat)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Float])
      Success(((x: Any) => almCast[Double](x).map(_.toFloat)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[Double])
      Success(((x: Any) => almCast[Double](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.lang.Double])
      Success(((x: Any) => almCast[Double](x)).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[BigDecimal])
      Success(((x: Any) => almCast[String](x).flatMap(parseDecimalAlm(_).withIdentifierOnFailure(key))).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[org.joda.time.DateTime])
      Success(((x: Any) => almCast[String](x).flatMap(parseDateTimeAlm(_).withIdentifierOnFailure(key))).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.util.UUID])
      Success(((x: Any) => almCast[String](x).flatMap(parseUuidAlm(_).withIdentifierOnFailure(key))).asInstanceOf[Any => AlmValidation[A]])
    else if (clazz == classOf[_root_.java.net.URI])
      Success(((x: Any) => almCast[String](x).flatMap(parseUriAlm(_).withIdentifierOnFailure(key))).asInstanceOf[Any => AlmValidation[A]])
    else
      Failure(UnspecifiedProblem("No primitive rematerializer found for '%s'".format(clazz.getName())))
  }

  def getPrimitiveRematerializerFor[A](key: String)(implicit mA: ClassTag[A]): AlmValidation[Any => AlmValidation[A]] =
    getPrimitiveRematerializer[A](key, mA.runtimeClass)

  def createTuple[A, B](rematA: Any => AlmValidation[A])(mapB: Any => AlmValidation[B])(kv: List[Any])(implicit m: ClassTag[A]): AlmValidation[(A, B)] = {
    val (key, value) =
      kv match {
        case Nil => (None, None)
        case k :: v :: Nil => (Some(k), Some(v))
        case k :: Nil => (Some(k), None)
        case _ => (None, None)
      }

    (key, value) match {
      case (Some(k), Some(v)) => rematA(k).flatMap(k => mapB(v).map(v => (k, v)))
      case (None, Some(_)) => KeyNotFoundProblem("Can not create key value tuple because the key entry is missing").failure
      case (Some(_), None) => KeyNotFoundProblem("Can not create key value tuple because the value entry is missing").failure
      case (None, None) => KeyNotFoundProblem("Can not create key value tuple because bothe the key entry and the value entry are missing").failure
    }
  }

  def createTuples[A, B](mapB: Any => AlmValidation[B])(kvPairs: List[Any])(implicit m: ClassTag[A]): AlmValidation[List[(A, B)]] =
    computeSafely {
      getPrimitiveRematerializerFor[A]("key").flatMap(rematA =>
        kvPairs.map(x => createTuple(rematA)(mapB)(x.asInstanceOf[List[Any]]))
          .map(_.toAgg)
          .sequence[({ type l[a] = scalaz.Validation[AggregateProblem, a] })#l, (A, B)])
    }
}

class FromJsonMapRematerializer(jsonMap: Map[String, Any], protected val fetchBlobData: BlobFetch)(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects) extends RematerializerWithBlobBlobFetch with RematerializerBasedOnOptionGetters {
  import FromJsonMapRematerializerFuns._
  import funs.hasRecomposers._

  private def get(key: String): Option[Any] =
    jsonMap.get(key).flatMap(v => if (v == null) None else Some(v))

  protected def trySpawnNew(ident: String): AlmValidation[Option[Rematerializer]] =
    option.cata(get(ident))(
      s => s match {
        case aMap: Map[_, _] =>
          Some(spawnNew(aMap.asInstanceOf[Map[String, Any]])).success
        case x =>
          UnspecifiedProblem("Found a '%s' for ident %s but a Map was required".format(x, ident)).failure
      },
      None.success)

  protected def spawnNew(from: Map[String, Any]): Rematerializer =
    FromJsonMapRematerializer(from, fetchBlobData)(hasRecomposers, functionObjects)

  def tryGetString(ident: String) = option.cata(get(ident))(almCast[String](_).map(Some(_)), None.success)

  def tryGetBoolean(ident: String) = option.cata(get(ident))(almCast[Boolean](_).map(Some(_)), None.success)

  def tryGetByte(ident: String) = option.cata(get(ident))(almCast[Double](_).map(x => Some(x.toByte)), None.success)
  def tryGetInt(ident: String) = option.cata(get(ident))(almCast[Double](_).map(x => Some(x.toInt)), None.success)
  def tryGetLong(ident: String) = option.cata(get(ident))(almCast[Double](_).map(x => Some(x.toLong)), None.success)
  def tryGetBigInt(ident: String) = option.cata(get(ident))(almCast[String](_).flatMap(parseBigIntAlm(_).withIdentifierOnFailure(ident)).map(Some(_)), None.success)

  def tryGetFloat(ident: String) = option.cata(get(ident))(almCast[Double](_).map(x => Some(x.toFloat)), None.success)
  def tryGetDouble(ident: String) = option.cata(get(ident))(almCast[Double](_).map(Some(_)), None.success)
  def tryGetBigDecimal(ident: String) = option.cata(get(ident))(almCast[String](_).flatMap(parseDecimalAlm(_).withIdentifierOnFailure(ident)).map(Some(_)), None.success)

  def tryGetByteArray(ident: String) =
    option.cata(get(ident))(almCast[List[Double]](_).map(x => Some(x.toArray.map(_.toByte))), None.success)
  def tryGetByteArrayFromBase64Encoding(ident: String) =
    option.cata(get(ident))(almCast[String](_).flatMap(parseBase64Alm(_).withIdentifierOnFailure(ident)).map(Some(_)), None.success)
  def tryGetByteArrayFromBlobEncoding(ident: String) = tryGetByteArrayFromBase64Encoding(ident)

  def tryGetDateTime(ident: String) = option.cata(get(ident))(almCast[String](_).flatMap(parseDateTimeAlm(_).withIdentifierOnFailure(ident)).map(Some(_)), None.success)

  def tryGetUri(ident: String) = option.cata(get(ident))(almCast[String](_).flatMap(parseUriAlm(_).withIdentifierOnFailure(ident)).map(Some(_)), None.success)

  def tryGetUuid(ident: String) = option.cata(get(ident))(almCast[String](_).flatMap(parseUuidAlm(_).withIdentifierOnFailure(ident)).map(Some(_)), None.success)

  def tryGetJson(ident: String) = option.cata(get(ident))(almCast[String](_).map(Some(_)), None.success)
  def tryGetXml(ident: String) = option.cata(get(ident))(almCast[String](_).flatMap(parseXmlAlm(_).withIdentifierOnFailure(ident)).map(Some(_)), None.success)

  def tryGetBlob(ident: String): AlmValidation[Option[Array[Byte]]] =
    tryGetRematerializedBlob(ident)

  def tryGetComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]) =
    trySpawnNew(ident).flatMap(rematOpt =>
      option.cata(rematOpt)(
        remat => recomposer.recompose(remat).map(Some(_)),
        None.success))

  def tryGetComplexTypeFixed[T <: AnyRef](ident: String)(implicit m: ClassTag[T]): AlmValidation[Option[T]] =
    hasRecomposers.getRecomposer[T](m.runtimeClass).flatMap(recomposer =>
      tryGetComplexType[T](ident, recomposer))

  def tryGetComplexType[T <: AnyRef](ident: String)(implicit m: ClassTag[T]): AlmValidation[Option[T]] =
    trySpawnNew(ident).flatMap(rematOpt =>
      option.cata(rematOpt)(
        remat => hasRecomposers.lookUpFromRematerializer(remat, Some(m.runtimeClass)).flatMap(recomposer =>
          recomposer.recomposeRaw(remat).flatMap(x => almCast[T](x)).map(Some(_))),
        None.success))

  def tryGetPrimitiveMA[M[_], A](ident: String)(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    option.cata(get(ident))(
      elem =>
        boolean.fold(
          elem.isInstanceOf[List[_]],
          functionObjects.getConvertsMAToNA[List, M].flatMap(converterToN =>
            getPrimitiveRematerializerFor[A](ident).flatMap(rmForA =>
              almCast[List[Any]](elem).flatMap(lx =>
                computeSafely(lx.map(rmForA(_).toAgg).sequence).flatMap(la =>
                  converterToN.convert[A](la)).map(Some(_))))),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type List[_]".format(ident)).failure),
      None.success)

  def tryGetComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    option.cata(get(ident))(
      mx =>
        boolean.fold(
          mx.isInstanceOf[List[_]],
          functionObjects.getConvertsMAToNA[List, M].flatMap(converter =>
            computeSafely {
              val validations =
                mx.asInstanceOf[List[Any]].map(elem =>
                  recomposer.recompose(spawnNew(elem.asInstanceOf[Map[String, Any]]))).map(_.toAgg)
              val sequenced = validations.sequence.flatMap(converter.convert(_))
              sequenced.map(x => Some(x))
            }),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type List[Any]".format(ident)).failure),
      None.success)

  def tryGetComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    hasRecomposers.getRecomposer[A](RiftDescriptor(mA.runtimeClass)).flatMap(recomposer =>
      tryGetComplexMA[M, A](ident, recomposer))

  def tryGetComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    option.cata(get(ident))(
      mx =>
        boolean.fold(
          mx.isInstanceOf[List[_]],
          functionObjects.getConvertsMAToNA[List, M].flatMap(converter =>
            computeSafely {
              val validations =
                mx.asInstanceOf[List[Any]].map(elem =>
                  recomposeWithLookedUpRawRecomposerFromRematerializer(spawnNew(elem.asInstanceOf[Map[String, Any]])).map(_.asInstanceOf[A]).toAgg)
              val sequenced = validations.sequence.flatMap(converter.convert(_))
              sequenced.map(x => Some(x))
            }),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type List[Any]".format(ident)).failure),
      None.success)

  def tryGetMA[M[_], A](ident: String)(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    option.cata(get(ident))(
      mx =>
        boolean.fold(
          mM.runtimeClass.isAssignableFrom(mx.getClass),
          functionObjects.getMAFunctions[M].flatMap(fo =>
            computeSafely {
              val validations = fo.map(mx.asInstanceOf[M[Map[String, Any]]])(mapToAny[A](ident))
              val sequenced = fo.sequenceValidations(fo.map(validations)(_.toAgg))
              sequenced.map(Some(_))
            }),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type M[_](%s[_]). It is of type '%s'".format(ident, mM.runtimeClass.getName(), mx.getClass.getName())).failure),
      None.success)

  def tryGetPrimitiveMap[A, B](ident: String)(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Option[Map[A, B]]] =
    (TypeHelpers.isPrimitiveType(mA.runtimeClass), TypeHelpers.isPrimitiveType(mB.runtimeClass)) match {
      case (true, true) =>
        get(ident).map(any =>
          computeSafely {
            getPrimitiveRematerializerFor[B](ident).flatMap(rematB =>
              createTuples[A, B](rematB)(any.asInstanceOf[List[Any]]).map(tuples =>
                tuples.toMap.asInstanceOf[Map[A, B]]))
          }).validationOut
      case (false, true) => UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not rematerialize primitive map for %s: B(%s) is not a primitive type".format(ident, mB.runtimeClass.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.runtimeClass.getName(), mB.runtimeClass.getName())).failure
    }

  def tryGetComplexMap[A, B <: AnyRef](ident: String, recomposer: Recomposer[B])(implicit mA: ClassTag[A]): AlmValidation[Option[Map[A, B]]] = {
    def rematerialize(x: Any): AlmValidation[B] =
      recomposer.recompose(spawnNew(x.asInstanceOf[Map[String, Any]]))

    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      option.cata(get(ident))(
        any =>
          computeSafely {
            createTuples[A, B](rematerialize)(any.asInstanceOf[List[Any]]).map(tuples =>
              tuples.toMap.asInstanceOf[Map[A, B]])
          }.map(Some(_)),
        None.success),
      UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
  }

  def tryGetComplexMapFixed[A, B <: AnyRef](ident: String)(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Option[Map[A, B]]] =
    hasRecomposers.getRecomposer[B](RiftDescriptor(mB.runtimeClass)).flatMap(recomposer => tryGetComplexMap[A, B](ident, recomposer))

  def tryGetComplexMapLoose[A, B <: AnyRef](ident: String)(implicit mA: ClassTag[A]): AlmValidation[Option[Map[A, B]]] = {
    def rematerialize(x: Any): AlmValidation[B] =
      recomposeWithLookedUpRawRecomposerFromRematerializer(spawnNew(x.asInstanceOf[Map[String, Any]])).map(_.asInstanceOf[B])

    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      option.cata(get(ident))(
        any =>
          computeSafely {
            createTuples[A, B](rematerialize)(any.asInstanceOf[List[Any]]).map(tuples =>
              tuples.toMap.asInstanceOf[Map[A, B]])
          }.map(Some(_)),
        None.success),
      UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
  }

  def tryGetMap[A, B](ident: String)(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Option[Map[A, B]]] = {
    def rematerialize(x: Any): AlmValidation[B] = mapToAny[B](ident)(x)

    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      option.cata(get(ident))(
        any =>
          computeSafely {
            createTuples[A, B](rematerialize)(any.asInstanceOf[List[Any]]).map(tuples =>
              tuples.toMap.asInstanceOf[Map[A, B]])
          }.map(Some(_)),
        None.success),
      UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
  }

  def tryGetRiftDescriptor = tryGetComplexType(RiftDescriptor.defaultKey, riftwarp.serialization.common.RiftDescriptorRecomposer)

  private def mapToAny[A](ident: String)(what: Any)(implicit m: ClassTag[A]): AlmValidation[A] =
    getPrimitiveRematerializer[A](ident, what.getClass).fold(
      prob =>
        if (classOf[Map[_, _]].isAssignableFrom(what.getClass))
          computeSafely {
          recomposeWithLookedUpRawRecomposerFromRematerializer(spawnNew(what.asInstanceOf[Map[String, Any]]), m.runtimeClass).map(_.asInstanceOf[A])
        }
        else
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is neither a primitive type nor a decomposer could be found. I was trying to decompose '%s'".format(ident, what.getClass.getName())).failure,
      rematPrimitive =>
        rematPrimitive(what))

}

object FromJsonMapRematerializer extends RematerializerFactory[DimensionStdLibJsonMap] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionStdLibJsonMap].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(jsonMap: Map[String, Any], fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): FromJsonMapRematerializer = new FromJsonMapRematerializer(jsonMap, fetchBlobs)(hasRecomposers, hasFunctionObject)
  def apply(jsonMap: DimensionStdLibJsonMap, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): FromJsonMapRematerializer = apply(jsonMap.manifestation, fetchBlobs)(hasRecomposers, hasFunctionObject)
  def createRematerializer(from: DimensionStdLibJsonMap, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializer] = apply(from, fetchBlobs)(hasRecomposers, hasFunctionObject).success
}

object FromJsonStringRematerializer extends RematerializerFactory[DimensionString] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionString].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(json: String, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializer] = {
    def transformMap(m: Map[String, Any]) =
      m.transform {
        case (k, v) => resolveType(v)
      }

    def resolveType(input: Any): Any = input match {
      case JSONObject(data) => transformMap(data)
      case JSONArray(data) => data.map(resolveType)
      case x => x
    }
    // Always instantiate a new parser because the singleton from the standard library is not threadsafe!
    val parser = new scala.util.parsing.json.Parser
    parser.phrase(parser.root)(new parser.lexical.Scanner(json)) match {
      case parser.Success(result, _) =>
        result match {
          case JSONObject(data) =>
            FromJsonMapRematerializer(transformMap(data), fetchBlobs).success
          case x =>
            UnspecifiedProblem("'%s' is not valid for dematerializing. A Map[String, Any] is required".format(x)).failure
        }

      case parser.NoSuccess(msg, _) =>
        ParsingProblem(msg).withInput(json).failure
    }
  }

  //  def apply(json: DimensionString, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializer] = apply(json.manifestation, fetchBlobs)(hasRecomposers, hasFunctionObject)
  def createRematerializer(from: DimensionString, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializer] =
    apply(from.manifestation, fetchBlobs)(hasRecomposers, hasFunctionObject)
}

import scalaz.Cord
object FromJsonCordRematerializer extends RematerializerFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(json: Cord, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializer] =
    FromJsonStringRematerializer.createRematerializer(DimensionString(json.toString), fetchBlobs)(hasRecomposers, hasFunctionObject)
  def apply(json: DimensionCord, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializer] =
    FromJsonStringRematerializer.createRematerializer(DimensionString(json.manifestation.toString), fetchBlobs)(hasRecomposers, hasFunctionObject)
  def createRematerializer(from: DimensionCord, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromJsonMapRematerializer] =
    apply(from, fetchBlobs)(hasRecomposers, hasFunctionObject)
}