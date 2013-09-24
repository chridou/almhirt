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



object ToBsonDematerializer extends DematerializerTemplate[BSONDocument] {
  type ValueRepr = BSONValue
  
  val channel = "bson"
  val dimension = classOf[BSONDocument].getName()
  
  protected def valueReprToDim(repr: Cord): Cord @@ WarpTags.Json =
    WarpTags.JsonCord(repr)

  protected override final def getPrimitiveRepr(prim: WarpPrimitive): BSONValue =
    prim match {
      case WarpBoolean(value) => BSONBoolean(value)
      case WarpString(value) => BSONString(value)
      case WarpByte(value) => BSONInteger(value.toInt)
      case WarpInt(value) => BSONInteger(value)
      case WarpLong(value) => BSONLong(value)
      case WarpBigInt(value) => BSONString(value.toString)
      case WarpFloat(value) => BSONDouble(value)
      case WarpDouble(value) => BSONDouble(value)
      case WarpBigDecimal(value) => BSONString(value.toString)
      case WarpUuid(value) => BSONBinary(UuidConverter.uuidToBytes(value), Subtype.UuidSubtype)
      case WarpUri(value) => BSONString(value.toString)
      case WarpDateTime(value) => BSONString(value.toString)
      case WarpLocalDateTime(value) => BSONString(value.toString)
      case WarpDuration(value) => BSONString(value.toString)
    }

  protected override def getObjectRepr(warpObject: WarpObject): BSONValue = {
    val head =
      warpObject.warpDescriptor match {
        case Some(rd) => 
          Cord(s"""{"${WarpDescriptor.defaultKey}":"${rd.toParsableString(";")}"""") ++ (if(warpObject.elements.isEmpty) Cord.empty else Cord(","))
        case None => Cord("{")
      }
    val elements =
      if (warpObject.elements.isEmpty)
        Cord("")
      else {
        val items = warpObject.elements.map(elem => createElemRepr(elem))
        items.drop(1).fold(items.head) { case (acc, x) => (acc :- ',') ++ x }
      }
    head ++ elements :- '}'
  }

  protected override def foldReprs(elems: Traversable[ValueRepr]): BSONValue =
    foldParts(elems.toList)

  protected override def foldTuple2Reprs(tuple: (BSONValue, BSONValue)): BSONValue =
    foldParts(tuple._1 :: tuple._2 :: Nil)

  protected override def foldTuple3Reprs(tuple: (BSONValue, BSONValue, BSONValue)): BSONValue =
    foldParts(tuple._1 :: tuple._2 :: tuple._3 :: Nil)
    
  protected override def foldAssocRepr(assoc: Traversable[(BSONValue, BSONValue)]): BSONValue =
    foldParts(assoc.toList.map(x => foldTuple2Reprs(x)))
    
  protected override def foldTreeRepr(tree: scalaz.Tree[BSONValue]): BSONValue =
    foldParts(tree.rootLabel :: foldParts(tree.subForest.map(foldTreeRepr).toList) :: Nil)

  protected override def foldByteArrayRepr(bytes: IndexedSeq[Byte]): BSONValue =
    foldParts(bytes.map(b => Cord(b.toString)).toList)

  protected override def foldBlobRepr(bytes: IndexedSeq[Byte]): BSONValue = 
    getObjectRepr(Base64BlobWarpPacker.asWarpObject(bytes))

  private def createElemRepr(elem: WarpElement): BSONValue =
    elem.value match {
      case Some(v) => s""""${elem.label}":""" + transform(v)
      case None => BSONElement()
    }


  @tailrec
  private def createInnerJson(rest: List[Cord], acc: Cord): BSONValue =
    rest match {
      case Nil => Cord("[]")
      case last :: Nil => '[' -: (acc ++ last) :- ']'
      case h :: t => createInnerJson(t, acc ++ h :- ',')
    }

  def foldParts(items: List[Cord]): BSONValue = createInnerJson(items, Cord.empty)
}