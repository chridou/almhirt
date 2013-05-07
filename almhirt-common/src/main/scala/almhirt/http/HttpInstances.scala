package almhirt.http

import almhirt.common._

trait HttpRequestInstances[T] {
  def contentTypeExtractor: HttpContentTypeExtractor[T]
  def payloadExtractor: PayloadExtractor[T]
}

trait HttpInstances {
  def channelExtractor: ChannelExtractor
  def classifiesChannels: ClassifiesChannels
  def errorResponseGenerator: HttpErrorResponseGenerator
}

