package riftwarp.http

import almhirt.http.HttpStatusCode

case class RiftHttpResponse(statusCode: HttpStatusCode, data: RiftHttpData)