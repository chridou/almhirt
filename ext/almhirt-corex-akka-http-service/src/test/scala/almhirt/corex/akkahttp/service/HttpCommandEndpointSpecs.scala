package almhirt.corex.akkahttp.service

//import org.scalatest._
//import spray.testkit.ScalatestRouteTest
//import scala.concurrent.duration.FiniteDuration
//import akka.actor._
//import almhirt.testkit._
//import almhirt.common._
//import almhirt.almvalidation.kit._
//import almhirt.commanding._
//import almhirt.components._
//import almhirt.components.impl._
//import almhirt.corex.spray.marshalling._
//import almhirt.core.types._
//import almhirt.httpx.spray.marshalling._
//import riftwarp.RiftWarp
//import riftwarp.util.WarpHttpSerializer
//import riftwarp.util.RiftCommandStringSerializer
//import riftwarp.serialization.common.CommonHttpSerializersByRiftWarp
//import riftwarp.HasRiftWarp
//import spray.http._
//import spray.routing.HttpService
//import spray.http.StatusCodes._
//import almhirt.http.MediaTypeVendorProvider
//import almhirt.serialization.HasCommonHttpSerializers
//import almhirt.core.types.serialization.HasCoreHttpSerializers
//import riftwarp.serialization.common.CommonHttpSerializersByRiftWarp
//import almhirt.corex.riftwarp.serializers.CoreHttpSerializersByRiftWarp
//import almhirt.http._
//import almhirt.corex.spray._
//import almhirt.core.http._
//import almhirt.httpx.spray.service.AlmHttpEndpoint
//
//object Requirements {
//  val riftWarp: RiftWarp = {
//    val rw = RiftWarp()
//    almhirt.corex.riftwarp.serialization.Serialization.addRiftWarpRegistrations(rw)
//    almhirt.testkit.AR1.Serialization.addAr1Serializers(rw)
//    rw
//  }
//  val almhirtProvider = MediaTypeVendorProvider("almhirt")
//  val commonHttpSerializers: HasCommonHttpSerializers = new { val myRiftWarp = riftWarp } with CommonHttpSerializersByRiftWarp with HasRiftWarp
//  val commonContentTypeProviders: HasCommonContentTypeProviders = new { val vendorProvider = MediaTypeVendorProvider("almhirt") } with HasCommonAlmMediaTypesProviders with VendorBasedCommonAlmMediaTypesProviders with CommonContentTypeProvidersFromMediaTypes
//  val coreHttpSerializers: HasCoreHttpSerializers = new { val myRiftWarp = riftWarp } with CoreHttpSerializersByRiftWarp with HasRiftWarp
//  val coreContentTypeProviders: HasCoreContentTypeProviders = new { val vendorProvider = MediaTypeVendorProvider("almhirt") } with HasCoreAlmMediaTypesProviders with VendorBasedCoreAlmMediaTypesProviders with CoreContentTypeProvidersFromMediaTypes
//
//}
//
//class HttpCommandEndpointSpecs extends FunSpec
//  with AlmHttpEndpoint
//  with ScalatestRouteTest
//  with Matchers
//  with HasCommonMarshallers with CommonMarshallerInstances
//  with HasCommonUnmarshallers with CommonUnmarshallerInstances
//  with HasCoreMarshallers
//  with CoreMarshallerInstances
//  with HasCoreUnmarshallers
//  with CoreUnmarshallerInstances
//  with HttpCommandEndpoint
//  with HttpService {
//  def actorRefFactory = system
//
//  val almhirtProvider = MediaTypeVendorProvider("almhirt")
//
//  lazy val commonHttpSerializers: HasCommonHttpSerializers = Requirements.commonHttpSerializers
//
//  lazy val commonContentTypeProviders: HasCommonContentTypeProviders = Requirements.commonContentTypeProviders
//
//  lazy val coreHttpSerializers: HasCoreHttpSerializers = Requirements.coreHttpSerializers
//
//  lazy val coreContentTypeProviders: HasCoreContentTypeProviders = Requirements.coreContentTypeProviders
//
//  val (myAlmhirt, closeHandle) = almhirt.core.Almhirt.notFromConfig(system).awaitResultOrEscalate(FiniteDuration(2, "s"))
//
//  implicit def theAlmhirt = myAlmhirt
//
//  val tracker = {
//    val props = ExecutionStateTracker.props(theAlmhirt).resultOrEscalate
//    system.actorOf(props, "execution_state_tracker")
//  }
//
//  override val endpoint = CommandEndpoint(tracker)(theAlmhirt)
//  override val maxSyncDuration = theAlmhirt.durations.shortDuration
//  override val executionContext = theAlmhirt.futuresExecutor
//
//  lazy val commandSerializer = WarpHttpSerializer.command(Requirements.riftWarp)
//  lazy val execStateSerializer = WarpHttpSerializer[ExecutionState](Requirements.riftWarp)
//  lazy val problemSerializer = WarpHttpSerializer.problem(Requirements.riftWarp)
//
//  val commandWithoutTrackingId = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")
//  val commandWithoutTrackingIdJson = commandSerializer.serialize("json")(commandWithoutTrackingId).resultOrEscalate._1.value.asInstanceOf[String]
//  val commandWithoutTrackingIdXml = commandSerializer.serialize("xml")(commandWithoutTrackingId).resultOrEscalate._1.value.asInstanceOf[String]
//  val commandWithTrackingId = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a").track
//  val commandWithTrackingIdJson = commandSerializer.serialize("json")(commandWithoutTrackingId).resultOrEscalate._1.value.asInstanceOf[String]
//  val commandWithTrackingIdXml = commandSerializer.serialize("xml")(commandWithoutTrackingId).resultOrEscalate._1.value.asInstanceOf[String]
//
//  private val executeCommandRoute = executeCommandTerminator
//
//  describe("HttpCommandEndpoint - executeCommand") {
//    it("""should return a MethodNotAllowed error for GET requests to the "/execute" path""") {
//      Get("/execute") ~> sealRoute(executeCommandRoute) ~> check {
//        status should equal(MethodNotAllowed)
//        responseAs[String] === "HTTP method not allowed, supported methods: PUT"
//      }
//    }
//
//    it("""should return a MethodNotAllowed error for POST requests to the "/execute" path""") {
//      Post("/execute") ~> sealRoute(executeCommandRoute) ~> check {
//        status should equal(MethodNotAllowed)
//        responseAs[String] === "HTTP method not allowed, supported methods: PUT"
//      }
//    }
//
//    it("""should accept a command without a tracking id PUT to the "/execute" path with contentType "application/vnd.acme.Command+json" and respond with an empty body.""") {
//      Put("/execute", commandWithoutTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
//        status should equal(Accepted)
//        responseAs[String] should equal("")
//      }
//    }
//
//    it("""should accept a command with a tracking id PUT to the "/execute" path with contentType "application/vnd.acme.Command+json" and respond with the tracking id.""") {
//      Put("/execute", commandWithTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
//        status should equal(Accepted)
//        responseAs[String] should equal(commandWithTrackingId.trackingId)
//      }
//    }
//
//    it("""should accept a command with a tracking id PUT to the "/execute?tracked" path with contentType "application/vnd.acme.Command+json" and respond with the tracking id.""") {
//      Put("/execute?tracked", commandWithTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
//        status should equal(Accepted)
//        responseAs[String] should equal(commandWithTrackingId.trackingId)
//      }
//    }
//
//    it("""should accept a command without a tracking id PUT to the "/execute?tracked" path with contentType "application/vnd.acme.Command+json" and respond with a new tracking id.""") {
//      Put("/execute?tracked", commandWithoutTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
//        status should equal(Accepted)
//        responseAs[String] should not be ('empty)
//      }
//    }
//
//    //    it("""should accept a command with a tracking id PUT to the "/execute?sync" path with contentType "application/vnd.acme.Command+json" and respond with an ExecutionSuccessful.""") {
//    //      val cmd = commandWithoutTrackingId.track
//    //      val execState = ExecutionSuccessful(cmd.trackingId, "ahh!")
//    //      tracker ! ExecutionStateChanged(execState)
//    //      Put("/execute?sync", cmd)(commandMarshaller) ~> executeCommandRoute ~> check {
//    //        status should equal(OK)
//    //        responseAs[ExecutionState] should equal(execState)
//    //      }
//    //    }
//
//    //    it("""should rsssseturn a MethodNotAllowed error for POST requests to the "/execute" path""") {
//    //      Put("/execute", "xx")  ~> sealRoute(executeCommandRoute) ~> check {
//    //        status should equal(UnsupportedMediaType)
//    //      }
//    //    }
//
//  }
//}