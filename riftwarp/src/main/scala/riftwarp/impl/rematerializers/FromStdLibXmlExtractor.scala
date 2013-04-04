package riftwarp.impl.rematerializers

import scala.xml.{ Elem => XmlElem }
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import almhirt.xml.all._
import riftwarp._
import riftwarp.components.HasRecomposers
import riftwarp.components.ExtractorFactory

class FromStdLibXmlExtractor(current: XmlElem, path: List[String], override val blobPolicy: BlobDeserializationPolicy)(implicit hasRecomposers: HasRecomposers) extends ExtractorTemplate[DimensionXmlElem](path) with NoneHandlingExtractor {
  override val rematerializer = FromStdLibXmlRematerializer

  override def getValue(ident: String): AlmValidation[XmlElem] = (current \! ident).flatMap(getFirstChildNode(_))

  override def spawnNew(ident: String)(value: XmlElem): AlmValidation[Extractor] = (new FromStdLibXmlExtractor(value, ident :: path, blobPolicy)).success

  override def hasValue(ident: String) = getValue(ident: String).isSuccess

  override def getRiftDescriptor: AlmValidation[RiftDescriptor] =
    (current \@ "type").flatMap(potentialDescriptor => RiftDescriptor.parse(potentialDescriptor))

  override def tryGetRiftDescriptor: AlmValidation[Option[RiftDescriptor]] =
    (current \@? "type") match {
      case None => None.success
      case Some(attValue) => RiftDescriptor.parse(attValue).map(Some(_))
    }
}

object FromStdLibXmlExtractor extends ExtractorFactory[DimensionXmlElem] {
  val channel = RiftXml()
  val tDimension = classOf[DimensionXmlElem].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(current: XmlElem, blobPolicy: BlobDeserializationPolicy)(implicit hasRecomposers: HasRecomposers): FromStdLibXmlExtractor =
    new FromStdLibXmlExtractor(current, current.label :: Nil, blobPolicy)
  def apply(current: XmlElem)(implicit hasRecomposers: HasRecomposers): FromStdLibXmlExtractor =
    new FromStdLibXmlExtractor(current, current.label :: Nil, BlobIntegrationDisabled)
  def createExtractor(from: DimensionXmlElem, blobPolicy: BlobDeserializationPolicy)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] =
    apply(from.manifestation, blobPolicy).success
}

object FromStdLibXmlStringExtractor extends ExtractorFactory[DimensionString] {
  val channel = RiftXml()
  val tDimension = classOf[DimensionString].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  def apply(from: String, blobPolicy: BlobDeserializationPolicy)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] =
    xmlFromString(from).map(xml => FromStdLibXmlExtractor(xml, blobPolicy))

  def createExtractor(from: DimensionString, blobPolicy: BlobDeserializationPolicy)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] =
    apply(from.manifestation, blobPolicy)(hasRecomposers)
}

object FromStdLibXmlCordExtractor extends ExtractorFactory[DimensionCord] {
  val channel = RiftXml()
  val tDimension = classOf[DimensionCord].asInstanceOf[Class[_ <: RiftDimension]]
  val toolGroup = ToolGroupStdLib()

  import scalaz.Cord
  def apply(from: Cord, blobPolicy: BlobDeserializationPolicy)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] =
    FromStdLibXmlStringExtractor(from.toString, blobPolicy)

  def createExtractor(from: DimensionCord, blobPolicy: BlobDeserializationPolicy)(implicit hasRecomposers: HasRecomposers): AlmValidation[FromStdLibXmlExtractor] =
    apply(from.manifestation, blobPolicy)(hasRecomposers)
}