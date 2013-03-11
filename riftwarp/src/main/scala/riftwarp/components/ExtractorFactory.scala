package riftwarp.components

import almhirt.common._
import riftwarp._

trait ExtractorFactory[TDimension <: RiftDimension] {
  /**
   * Xml, Json, etc
   */
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def createExtractor(from: TDimension, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[Extractor]
  def createExtractorRaw(from: AnyRef, fetchBlobs: BlobFetch)(implicit hasRecomposers: HasRecomposers): AlmValidation[Extractor] = 
    createExtractor(from.asInstanceOf[TDimension], fetchBlobs)(hasRecomposers)
  def createExtractor(from: TDimension)(implicit hasRecomposers: HasRecomposers): AlmValidation[Extractor] = createExtractor(from, NoFetchBlobFetch)(hasRecomposers)
  def createExtractorRaw(from: AnyRef)(implicit hasRecomposers: HasRecomposers): AlmValidation[Extractor] = 
    createExtractorRaw(from, NoFetchBlobFetch)(hasRecomposers)
}
