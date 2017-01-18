package almhirt.corex.akkahttp.service

import scala.language.postfixOps
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.tracking._
import almhirt.httpx.akkahttp.marshalling._
import almhirt.httpx.akkahttp.service.AlmHttpEndpoint
import almhirt.context.AlmhirtContext
import almhirt.akkax._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._
import almhirt.context.HasAlmhirtContext

object HttpCommandEndpointFactory {
  case class HttpCommandEndpointParams(
    commandEndpoint: ActorRef,
    maxSyncDuration: scala.concurrent.duration.FiniteDuration,
    exectionContextSelector: ExtendedExecutionContextSelector,
    commandUnmarshaller: FromEntityUnmarshaller[Command],
    commandResponseMarshaller: ToEntityMarshaller[CommandResponse],
    problemMarshaller: ToEntityMarshaller[Problem])

  def paramsFactory(implicit ctx: AlmhirtContext): AlmValidation[(ActorRef, FromEntityUnmarshaller[Command], ToEntityMarshaller[CommandResponse], ToEntityMarshaller[Problem]) ⇒ HttpCommandEndpointParams] = {
    import com.typesafe.config.Config
    import almhirt.configuration._
    import scala.concurrent.duration.FiniteDuration
    for {
      section ← ctx.config.v[Config]("almhirt.http.endpoints.command-endpoint")
      maxSyncDuration ← section.v[FiniteDuration]("max-sync-duration")
      selector ← section.v[ExtendedExecutionContextSelector]("execution-context-selector")
    } yield {
      (commandEndpoint: ActorRef, commandUnmarshaller: FromEntityUnmarshaller[Command], commandResponseMarshaller: ToEntityMarshaller[CommandResponse], problemMarshaller: ToEntityMarshaller[Problem]) ⇒
        HttpCommandEndpointParams(commandEndpoint, maxSyncDuration, selector, commandUnmarshaller, commandResponseMarshaller, problemMarshaller)
    }
  }

}

trait HttpCommandEndpointFactory extends Directives {
  me: Actor with AlmHttpEndpoint with HasAlmhirtContext ⇒

  def createCommandEndpoint(params: HttpCommandEndpointFactory.HttpCommandEndpointParams) = {

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