package almhirt.http

final case class HttpRequest(contentType: Option[String], acceptsContent: Set[String], content: Array[Byte])