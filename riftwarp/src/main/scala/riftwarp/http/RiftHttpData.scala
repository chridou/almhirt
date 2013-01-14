package riftwarp.http

sealed trait RiftHttpData
case class RiftHttpStringData(contentType: HttpContentType, content: String) extends RiftHttpData
case class RiftHttpBinaryData(contentType: HttpContentType, content: Array[Byte]) extends RiftHttpData
case object RiftHttpNoContentData extends RiftHttpData
