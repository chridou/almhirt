package almhirt.corex.spray.endpoints

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import spray.testkit.ScalatestRouteTest
import almhirt.testkit._
import scala.concurrent.duration.FiniteDuration
import akka.actor._
import spray.routing.HttpService
import spray.http.StatusCodes._
import riftwarp.RiftWarp
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.components._
import almhirt.components.impl._
import almhirt.corex.spray.marshalling._
import riftwarp.util.RiftCommandStringSerializer
import almhirt.commanding._
import spray.http._
import almhirt.domain.AggregateRootRef

class HttpCommandEndpointSpecs extends FunSpec with ShouldMatchers with HttpCommandEndpoint with ScalatestRouteTest {
  def actorRefFactory = system

  lazy val myRiftwarp: RiftWarp = {
    val rw = RiftWarp()
    almhirt.corex.riftwarp.serialization.RiftWarpUtilityFuns.addRiftWarpRegistrations(rw)
    almhirt.testkit.AR1.Serialization.addAr1Serializers(rw)
    rw
  }

  val (myAlmhirt, closeHandle) = almhirt.core.Almhirt.notFromConfig(system).awaitResultOrEscalate(FiniteDuration(2, "s"))

  implicit def theAlmhirt = myAlmhirt

  val tracker = {
    val props = Props(
      new ExecutionStateTracker with ExecutionTrackerTemplate with InMemoryExecutionStateTracker with Actor with ActorLogging {
        val publishTo = theAlmhirt.messageBus
        val canCreateUuidsAndDateTimes = theAlmhirt
        val executionContext = theAlmhirt.futuresExecutor
        val secondLevelMaxAskDuration = scala.concurrent.duration.FiniteDuration(3, "s")
        def receive: Receive = handleTrackingMessage
      })
    system.actorOf(props, "execution_state_tracker")
  }

  override val endpoint = CommandEndpoint(tracker)(theAlmhirt)
  override val maxSyncDuration = theAlmhirt.durations.shortDuration
  override val executionContext = theAlmhirt.futuresExecutor

  lazy val `application/vnd.acme.Command+json` = MediaTypes.register(MediaType.custom("""application/vnd.acme.Command+json"""))
  lazy val `application/vnd.acme.Command+xml` = MediaTypes.register(MediaType.custom("""application/vnd.acme.Command+xml"""))
  lazy val `application/vnd.acme.ExecutionState+json` = MediaTypes.register(MediaType.custom("""application/vnd.acme.ExecutionState+json"""))
  lazy val `application/vnd.acme.ExecutionState+xml` = MediaTypes.register(MediaType.custom("""application/vnd.acme.ExecutionState+xml"""))
  lazy val `application/vnd.acme.Problem+json` = MediaTypes.register(MediaType.custom("""application/vnd.acme.ExecutionState+json"""))
  lazy val `application/vnd.acme.Problem+xml` = MediaTypes.register(MediaType.custom("""application/vnd.acme.ExecutionState+xml"""))

  lazy val commandStringSerializer = RiftCommandStringSerializer.apply(myRiftwarp)
  lazy val execStateStringSerializer = riftwarp.util.Serializers.createSpecificForStrings[ExecutionState](myRiftwarp)
  lazy val problemStringSerializer = riftwarp.util.Serializers.createSpecificForStrings[Problem](myRiftwarp)

  val commandWithoutTrackingId = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a")
  val commandWithoutTrackingIdJson = commandStringSerializer.serialize("json")(commandWithoutTrackingId).resultOrEscalate._1
  val commandWithoutTrackingIdXml = commandStringSerializer.serialize("xml")(commandWithoutTrackingId).resultOrEscalate._1
  val commandWithTrackingId = AR1ComCreateAR1(DomainCommandHeader(AggregateRootRef(theAlmhirt.getUuid)), "a").track
  val commandWithTrackingIdJson = commandStringSerializer.serialize("json")(commandWithoutTrackingId).resultOrEscalate._1
  val commandWithTrackingIdXml = commandStringSerializer.serialize("xml")(commandWithoutTrackingId).resultOrEscalate._1

