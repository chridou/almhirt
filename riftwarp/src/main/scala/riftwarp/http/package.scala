package riftwarp

import riftwarp._
package object http {

  /**
   * A function that serializes an AnyRef and returns a response or handles errors and creates an error response for the error that occurred when serializing
   */
  type HttpResponseWorkflow[T] = RiftChannel with RiftHttpChannel => AnyRef => T

  sealed trait RiftHttpResponse
  case class RiftHttpStringResponse(dim: RiftStringBasedDimension, contentType: String) extends RiftHttpResponse
  case class RiftHttpBinaryResponse(dim: RiftByteArrayBasedDimension, contentType: String) extends RiftHttpResponse

}