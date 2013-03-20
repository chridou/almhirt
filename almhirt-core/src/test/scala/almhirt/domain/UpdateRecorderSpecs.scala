package almhirt.domain

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.util.{ UUID => JUUID }
import almhirt.almvalidation.kit._
import almhirt.common._
import almhirt.core.test._

class UpdateRecorderSpecs extends WordSpec with ShouldMatchers {
  implicit object ccuad extends CanCreateUuidsAndDateTimes  
  
  val person = TestPerson("Peter").ar.forceResult

  "UpdateRecorder" when {
    "starting with an AR" should {
      "accept an AR with version 1" in {
        UpdateRecorder.startWith(person).isAccepted should be(true)
      }
      "reject an AR with version 0" in {
        UpdateRecorder.startWith(person.copy(ref = person.ref.copy(version = 0L))).isRejected should be(true)
      }
      "target the same version as the AR's version" in {
        UpdateRecorder.startWith(person).targetVersion should equal(person.version)
      }
      "require to be targetted on the  AR's version" in {
        UpdateRecorder.startWith(person).requiredTargetVersionOnNextEvent should equal(person.version)
      }
    }
    "starting from an AR(version=1) creating a resulting AR with version 2 and 1 event" should {
      "be accepted" in {
        person.changeName("Bob").isAccepted should be(true)
      }
      "really result in an AR with version 2" in {
        person.changeName("Bob").ar.forceResult.version should equal(2)
      }
      "really have created 1 event" in {
        person.changeName("Bob").events should have size (1)
      }
      "have created 1 event targetting version 1" in {
        person.changeName("Bob").events.head.aggVersion should equal(1)
      }
      "target version 1" in {
        person.changeName("Bob").targetVersion should equal(1)
      }
      "require to be targetted on version 2" in {
        person.changeName("Bob").requiredTargetVersionOnNextEvent should equal(2)
      }
    }
    "mapping over an UR already containg an AR with version 1" should {
      "map to another AR with version 1 and the same id even though a property has been changed" in {
        UpdateRecorder.startWith(person).map(ar => ar.copy(name = "Bob")).isAccepted should be(true)
      }
      "not map to another AR with a different version and the same id" in {
        UpdateRecorder.startWith(person).map(ar => ar.copy(ref = person.ref.copy(version = 2L))).isRejected should be(true)
      }
      "not map to another AR with the same version and a different id" in {
        UpdateRecorder.startWith(person).map(ar => ar.copy(ref = person.ref.copy(id = JUUID.randomUUID))).isRejected should be(true)
      }
      "not map to another AR with a different version and a different  id" in {
        UpdateRecorder.startWith(person).map(ar => ar.copy(ref = (JUUID.randomUUID(), 2L))).isRejected should be(true)
      }
    }
    "flatMapping over an UR already containg an AR with version 1 and updating the AR with 1 event targeting version 1" should {
      "be accepted" in {
        val rec = UpdateRecorder.startWith(person).flatMap { ar =>
          val inner = ar.changeName("Bob")
          inner
        }
        rec.isAccepted should be(true)
      }
      "result in an AR with version 2" in {
        UpdateRecorder.startWith(person).flatMap(_.changeName("Bob")).ar.forceResult.version should equal(2)
      }
      "really have created 1 event" in {
        UpdateRecorder.startWith(person).flatMap(_.changeName("Bob")).events should have size (1)
      }
      "have created 1 event targetting version 1" in {
        UpdateRecorder.startWith(person).flatMap(_.changeName("Bob")).events.head.aggVersion should equal(1)
      }
      "target version 1" in {
        UpdateRecorder.startWith(person).flatMap(_.changeName("Bob")).targetVersion should equal(1)
      }
      "require to be targetted on version 2" in {
        UpdateRecorder.startWith(person).flatMap(_.changeName("Bob")).requiredTargetVersionOnNextEvent should equal(2)
      }

    }
  }
}