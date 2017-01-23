package almhirt.corex.spray.service

import scala.language.postfixOps
import scalaz.Validation.FlatMap._
import akka.actor.Actor
import almhirt.common._
import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.context.{ HasAlmhirtContext, AlmhirtContext }
import spray.httpx.marshalling.Marshaller
import spray.http.StatusCodes
import spray.routing.Directives
import java.time.{ LocalDateTime, ZonedDateTime }
import spray.routing.RequestContext

object HttpUtilitiesEndpointFactory {
  final case class HttpUtilitiesEndpointParams(
    returnConfigEnabled: Boolean,
    localDateTimeMarshaller: Marshaller[LocalDateTime],
    dateTimeMarshaller: Marshaller[ZonedDateTime],
    stringMarshaller: Marshaller[String],
    uuidMarshaller: Marshaller[java.util.UUID],
    problemMarshaller: Marshaller[Problem])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(Marshaller[LocalDateTime], Marshaller[ZonedDateTime], Marshaller[String], Marshaller[java.util.UUID], Marshaller[Problem]) ⇒ HttpUtilitiesEndpointParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section ← ctx.config.v[Config]("almhirt.http.endpoints.utilities-endpoint")
      returnConfigEnabled ← section.v[Boolean]("return-config-enabled")
    } yield {
      (localDateTimeMarshaller: Marshaller[LocalDateTime],
      dateTimeMarshaller: Marshaller[ZonedDateTime],
      stringMarshaller: Marshaller[String],
      uuidMarshaller: Marshaller[java.util.UUID],
      problemMarshaller: Marshaller[Problem]) ⇒
        HttpUtilitiesEndpointParams(returnConfigEnabled, localDateTimeMarshaller, dateTimeMarshaller, stringMarshaller, uuidMarshaller, problemMarshaller)
    }
  }
}

trait HttpUtilitiesEndpointFactory extends Directives { me: Actor with AlmHttpEndpoint with HasAlmhirtContext ⇒

  def createUtilitiesEndpoint(params: HttpUtilitiesEndpointFactory.HttpUtilitiesEndpointParams): RequestContext ⇒ Unit = {

    implicit val problemMarshaller = params.problemMarshaller
    implicit val dateTimeMarshaller = params.dateTimeMarshaller
    implicit val localDateTimeMarshaller = params.localDateTimeMarshaller
    implicit val stringMarshaller = params.stringMarshaller
    implicit val uuidMarshaller = params.uuidMarshaller

    pathPrefix("config") {
      pathEnd {
        get { ctx ⇒
          if (params.returnConfigEnabled)
            ctx.complete(almhirtContext.config.root().render())
          else
            ctx.complete(StatusCodes.Forbidden, "Sorry...")
        }
      }
    } ~ pathPrefix("date") {
      parameter('local ?) { local ⇒
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
        parameter('base64 ?) { base64param ⇒
          get {
            if (base64param.isDefined)
              complete(almhirtContext.getUuid)
            else
              complete(almhirt.converters.MiscConverters.uuidToBase64String(almhirtContext.getUuid))
          }
        }
      }
    } ~ pathPrefix("convert") {
      pathPrefix("uuid-string-to-base64" / Segment) { uuidStr ⇒
        pathEnd {
          get {
            implicit ctx ⇒
              almhirt.converters.MiscConverters.uuidStringToBase64(uuidStr).completeRequestOk
          }
        }
      } ~
        pathPrefix("base64-to-uuid" / Segment) { b64 ⇒
          pathEnd {
            get {
              implicit ctx ⇒
                almhirt.converters.MiscConverters.base64ToUuid(b64).completeRequestOk
            }
          }
        } ~
        pathPrefix("base64-to-uuid-string" / Segment) { b64 ⇒
          pathEnd {
            get {
              implicit ctx ⇒
                almhirt.converters.MiscConverters.base64ToUuidString(b64).completeRequestOk
            }
          }
        }
    }
  }
}