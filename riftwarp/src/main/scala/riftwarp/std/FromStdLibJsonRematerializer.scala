package riftwarp.std

import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import scala.util.parsing.json.JSONArray
import scala.util.parsing.json.JSONObject

object FromStdLibJsonRematerializer extends Rematerializer[Any @@ WarpTags.JsonStdLib] {
  import scala.util.parsing.json._
  override def rematerialize(what: Any @@ WarpTags.JsonStdLib): AlmValidation[WarpPackage] =
    extract(what)

  private def extract(what: Any): AlmValidation[WarpPackage] =
    what match {
      case obj: JSONObject => extractObject(obj)
      case arr: JSONArray => extractCollection(arr)
      case other => extractPrimitive(other)
    }

  private def extractObject(what: JSONObject): AlmValidation[WarpObject] = {
    val elements = what.obj
    for {
      descriptorAndRest <- if (elements.contains(RiftDescriptor.defaultKey))
        for {
          rdElem <- extractPrimitive(elements(RiftDescriptor.defaultKey))
          rdStr <- rdElem match {
            case WarpString(rd) => rd.success
            case x => UnspecifiedProblem(s"${x.toString()} is not valid as a riftdescriptor").failure
          }
          rd <- RiftDescriptor.parse(rdStr)
        } yield (Some(rd), elements - RiftDescriptor.defaultKey)
      else
        (None, elements).success
      objElems <- descriptorAndRest._2.map(labelAndValue => extract(labelAndValue._2).map((labelAndValue._1, _)).toAgg)
        .toVector.sequence
    } yield WarpObject(descriptorAndRest._1, objElems.map(x => WarpElement(x._1, x._2)))
  }

  private def extractCollection(what: JSONArray): AlmValidation[WarpCollection] =
    what.list.map(item => extract(item).toAgg).sequence.map(x => WarpCollection(x.toVector))

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