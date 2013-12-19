package almhirt.http

import org.scalatest._

class AlmMediaTypeTests  extends FunSuite with Matchers {
  test("A MediaType with a binary representation should return true on .binary") {
    val mt = AlmMediaType("application", AlmMediaSubTypeParts(NoVendor, StructuredContent("MyType", "msgpack")), true, BinaryMedia, Seq.empty, false)
    mt.binary should equal(true)
  }
  
  test("A binary MediaType(msgpack) created with AlmMediaType.applicationStructured should return on .binary") {
    val mt = AlmMediaType.applicationStructured(NoVendor, "MyType", "msgpack")
    mt.binary should equal(true)
  }
  
}