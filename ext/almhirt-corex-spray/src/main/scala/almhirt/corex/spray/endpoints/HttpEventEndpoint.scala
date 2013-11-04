package almhirt.corex.spray.endpoints

import scala.language.postfixOps
import almhirt.common._
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller

trait HttpEventEndpoint extends Directives {
  implicit def problemMarshaller: Marshaller[Problem]
  implicit def eventUnmarshaller: Unmarshaller[Event]

  def publish(payload: Any)

  val putEventDirective = put & entity(as[Event])

  val eventEndpointTerminator = putEventDirective {
    event =>
      ctx => {
        publish(event)
        ctx.complete(StatusCodes.Accepted, event.eventId.toString())
      }
  }
}