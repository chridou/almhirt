package almhirt.httpx.akkahttp

import org.scalatest._
import almhirt.http._
import akka.http.scaladsl.model.MediaType

class MediaTypeConversionTests extends FunSuite with Matchers {
  test("A binary MediaType(msgpack) created with AlmMediaType.applicationStructured should return on .binary") {
    val mt = AlmMediaType.applicationStructured(NoVendor, "MyType", "msgpack").toAkkaHttpMediaType
    mt.binary should equal(true)
  }

  test("Covert a custom akkahttp media type to an alm media type") {
    val amt = MediaType.custom("application/vnd.dial.VestigoDocumentCommand+msgpack", true, MediaType.Compressible, List.empty).toAlmMediaType
    info(amt.value)
  }
}