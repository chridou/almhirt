package riftwarp.http

import scalaz.std._
import almhirt.common._
import riftwarp._

sealed trait HttpContentType {
  def headerValue: String
  def channel: RiftChannel with RiftHttpChannel
  def args: Map[String, String]
  def tryGetTypeDescriptor: Option[TypeDescriptor]
}

case class HttpChannelContentType(channel: RiftChannel with RiftHttpChannel, args: Map[String, String] = Map()) extends HttpContentType {
  def headerValue = (channel.httpContentType :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
  val tryGetTypeDescriptor = None
}

case class HttpQualifiedContentType(typeDescriptor: TypeDescriptor, channel: RiftChannel with RiftHttpChannel, args: Map[String, String] = Map()) extends HttpContentType {
  def headerValue = {
    val typeStr = "vnd." + typeDescriptor.identifier + "+" + channel.httpContentTypeExt
    val protocol = option.cata(typeDescriptor.version)(v => typeStr + ";version=" + v, typeStr)
    (protocol :: (args.map { case (k, v) => k + "=" + v }.toList)).mkString(";")
  }
  val tryGetTypeDescriptor = Some(typeDescriptor)
}

object HttpContentType {
  def apply(channel: RiftChannel with RiftHttpChannel, args: Map[String, String]): HttpContentType = 
    HttpChannelContentType(channel, args)
  def apply(typeDescriptor: TypeDescriptor, channel: RiftChannel with RiftHttpChannel, args: Map[String, String]): HttpContentType = 
    HttpQualifiedContentType(typeDescriptor, channel, args)
  def apply(clazz: Class[_], version: Int, channel: RiftChannel with RiftHttpChannel, args: Map[String, String]): HttpContentType =
    HttpQualifiedContentType(TypeDescriptor(clazz, version), channel, args)
  def apply(clazz: Class[_], channel: RiftChannel with RiftHttpChannel, args: Map[String, String]): HttpContentType =
    HttpQualifiedContentType(TypeDescriptor(clazz), channel, args)
    
  val PlainText = HttpChannelContentType(RiftChannel.Text)  
    
  def parse(rawContent: String): AlmValidation[HttpContentType] =
    sys.error("")
}