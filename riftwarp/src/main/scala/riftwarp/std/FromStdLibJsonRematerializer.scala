package riftwarp.std

import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import scala.util.parsing.json.JSONArray
import scala.util.parsing.json.JSONObject

object FromStdLibJsonRematerializer extends Rematerializer[Any @@ WarpTags.JsonStdLib] {
  override val channels = Set(WarpChannels.`rift-json-std`)
  
  import scala.util.parsing.json._
  override def rematerialize(what: Any @@ WarpTags.JsonStdLib, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    extract(what)

  private def extract(what: Any): AlmValidation[WarpPackage] =
    what match {
      case obj: JSONObject ⇒
        extractObject(obj).flatMap(pkg ⇒
          pkg.warpDescriptor match {
            case Some(Base64BlobWarpUnpacker.warpDescriptor) ⇒
              Base64BlobWarpUnpacker.unpack(pkg)(WarpUnpackers.NoWarpUnpackers).map(WarpBlob(_))
            case _ ⇒
              pkg.success
          })
      case arr: JSONArray ⇒ extractCollection(arr)
      case other ⇒ extractPrimitive(other)
    }

  private def extractObject(what: JSONObject): AlmValidation[WarpObject] = {
    val elements = what.obj
    for {
      descriptorAndRest <- if (elements.contains(WarpDescriptor.defaultKey))
        for {
          rdElem <- extractPrimitive(elements(WarpDescriptor.defaultKey))
          rdStr <- rdElem match {
            case WarpString(rd) ⇒ rd.success
            case x ⇒ UnspecifiedProblem(s"${x.toString()} is not valid as a riftdescriptor").failure
          }
          rd <- WarpDescriptor.parse(rdStr)
        } yield (Some(rd), elements - WarpDescriptor.defaultKey)
      else
        (None, elements).success
      objElems <- mapJsonMapToWarpElement(descriptorAndRest._2)
    } yield WarpObject(descriptorAndRest._1, objElems)
  }

  private def mapJsonMapToWarpElement(jsonElems: Map[String, Any]): AlmValidation[Vector[WarpElement]] =
    jsonElems.map(labelAndValue ⇒
      labelAndValue._2 match {
        case null ⇒ WarpElement(labelAndValue._1, None).success
        case v ⇒ extract(v).map(x ⇒ WarpElement(labelAndValue._1, Some(x))).toAgg
      }).toVector.sequence

  private def extractCollection(what: JSONArray): AlmValidation[WarpCollection] =
    what.list.map(item ⇒ extract(item).toAgg).sequence.map(x ⇒ WarpCollection(x.toVector))

  private def extractPrimitive(what: Any): AlmValidation[WarpPrimitive] = {
    val clazz = what.getClass()
    if (clazz == classOf[String])
      almCast[String](what).map(WarpString(_))
    else if (clazz == classOf[_root_.java.lang.String])
      almCast[String](what).map(WarpString(_))
    else if (clazz == classOf[Boolean])
      almCast[Boolean](what).map(WarpBoolean(_))
    else if (clazz == classOf[_root_.java.lang.Boolean])
      almCast[Boolean](what).map(WarpBoolean(_))
    else if (clazz == classOf[Double])
      almCast[Double](what).map(WarpDouble(_))
    else if (clazz == classOf[_root_.java.lang.Double])
      almCast[Double](what).map(WarpDouble(_))
    else if (clazz == classOf[Byte])
      almCast[Byte](what).map(WarpByte(_))
    else if (clazz == classOf[_root_.java.lang.Byte])
      almCast[Byte](what).map(WarpByte(_))
    else if (clazz == classOf[Int])
      almCast[Int](what).map(WarpInt(_))
    else if (clazz == classOf[_root_.java.lang.Integer])
      almCast[Int](what).map(WarpInt(_))
    else if (clazz == classOf[Long])
      almCast[Long](what).map(WarpLong(_))
    else if (clazz == classOf[_root_.java.lang.Long])
      almCast[Long](what).map(WarpLong(_))
    else if (clazz == classOf[Float])
      almCast[Float](what).map(WarpFloat(_))
    else if (clazz == classOf[_root_.java.lang.Float])
      almCast[Float](what).map(WarpFloat(_))
    else
      Failure(UnspecifiedProblem(s"""No primitive rematerialization found for "${clazz.getName()}""""))
  }
}