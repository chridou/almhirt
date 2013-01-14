package riftwarp

import almhirt.http.HttpSuccess
import riftwarp._

package object http {

  /**
   * A function that serializes an AnyRef and returns a response or handles errors and creates an error response for the error that occurred when serializing
   */
  type HttpResponseWorkflow[T] = RiftChannel with RiftHttpChannel => (AnyRef, HttpSuccess) => T
}