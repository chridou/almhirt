package almhirt.http

trait HttpMessage {
  def content: HttpContent
}

final case class HttpRequest(content: HttpContent, acceptsContent: List[String])
final case class HttpResponse(status: HttpStatusCode, content: HttpContent)