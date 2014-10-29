package riftwarpx.mongo

import org.joda.time.{ LocalDateTime, DateTimeZone }
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.converters.BinaryConverter
import reactivemongo.bson._
import riftwarp._

object FromBsonRematerializer extends Rematerializer[BSONValue] {
  import BsonConverter._
  override val channels = Set(WarpChannels.`rift-bson-reactive-mongo`)
  override def rematerialize(what: BSONValue, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    extract(what, Nil)

  private def extract(what: BSONValue, path: List[String]): AlmValidation[WarpPackage] =
    what match {
      case obj: BSONDocument ⇒ extractObject(obj, path)
      case arr: BSONArray ⇒ extractCollection(arr, path)
      case BSONBinary(value, Subtype.GenericBinarySubtype) ⇒ WarpBytes(value.readArray(value.size)).success
      case other ⇒ extractPrimitive(other, path)
    }

  private def extractObject(what: BSONDocument, path: List[String]): AlmValidation[WarpObject] = {
    val elements = scala.collection.mutable.HashMap(what.elements: _*)
    for {
      warpdescriptor ← elements.get(WarpDescriptor.defaultKey) match {
        case Some(BSONString(str)) ⇒
          elements.remove(WarpDescriptor.defaultKey)
          WarpDescriptor.parse(str).map(Some(_))
        case Some(x) ⇒
          ParsingProblem(s""""${x.getClass().getName()}" is not valid as a warp descriptor.""").failure
        case None ⇒
          None.success
      }
      warpElements ← mapWarpElements(elements, path)

    } yield WarpObject(warpdescriptor, warpElements)
  }

  private def mapWarpElements(bsonElems: scala.collection.mutable.HashMap[String, BSONValue], path: List[String]): AlmValidation[Vector[WarpElement]] =
    bsonElems.map(labelAndValue ⇒
      labelAndValue._2 match {
        case BSONNull ⇒ WarpElement(labelAndValue._1, None).success
        case v ⇒ extract(v, labelAndValue._1 :: path).map(x ⇒ WarpElement(labelAndValue._1, Some(x))).toAgg
      }).toVector.sequence

  private def extractCollection(what: BSONArray, path: List[String]): AlmValidation[WarpCollection] =
    what.values.map(item ⇒ extract(item, path).toAgg).sequence.map(x ⇒ WarpCollection(x.toVector))

  private def extractPrimitive(what: BSONValue, path: List[String]): AlmValidation[WarpPrimitive] = {
    what match {
      case BSONBoolean(value) ⇒ WarpBoolean(value).success
      case BSONString(value) ⇒ WarpString(value).success
      case BSONInteger(value) ⇒ WarpInt(value).success
      case BSONLong(value) ⇒ WarpLong(value).success
      case BSONDouble(value) ⇒ WarpDouble(value).success
      case BSONTimestamp(value) ⇒ WarpLocalDateTime(new LocalDateTime(value, DateTimeZone.UTC)).success
      case BSONBinary(value, Subtype.UuidSubtype) ⇒ WarpUuid(BinaryConverter.bytesToUuid(value.readArray(16))).success
      case BSONBinary(value, Subtype.OldUuidSubtype) ⇒ WarpUuid(BinaryConverter.bytesBigEndianToUuid(value.readArray(16))).success
      case BSONBinary(value, st) ⇒
        Failure(SerializationProblem(s"A BSONBinary with subtype ${st.toString()} is not a primitive type. The path is ${path.reverse.mkString("[", "->", "]")}."))
      case x ⇒
        Failure(SerializationProblem(s"""No primitive rematerialization found for "${what.getClass.getName()}""""))
    }
  }
}