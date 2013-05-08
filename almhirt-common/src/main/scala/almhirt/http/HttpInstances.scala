package almhirt.http

import almhirt.common._

trait HttpInstances {
  def channelExtractor: ChannelExtractor
  def classifiesChannels: ClassifiesChannels
  def errorResponseGenerator: HttpErrorResponseGenerator
}

