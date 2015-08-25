package riftwarpx.mongo

import scala.annotation.tailrec
import scalaz._
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
import almhirt.almvalidation.kit._
import almhirt.common._
import reactivemongo.bson._
import riftwarp._
import riftwarp.std.DematerializerTemplate

object ToBsonDematerializer extends DematerializerTemplate[BSONValue] {
  type ValueRepr = BSONValue
  type ObjRepr = BSONDocument

  override val channels = Set(WarpChannels.`rift-bson-reactive-mongo`)

  protected def valueReprToDim(repr: BSONValue): BSONValue =
    repr

  protected override final def getPrimitiveRepr(prim: WarpPrimitive): BSONValue =
    prim match {
      case WarpBoolean(value) ⇒ BSONBoolean(value)
      case WarpString(value) ⇒ BSONString(value)
      case WarpByte(value) ⇒ BSONInteger(value.toInt)
      case WarpShort(value) ⇒ BSONInteger(value.toInt)
      case WarpInt(value) ⇒ BSONInteger(value)
      case WarpLong(value) ⇒ BSONLong(value)
      case WarpBigInt(value) ⇒ BSONString(value.toString)
      case WarpFloat(value) ⇒ BSONDouble(value)
      case WarpDouble(value) ⇒ BSONDouble(value)
      case WarpBigDecimal(value) ⇒ BSONString(value.toString)
      case WarpUuid(value) ⇒ BsonConverter.uuidToBson(value)
      case WarpUri(value) ⇒ BSONString(value.toString)
      case WarpDateTime(value) ⇒ BSONString(value.toString)
      case WarpLocalDateTime(localDateTime) ⇒ BSONTimestamp(localDateTime.atOffset(_root_.java.time.ZoneOffset.UTC).toInstant().toEpochMilli())
      case WarpDuration(value) ⇒ BSONLong(value.toNanos)
    }

  protected override def getObjectRepr(warpObject: WarpObject): BSONDocument = {
    val head: Vector[(String, BSONValue)] =
      warpObject.warpDescriptor.map(rd ⇒ (WarpDescriptor.defaultKey → BSONString(rd.toParsableString()))).toVector

    val elements =
      head ++ (
        if (warpObject.elements.isEmpty)
          Vector.empty[(String, BSONValue)]
        else
          warpObject.elements.map(elem ⇒ createElemRepr(elem)).flatten)

    BSONDocument(elements)
  }

  protected override def foldReprs(elems: Traversable[ValueRepr]): BSONValue =
    BSONArray(elems.toList)

  protected override def foldTuple2Reprs(tuple: (BSONValue, BSONValue)): BSONValue =
    BSONArray(tuple._1, tuple._2)

  protected override def foldTuple3Reprs(tuple: (BSONValue, BSONValue, BSONValue)): BSONValue =
    BSONArray(tuple._1, tuple._2, tuple._3)

  protected override def foldAssocRepr(assoc: Traversable[(BSONValue, BSONValue)]): BSONValue =
    BSONArray(assoc.map(x ⇒ foldTuple2Reprs(x)))

  //@tailrec
  protected override def foldTreeRepr(tree: scalaz.Tree[BSONValue]): BSONValue =
    BSONArray(tree.rootLabel, tree.subForest.map(foldTreeRepr))

  protected override def foldByteArrayRepr(bytes: IndexedSeq[Byte]): BSONValue =
    BSONBinary(bytes.toArray, Subtype.GenericBinarySubtype)

  protected override def foldBlobRepr(bytes: IndexedSeq[Byte]): BSONValue =
    foldByteArrayRepr(bytes)

  private def createElemRepr(elem: WarpElement): Option[(String, BSONValue)] =
    elem.value map (v ⇒ elem.label → transform(v))
}