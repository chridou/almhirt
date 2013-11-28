package almhirt.corex.spray.service

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import spray.testkit.ScalatestRouteTest
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import almhirt.testkit._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.commanding._
import almhirt.components._
import almhirt.components.impl._
import almhirt.corex.spray.marshalling._
import almhirt.corex.spray.VendorBasedCoreMediaTypesProviders
import almhirt.httpx.spray.MediaTypeVendorProvider
import almhirt.httpx.spray.VendorBasedCommonMediaTypesProviders
import almhirt.corex.riftwarp.serializers.HasCoreWireSerializersByRiftWarp
import almhirt.core.types._
import almhirt.httpx.spray.marshalling._
import riftwarp.RiftWarp
import riftwarp.util.WarpWireSerializer
import riftwarp.util.RiftCommandStringSerializer
import riftwarp.serialization.common.HasCommonWireSerializersByRiftWarp
import riftwarp.HasRiftWarp
import spray.http._
import spray.routing.HttpService
import spray.http.StatusCodes._

class HttpCommandEndpointSpecs extends FunSpec 
	with ScalatestRouteTest 
    with HasRiftWarp
	with ShouldMatchers 
	with VendorBasedCommonMediaTypesProviders
	with CommonContentTypeProvidersFromMediaTypes 
	with HasCommonWireSerializersByRiftWarp
	with HasCommonMarshallers 
	with HasCommonUnmarshallers 
	with HasCoreWireSerializersByRiftWarp
	with VendorBasedCoreMediaTypesProviders
	with CoreContentTypeProvidersFromMediaTypes
	with HasCoreMarshallers 
	with HttpCommandEndpoint  
	with HttpService {
  def actorRefFactory = system
  
  override lazy val vendorProvider = MediaTypeVendorProvider("almhirt")

  override lazy val myRiftWarp: RiftWarp = {
    val rw = RiftWarp()
    almhirt.corex.riftwarp.serialization.RiftWarpUtilityFuns.addRiftWarpRegistrations(rw)
    almhirt.testkit.AR1.Serialization.addAr1Serializers(rw)
    rw
  }

  val (myAlmhirt, closeHandle) = almhirt.core.Almhirt.notFromConfig(system).awaitResultOrEscalate(FiniteDuration(2, "s"))

  implicit def theAlmhirt = myAlmhirt

  val tracker = {
    val props = ExecutionStateTracker.props(theAlmhirt).resultOrEscalate
    system.actorOf(props, "execution_state_tracker")
  }

  override val endpoint = CommandEndpoint(tracker)(theAlmhirt)
  override val maxSyncDuration = theAlmhirt.durations.shortDuration
  override val executionContext = theAlmhirt.futuresExecutor


  lazy val commandSerializer = WarpWireSerializer.command(myRiftWarp)
  lazy val execStateSerializer = WarpWireSerializer[ExecutionState, ExecutionState](myRiftWarp)
  lazy val problemSerializer = WarpWireSerializer.problem(myRiftWarp)

  val commandWithoutTrackingId = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")
  val commandWithoutTrackingIdJson = commandSerializer.serialize("json")(commandWithoutTrackingId).resultOrEscalate._1.value.asInstanceOf[String]
  val commandWithoutTrackingIdXml = commandSerializer.serialize("xml")(commandWithoutTrackingId).resultOrEscalate._1.value.asInstanceOf[String]
  val commandWithTrackingId = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a").track
  val commandWithTrackingIdJson = commandSerializer.serialize("json")(commandWithoutTrackingId).resultOrEscalate._1.value.asInstanceOf[String]
  val commandWithTrackingIdXml = commandSerializer.serialize("xml")(commandWithoutTrackingId).resultOrEscalate._1.value.asInstanceOf[String]

  private val executeCommandRoute = executeCommandTerminator

  describe("HttpCommandEndpoint - executeCommand") {
    it("""should return a MethodNotAllowed error for GET requests to the "/execute" path""") {
      Get("/execute") ~> sealRoute(executeCommandRoute) ~> check {
        status should equal(MethodNotAllowed)
        responseAs[String] === "HTTP method not allowed, supported methods: PUT"
      }
    }

    it("""should return a MethodNotAllowed error for POST requests to the "/execute" path""") {
      Post("/execute") ~> sealRoute(executeCommandRoute) ~> check {
        status should equal(MethodNotAllowed)
        responseAs[String] === "HTTP method not allowed, supported methods: PUT"
      }
    }

    it("""should accept a command without a tracking id PUT to the "/execute" path with contentType "application/vnd.acme.Command+json" and respond with an empty body.""") {
      Put("/execute", commandWithoutTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
        status should equal(Accepted)
        responseAs[String] should equal("")
      }
    }

    it("""should accept a command with a tracking id PUT to the "/execute" path with contentType "application/vnd.acme.Command+json" and respond with the tracking id.""") {
      Put("/execute", commandWithTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
        status should equal(Accepted)
        responseAs[String] should equal(commandWithTrackingId.trackingId)
      }
    }

    it("""should accept a command with a tracking id PUT to the "/execute?tracked" path with contentType "application/vnd.acme.Command+json" and respond with the tracking id.""") {
      Put("/execute?tracked", commandWithTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
        status should equal(Accepted)
        responseAs[String] should equal(commandWithTrackingId.trackingId)
      }
    }

    it("""should accept a command without a tracking id PUT to the "/execute?tracked" path with contentType "application/vnd.acme.Command+json" and respond with a new tracking id.""") {
      Put("/execute?tracked", commandWithoutTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
        status should equal(Accepted)
        responseAs[String] should not be ('empty)
      }
    }

//    it("""should accept a command with a tracking id PUT to the "/execute?sync" path with contentType "application/vnd.acme.Command+json" and respond with an ExecutionSuccessful.""") {
//      val cmd = commandWithoutTrackingId.track
//      val execState = ExecutionSuccessful(cmd.trackingId, "ahh!")
//      tracker ! ExecutionStateChanged(execState)
//      Put("/execute?sync", cmd)(commandMarshaller) ~> executeCommandRoute ~> check {
//        status should equal(OK)
//        responseAs[ExecutionState] should equal(execState)
//      }
//    }
    
//    it("""should rsssseturn a MethodNotAllowed error for POST requests to the "/execute" path""") {
//      Put("/execute", "xx")  ~> sealRoute(executeCommandRoute) ~> check {
//        status should equal(UnsupportedMediaType)
//      }
//    }
    
  }
}