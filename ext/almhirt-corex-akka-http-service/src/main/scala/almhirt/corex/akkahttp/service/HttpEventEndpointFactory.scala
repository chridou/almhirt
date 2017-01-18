package almhirt.corex.akkahttp.service

import scala.language.postfixOps
import almhirt.common._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._
import almhirt.akkax._
import almhirt.httpx.spray.marshalling._

trait HttpEventEndpointFactory extends Directives { 
  def publish(payload: AnyRef)

  def createEventEndpoint(publish: AnyRef ⇒ Unit)(implicit eventUnmarshaller: FromEntityUnmarshaller[Event]) = {

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