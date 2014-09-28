package almhirt.converters

import java.util.{ UUID ⇒ JUUID }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import org.scalatest._

class UuidTests extends FunSuite with Matchers {
  import MiscConverters._

  test("A base64 encoded uuid should be convertable to a UUID") {
    for (i <- 1 to 10000) {
      val uuid = JUUID.randomUUID()
      val b64 = uuidToBase64String(uuid)
      base64StringToUuid(b64) should equal(scalaz.Success(uuid))
    }
  }

  test("A uuid string should be convertable to a base 64 string") {
    for (i <- 1 to 10000) {
      val uuid = JUUID.randomUUID()
      val uuidStr = uuid.toString
      val b64 = uuidStrToBase64Str(uuidStr).forceResult
      b64 should equal(uuidToBase64String(uuid))
    }
  }
  
  test("A invalid uuid string should not be convertable to a base64 string") {
    uuidStrToBase64Str("aaa").isFailure should be(true)
  }
}
