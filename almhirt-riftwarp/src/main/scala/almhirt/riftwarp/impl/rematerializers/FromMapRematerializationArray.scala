package almhirt.riftwarp.impl.rematerializers

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.riftwarp._
import almhirt.riftwarp.TypeDescriptor

class FromMapRematerializationArray(theMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers) extends RematiarializationArrayBasedOnOptionGetters {
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
          FromMapRematerializationArray.createRematerializationArray(elemAsMap).bind(rematerializationArray =>
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
          FromMapRematerializationArray.createRematerializationArray(elemAsMap).bind { rematerializationArray =>
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

  def tryGetTypeDescriptor = option.cata(theMap.get(TypeDescriptor.defaultKey))(almCast[TypeDescriptor](_).map(Some(_)), None.success)
}

object FromMapRematerializationArray extends FromMapRematerializationArrayFactory {
  val descriptor = RiftFullDescriptor(RiftMap, ToolGroupRiftStd)
  def apply()(implicit hasRecomposers: HasRecomposers): FromMapRematerializationArray = apply(Map.empty)
  def apply(state: Map[String, Any])(implicit hasRecomposers: HasRecomposers): FromMapRematerializationArray = new FromMapRematerializationArray(state)
  def createRematerializationArray(from: Map[String, Any])(implicit hasRecomposers: HasRecomposers): AlmValidation[RematerializationArray] = apply(from).success
}