package riftwarpx.sprayjson

import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import spray.json._
import riftwarp.std.Base64BlobWarpUnpacker

object FromSprayJsonRematerializer extends Rematerializer[JsValue] {
  override def rematerialize(what: JsValue, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    extract(what)

  private def extract(what: JsValue): AlmValidation[WarpPackage] =
    what match {
      case obj: JsObject =>
        extractObject(obj).flatMap(pkg =>
          pkg.warpDescriptor match {
            case Some(Base64BlobWarpUnpacker.warpDescriptor) =>
              Base64BlobWarpUnpacker.unpack(pkg)(WarpUnpackers.NoWarpUnpackers).map(WarpBlob(_))
            case _ =>
              pkg.success
          })
      case arr: JsArray => extractCollection(arr)
      case other => extractPrimitive(other)
    }

  private def extractObject(what: JsObject): AlmValidation[WarpObject] = {
    val elements = what.fields
    for {
      descriptorAndRest <- if (elements.contains(WarpDescriptor.defaultKey))
        for {
          rdElem <- extractPrimitive(elements(WarpDescriptor.defaultKey))
          rdStr <- rdElem match {
            case WarpString(rd) => rd.success
            case x => UnspecifiedProblem(s"${x.toString()} is not valid as a riftdescriptor").failure
          }
          rd <- WarpDescriptor.parse(rdStr)
        } yield (Some(rd), elements - WarpDescriptor.defaultKey)
      else
        (None, elements).success
      objElems <- mapJsonMapToWarpElement(descriptorAndRest._2)
    } yield WarpObject(descriptorAndRest._1, objElems)
  }

  private def mapJsonMapToWarpElement(jsonElems: Map[String, JsValue]): AlmValidation[Vector[WarpElement]] =
    jsonElems.map(labelAndValue =>
      labelAndValue._2 match {
        case JsNull => WarpElement(labelAndValue._1, None).success
        case v => extract(v).map(x => WarpElement(labelAndValue._1, Some(x))).toAgg
      }).toVector.sequence

  private def extractCollection(what: JsArray): AlmValidation[WarpCollection] =
    what.elements.map(item => extract(item).toAgg).sequence.map(x => WarpCollection(x.toVector))

  private def extractPrimitive(what: JsValue): AlmValidation[WarpPrimitive] = {
    what match {
      case JsString(v) => WarpString(v).success
      case JsBoolean(v) => WarpBoolean(v).success
      case JsNumber(v) => WarpBigDecimal(v).success
      case x =>  Failure(UnspecifiedProblem(s"""No primitive rematerialization found for "$x}""""))
    }
  }
}