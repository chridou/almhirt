package riftwarp.http

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
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

  protected def processArgs(args: List[String]): Map[String, String] =
    args
      .map(_.split("=").map(_.trim()))
      .map { case Array(k, v) => Some((k, v)) case _ => None }
      .flatten
      .toMap

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

  override def parse(rawContent: String): AlmValidation[RiftHttpContentTypeWithChannel] =
    if (rawContent.trim().isEmpty())
      ParsingProblem("Cannot parse a content type from an empty string.").failure
    else {
      val parts = rawContent.split(";").map(_.trim()).toList
      val sample = "application/vnd."
      if (parts.head.startsWith(sample)) {
        val clean = parts.head.substring(0, sample.length()).split("+").map(_.trim())
        clean match {
          case Array(identifier, channelRaw) =>
            channels.lookUpFromHttpContentType(channelRaw).flatMap { channel =>
              val args = processArgs(parts.tail)
              (option.cata(args.get("version"))(
                v => almhirt.almvalidation.funs.parseIntAlm(v, "version").map(x => Some(x)),
                None.success)).map(version =>
                  RiftHttpQualifiedContentType(RiftDescriptor(identifier, version), channel, args))
            }
          case x =>
            ParsingProblem("$x is no valid content type pattern").failure
        }
      } else {
        channels.lookUpFromHttpContentType(parts.head).map(channel =>
          RiftHttpChannelContentType(channel, processArgs(parts.tail)))
      }
    }
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

  override def parse(rawContent: String): AlmValidation[RiftHttpContentTypeWithChannel] =
    if (rawContent.trim().isEmpty())
      ParsingProblem("Cannot parse a content type from an empty string.").failure
    else {
      val parts = rawContent.split(";").map(_.trim()).toList
      val sample = "application/vnd." + contentTypePrefix + "."
      if (parts.head.startsWith(sample)) {
        val clean = parts.head.substring(0, sample.length()).split("+").map(_.trim())
        clean match {
          case Array(identifier, channelRaw) =>
            channels.lookUpFromHttpContentType(channelRaw).flatMap { channel =>
              val args = processArgs(parts.tail)
              (option.cata(args.get("version"))(
                v => almhirt.almvalidation.funs.parseIntAlm(v, "version").map(x => Some(x)),
                None.success)).map(version =>
                  RiftHttpQualifiedContentType(RiftDescriptor(identifier, version), channel, args))
            }
          case x =>
            ParsingProblem("$x is no valid content type pattern").failure
        }
      } else {
        channels.lookUpFromHttpContentType(parts.head).map(channel =>
          RiftHttpChannelContentType(channel, processArgs(parts.tail)))
      }
    }
}

