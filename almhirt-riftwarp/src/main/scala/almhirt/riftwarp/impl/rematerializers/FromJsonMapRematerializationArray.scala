package almhirt.riftwarp.impl.rematerializers

import scala.util.parsing.json._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.riftwarp._

class FromJsonMapRematerializationArray(jsonMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers) extends RematiarializationArrayBasedOnOptionGetters {
  private def get(key: String): Option[Any] =
	jsonMap.get(key).flatMap(v => if(v == null) None else Some(v))
  
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
          FromJsonMapRematerializationArray.createRematerializationArray(elemAsMap).bind(rematerializationArray =>
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
          FromJsonMapRematerializationArray.createRematerializationArray(elemAsMap).bind { rematerializationArray =>
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

  def tryGetTypeDescriptor = 
    option.cata(get(TypeDescriptor.defaultKey))(almCast[String](_).bind(TypeDescriptor.parse(_)).map(Some(_)), None.success)
}

object FromJsonMapRematerializationArray extends RematerializationArrayFactory[Map[String, Any]] {
  val descriptor = RiftFullDescriptor(RiftJson, ToolGroupStdLib)
  def apply(jsonMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers): FromJsonMapRematerializationArray = new FromJsonMapRematerializationArray(jsonMap)
  def createRematerializationArray(from: Map[String, Any])(implicit hasRecomposers: HasRecomposers): AlmValidation[FromJsonMapRematerializationArray] = apply(from).success
}

object FromJsonStringRematerializationArray extends RematerializationArrayFactory[String] {
  val descriptor = RiftFullDescriptor(RiftJson, ToolGroupStdLib)
  def apply(json: String)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromJsonMapRematerializationArray] = {
    JSON.parseFull(json) match {
      case Some(map) => 
        almCast[Map[String, Any]](map).map(FromJsonMapRematerializationArray(_))
      case None => 
        ParsingProblem("Could not parse JSON", input = Some(json)).failure
    }
  }
  def createRematerializationArray(from: String)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromJsonMapRematerializationArray] = apply(from)
}

import scalaz.Cord
object FromJsonCordRematerializationArray extends RematerializationArrayFactory[Cord] {
  val descriptor = RiftFullDescriptor(RiftJson, ToolGroupStdLib)
  def apply(json: Cord)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromJsonMapRematerializationArray] = 
    FromJsonStringRematerializationArray.createRematerializationArray(json.toString)
  def createRematerializationArray(from: Cord)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromJsonMapRematerializationArray] = 
    apply(from)
}