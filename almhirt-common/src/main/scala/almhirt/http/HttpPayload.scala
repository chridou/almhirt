package almhirt.http

import almhirt.common.AlmValidation

sealed trait HttpPayload { def data: Any }
final case class BinaryPayload(data: Array[Byte]) extends HttpPayload
final case class TextPayload(data: String) extends HttpPayload


trait PayloadExtractor[T] {
  final def apply(from: T, classifier: ChannelClassifier): AlmValidation[HttpPayload] = getPayload(from, classifier)
  def getPayload(from: T, classifier: ChannelClassifier): AlmValidation[HttpPayload]
}