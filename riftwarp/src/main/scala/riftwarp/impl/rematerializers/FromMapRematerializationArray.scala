package riftwarp.impl.rematerializers

import scalaz._, Scalaz._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.ma._

class FromMapRematerializationArray(theMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers, functionObjects: HasFunctionObjects) extends RematerializationArrayBasedOnOptionGetters {
  def tryGetString(ident: String) = option.cata(theMap.get(ident))(almCast[String](_).map(Some(_)), None.success)

  def tryGetBoolean(ident: String) = option.cata(theMap.get(ident))(almCast[Boolean](_).map(Some(_)), None.success)

  def tryGetByte(ident: String) = option.cata(theMap.get(ident))(almCast[Byte](_).map(Some(_)), None.success)
  def tryGetInt(ident: String) = option.cata(theMap.get(ident))(almCast[Int](_).map(Some(_)), None.success)
  def tryGetLong(ident: String) = option.cata(theMap.get(ident))(almCast[Long](_).map(Some(_)), None.success)
  def tryGetBigInt(ident: String) = option.cata(theMap.get(ident))(almCast[BigInt](_).map(Some(_)), None.success)

  def tryGetFloat(ident: String) = option.cata(theMap.get(ident))(almCast[Float](_).map(Some(_)), None.success)
  def tryGetDouble(ident: String) = option.cata(theMap.get(ident))(almCast[Double](_).map(Some(_)), None.success)
  def tryGetBigDecimal(ident: String) = option.cata(theMap.get(ident))(almCast[BigDecimal](_).map(Some(_)), None.success)

  def tryGetByteArray(ident: String) = option.cata(theMap.get(ident))(almCast[Array[Byte]](_).map(Some(_)), None.success)
  def tryGetBlob(ident: String) = option.cata(theMap.get(ident))(almCast[Array[Byte]](_).map(Some(_)), None.success)

  def tryGetDateTime(ident: String) = option.cata(theMap.get(ident))(almCast[org.joda.time.DateTime](_).map(Some(_)), None.success)

  def tryGetUuid(ident: String) = option.cata(theMap.get(ident))(almCast[_root_.java.util.UUID](_).map(Some(_)), None.success)

  def tryGetJson(ident: String) = option.cata(theMap.get(ident))(almCast[String](_).map(Some(_)), None.success)
  def tryGetXml(ident: String) = option.cata(theMap.get(ident))(almCast[scala.xml.Node](_).map(Some(_)), None.success)

  def tryGetComplexType[T <: AnyRef](ident: String, recomposer: Recomposer[T]) =
    theMap.get(ident) match {
      case Some(elem) =>
        almCast[Map[String, Any]](elem).bind { elemAsMap =>
          FromMapRematerializationArray.createRematerializationArray(DimensionRawMap(elemAsMap)).bind(rematerializationArray =>
            recomposer.recompose(rematerializationArray))
        }.map(res =>
          Some(res))
      case None =>
        None.success
    }

