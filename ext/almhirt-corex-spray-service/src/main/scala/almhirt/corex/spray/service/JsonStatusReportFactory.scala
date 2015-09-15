package almhirt.corex.spray.service

import java.time.LocalDateTime
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
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
import almhirt.httpx.spray.service.AlmHttpEndpoint
import almhirt.context.HasAlmhirtContext
import almhirt.httpx.spray.service.AlmHttpProblemTerminator
import spray.routing.Directives
import spray.httpx.marshalling.Marshaller
import spray.http.StatusCodes
import spray.routing.RequestContext
import spray.routing.HttpService
import org.json4s.CustomSerializer
import org.json4s._
import org.json4s.native.JsonMethods._

class JsonStatusReportFactory(private val context: ActorContext)(implicit almhirtContext: AlmhirtContext, problemMarshaller: Marshaller[Problem]) extends Directives with spray.httpx.Json4sSupport {
  import almhirt.akkax.reporting._

  class Json4SRBasicValueSerializer extends CustomSerializer[AST.RBasicValue](format ⇒ (
    {
      case _ ⇒ ???
    },
    {
      case AST.RComponentState(state: ComponentState)         ⇒ JString(state.parsableString)
      case AST.RString(value: String)                         ⇒ JString(value)
      case AST.RInteger(value: Long)                          ⇒ JInt(value)
      case AST.RFloat(value: Double)                          ⇒ JDouble(value)
      case AST.RBool(value: Boolean)                          ⇒ JBool(value)
      case AST.RLocalDateTime(value: java.time.LocalDateTime) ⇒ JString(value.toString)
      case AST.RZonedDateTime(value: java.time.ZonedDateTime) ⇒ JString(value.toString)
      case AST.RError(message: String)                        ⇒ JString(s"ERROR: $message")
      case AST.RNotAvailable                                  ⇒ JString("N/A")
    }))

  class Json4SReportSerializer extends CustomSerializer[AST.RReport](format ⇒ (
    {
      case _ ⇒ ???
    },
    {
      case AST.RReport(fields)         ⇒ 
        JObject(fields.map(field => JField(field.label, Extraction.decompose(field.value))): _*)
    }))
    
  implicit override val json4sFormats = org.json4s.native.Serialization.formats(NoTypeHints) + new Json4SRBasicValueSerializer
  //implicit override val json4sFormats = org.json4s.DefaultFormats + new Json4SComponentStateSerializer

  def createJsonStatusReportRoute(maxCallDuration: FiniteDuration)(implicit executor: ExecutionContext): RequestContext ⇒ Unit = {
    pathPrefix(Segment / Segment) { (appName, componentName) ⇒
      get { ctx ⇒
        val herder = context.actorSelection(almhirtContext.localActorPaths.herder)
        val fut = (herder ? StatusReportMessages.GetStatusReportFor(ComponentId(AppName(appName), ComponentName(componentName))))(maxCallDuration).mapCastTo[StatusReportMessages.GetStatusReportForRsp].mapV {
          case StatusReportMessages.StatusReportFor(_, report) ⇒
            scalaz.Success(report)
          case StatusReportMessages.GetStatusReportForFailed(_, prob) ⇒
            scalaz.Failure(prob)
        }
        fut.fold(
          problem ⇒ implicitly[AlmHttpProblemTerminator].terminateProblem(ctx, problem)(problemMarshaller),
          report ⇒ ctx.complete(report))
      }
    }
  }

}
