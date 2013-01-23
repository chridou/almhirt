package riftwarp.impl.rematerializers

import language.higherKinds
import scala.reflect.ClassTag
import scala.xml.Elem
import scalaz._, Scalaz._
import scalaz.std._
import almhirt.common._
import almhirt.syntax.xml._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._
import riftwarp.components._

private[rematerializers] object FromXmlElemRematerializerFuns {
  def getPrimitiveRematerializerFor[A](key: String)(implicit mA: ClassTag[A]): AlmValidation[Elem => AlmValidation[A]] = {
    if (mA.runtimeClass.isAssignableFrom(classOf[String]))
      Success(((x: Elem) => x.text.success).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[Boolean]))
      Success(((x: Elem) => parseBooleanAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[Byte]))
      Success(((x: Elem) => parseByteAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[Int]))
      Success(((x: Elem) => parseIntAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[Long]))
      Success(((x: Elem) => parseLongAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[BigInt]))
      Success(((x: Elem) => parseBigIntAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[Float]))
      Success(((x: Elem) => parseFloatAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[Double]))
      Success(((x: Elem) => parseDoubleAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[BigDecimal]))
      Success(((x: Elem) => parseDecimalAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[org.joda.time.DateTime]))
      Success(((x: Elem) => parseDateTimeAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[_root_.java.util.UUID]))
      Success(((x: Elem) => parseUuidAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else if (mA.runtimeClass.isAssignableFrom(classOf[_root_.java.net.URI]))
      Success(((x: Elem) => parseUriAlm(x.text, key)).asInstanceOf[Elem => AlmValidation[A]])
    else
      Failure(UnspecifiedProblem("No primitive rematerializer found for '%s'".format(mA.runtimeClass.getName())))
  }

  def createTuple[A, B](mapA: Elem => AlmValidation[A])(mapB: Elem => AlmValidation[B])(kv: Elem)(implicit m: ClassTag[A]): AlmValidation[(A, B)] = {
    for {
      k <- kv \! "k"
      v <- kv \! "v"
      a <- mapA(k)
      b <- mapB(v)
    } yield (a, b)
  }

  def createTuples[A, B](mapB: Elem => AlmValidation[B])(kvPairs: List[Elem])(implicit m: ClassTag[A]): AlmValidation[List[(A, B)]] =
    computeSafely {
      getPrimitiveRematerializerFor[A]("key").flatMap(rematA =>
        kvPairs.map(x => createTuple(rematA)(mapB)(x))
          .map(_.toAgg)
          .sequence[({ type l[a] = scalaz.Validation[AggregateProblem, a] })#l, (A, B)])
    }
}

class FromXmlElemRematerializer(stillInWarp: Elem, protected val fetchBlobData: BlobFetch)(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects) extends RematerializerWithBlobBlobFetch with RematerializerBasedOnOptionGetters {
  import FromXmlElemRematerializerFuns._
  import riftwarp.funs.hasRecomposers._

  protected def spawnNew(from: Elem): FromXmlElemRematerializer =
    new FromXmlElemRematerializer(from, fetchBlobData)(hasRecomposers, functionObjects)

  protected def trySpawnNew(ident: String): AlmValidation[Option[Rematerializer]] =
    tryGet(ident).map(elemOpt => elemOpt.map(spawnNew(_)))
    
  private def tryGet(ident: String): AlmValidation[Option[Elem]] = stillInWarp \? ident

  private def getCollectionElements(ident: String): AlmValidation[Option[List[Elem]]] =
    tryGet(ident).flatMap(elemOpt =>
      option.cata(elemOpt)(
        elem =>
          (elem \* "Collection") match {
            case Seq() => None.success
            case Seq(x) => Some(x.elems.toList).success
            case _ => UnspecifiedProblem("More than one child element found for 'Collection' in '%s'".format(ident)).failure
          },
        None.success))

  private def tryGetElemThenMapForPrimitive[T](ident: String, map: String => AlmValidation[T]): AlmValidation[Option[T]] =
    tryGet(ident).flatMap(elemOpt =>
      option.cata(elemOpt)(
        elem =>
          boolean.fold(
            elem.text.trim.isEmpty,
            Success(None),
            map(elem.text).map(Some(_))),
        Success(None)))

  private def spawnNewForIdentAndThenMap[T](ident: String, thenMap: FromXmlElemRematerializer => AlmValidation[T]): AlmValidation[Option[T]] =
    tryGet(ident).flatMap(elemOpt =>
      option.cata(elemOpt)(
        node =>
          node.child match {
            case Seq(what) =>
              what match {
                case elem: Elem =>
                  thenMap(spawnNew(elem)).map(Some(_))
                case x => UnspecifiedProblem("A scala.xml.elem was required but %s' was found".format(x)).failure
              }
            case x => UnspecifiedProblem("Exactly one child required. Found %d".format(x.size)).failure
          },
        Success(None)))

  def tryGetString(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => txt.success)

  def tryGetBoolean(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseBooleanAlm(txt, ident))

  def tryGetByte(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseByteAlm(txt, ident))
  def tryGetInt(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseIntAlm(txt, ident))
  def tryGetLong(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseLongAlm(txt, ident))
  def tryGetBigInt(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseBigIntAlm(txt, ident))

  def tryGetFloat(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseFloatAlm(txt, ident))
  def tryGetDouble(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseDoubleAlm(txt, ident))
  def tryGetBigDecimal(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseDecimalAlm(txt, ident))

  def tryGetByteArray(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseByteArrayAlm(txt, ident))
  def tryGetByteArrayFromBase64Encoding(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseBase64Alm(txt, ident))
  def tryGetByteArrayFromBlobEncoding(ident: String) = tryGetByteArrayFromBase64Encoding(ident)

  def tryGetDateTime(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseDateTimeAlm(txt, ident))

  def tryGetUri(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseUriAlm(txt, ident))

  def tryGetUuid(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => parseUuidAlm(txt, ident))

  def tryGetJson(ident: String) = tryGetElemThenMapForPrimitive(ident, txt => txt.success)
  def tryGetXml(ident: String) = tryGet(ident)

  def tryGetBlob(ident: String): AlmValidation[Option[Array[Byte]]] =
    tryGetRematerializedBlob(ident)

  def tryGetComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]) =
    spawnNewForIdentAndThenMap(ident, remat => recomposer.recompose(remat))

  def tryGetComplexTypeFixed[T <: AnyRef](ident: String)(implicit m: ClassTag[T]): AlmValidation[Option[T]] =
    hasRecomposers.getRecomposer[T](m.runtimeClass).flatMap(recomposer =>
      tryGetComplexType[T](ident, recomposer))

  def tryGetComplexType[T <: AnyRef](ident: String)(implicit m: ClassTag[T]): AlmValidation[Option[T]] =
    spawnNewForIdentAndThenMap(ident, remat =>
      hasRecomposers.lookUpFromRematerializer(remat, Some(m.runtimeClass)).flatMap(recomposer =>
        recomposer.recomposeRaw(remat).flatMap(x => almCast[T](x))))

  def tryGetPrimitiveMA[M[_], A](ident: String)(implicit mM: ClassTag[M[A]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    getPrimitiveRematerializerFor[A](ident).flatMap(primRemat =>
      functionObjects.getConvertsMAToNA[List, M].flatMap(converterToN =>
        getCollectionElements(ident).flatMap(elemsOpt =>
          option.cata(elemsOpt)(
            elems =>
              elems.map(primRemat(_).toAgg).sequence.flatMap(seq =>
                converterToN.convert(seq)).map(Some(_)),
            None.success))))

  def tryGetComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    getCollectionElements(ident).flatMap(elemsOpt =>
      functionObjects.getConvertsMAToNA[List, M].flatMap(converterToN =>
        option.cata(elemsOpt)(
          elems =>
            elems.map(elem => recomposer.recompose(spawnNew(elem)).toAgg).sequence.flatMap(seq =>
              converterToN.convert(seq)).map(Some(_)),
          None.success)))

  def tryGetComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    hasRecomposers.getRecomposer[A](RiftDescriptor(mA.runtimeClass)).flatMap(recomposer =>
      tryGetComplexMA[M, A](ident, recomposer))

  def tryGetComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    getCollectionElements(ident).flatMap(elemsOpt =>
      functionObjects.getConvertsMAToNA[List, M].flatMap(converterToN =>
        option.cata(elemsOpt)(
          elems =>
            elems.map(elem => recomposeWithLookUpFromRematerializer[A](spawnNew(elem)).toAgg).sequence.flatMap(seq =>
              converterToN.convert(seq)).map(ma => Some(ma)),
          None.success)))
          

  def tryGetMA[M[_], A](ident: String)(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Option[M[A]]] =
    getCollectionElements(ident).flatMap(elemsOpt =>
      functionObjects.getConvertsMAToNA[List, M].flatMap(converterToN =>
        option.cata(elemsOpt)(
          elems =>
            elems.map(elem => mapToAny[A](ident)(elem).toAgg).sequence.flatMap(seq =>
              converterToN.convert(seq)).map(ma => Some(ma)),
          None.success)))

  def tryGetPrimitiveMap[A, B](ident: String)(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Option[Map[A, B]]] =
    (TypeHelpers.isPrimitiveType(mA.runtimeClass), TypeHelpers.isPrimitiveType(mB.runtimeClass)) match {
      case (true, true) =>
        for {
          rematB <- getPrimitiveRematerializerFor[B](ident)
          elems <- getCollectionElements(ident)
          tuples <- elems.map(createTuples[A, B](rematB)).validationOut
        } yield tuples.map(_.toMap)
      case (false, true) => UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not rematerialize primitive map for %s: B(%s) is not a primitive type".format(ident, mB.runtimeClass.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.runtimeClass.getName(), mB.runtimeClass.getName())).failure
    }

  def tryGetComplexMap[A, B <: AnyRef](ident: String, recomposer: Recomposer[B])(implicit mA: ClassTag[A]): AlmValidation[Option[Map[A, B]]] = {
    def rematerialize(x: Elem): AlmValidation[B] = recomposer.recompose(spawnNew(x))
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      for {
        elems <- getCollectionElements(ident)
        tuples <- elems.map(createTuples[A, B](rematerialize)).validationOut
      } yield tuples.map(_.toMap),
      UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
  }

  def tryGetComplexMapFixed[A, B <: AnyRef](ident: String)(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Option[Map[A, B]]] =
    hasRecomposers.getRecomposer[B](RiftDescriptor(mB.runtimeClass)).flatMap(recomposer => tryGetComplexMap[A, B](ident, recomposer))

  def tryGetComplexMapLoose[A, B <: AnyRef](ident: String)(implicit mA: ClassTag[A]): AlmValidation[Option[Map[A, B]]] = {
    def rematerialize(x: Elem): AlmValidation[B] = recomposeWithLookUpFromRematerializer(spawnNew(x))
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      for {
        elems <- getCollectionElements(ident)
        tuples <- elems.map(createTuples[A, B](rematerialize)).validationOut
      } yield tuples.map(_.toMap),
      UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
  }

  def tryGetMap[A, B](ident: String)(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Option[Map[A, B]]] = {
    def rematerialize(x: Elem): AlmValidation[B] = mapToAny[B](ident)(x)
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.runtimeClass),
      for {
        elems <- getCollectionElements(ident)
        tuples <- elems.map(createTuples[A, B](rematerialize)).validationOut
      } yield tuples.map(_.toMap),
      UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.runtimeClass.getName())).failure)
  }

  def tryGetRiftDescriptor =
    (stillInWarp \\ "@riftDescriptor").headOption.map(node => RiftDescriptor.parse(node.text)).validationOut

  private def mapToAny[A](ident: String)(what: Elem)(implicit m: ClassTag[A]): AlmValidation[A] =
    getPrimitiveRematerializerFor[A](ident).fold(
      prob =>
        if (classOf[Map[_, _]].isAssignableFrom(what.getClass))
          computeSafely {
          recomposeWithLookedUpRawRecomposerFromRematerializer(spawnNew(what), m.runtimeClass).map(_.asInstanceOf[A])
        }
        else
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is neither a primitive type nor a decomposer could be found. I was trying to decompose '%s'".format(ident, what.getClass.getName())).failure,
      rematPrimitive =>
        rematPrimitive(what))
}

object FromXmlElemRematerializer extends RematerializerFactory[DimensionXmlElem] {
  val channel = RiftXml()
  val tDimension = classOf[DimensionXmlElem].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(elem: Elem, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): FromXmlElemRematerializer =
    new FromXmlElemRematerializer(elem, fetchBlobs)
  def createRematerializer(from: DimensionXmlElem, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[FromXmlElemRematerializer] =
    apply(from.manifestation, fetchBlobs)(hasRecomposers, hasFunctionObject).success
}