  override lazy val executionStateMarshaller = ExecutionStateMarshalling.marshaller(execStateStringSerializer, null, `application/vnd.acme.ExecutionState+json`, `application/vnd.acme.ExecutionState+xml`).resultOrEscalate
  implicit lazy val executionStateUnmarshaller = ExecutionStateMarshalling.unmarshaller(execStateStringSerializer, null, `application/vnd.acme.ExecutionState+json`, `application/vnd.acme.ExecutionState+xml`).resultOrEscalate
  override lazy val problemMarshaller = ProblemMarshalling.marshaller(problemStringSerializer, null, `application/vnd.acme.Problem+json`, `application/vnd.acme.Problem+xml`).resultOrEscalate
  override lazy val commandUnmarshaller = CommandMarshalling.unmarshaller(commandStringSerializer, null, `application/vnd.acme.Command+json`, `application/vnd.acme.Command+xml`).resultOrEscalate
  lazy val commandMarshaller = CommandMarshalling.marshaller(commandStringSerializer, null, `application/vnd.acme.Command+json`, `application/vnd.acme.Command+xml`).resultOrEscalate

  private val executeCommandRoute = executeCommandRoutePart

  describe("HttpCommandEndpoint - executeCommand") {
    it("""should return a MethodNotAllowed error for GET requests to the "/execute" path""") {
      Get("/execute") ~> sealRoute(executeCommandRoute) ~> check {
        status should equal(MethodNotAllowed)
        entityAs[String] === "HTTP method not allowed, supported methods: PUT"
      }
    }

    it("""should return a MethodNotAllowed error for POST requests to the "/execute" path""") {
      Post("/execute") ~> sealRoute(executeCommandRoute) ~> check {
        status should equal(MethodNotAllowed)
        entityAs[String] === "HTTP method not allowed, supported methods: PUT"
      }
    }

    it("""should accept a command without a tracking id PUT to the "/execute" path with contentType "application/vnd.acme.Command+json" and respond with an empty body.""") {
      Put("/execute", commandWithoutTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
        status should equal(Accepted)
        entityAs[String] should equal("")
      }
    }

    it("""should accept a command with a tracking id PUT to the "/execute" path with contentType "application/vnd.acme.Command+json" and respond with the tracking id.""") {
      Put("/execute", commandWithTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
        status should equal(Accepted)
        entityAs[String] should equal(commandWithTrackingId.trackingId)
      }
    }

    it("""should accept a command with a tracking id PUT to the "/execute?tracked" path with contentType "application/vnd.acme.Command+json" and respond with the tracking id.""") {
      Put("/execute?tracked", commandWithTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
        status should equal(Accepted)
        entityAs[String] should equal(commandWithTrackingId.trackingId)
      }
    }

    it("""should accept a command without a tracking id PUT to the "/execute?tracked" path with contentType "application/vnd.acme.Command+json" and respond with a new tracking id.""") {
      Put("/execute?tracked", commandWithoutTrackingId)(commandMarshaller) ~> executeCommandRoute ~> check {
        status should equal(Accepted)
        entityAs[String] should not be ('empty)
      }
    }

    it("""should accept a command with a tracking id PUT to the "/execute?sync" path with contentType "application/vnd.acme.Command+json" and respond with an ExecutionSuccessful.""") {
      val cmd = commandWithoutTrackingId.track
      val execState = ExecutionSuccessful(cmd.trackingId, "ahh!")
      tracker ! ExecutionStateChanged(execState)
      Put("/execute?sync", cmd)(commandMarshaller) ~> executeCommandRoute ~> check {
        status should equal(OK)
        entityAs[ExecutionState] should equal(execState)
      }
    }
  }
}