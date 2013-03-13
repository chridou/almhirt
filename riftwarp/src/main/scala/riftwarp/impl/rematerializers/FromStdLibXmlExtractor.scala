package riftwarp.impl.rematerializers

import scala.xml.{ Elem => XmlElem }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.xml.all._
import riftwarp._
import riftwarp.components.HasRecomposers
import riftwarp.components.ExtractorFactory

class FromStdLibXmlExtractor(current: XmlElem, fetchBlobData: BlobFetch)(implicit hasRecomposers: HasRecomposers) extends ExtractorTemplate[DimensionXmlElem](fetchBlobData) with NoneHandlingExtractor {
  override val rematerializer = FromStdLibXmlRematerializer

  def getValue(ident: String): AlmValidation[XmlElem] =
    (current \? ident).fold(
      fail => fail.failure,
      succ => succ match {
        case Some(v) => v.success
        case None => KeyNotFoundProblem(s"No value found for key '$ident'").failure
      })

  def spawnNew(value: XmlElem): AlmValidation[Extractor] = (new FromStdLibXmlExtractor(value, fetchBlobData)).success

  def hasValue(ident: String) = getValue(ident: String).isSuccess

  override def getRiftDescriptor: AlmValidation[RiftDescriptor] =
    (current \@ "type").flatMap(potentialDescriptor => RiftDescriptor.parse(potentialDescriptor))

  override def tryGetRiftDescriptor: AlmValidation[Option[RiftDescriptor]] =
    if (hasValue(RiftDescriptor.defaultKey)) getRiftDescriptor.map(Some(_)) else None.success
}

object FromStdLibXmlExtractor extends ExtractorFactory[DimensionXmlElem] {
  val channel = RiftXml()
  val tDimension = classOf[DimensionXmlElem].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(current: XmlElem, fetchBlobData: BlobFetch)(implicit hasRecomposers: HasRecomposers): FromStdLibXmlExtractor =
    new FromStdLibXmlExtractor(current, fetchBlobData)
  def apply(current: XmlElem)(implicit hasRecomposers: HasRecomposers): FromStdLibXmlExtractor =
    new FromStdLibXmlExtractor(current, NoFetchBlobFetch)
  def createExtractor(from: DimensionXmlElem, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] =
    apply(from.manifestation, fetchBlobs).success
}

object FromStdLibXmlStringExtractor extends ExtractorFactory[DimensionString] {
  val channel = RiftXml()
  val tDimension = classOf[DimensionString].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(from: String, fetchBlobData: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] = 
    xmlFromString(from).map(xml => FromStdLibXmlExtractor(xml, fetchBlobData) )

  def createExtractor(from: DimensionString, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] =
    apply(from.manifestation, fetchBlobs)(hasRecomposers)
}


object FromStdLibXmlCordExtractor extends ExtractorFactory[DimensionCord] {
  val channel = RiftXml()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  import scalaz.Cord
  def apply(from: Cord, fetchBlobData: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] = 
    FromStdLibXmlStringExtractor(from.toString, fetchBlobData)

  def createExtractor(from: DimensionCord, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] =
    apply(from.manifestation, fetchBlobs)(hasRecomposers)
}