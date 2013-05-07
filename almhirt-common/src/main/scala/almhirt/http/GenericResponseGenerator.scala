package almhirt.http

import almhirt.common._

trait SpecialResponseGenerator[TRes] {
  final def apply(from: HttpResponse): AlmValidation[TRes] = createResponse(from)
  def createResponse(from: HttpResponse): AlmValidation[TRes]
}