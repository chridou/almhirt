package riftwarp.http

sealed trait RiftHttpResponse
case class RiftHttpStringResponse(contentType: HttpContentType, content: String) extends RiftHttpResponse
case class RiftHttpBinaryResponse(contentType: HttpContentType, content: Array[Byte]) extends RiftHttpResponse
case object RiftHttpNoContentResponse extends RiftHttpResponse