  def tryGetComplexType[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]] = {
    theMap.get(ident) match {
      case Some(elem) =>
        almCast[Map[String, Any]](elem).bind { elemAsMap =>
          FromMapRematerializationArray.createRematerializationArray(DimensionRawMap(elemAsMap)).bind { rematerializationArray =>
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

  def tryGetComplexTypeFixed[T <: AnyRef](ident: String)(implicit m: Manifest[T]): AlmValidation[Option[T]] = {
    theMap.get(ident) match {
      case Some(elem) =>
        almCast[Map[String, Any]](elem).bind { elemAsMap =>
          FromMapRematerializationArray.createRematerializationArray(DimensionRawMap(elemAsMap)).bind { rematerializationArray =>
            val td = TypeDescriptor(m.erasure)
            hasRecomposers.tryGetRecomposer[T](td) match {
              case Some(recomposer) => recomposer.recompose(rematerializationArray).map(Some(_))
              case None => UnspecifiedProblem("No recomposer found for ident '%s' and type descriptor '%s'".format(ident, td)).failure
            }
          }
        }
      case None =>
        None.success
    }
  }

  def tryGetPrimitiveMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    option.cata(theMap.get(ident))(almCast[M[A]](_).map(Some(_)), None.success)

  def tryGetComplexMA[M[_], A <: AnyRef](ident: String, recomposer: Recomposer[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    option.cata(theMap.get(ident))(
      mx =>
        boolean.fold(
          mM.erasure.isAssignableFrom(mx.getClass),
          functionObjects.getMAFunctions[M].bind(fo =>
            computeSafely {
              val validations =
                fo.map(mx.asInstanceOf[M[Map[String, Any]]])(elem =>
                  FromMapRematerializationArray.createRematerializationArray(DimensionRawMap(elem)).bind(remat =>
                    recomposer.recompose(remat)))
              val sequenced = fo.sequenceValidations(fo.map(validations)(_.toAgg))
              sequenced.map(Some(_))
            }),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type M[_](%s[_]). It is of type '%s'".format(ident, mM.erasure.getName(), mx.getClass.getName())).failure),
      None.success)

  def tryGetComplexMAFixed[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    hasRecomposers.getRecomposer[A](TypeDescriptor(mA.erasure)).bind(recomposer =>
      tryGetComplexMA[M, A](ident, recomposer))

  def tryGetComplexMALoose[M[_], A <: AnyRef](ident: String)(implicit mM: Manifest[M[_]]): AlmValidation[Option[M[A]]] = {
    option.cata(theMap.get(ident))(
      mx =>
        boolean.fold(
          mM.erasure.isAssignableFrom(mx.getClass),
          functionObjects.getMAFunctions[M].bind(fo =>
            computeSafely {
              val validations =
                fo.map(mx.asInstanceOf[M[Map[String, Any]]])(elem =>
                  FromMapRematerializationArray.createRematerializationArray(DimensionRawMap(elem)).bind(remat =>
                    remat.getTypeDescriptor.bind(typeDescriptor =>
                      hasRecomposers.getRawRecomposer(typeDescriptor).bind(recomposer =>
                        recomposer.recomposeRaw(remat).map(_.asInstanceOf[A])))))
              val sequenced = fo.sequenceValidations(fo.map(validations)(_.toAgg))
              sequenced.map(Some(_))
            }),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type M[_](%s[_]). It is of type '%s'".format(ident, mM.erasure.getName(), mx.getClass.getName())).failure),
      None.success)
  }

  def tryGetMA[M[_], A](ident: String)(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Option[M[A]]] =
    option.cata(theMap.get(ident))(
      mx =>
        boolean.fold(
          mM.erasure.isAssignableFrom(mx.getClass),
          functionObjects.getMAFunctions[M].bind(fo =>
            computeSafely {
              val validations = fo.map(mx.asInstanceOf[M[Map[String, Any]]])(mapToAny[A](ident))
              val sequenced = fo.sequenceValidations(fo.map(validations)(_.toAgg))
              sequenced.map(Some(_))
            }),
          UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is not of type M[_](%s[_]). It is of type '%s'".format(ident, mM.erasure.getName(), mx.getClass.getName())).failure),
      None.success)

  def tryGetPrimitiveMap[A, B](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Option[Map[A, B]]] =
    (TypeHelpers.isPrimitiveType(mA.erasure), TypeHelpers.isPrimitiveType(mB.erasure)) match {
      case (true, true) => inTryCatch { theMap.get(ident).map(_.asInstanceOf[Map[A, B]]) }
      case (false, true) => UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure
      case (true, false) => UnspecifiedProblem("Could not rematerialize primitive map for %s: B(%s) is not a primitive type".format(ident, mB.erasure.getName())).failure
      case (false, false) => UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) and B(%s) are not primitive types".format(ident, mA.erasure.getName(), mB.erasure.getName())).failure
    }

  def tryGetComplexMap[A, B <: AnyRef](ident: String, recomposer: Recomposer[B])(implicit mA: Manifest[A]): AlmValidation[Option[Map[A, B]]] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      option.cata(theMap.get(ident))(
        theMap =>
          computeSafely {
            theMap.asInstanceOf[Map[A, Map[String, AnyRef]]].map {
              case (a, b) =>
                recomposer.recompose(FromMapRematerializationArray(DimensionRawMap(b))).map(decomposed =>
                  (a, decomposed)).toAgg
            }.toList.sequence.map(items =>
              items.toMap).map(Some(_))
          },
        None.success),
      UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def tryGetComplexMapFixed[A, B <: AnyRef](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Option[Map[A, B]]] =
    hasRecomposers.getRecomposer[B](TypeDescriptor(mB.erasure)).bind(recomposer => tryGetComplexMap[A, B](ident, recomposer))

  def tryGetComplexMapLoose[A, B <: AnyRef](ident: String)(implicit mA: Manifest[A]): AlmValidation[Option[Map[A, B]]] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      option.cata(theMap.get(ident))(
        theMap =>
          computeSafely {
            theMap.asInstanceOf[Map[A, Map[String, AnyRef]]].map {
              case (a, b) =>
                val remat = FromMapRematerializationArray(DimensionRawMap(b))
                remat.getTypeDescriptor.bind(td =>
                  hasRecomposers.getRawRecomposer(td).bind(recomposer =>
                    recomposer.recomposeRaw(remat).map(decomposed =>
                      (a, decomposed.asInstanceOf[B])))).toAgg
            }.toList.sequence.map(items =>
              items.toMap).map(Some(_))
          },
        None.success),
      UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def tryGetMap[A, B](ident: String)(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Option[Map[A, B]]] =
    boolean.fold(
      TypeHelpers.isPrimitiveType(mA.erasure),
      option.cata(theMap.get(ident))(
        theMap =>
          computeSafely {
            theMap.asInstanceOf[Map[A, Map[String, AnyRef]]].map {
              case (a, b) =>
                mapToAny[B](ident)(b).map(decomposed => (a, decomposed)).toAgg
            }.toList.sequence.map(items =>
              items.toMap).map(Some(_))
          },
        None.success),
      UnspecifiedProblem("Could not rematerialize primitive map for %s: A(%s) is not a primitive type".format(ident, mA.erasure.getName())).failure)

  def tryGetTypeDescriptor = option.cata(theMap.get(TypeDescriptor.defaultKey))(almCast[TypeDescriptor](_).map(Some(_)), None.success)

  private def mapToAny[A](ident: String)(what: Any): AlmValidation[A] =
    if (TypeHelpers.isPrimitiveValue(what))
      what.asInstanceOf[A].success
    else if (classOf[Map[_, _]].isAssignableFrom(what.getClass))
      computeSafely {
        FromMapRematerializationArray.createRematerializationArray(DimensionRawMap(what.asInstanceOf[Map[String, Any]])).bind(remat =>
          remat.getTypeDescriptor.bind(typeDescriptor =>
            hasRecomposers.getRawRecomposer(typeDescriptor).bind(recomposer =>
              recomposer.recomposeRaw(remat).map(_.asInstanceOf[A]))))
      }
    else
      UnspecifiedProblem("Cannot rematerialize at ident '%s' because it is neither a primitive type nor a decomposer could be found. I was trying to decompose '%s'".format(ident, what.getClass.getName())).failure

}

object FromMapRematerializationArray extends RematerializationArrayFactory[DimensionRawMap] {
  val channel = RiftMap()
  val tDimension = classOf[DimensionRawMap].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupRiftStd()

  def apply()(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): FromMapRematerializationArray = apply(Map.empty[String, Any])
  def apply(state: Map[String, Any])(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): FromMapRematerializationArray = new FromMapRematerializationArray(state)
  def apply(state: DimensionRawMap)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): FromMapRematerializationArray = new FromMapRematerializationArray(state.manifestation)
  def createRematerializationArray(from: DimensionRawMap)(implicit hasRecomposers: HasRecomposers, hasFunctionObject: HasFunctionObjects): AlmValidation[RematerializationArray] = apply(from).success
}