package almhirt.http.unfiltered

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import unfiltered.response._
import almhirt.http.BinaryPayload
import almhirt.http.TextPayload
import almhirt.http.ResponseConsumer

trait UnfilteredResponseConsumer extends ResponseConsumer[unfiltered.Async.Responder[Any]]{
  override def letConsume(responder: unfiltered.Async.Responder[Any], response: almhirt.http.HttpResponse) {
    val resp = response.content match {
      case almhirt.http.HttpContent(ct, payload) => 
        payload match {
          case BinaryPayload(data) => Status(response.status.code) ~> ResponseBytes(data) ~> ContentType(ct.toContentTypeString)
          case TextPayload(text) => Status(response.status.code) ~> ResponseString(text) ~> ContentType(ct.toContentTypeString)
        }
      case almhirt.http.HttpNoContent => Status(response.status.code) ~> NoContent
    }
    responder.respond(resp)
  }
}