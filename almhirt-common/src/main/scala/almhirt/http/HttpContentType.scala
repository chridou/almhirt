package almhirt.http

import almhirt.common.AlmValidation

trait HttpContentTypeExtractor[T]{
  final def apply(from: T): AlmValidation[HttpContentType] = extractContentType(from)
  def extractContentType(from: T): AlmValidation[HttpContentType]
}

final case class HttpContentType(primary: String, options: Map[String, String])



