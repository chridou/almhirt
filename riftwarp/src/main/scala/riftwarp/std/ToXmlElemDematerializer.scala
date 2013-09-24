package riftwarp.std
import scala.annotation.tailrec
import scala.xml.{ Elem => XmlElem, NodeSeq, Null, TopScope, UnprefixedAttribute }
import scalaz._
import almhirt.common._
import riftwarp._

object ToNoisyXmlElemDematerializer extends DematerializerTemplate[XmlElem] {
  type ValueRepr = XmlElem
  type ObjRepr = XmlElem

  val channel = "xml"
  val dimension = classOf[XmlElem].getName()

  protected def valueReprToDim(repr: XmlElem): XmlElem =
    repr

  protected override final def getPrimitiveRepr(prim: WarpPrimitive): XmlElem =
    prim match {
      case WarpBoolean(value) => <Value type="Boolean">{ value.toString }</Value>
      case WarpString(value) => <Value type="String">{ value }</Value>
      case WarpByte(value) => <Value type="Byte">{ value.toString }</Value>
      case WarpInt(value) => <Value type="Int">{ value.toString }</Value>
      case WarpLong(value) => <Value type="Long">{ value.toString }</Value>
      case WarpBigInt(value) => <Value type="BigInt">{ value.toString }</Value>
      case WarpFloat(value) => <Value type="Float">{ value.toString }</Value>
      case WarpDouble(value) => <Value type="Double">{ value.toString }</Value>
      case WarpBigDecimal(value) => <Value type="BigDecimal">{ value.toString }</Value>
      case WarpUuid(value) => <Value type="Uuid">{ value.toString }</Value>
      case WarpUri(value) => <Value type="Uri">{ value.toString }</Value>
      case WarpDateTime(value) => <Value type="DateTime">{ value.toString() }</Value>
      case WarpLocalDateTime(value) => <Value type="LocalDateTime">{ value.toString() }</Value>
      case WarpDuration(value) => <Value type="Duration">{ value.toString() }</Value>
    }

  protected override def getObjectRepr(warpObject: WarpObject): XmlElem = {
    val elems = warpObject.elements.map(createElemRepr)
    warpObject.warpDescriptor match {
      case Some(desc) => XmlElem(null, desc.unqualifiedName, new UnprefixedAttribute("type", desc.toParsableString(), new UnprefixedAttribute("style", "noisy", Null)), TopScope, true, elems: _*)
      case None => XmlElem(null, "Something", new UnprefixedAttribute("style", "noisy", Null), TopScope, true, elems: _*)
    }
  }

  protected override def foldReprs(elems: Traversable[ValueRepr]): XmlElem =
    createCollectionInnerXml("Collection", elems.toList, NodeSeq.Empty)

  protected override def foldTuple2Reprs(tuple: (ValueRepr, ValueRepr)): XmlElem =
    <Tuple2><a>{ tuple._1 }</a><b>{ tuple._2 }</b></Tuple2>

  protected override def foldTuple3Reprs(tuple: (ValueRepr, ValueRepr, ValueRepr)): XmlElem =
    <Tuple3><a>{ tuple._1 }</a><b>{ tuple._2 }</b><c>{ tuple._3 }</c></Tuple3>
    
  protected override def foldAssocRepr(assoc: Traversable[(ValueRepr, ValueRepr)]): XmlElem =
    createCollectionInnerXml("Assoc", assoc.toList.map(x => foldTuple2Reprs(x)), NodeSeq.Empty)

  protected override def foldTreeRepr(tree: scalaz.Tree[ValueRepr]): XmlElem =
    <Tree>{ foldTree(tree) }</Tree>

  protected override def foldByteArrayRepr(bytes: IndexedSeq[Byte]): XmlElem =
    <Bytes type="Bytes">{ bytes.mkString(",") }</Bytes>

  protected override def foldBlobRepr(bytes: IndexedSeq[Byte]): XmlElem = {
    val base64 = org.apache.commons.codec.binary.Base64.encodeBase64String(bytes.toArray)
    <Base64 type="Base64">{ scala.xml.PCData(base64) }</Base64>
  }

  private def createElemRepr(elem: WarpElement): XmlElem =
    elem.value match {
      case Some(v) => XmlElem(null, elem.label, Null, TopScope, true, transform(v))
      case None => XmlElem(null, elem.label, Null, TopScope, true)
    }

  @tailrec
  private def createCollectionInnerXml(name: String, rest: List[XmlElem], acc: NodeSeq): XmlElem =
    rest match {
      case Nil => XmlElem(null, name, Null, TopScope, true, acc: _*)
      case h :: t => createCollectionInnerXml(name, t, acc ++ h)
    }

  private def foldTree(tree: scalaz.Tree[XmlElem]): XmlElem = {
    val items = tree.subForest.map(foldTree).toList
    if (items.isEmpty)
      XmlElem(null, "leaf", Null, TopScope, true, tree.rootLabel)
    else {
      val elems = createCollectionInnerXml("subforest", tree.subForest.map(foldTree).toList, NodeSeq.Empty)
      XmlElem(null, "node", Null, TopScope, true, (<label>{tree.rootLabel}</label> :: elems :: Nil): _*)
    }
  }
}