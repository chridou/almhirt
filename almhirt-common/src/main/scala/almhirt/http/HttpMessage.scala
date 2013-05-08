package almhirt.http

sealed trait HttpMessage{
  def content: HttpContentContainer
}

final case class HttpRequest(content: HttpContentContainer, acceptsContent: List[(HttpContentType, Option[Double])]) extends HttpMessage {
  def preferredContentType: HttpContentType =
    acceptsContent match {
      case x :: xs => x._1
      case Nil => 
        content match {
          case HttpNoContent => HttpContentType("text/plain", Map.empty)
          case HttpContent(ct,_) => ct
        }
    }
}

final case class HttpResponse(status: HttpStatusCode, content: HttpContentContainer) extends HttpMessage