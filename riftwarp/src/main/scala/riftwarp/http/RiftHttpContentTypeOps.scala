package riftwarp.http

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp.components.KnowsChannels

trait RiftHttpContentTypeOps {
  def headerValue(contentType: RiftHttpContentType): AlmValidation[String] =
    contentType match {
      case RiftHttpNoContentContentType =>
        UnspecifiedProblem("RiftHttpNoContentContentType has no header value").failure
      case withChannel: RiftHttpContentTypeWithChannel =>
        safeHeaderValue(withChannel).success
    }

  def safeHeaderValue(contentType: RiftHttpContentTypeWithChannel): String
  def parse(rawContent: String): AlmValidation[RiftHttpContentTypeWithChannel]
}

class RiftHttpContentTypeWithPrefixOps(contentTypePrefix: String, channels: KnowsChannels) extends RiftHttpContentTypeOps {
  require(!contentTypePrefix.trim().isEmpty())
  
  override def safeHeaderValue(contentType: RiftHttpContentTypeWithChannel): String =
    contentType match {
      case RiftHttpChannelContentType(channel, args) =>
        (channel.httpContentType :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
      case RiftHttpQualifiedContentType(riftDescriptor, channel, args) =>
        val typeStr = "application/vnd." + contentTypePrefix + "." + riftDescriptor.identifier + "+" + channel.httpContentTypeExt
        val protocol = option.cata(riftDescriptor.version)(v => typeStr + ";version=" + v, typeStr)
        (protocol :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
    }

  override def parse(rawContent: String): AlmValidation[RiftHttpContentTypeWithChannel] = {
    if (rawContent.trim().isEmpty())
      BadDataProblem("Raw content do parse a content type from may not be empty").failure
    else {
      val parts = rawContent.split(";").map(_.trim())
      val sample = "application/vnd." + contentTypePrefix + "."
      if (parts(0).startsWith(sample)) {
        ???
      } else {
        ???
      }
    }
  }
}

class RiftHttpContentTypeWithoutPrefixOps(channels: KnowsChannels) extends RiftHttpContentTypeOps {
  override def safeHeaderValue(contentType: RiftHttpContentTypeWithChannel): String =
    contentType match {
      case RiftHttpChannelContentType(channel, args) =>
        (channel.httpContentType :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
      case RiftHttpQualifiedContentType(riftDescriptor, channel, args) =>
        val typeStr = "application/vnd." + riftDescriptor.identifier + "+" + channel.httpContentTypeExt
        val protocol = option.cata(riftDescriptor.version)(v => typeStr + ";version=" + v, typeStr)
        (protocol :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
    }

  override def parse(rawContent: String): AlmValidation[RiftHttpContentTypeWithChannel] = {
    if (rawContent.trim().isEmpty())
      BadDataProblem("Raw content do parse a content type from may not be empty").failure
    else {
      ???
    }
  }
}
