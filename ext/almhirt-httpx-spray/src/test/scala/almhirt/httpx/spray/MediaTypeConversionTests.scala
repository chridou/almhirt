package almhirt.httpx.spray

import org.scalatest._
import almhirt.http._

class MediaTypeConversionTests  extends FunSuite with Matchers {
  test("A binary MediaType(msgpack) created with AlmMediaType.applicationStructured should return on .binary") {
    val mt = AlmMediaType.applicationStructured(NoVendor, "MyType", "msgpack").toSprayMediaType
    mt.binary should equal(true)
  }
}