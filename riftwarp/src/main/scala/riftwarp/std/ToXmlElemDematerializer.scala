package riftwarp.std
import scala.annotation.tailrec
import scala.xml.{ Elem => XmlElem, NodeSeq, Null, TopScope, UnprefixedAttribute }
import scalaz._
import almhirt.common._
import riftwarp._

object ToNoisyXmlElemDematerializer extends DematerializerTemplate[XmlElem] {
  type ValueRepr = XmlElem

  protected def valueReprToDim(repr: XmlElem): XmlElem =
    repr

  protected override final def getPrimitiveRepr(prim: WarpPrimitive): XmlElem =
    prim match {
      case WarpBoolean(value) => <value type="Boolean">{ value.toString }</value>
      case WarpString(value) => <value type="String">{ value }</value>
      case WarpByte(value) => <value type="Byte">{ value.toString }</value>
      case WarpInt(value) => <value type="Int">{ value.toString }</value>
      case WarpLong(value) => <value type="Long">{ value.toString }</value>
      case WarpBigInt(value) => <value type="BigInt">{ value.toString }</value>
      case WarpFloat(value) => <value type="Float">{ value.toString }</value>
      case WarpDouble(value) => <value type="Double">{ value.toString }</value>
      case WarpBigDecimal(value) => <value type="BigDecimal">{ value.toString }</value>
      case WarpUuid(value) => <value type="Uuid">{ value.toString }</value>
      case WarpUri(value) => <value type="Uri">{ value.toString }</value>
      case WarpDateTime(value) => <value type="DateTime">{ value.toString() }</value>
    }

  protected override def getObjectRepr(warpObject: WarpObject): XmlElem = {
    val elems = warpObject.elements.map(createElemRepr)
    warpObject.warpDescriptor match {
      case Some(desc) => XmlElem(null, desc.unqualifiedName, new UnprefixedAttribute("type", desc.toParsableString(), new UnprefixedAttribute("style", "noisy", Null)), TopScope, true, elems: _*)
      case None => XmlElem(null, "Something", new UnprefixedAttribute("style", "noisy", Null), TopScope, true, elems: _*)
    }
  }

  protected override def foldReprs(elems: Traversable[ValueRepr]): XmlElem =
    foldParts(elems.toList)

  protected override def foldTupleReprs(tuple: (ValueRepr, ValueRepr)): XmlElem =
    foldParts(tuple._1 :: tuple._2 :: Nil)

  protected override def foldTreeRepr(tree: scalaz.Tree[ValueRepr]): XmlElem =
    foldParts(tree.rootLabel :: foldParts(tree.subForest.map(foldTreeRepr).toList) :: Nil)

  protected override def foldByteArrayRepr(bytes: Array[Byte]): XmlElem =
    <bytes type="Bytes">{ bytes.mkString(",") }</bytes>

  protected override def foldBlobRepr(bytes: Array[Byte]): XmlElem = {
    val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes)
    <base64 type="Base64">{ scala.xml.PCData(base64) }</base64>
  }

  private def createElemRepr(elem: WarpElement): XmlElem =
    elem.value match {
      case Some(v) => XmlElem(null, elem.label, Null, TopScope, true, transform(v))
      case None => XmlElem(null, elem.label, Null, TopScope, true)
    }

  @tailrec
  private def createInnerXml(rest: List[XmlElem], acc: NodeSeq): XmlElem =
    rest match {
      case Nil => <collection>{ acc }</collection>
      case h :: t => createInnerXml(t, acc ++ h)
    }

  private def foldParts(items: List[XmlElem]): XmlElem = createInnerXml(items, NodeSeq.Empty)

  private def foldTree(tree: scalaz.Tree[XmlElem]): XmlElem =
    foldParts(tree.rootLabel :: foldParts(tree.subForest.map(foldTree).toList) :: Nil)

}