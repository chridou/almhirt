package riftwarp.components

import almhirt.common._
import almhirt.serialization._
import riftwarp._

trait ExtractorFactory[TDimension <: RiftDimension] {
  /**
   * Xml, Json, etc
   */
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createExtractor(from: TDimension, blobPolicy: BlobDeserializationPolicy)(implicit hasRecomposers: HasRecomposers): AlmValidation[Extractor]
  def createExtractorRaw(from: AnyRef, blobPolicy: BlobDeserializationPolicy)(implicit hasRecomposers: HasRecomposers): AlmValidation[Extractor] = 
    createExtractor(from.asInstanceOf[TDimension], blobPolicy)(hasRecomposers)
  def createExtractor(from: TDimension)(implicit hasRecomposers: HasRecomposers): AlmValidation[Extractor] = createExtractor(from, BlobIntegrationDisabled)(hasRecomposers)
  def createExtractorRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers): AlmValidation[Extractor] = 
    createExtractorRaw(from, BlobIntegrationDisabled)(hasRecomposers)
}
