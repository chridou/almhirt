package almhirt.domain

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.util.{ UUID => JUUID }
import almhirt.almvalidation.kit._
import almhirt.core.types._
import almhirt.common._

class UpdateRecorderSpecs extends WordSpec with ShouldMatchers {
  implicit val ccuad = CanCreateUuidsAndDateTimes()
  
  val (testAr, events) = TestAr.fromScratch(ccuad.getUuid, "a").result.forceResult

  "UpdateRecorder" when {
    "starting with an AR" should {
      "accept an AR with version 1" in {
        UpdateRecorder.startWith(testAr).isAccepted should be(true)
      }
      "reject an AR with version 0" in {
        UpdateRecorder.startWith(testAr.copy(ref = testAr.ref.copy(version = 0L))).isRejected should be(true)
      }
      "target the same version as the AR's version" in {
        UpdateRecorder.startWith(testAr).targetVersion should equal(testAr.version)
      }
      "require to be targetted on the AR's version" in {
        UpdateRecorder.startWith(testAr).requiredTargetVersionOnNextEvent should equal(testAr.version)
      }
    }
    "starting from an AR(version=1) creating a resulting AR with version 2 and 1 event" should {
      "be accepted" in {
        testAr.changeB(Some("b")).isAccepted should be(true)
      }
      "really result in an AR with version 2" in {
        testAr.changeB(Some("b")).ar.forceResult.version should equal(2)
      }
      "really have created 1 event" in {
        testAr.changeB(Some("b")).events should have size (1)
      }
      "have created 1 event targetting version 1" in {
        testAr.changeB(Some("b")).events.head.aggVersion should equal(1)
      }
      "target version 1" in {
        testAr.changeB(Some("b")).targetVersion should equal(1)
      }
      "require to be targetted on version 2" in {
        testAr.changeB(Some("b")).requiredTargetVersionOnNextEvent should equal(2)
      }
    }
    "mapping over an UR already containg an AR with version 1" should {
      "map to another AR with version 1 and the same id even though a property has been changed" in {
        UpdateRecorder.startWith(testAr).map(ar => ar.copy(theB = Some("Bob"))).isAccepted should be(true)
      }
      "not map to another AR with a different version and the same id" in {
        UpdateRecorder.startWith(testAr).map(ar => ar.copy(ref = testAr.ref.copy(version = 2L))).isRejected should be(true)
      }
      "not map to another AR with the same version and a different id" in {
        UpdateRecorder.startWith(testAr).map(ar => ar.copy(ref = testAr.ref.copy(id = JUUID.randomUUID))).isRejected should be(true)
      }
      "not map to another AR with a different version and a different id" in {
        UpdateRecorder.startWith(testAr).map(ar => ar.copy(ref = (JUUID.randomUUID(), 2L))).isRejected should be(true)
      }
    }
    "flatMapping over an UR already containg an AR with version 1 and updating the AR with 1 event targeting version 1" should {
      "be accepted" in {
        val rec = UpdateRecorder.startWith(testAr).flatMap { ar =>
          val inner = ar.changeB(Some("b"))
          inner
        }
        rec.isAccepted should be(true)
      }
      "result in an AR with version 2" in {
        UpdateRecorder.startWith(testAr).flatMap(_.changeB(Some("b"))).ar.forceResult.version should equal(2)
      }
      "really have created 1 event" in {
        UpdateRecorder.startWith(testAr).flatMap(_.changeB(Some("b"))).events should have size (1)
      }
      "have created 1 event targetting version 1" in {
        UpdateRecorder.startWith(testAr).flatMap(_.changeB(Some("b"))).events.head.aggVersion should equal(1)
      }
      "target version 1" in {
        UpdateRecorder.startWith(testAr).flatMap(_.changeB(Some("b"))).targetVersion should equal(1)
      }
      "require to be targetted on version 2" in {
        UpdateRecorder.startWith(testAr).flatMap(_.changeB(Some("b"))).requiredTargetVersionOnNextEvent should equal(2)
      }

    }
  }
}