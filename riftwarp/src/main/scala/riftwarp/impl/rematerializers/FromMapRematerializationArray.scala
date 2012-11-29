package riftwarp.impl.rematerializers

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

  def tryGetTypeDescriptor = option.cata(theMap.get(TypeDescriptor.defaultKey))(almCast[TypeDescriptor](_).map(Some(_)), None.success)
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