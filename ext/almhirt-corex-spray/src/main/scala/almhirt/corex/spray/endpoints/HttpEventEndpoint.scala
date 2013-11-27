package almhirt.corex.spray.endpoints

import scala.language.postfixOps
import almhirt.common._
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import almhirt.httpx.spray.marshalling._

trait HttpEventEndpoint extends Directives { self: HasCommonMarshallers with HasCommonUnmarshallers =>
  def publish(payload: AnyRef)

  val putEventDirective = put & entity(as[Event])

  val eventEndpointTerminator = putEventDirective {
    event =>
      ctx => {
        publish(event)
        ctx.complete(StatusCodes.Accepted, event.eventId.toString())
      }
  }
}