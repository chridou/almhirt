package almhirt.corex.spray.service

import scala.language.postfixOps
import almhirt.common._
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import almhirt.akkax._
import almhirt.httpx.spray.marshalling._

trait HttpEventEndpointFactory extends Directives { 
  def publish(payload: AnyRef)

  def createEventEndpoint(publish: AnyRef ⇒ Unit)(implicit eventUnmarshaller: Unmarshaller[Event]): RequestContext ⇒ Unit = {

    val putEventDirective = (post) & entity(as[Event])

    putEventDirective {
      event ⇒
        ctx ⇒ {
          publish(event)
          ctx.complete(StatusCodes.Accepted, event.eventId.value.toString())
        }
    }
  }
}