package almhirt.httpx.spray

import org.scalatest._
import almhirt.http._
import spray.http.MediaType

class MediaTypeConversionTests extends FunSuite with Matchers {
  test("A binary MediaType(msgpack) created with AlmMediaType.applicationStructured should return on .binary") {
    val mt = AlmMediaType.applicationStructured(NoVendor, "MyType", "msgpack").toSprayMediaType
    mt.binary should equal(true)
  }

  test("Covert a custom spray media type to an alm media type") {
    val amt = MediaType.custom("application/vnd.dial.VestigoDocumentCommand+msgpack").toAlmMediaType
    info(amt.value)
  }
}