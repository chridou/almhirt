package riftwarp.http

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

sealed trait HttpContentType {
  def tryGetHeaderValue: Option[String]
  def tryGetChannel: Option[RiftHttpChannel]
  def tryGetTypeDescriptor: Option[TypeDescriptor]
  def args: Map[String, String]

  def getChannel: AlmValidation[RiftHttpChannel] = option.cata(tryGetChannel)(ch => ch.success, ElementNotFoundProblem("channel").failure)
  def getHeaderValue: AlmValidation[String] = option.cata(tryGetHeaderValue)(v => v.success, ElementNotFoundProblem("headerValue").failure)
  def getTypeDescriptor: AlmValidation[TypeDescriptor] = option.cata(tryGetTypeDescriptor)(v => v.success, ElementNotFoundProblem("TypeDescriptor").failure)
}

case object HttpNoContentContentType extends HttpContentType {
  val tryGetHeaderValue = None
  val tryGetChannel = None
  val args = Map.empty
  val tryGetTypeDescriptor = None
}

sealed trait HttpContentTypeWithChannel extends HttpContentType {
  def channel: RiftHttpChannel
  def headerValue: String
}

case class HttpChannelContentType(channel: RiftHttpChannel, args: Map[String, String] = Map()) extends HttpContentTypeWithChannel {
  val headerValue = (channel.httpContentType :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
  def tryGetHeaderValue = Some(headerValue)
  def tryGetChannel = Some(channel)
  val tryGetTypeDescriptor = None
}

case class HttpQualifiedContentType(typeDescriptor: TypeDescriptor, channel: RiftHttpChannel, args: Map[String, String] = Map()) extends HttpContentTypeWithChannel {
  def headerValue = {
    val typeStr = "vnd." + typeDescriptor.identifier + "+" + channel.httpContentTypeExt
    val protocol = option.cata(typeDescriptor.version)(v => typeStr + ";version=" + v, typeStr)
    (protocol :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
  }
  def tryGetHeaderValue = Some(headerValue)
  def tryGetChannel = Some(channel)
  val tryGetTypeDescriptor = Some(typeDescriptor)
}

object HttpContentType {
  def apply(): HttpContentType = HttpNoContentContentType

  def apply(channel: RiftHttpChannel, args: Map[String, String]): HttpContentType =
    HttpChannelContentType(channel, args)
  def apply(typeDescriptor: TypeDescriptor, channel: RiftHttpChannel, args: Map[String, String]): HttpContentType =
    HttpQualifiedContentType(typeDescriptor, channel, args)
  def apply(clazz: Class[_], version: Int, channel: RiftHttpChannel, args: Map[String, String]): HttpContentType =
    HttpQualifiedContentType(TypeDescriptor(clazz, version), channel, args)
  def apply(clazz: Class[_], channel: RiftHttpChannel, args: Map[String, String]): HttpContentType =
    HttpQualifiedContentType(TypeDescriptor(clazz), channel, args)

  val PlainText = HttpChannelContentType(RiftChannel.Text)

  def parse(rawContent: String): AlmValidation[HttpContentType] =
    sys.error("")
}