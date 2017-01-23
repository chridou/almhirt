package almhirt.corex.spray.service

import scala.language.postfixOps
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.tracking._
import almhirt.httpx.spray.marshalling._
import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.context.AlmhirtContext
import almhirt.akkax._
import spray.routing._
import spray.http._
import spray.routing.directives._
import spray.httpx.unmarshalling.Unmarshaller
import spray.httpx.marshalling.Marshaller
import almhirt.context.HasAlmhirtContext

object HttpCommandEndpointFactory {
  case class HttpCommandEndpointParams(
    commandEndpoint: ActorRef,
    maxSyncDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    commandUnmarshaller: Unmarshaller[Command],
    commandResponseMarshaller: Marshaller[CommandResponse],
    problemMarshaller: Marshaller[Problem])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(ActorRef, Unmarshaller[Command], Marshaller[CommandResponse], Marshaller[Problem]) ⇒ HttpCommandEndpointParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section ← ctx.config.v[Config]("almhirt.http.endpoints.command-endpoint")
      maxSyncDuration ← section.v[FiniteDuration]("max-sync-duration")
      selector ← section.v[ExtendedExecutionContextSelector]("execution-context-selector")
    } yield {
      (commandEndpoint: ActorRef, commandUnmarshaller: Unmarshaller[Command], commandResponseMarshaller: Marshaller[CommandResponse], problemMarshaller: Marshaller[Problem]) ⇒
        HttpCommandEndpointParams(commandEndpoint, maxSyncDuration, selector, commandUnmarshaller, commandResponseMarshaller, problemMarshaller)
    }
  }

}

trait HttpCommandEndpointFactory extends Directives {
  me: Actor with AlmHttpEndpoint with HasAlmhirtContext ⇒

  def createCommandEndpoint(params: HttpCommandEndpointFactory.HttpCommandEndpointParams): RequestContext ⇒ Unit = {

    implicit val execCtx = params.exectionContextSelector.select(me.almhirtContext, me.context)
    implicit val commandUnmarshaller = params.commandUnmarshaller
    implicit val commandResponseMarshaller = params.commandResponseMarshaller
    implicit val problemMarshaller = params.problemMarshaller

    val executeCommand = post & entity(as[Command])

    pathPrefix("execute") {
      pathEnd {
        parameter('flat ?) { flatParam ⇒

          executeCommand { cmd ⇒
            implicit ctx ⇒ {
              val flattened = flatParam.map(_.toLowerCase() == "true").getOrElse(false)
              ((params.commandEndpoint ? cmd)(params.maxSyncDuration)).mapCastTo[CommandResponse].completeCommandResponse(flattened)
            }
          }
        }
      }
    }
  }
}