package almhirt.corex.spray.service

import scala.language.postfixOps
import spray.routing.Directives
import almhirt.common._
import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.context.HasAlmhirtContext
import akka.actor.Actor
import spray.httpx.marshalling.Marshaller
import org.joda.time.LocalDateTime
import org.joda.time.DateTime
import almhirt.context.AlmhirtContext

object HttpUtilitiesEndpoint {
  final case class HttpUtilitiesEndpointMarshallers(
    localDateTimeMarshaller: Marshaller[LocalDateTime],
    dateTimeMarshaller: Marshaller[DateTime],
    stringMarshaller: Marshaller[String],
    uuidMarshaller: Marshaller[java.util.UUID],
    problemMarshaller: Marshaller[Problem])

}

trait HttpUtilitiesEndpoint extends Directives { me: Actor with AlmHttpEndpoint with HasAlmhirtContext =>
  import HttpUtilitiesEndpoint._

  protected def httpUtilitiesEndpointMarshallers: HttpUtilitiesEndpointMarshallers

  implicit private val problemMarshaller = httpUtilitiesEndpointMarshallers.problemMarshaller
  implicit private val dateTimeMarshaller = httpUtilitiesEndpointMarshallers.dateTimeMarshaller
  implicit private val localDateTimeMarshaller = httpUtilitiesEndpointMarshallers.localDateTimeMarshaller
  implicit private val stringMarshaller = httpUtilitiesEndpointMarshallers.stringMarshaller
  implicit private val uuidMarshaller = httpUtilitiesEndpointMarshallers.uuidMarshaller

  val utilitiesTerminator =
    pathPrefix("config") {
      pathEnd {
        get {
          complete(almhirtContext.config.root().render())
        }
      }
    } ~ pathPrefix("date") {
      parameter('local ?) { local =>
        pathEnd {
          get {
            if (local.isDefined) {
              complete(almhirtContext.getDateTime)
            } else {
              complete(almhirtContext.getUtcTimestamp)
            }
          }
        }
      }
    } ~ pathPrefix("unique-string") {
      pathEnd {
        get {
          complete(almhirtContext.getUniqueString)
        }
      }
    } ~ pathPrefix("uuid") {
      pathEnd {
        parameter('base64 ?) { base64param =>
          get {
            if (base64param.isDefined)
              complete(almhirtContext.getUuid)
            else
              complete(almhirt.converters.MiscConverters.uuidToBase64String(almhirtContext.getUuid))
          }
        }
      }
    } ~ pathPrefix("convert") {
      pathPrefix("uuid-string-to-base64" / Segment) { uuidStr =>
        pathEnd {
          get {
            implicit ctx =>
              almhirt.converters.MiscConverters.uuidStringToBase64(uuidStr).completeRequestOk
          }
        }
      } ~
        pathPrefix("base64-to-uuid" / Segment) { b64 =>
          pathEnd {
            get {
              implicit ctx =>
                almhirt.converters.MiscConverters.base64ToUuid(b64).completeRequestOk
            }
          }
        } ~
        pathPrefix("base64-to-uuid-string" / Segment) { b64 =>
          pathEnd {
            get {
              implicit ctx =>
                almhirt.converters.MiscConverters.base64ToUuidString(b64).completeRequestOk
            }
          }
        }
    }
}