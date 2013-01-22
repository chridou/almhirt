package riftwarp

import almhirt.http.HttpSuccess
import riftwarp._

package object http {
  implicit object RiftHttpContentTypeEqual extends scalaz.Equal[RiftHttpContentType]{
    def equal(c1: RiftHttpContentType, c2: RiftHttpContentType): Boolean = c1 == c2
  }

  implicit object RiftHttpContentTypeWithChannelEqual extends scalaz.Equal[RiftHttpContentTypeWithChannel]{
    def equal(c1: RiftHttpContentTypeWithChannel, c2: RiftHttpContentTypeWithChannel): Boolean = c1 == c2
  }

  implicit object RiftHttpQualifiedContentTypeEqual extends scalaz.Equal[RiftHttpQualifiedContentType]{
    def equal(c1: RiftHttpQualifiedContentType, c2: RiftHttpQualifiedContentType): Boolean = c1 == c2
  }
  
}