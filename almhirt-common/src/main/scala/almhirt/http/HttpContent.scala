package almhirt.http

sealed trait HttpContentContainer

final case class HttpContent(contentType: HttpContentType, payload: HttpBody) extends HttpContentContainer
case object HttpNoContent extends HttpContentContainer

