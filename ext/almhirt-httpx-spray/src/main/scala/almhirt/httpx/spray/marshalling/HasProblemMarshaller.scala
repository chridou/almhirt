package almhirt.httpx.spray.marshalling

import almhirt.common._
import spray.httpx.marshalling.Marshaller

trait HasProblemMarshaller {
  implicit def problemMarshaller: Marshaller[Problem]
}