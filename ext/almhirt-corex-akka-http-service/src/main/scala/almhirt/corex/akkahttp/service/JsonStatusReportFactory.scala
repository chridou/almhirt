package almhirt.corex.akkahttp.service

import java.time.LocalDateTime
import scala.concurrent.duration._
import scala.concurrent._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.problem._
import almhirt.almfuture.all._
import almhirt.akkax._
import almhirt.herder._
import almhirt.herder.HerderMessages._
import almhirt.context.AlmhirtContext
import almhirt.httpx.akkahttp.service.AlmHttpEndpoint
import almhirt.context.HasAlmhirtContext
import almhirt.httpx.akkahttp.service._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import org.json4s.CustomSerializer
import org.json4s._
import org.json4s.native.JsonMethods._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

class JsonStatusReportFactory(private val context: ActorContext)(implicit almhirtContext: AlmhirtContext, problemMarshaller: ToEntityMarshaller[Problem]) extends Directives with Json4sSupport {
  import almhirt.akkax.reporting._

  def serializeAst(what: ezreps.ast.EzValue): JValue = ezreps.json4sx.SerializeJson4s.serializeAst(what)

  implicit override val json4sFormats = org.json4s.native.Serialization.formats(NoTypeHints)
  //implicit override val json4sFormats = org.json4s.DefaultFormats + new Json4SComponentStateSerializer

  def createJsonStatusReportRoute(maxCallDuration: FiniteDuration)(implicit executor: ExecutionContext) = {
    path(Segment / Segment) { (appName, componentName) ⇒
      parameters("no-noise".as[Boolean] ? false, "exclude-not-available".as[Boolean] ? false) { (noNoise, excludeNotAvailable) ⇒
        get { ctx ⇒
          val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
          val options = almhirt.akkax.reporting.StatusReportOptions.makeWithDefaults(noNoise, excludeNotAvailable)
          val fut = (herder ? StatusReportMessages.GetStatusReportFor(ComponentId(AppName(appName), ComponentName(componentName)), options))(maxCallDuration).mapCastTo[StatusReportMessages.GetStatusReportForRsp].mapV {
            case StatusReportMessages.StatusReportFor(_, report) ⇒
              scalaz.Success(report)
            case StatusReportMessages.GetStatusReportForFailed(_, prob) ⇒
              scalaz.Failure(prob)
          }
          fut.mapOrRecover(report => ctx.complete(
            compact(render(serializeAst(options.filter(report))))),
            problem => implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem))
            .flatMap(f => f)
        }
      }
    }
  }

}
