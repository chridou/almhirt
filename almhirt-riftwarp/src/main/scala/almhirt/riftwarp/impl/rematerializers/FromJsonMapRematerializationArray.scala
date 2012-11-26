package almhirt.riftwarp.impl.rematerializers

import scala.util.parsing.json._
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.riftwarp._

class FromJsonMapRematerializationArray(jsonMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories) extends RematerializationArrayBasedOnOptionGetters {
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
    get(ident) match {
      case Some(elem) =>
        almCast[List[A]](elem).bind(la =>
          hasRematerializers.tryGetCanRematerializePrimitiveMA[M, A, DimensionListAny](RiftJson()) match {
            case Some(crpma) =>
              crpma.rematerialize(DimensionListAny(la)).map(Some(_))
            case None =>
              UnspecifiedProblem("No rematerializer found for ident '%s' and M[A] '%s[%s]'".format(ident, mM.erasure.getName(), mA.erasure.getName())).failure
          })
      case None =>
        None.success
    }

  def tryGetTypeDescriptor =
    option.cata(get(TypeDescriptor.defaultKey))(almCast[String](_).bind(TypeDescriptor.parse(_)).map(Some(_)), None.success)
}

object FromJsonMapRematerializationArray extends RematerializationArrayFactory[DimensionStdLibJsonMap] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionStdLibJsonMap].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(jsonMap: Map[String, Any])(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): FromJsonMapRematerializationArray = new FromJsonMapRematerializationArray(jsonMap)
  def apply(jsonMap: DimensionStdLibJsonMap)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): FromJsonMapRematerializationArray = new FromJsonMapRematerializationArray(jsonMap.manifestation)
  def createRematerializationArray(from: DimensionStdLibJsonMap)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): AlmValidation[FromJsonMapRematerializationArray] = apply(from).success
}

object FromJsonStringRematerializationArray extends RematerializationArrayFactory[DimensionString] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionString].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(json: String)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): AlmValidation[FromJsonMapRematerializationArray] = {
    JSON.parseFull(json) match {
      case Some(map) =>
        almCast[Map[String, Any]](map).map(FromJsonMapRematerializationArray(_))
      case None =>
        ParsingProblem("Could not parse JSON", input = Some(json)).failure
    }
  }
  def apply(json: DimensionString)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): AlmValidation[FromJsonMapRematerializationArray] = apply(json.manifestation)
  def createRematerializationArray(from: DimensionString)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): AlmValidation[FromJsonMapRematerializationArray] = 
    apply(from)
}

import scalaz.Cord
object FromJsonCordRematerializationArray extends RematerializationArrayFactory[DimensionCord] {
  val channel = RiftJson()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  
  def apply(json: Cord)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): AlmValidation[FromJsonMapRematerializationArray] =
    FromJsonStringRematerializationArray.createRematerializationArray(DimensionString(json.toString))
  def apply(json: DimensionCord)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): AlmValidation[FromJsonMapRematerializationArray] =
    FromJsonStringRematerializationArray.createRematerializationArray(DimensionString(json.manifestation.toString))
  def createRematerializationArray(from: DimensionCord)(implicit hasRecomposers: HasRecomposers, hasRematerializers: HasRematerializationArrayFactories): AlmValidation[FromJsonMapRematerializationArray] =
    apply(from)
}