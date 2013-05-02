package riftwarp

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std._
import riftwarp.std.default._
import SerializationDefaults._

class JsonSerialization extends FunSuite with MustMatchers {
  test("A WarpString must dematerialize to the corresponding JSON String") {
    val res = WarpString("hallo").dematerialize[String @@ WarpTags.Json]
    res must equal(""""hallo"""")
  }

  test("A WarpBoolean must dematerialize to the corresponding JSON Boolean") {
    val res = WarpBoolean(true).dematerialize[String @@ WarpTags.Json]
    res must equal("""true""")
  }

  test("A WarpByte must dematerialize to the corresponding JSON Number") {
    val res = WarpByte(127.toByte).dematerialize[String @@ WarpTags.Json]
    res must equal("""127""")
  }

  test("A WarpInt must dematerialize to the corresponding JSON Number") {
    val res = WarpInt(12800000).dematerialize[String @@ WarpTags.Json]
    res must equal("""12800000""")
  }

  test("A WarpLong must dematerialize to the corresponding JSON Number") {
    val res = WarpLong(128000000000L).dematerialize[String @@ WarpTags.Json]
    res must equal("""128000000000""")
  }

  test("A WarpBigInt must dematerialize to the corresponding JSON String") {
    val res = WarpBigInt("1234567898765432123456789").dematerialize[String @@ WarpTags.Json]
    res must equal(""""1234567898765432123456789"""")
  }

  test("A WarpFloat must dematerialize to the corresponding JSON Number") {
    val res = WarpFloat(123.456.toFloat).dematerialize[String @@ WarpTags.Json]
    res must equal("""123.456""")
  }

  test("A WarpDouble must dematerialize to the corresponding JSON Number") {
    val res = WarpDouble(123.456).dematerialize[String @@ WarpTags.Json]
    res must equal("""123.456""")
  }

  test("A WarpBigDecimal must dematerialize to the corresponding JSON String") {
    val res = WarpBigDecimal("1233847837483.45623891237198732987").dematerialize[String @@ WarpTags.Json]
    res must equal(""""1233847837483.45623891237198732987"""")
  }

  test("A WarpUuid must dematerialize to the corresponding JSON String") {
    val uuid = JUUID.randomUUID()
    val res = WarpUuid(uuid).dematerialize[String @@ WarpTags.Json]
    res must equal(s""""${uuid.toString()}"""")
  }

  test("A WarpUri must dematerialize to the corresponding JSON String") {
    val uri = new java.net.URI("http://www.almhirt.org")
    val res = WarpUri(uri).dematerialize[String @@ WarpTags.Json]
    res must equal(s""""${uri.toString()}"""")
  }

  test("A WarpDateTime must dematerialize to the corresponding JSON String") {
    val dateTime = DateTime.now()
    val res = WarpDateTime(dateTime).dematerialize[String @@ WarpTags.Json]
    res must equal(s""""${dateTime.toString()}"""")
  }

  
  test("A WarpObject dematerialized must rematerialize to a WarpObject") {
    val obj = WarpObject(None, Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None) ))
    val dematerialized = obj.dematerialize[String @@ WarpTags.Json]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(obj)
  }

  test("WarpObject(PrimitiveTypes) dematerialized must rematerialize to an equal instance") {
    val obj = TestObjectA.pete.primitiveTypes.packFlat.forceResult
    val dematerialized = obj.dematerialize[String @@ WarpTags.Json]
    val rematerialized = dematerialized.rematerialize.forceResult
    val result = rematerialized.unpackFlat[PrimitiveTypes].forceResult
    result must equal(TestObjectA.pete.primitiveTypes)
  }

  test("WarpObject(PrimitiveMaps) dematerialized must rematerialize to an equal instance") {
    val objV = TestObjectA.pete.primitiveMaps.packFlat
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpackFlat[PrimitiveMaps]
    resultV.forceResult must equal(TestObjectA.pete.primitiveMaps)
  }
  
}