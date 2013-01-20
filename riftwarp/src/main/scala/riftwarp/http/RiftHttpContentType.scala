package riftwarp.http

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

sealed trait RiftHttpContentType {
  def tryGetHeaderValue: Option[String]
  def tryGetChannel: Option[RiftHttpChannel]
  def tryGetRiftDescriptor: Option[RiftDescriptor]
  def args: Map[String, String]

  def getChannel: AlmValidation[RiftHttpChannel] = option.cata(tryGetChannel)(ch => ch.success, ElementNotFoundProblem("channel").failure)
  def getHeaderValue: AlmValidation[String] = option.cata(tryGetHeaderValue)(v => v.success, ElementNotFoundProblem("headerValue").failure)
  def getRiftDescriptor: AlmValidation[RiftDescriptor] = option.cata(tryGetRiftDescriptor)(v => v.success, ElementNotFoundProblem("RiftDescriptor").failure)
}

case object RiftHttpNoContentContentType extends RiftHttpContentType {
  val tryGetHeaderValue = None
  val tryGetChannel = None
  val args = Map.empty[String, String]
  val tryGetRiftDescriptor = None
}

sealed trait RiftHttpContentTypeWithChannel extends RiftHttpContentType {
  def channel: RiftHttpChannel
  def headerValue: String
}

case class RiftHttpChannelContentType(channel: RiftHttpChannel, args: Map[String, String] = Map()) extends RiftHttpContentTypeWithChannel {
  val headerValue = (channel.httpContentType :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
  def tryGetHeaderValue = Some(headerValue)
  def tryGetChannel = Some(channel)
  val tryGetRiftDescriptor = None
}

case class RiftHttpQualifiedContentType(riftDescriptor: RiftDescriptor, channel: RiftHttpChannel, args: Map[String, String] = Map()) extends RiftHttpContentTypeWithChannel {
  def headerValue = {
    val typeStr = "vnd." + riftDescriptor.identifier + "+" + channel.httpContentTypeExt
    val protocol = option.cata(riftDescriptor.version)(v => typeStr + ";version=" + v, typeStr)
    (protocol :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
  }
  def tryGetHeaderValue = Some(headerValue)
  def tryGetChannel = Some(channel)
  val tryGetRiftDescriptor = Some(riftDescriptor)
}

object RiftHttpContentType {
  def apply(): RiftHttpContentType = RiftHttpNoContentContentType

  def apply(channel: RiftHttpChannel, args: Map[String, String]): RiftHttpContentTypeWithChannel =
    RiftHttpChannelContentType(channel, args)
  def apply(riftDescriptor: RiftDescriptor, channel: RiftHttpChannel, args: Map[String, String]): RiftHttpContentTypeWithChannel =
    RiftHttpQualifiedContentType(riftDescriptor, channel, args)
  def apply(clazz: Class[_], version: Int, channel: RiftHttpChannel, args: Map[String, String]): RiftHttpContentTypeWithChannel =
    RiftHttpQualifiedContentType(RiftDescriptor(clazz, version), channel, args)
  def apply(clazz: Class[_], channel: RiftHttpChannel, args: Map[String, String]): RiftHttpContentTypeWithChannel =
    RiftHttpQualifiedContentType(RiftDescriptor(clazz), channel, args)

  val PlainText = RiftHttpChannelContentType(RiftChannel.Text)

  def parse(rawContent: String): AlmValidation[RiftHttpContentTypeWithChannel] =
    sys.error("")
}