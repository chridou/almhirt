package almhirt.http

import almhirt.common.AlmValidation

trait HttpUnmarshaller[T] {
  def apply(from: HttpRequest): AlmValidation[T] = unmarshal(from)
  def unmarshal(from: HttpRequest): AlmValidation[T]
}