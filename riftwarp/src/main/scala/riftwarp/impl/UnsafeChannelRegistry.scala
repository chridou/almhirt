package riftwarp.impl

import scalaz.std.option
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp.components.ChannelRegistry
import riftwarp.RiftChannel

class UnsafeChannelRegistry extends ChannelRegistry {
  private val channels = collection.mutable.HashMap[String, RiftChannel]()
  private val httpChannels = collection.mutable.HashMap[String, RiftChannel]()

  def memoizeChannel(channel: RiftChannel) {
    channels += (channel.channelType -> channel)
    (List(channel.httpContentType, channel.httpContentTypeExt).flatten ++ channel.moreLookUpSymbols).foreach(x =>
      httpChannels += (x -> channel))
  }
  def getChannel(ident: String): AlmValidation[RiftChannel] =
    option.cata(channels.get(ident))(
      found => found.success,
      ElementNotFoundProblem("No channel found for '%s'".format(ident)).failure)

  def lookUpFromHttpContentType(contentType: String): AlmValidation[RiftChannel] =
    option.cata(httpChannels.get(contentType))(
      found => found.success,
      ElementNotFoundProblem("No channel found for content type '%s'".format(contentType)).failure)
}

object UnsafeChannelRegistry{
  def apply() = new UnsafeChannelRegistry
}