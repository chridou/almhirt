package riftwarpx.mongo

import org.joda.time.{LocalDateTime, DateTimeZone }
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.util.UuidConverter
import reactivemongo.bson._
import riftwarp._

object FromBsonRematerializer extends Rematerializer[BSONValue] {
  import BsonConverter._
  override def rematerialize(what: BSONValue, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    extract(what)

  private def extract(what: BSONValue): AlmValidation[WarpPackage] =
    what match {
      case obj: BSONDocument => extractObject(obj)
      case arr: BSONArray => extractCollection(arr)
      case other => extractPrimitive(other)
    }

  private def extractObject(what: BSONDocument): AlmValidation[WarpObject] = {
    val elements = scala.collection.mutable.HashMap(what.elements: _*)
    for {
      warpdescriptor <- elements.get(WarpDescriptor.defaultKey) match {
        case Some(BSONString(str)) =>
          elements.remove(WarpDescriptor.defaultKey)
          WarpDescriptor.parse(str).map(Some(_))
        case Some(x) =>
          ParsingProblem(s""""${x.getClass().getName()}" is not valid as a warp descriptor.""").failure
        case None =>
          None.success
      }
      warpElements <- mapWarpElements(elements)

    } yield WarpObject(warpdescriptor, warpElements)
  }

  private def mapWarpElements(bsonElems: scala.collection.mutable.HashMap[String, BSONValue]): AlmValidation[Vector[WarpElement]] =
    bsonElems.map(labelAndValue =>
      labelAndValue._2 match {
        case BSONNull => WarpElement(labelAndValue._1, None).success
        case v => extract(v).map(x => WarpElement(labelAndValue._1, Some(x))).toAgg
      }).toVector.sequence

  private def extractCollection(what: BSONArray): AlmValidation[WarpCollection] =
    what.values.map(item => extract(item).toAgg).sequence.map(x => WarpCollection(x.toVector))

  private def extractPrimitive(what: BSONValue): AlmValidation[WarpPrimitive] = {
    what match {
      case BSONBoolean(value) => WarpBoolean(value).success
      case BSONString(value) => WarpString(value).success
      case BSONInteger(value) => WarpInt(value).success
      case BSONLong(value) => WarpLong(value).success
      case BSONDouble(value) => WarpDouble(value).success
      case BSONDateTime(value) => WarpLocalDateTime(new LocalDateTime(value, DateTimeZone.UTC)).success
      case BSONBinary(value, Subtype.OldUuidSubtype) => WarpUuid(UuidConverter.bytesToUuid(value.readArray(16))).success
      case BSONBinary(value, st) => 
        Failure(UnspecifiedProblem(s"""A BSONBinary with subtype ${st.toString()} is not a primitive type."""))
      case x => 
        Failure(UnspecifiedProblem(s"""No primitive rematerialization found for "${what.getClass.getName()}""""))
    }
  }
}