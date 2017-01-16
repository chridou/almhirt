package almhirt.httpx.akkahttp.marshalling

import almhirt.common._
import akka.http.scaladsl.marshalling.ToEntityMarshaller

trait HasProblemMarshaller {
  implicit def problemMarshaller: ToEntityMarshaller[Problem]
}