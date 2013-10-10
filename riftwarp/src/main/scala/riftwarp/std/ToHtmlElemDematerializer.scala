package riftwarp.std

import scala.annotation.tailrec
import scala.xml.{ Elem => XmlElem, NodeSeq, Null, TopScope, UnprefixedAttribute }
import scalaz._
import almhirt.common._
import riftwarp._

object ToHtmlElemDematerializer extends DematerializerTemplate[XmlElem] {
  type ValueRepr = XmlElem
  type ObjRepr = XmlElem

  val channel = "html"
  val dimension = classOf[XmlElem].getName()

  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): XmlElem =
    <html>
      <head>
      </head>
      <body>
        { valueReprToDim(transform(what)) }
      </body>
    </html>

  protected def valueReprToDim(repr: XmlElem): XmlElem =
    repr

  protected override final def getPrimitiveRepr(prim: WarpPrimitive): XmlElem =
    prim match {
      case WarpBoolean(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpString(value) => <span class="primitive-value">{ value }</span>
      case WarpByte(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpInt(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpLong(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpBigInt(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpFloat(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpDouble(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpBigDecimal(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpUuid(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpUri(value) => <span class="primitive-value">{ value.toString }</span>
      case WarpDateTime(value) => <span class="primitive-value">{ value.toString() }</span>
      case WarpLocalDateTime(value) => <span class="primitive-value">{ value.toString() }</span>
      case WarpDuration(value) => <span class="primitive-value">{ value.defaultUnitString }</span>
    }

  protected override def getObjectRepr(warpObject: WarpObject): XmlElem = {
    warpObject.warpDescriptor match {
      case Some(desc) =>
        <div style="border:1px solid black; padding: 10px;">
          <h2>{ desc.unqualifiedName }</h2>
          <table>
            { warpObject.elements.map(createElemRepr) }
          </table>
        </div>
      case None =>
        <div class="border:1px solid black; padding: 10px;">
          <table>
            { warpObject.elements.map(createElemRepr) }
          </table>
        </div>
    }
  }

  protected override def foldReprs(elems: Traversable[XmlElem]): XmlElem = {
    val inner = elems.map(x => <tr><td>{ x }</td></tr>)
    <div>
      <span>{ s"Collection of ${elems.size} items" }</span><br/>
      <table>
        { inner }
      </table>
    </div>
  }

  protected override def foldTuple2Reprs(tuple: (XmlElem, XmlElem)): XmlElem =
    <span>{ s"(${tuple._1};${tuple._2})" }</span>

  protected override def foldTuple3Reprs(tuple: (XmlElem, XmlElem, XmlElem)): XmlElem =
    <span>{ s"(${tuple._1};${tuple._2};${tuple._3})" }</span>

  protected override def foldAssocRepr(assoc: Traversable[(XmlElem, XmlElem)]): XmlElem = {
    val inner = assoc.map(x => <tr><td valign="top">{ x._1 }</td><td valign="top">=</td><td>{ x._2 }</td></tr>)
    <div>
      <span>{ s"Associative collection of ${assoc.size} items" }</span><br/>
      <table>
        <tr><th>Key</th><th/><th>Value</th></tr>
        { inner }
      </table>
    </div>
  }

  protected override def foldTreeRepr(tree: scalaz.Tree[XmlElem]): XmlElem =
    <div>
      <span>Tree</span><br/>
      { foldTree(tree) }
    </div>

  protected override def foldByteArrayRepr(bytes: IndexedSeq[Byte]): XmlElem =
    <div>
      <span>{ s"${bytes.size} bytes" }</span><br/>
      <span>{ bytes.mkString("[", ",", "]") }</span>
    </div>

  protected override def foldBlobRepr(bytes: IndexedSeq[Byte]): XmlElem = {
    <i>{ s"Binary[${bytes.size}]" }</i>
  }

  private def createElemRepr(elem: WarpElement): XmlElem =
    elem.value match {
      case Some(v) => <tr><td valign="top"><b>{ elem.label + ":" }</b></td><td>{ transform(v) }</td></tr>
      case None => <tr><td valign="top"><b>{ elem.label + ":" }</b></td><td><i>{ "null" }</i></td></tr>
    }

  private def foldTree(tree: scalaz.Tree[XmlElem]): XmlElem =
    <table>
      <tr><th>Label</th><th>Subforest</th></tr>
      <tr><td>{ tree.rootLabel }</td><td>{ tree.subForest.map(foldTree) }</td></tr>
    </table>

}