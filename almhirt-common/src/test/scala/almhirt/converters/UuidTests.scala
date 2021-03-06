package almhirt.converters

import java.util.{ UUID ⇒ JUUID }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import org.scalatest._
import java.nio.ByteBuffer

class UuidTests extends FunSuite with Matchers {
  import MiscConverters._

  test("A base64 encoded uuid should be convertable to a UUID") {
    for (i ← 1 to 10000) {
      val uuid = JUUID.randomUUID()
      val b64 = uuidToBase64String(uuid)
      base64ToUuid(b64) should equal(scalaz.Success(uuid))
    }
  }

  test("A base64 encoded uuid should not contain a slash!") {
    for (i ← 1 to 100000) {
      val uuid = JUUID.randomUUID()
      val b64 = uuidToBase64String(uuid)
      b64.contains('/') should equal(false)
    }
  }
  
  test("A base64 encoded uuid has length 20 or 22!") {
    for (i ← 1 to 100000) {
      val uuid = JUUID.randomUUID()
      val b64 = uuidToBase64String(uuid)
      (b64.length == 20 || b64.length == 22) should equal(true)
    }
  }

  test("A uuid string should be convertable to a base 64 string") {
    for (i ← 1 to 10000) {
      val uuid = JUUID.randomUUID()
      val uuidStr = uuid.toString
      val b64 = uuidStringToBase64(uuidStr).forceResult
      b64 should equal(uuidToBase64String(uuid))
    }
  }

  test("uuid from/to string") {
    for (i ← 1 to 1000) {
      val uuid = JUUID.randomUUID()
      val uuidParsed = JUUID.fromString(uuid.toString())
      val uuidStr = uuid.toString
      uuidStr should equal(uuid.toString())
    }
  }

  val testUuid = JUUID.randomUUID()
  test(s"""uuid from string: ${testUuid.toString}""") {
    val uuidFromString = JUUID.fromString(testUuid.toString())
    uuidFromString should equal(testUuid)
  }

  test(s"""uuid → base → uuid: ${testUuid.toString}""") {
    val b64 = uuidToBase64String(testUuid)
    val uuid = base64ToUuid(b64).forceResult
    uuid should equal(testUuid)
  }

  test(s"""uuid → base → uuidStr: ${testUuid.toString}""") {
    val b64 = uuidToBase64String(testUuid)
    val uuidStr = base64ToUuidString(b64).forceResult
    uuidStr should equal(testUuid.toString())
  }

  val URL_SAFE_BASE64 = new org.apache.commons.codec.binary.Base64(true)
  test(s"""The base 64 representation of a uuid(${testUuid.toString}) is big endian(network-byte-order)""") {
    val leastSignificantBits = testUuid.getLeastSignificantBits()
    val mostSignificantBits = testUuid.getMostSignificantBits()
    val b64Str = uuidToBase64String(testUuid)
    val b64Bytes = URL_SAFE_BASE64.decode(b64Str)
    val longBuffer = ByteBuffer.wrap(b64Bytes).asLongBuffer
    val b64mostSignificantBits = longBuffer.get()
    val b64leastSignificantBits = longBuffer.get()
    b64mostSignificantBits should equal(mostSignificantBits)
    b64leastSignificantBits should equal(leastSignificantBits)
  }

  test("A invalid uuid string should not be convertable to a base64 string") {
    uuidStringToBase64("aaa").isFailure should be(true)
  }

  
  test("A invalid uuid string(too long) should not be convertable to a base64 string") {
    val uuidStr = testUuid.toString() + "1"
    uuidStringToBase64(uuidStr).isFailure should be(true)
  }
  
}
