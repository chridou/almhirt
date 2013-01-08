package riftwarp

import riftwarp._
package object http {

  /**
   * A function that serializes an AnyRef and returns a response or handles errors and creates an error response for the error that occurred when serializing
   */
  type HttpResponseWorkflow[T] = RiftChannel with RiftHttpChannel => AnyRef => T

  sealed trait RiftHttpResponse
  case class RiftHttpStringResponse(content: String, contentType: String) extends RiftHttpResponse
  case class RiftHttpBinaryResponse(content: Array[Byte], contentType: String) extends RiftHttpResponse
  case object RiftHttpNoContentResponse extends RiftHttpResponse

}