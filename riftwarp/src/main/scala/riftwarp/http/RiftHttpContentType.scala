package riftwarp.http

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

sealed trait RiftHttpContentType {
  def args: Map[String, String]

  def getHeaderValue(implicit ops: RiftHttpContentTypeOps): AlmValidation[String] = ops.headerValue(this)
  def getChannel: AlmValidation[RiftHttpChannel]
  def getRiftDescriptor: AlmValidation[RiftDescriptor]

  def tryGetHeaderValue(implicit ops: RiftHttpContentTypeOps) = getHeaderValue.toOption
  def tryGetChannel = getChannel.toOption
  def tryGetRiftDescriptor = getRiftDescriptor.toOption
}

case object RiftHttpNoContentContentType extends RiftHttpContentType {
  override val args = Map.empty[String, String]
  override val getChannel: AlmValidation[RiftHttpChannel] = UnspecifiedProblem("RiftHttpNoContentContentType has no channel").failure
  override val getRiftDescriptor: AlmValidation[RiftDescriptor] = UnspecifiedProblem("RiftHttpNoContentContentType has no RiftDescriptor").failure
}

sealed trait RiftHttpContentTypeWithChannel extends RiftHttpContentType {
  def channel: RiftHttpChannel
  def safeHeaderValue(implicit ops: RiftHttpContentTypeOps): String = ops.safeHeaderValue(this)
}

final case class RiftHttpChannelContentType(channel: RiftHttpChannel, args: Map[String, String] = Map()) extends RiftHttpContentTypeWithChannel {
  override val getChannel: AlmValidation[RiftHttpChannel] = channel.success
  override val getRiftDescriptor: AlmValidation[RiftDescriptor] = UnspecifiedProblem("RiftHttpChannelContentType has no RiftDescriptor").failure
}

final case class RiftHttpQualifiedContentType(riftDescriptor: RiftDescriptor, channel: RiftHttpChannel, args: Map[String, String] = Map()) extends RiftHttpContentTypeWithChannel {
  override val getChannel: AlmValidation[RiftHttpChannel] = channel.success
  override val getRiftDescriptor: AlmValidation[RiftDescriptor] = riftDescriptor.success
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

  val TextPlain = RiftHttpChannelContentType(RiftChannel.Text)
  val TextJson = RiftHttpChannelContentType(RiftChannel.Json)
  val TextXml = RiftHttpChannelContentType(RiftChannel.Xml)
  val NoContent = RiftHttpNoContentContentType

  def parse(rawContent: String)(implicit ops: RiftHttpContentTypeOps): AlmValidation[RiftHttpContentTypeWithChannel] = ops.parse(rawContent)
}