package almhirt.http

import almhirt.common._

trait HttpRequestExtractor[TFrom] {
  final def apply(from: TFrom)(implicit channelExtractor: ChannelExtractor, classifiesChannels: ClassifiesChannels) = extractRequest(from)
  def extractRequest(from: TFrom)(implicit channelExtractor: ChannelExtractor, classifiesChannels: ClassifiesChannels): AlmValidation[HttpRequest]
}

trait HttpRequestExtractorTemplate[TFrom] extends HttpRequestExtractor[TFrom] {
  def getRawContentType(from: TFrom): AlmValidation[String]
  def getAcceptsContent(from: TFrom): AlmValidation[List[(HttpContentType, Option[Double])]]
  def getBinaryContent(from: TFrom): AlmValidation[BinaryBody]
  def getTextContent(from: TFrom): AlmValidation[TextBody]

  override def extractRequest(from: TFrom)(implicit channelExtractor: ChannelExtractor, classifiesChannels: ClassifiesChannels): AlmValidation[HttpRequest] =
    for {
      rawContentType <- getRawContentType(from)
      contentType <- HttpContentType.fromString(rawContentType)
      channel <- channelExtractor(contentType)
      contentTypeClassifier <- classifiesChannels(channel)
      payload <- contentTypeClassifier match {
        case TextChannel => getTextContent(from)
        case BinaryChannel => getBinaryContent(from)
      }
      accepts <- getAcceptsContent(from)
    } yield HttpRequest(HttpContent(contentType, payload), accepts)

}