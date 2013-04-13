package riftwarp.impl.dematerializers

import java.util.{UUID => JUUID}
import scala.reflect.ClassTag
import scala.annotation.tailrec
import scala.xml.{ Elem => XmlElem, NodeSeq, UnprefixedAttribute, Null, TopScope}
import scalaz._, Scalaz._
import org.joda.time.DateTime
import almhirt.common._
import almhirt.serialization._
import riftwarp._
import riftwarp.components._

class ToXmlElemWarpSequencer(state: NodeSeq, descriptor: Option[RiftDescriptor], override val isRoot: Boolean, override val path: List[String])(implicit hasDecomposers: HasDecomposers) extends BaseWarpSequencer[DimensionXmlElem](classOf[DimensionXmlElem], hasDecomposers: HasDecomposers) with NoneIsHandledUnified[DimensionXmlElem] {
  val channel = RiftChannel.Xml
  val toolGroup = ToolGroup.StdLib
  
  val dematerializer = ToXmlElemDematerializer
  override def dematerialize = 
    DimensionXmlElem(
    descriptor match {
    case Some(desc) => XmlElem(null, desc.unqualifiedName, new UnprefixedAttribute("type", desc.toParsableString(), new UnprefixedAttribute("style", "noisy", Null)), TopScope, true, state: _*)
    case None => XmlElem(null, "Something", new UnprefixedAttribute("style", "noisy", Null), TopScope, true, state: _*)
  })
  
  protected def noneHandler(ident: String): ToXmlElemWarpSequencer = this

  protected def spawnNew(pathPrefix: List[String]): ToXmlElemWarpSequencer = new ToXmlElemWarpSequencer(NodeSeq.Empty, None, false, pathPrefix ++ path)

  protected override def addReprValue(ident: String, value: XmlElem): WarpSequencer[DimensionXmlElem] = addPart(ident, value)
  
  protected override def insertWarpSequencer(ident: String, warpSequencer: WarpSequencer[DimensionXmlElem]) =
    addPart(ident, warpSequencer.dematerialize.manifestation)

  def addPart(ident: String, part: XmlElem): ToXmlElemWarpSequencer = {
    val nextPart = XmlElem(null, ident, Null, TopScope, true, part)
    new ToXmlElemWarpSequencer(state ++ nextPart, descriptor, isRoot, path)
  }

  override def addRiftDescriptor(descriptor: RiftDescriptor) = 
    new ToXmlElemWarpSequencer(state, Some(descriptor), isRoot, path)
}

object ToXmlElemWarpSequencer extends WarpSequencerFactory[DimensionXmlElem] {
  val channel = RiftXml()
  val tDimension = classOf[DimensionXmlElem].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()
  def apply(path: List[String])(implicit hasDecomposers: HasDecomposers): ToXmlElemWarpSequencer = 
    new ToXmlElemWarpSequencer(NodeSeq.Empty, None, true, path)
  def apply(isRoot: Boolean, path: List[String])(implicit hasDecomposers: HasDecomposers): ToXmlElemWarpSequencer = 
    new ToXmlElemWarpSequencer(NodeSeq.Empty, None, isRoot, path)
  def createWarpSequencer(implicit hasDecomposers: HasDecomposers): AlmValidation[ToXmlElemWarpSequencer] =
    apply(true, Nil).success
}