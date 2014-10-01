package riftwarp

import org.scalatest._
import java.util.{ UUID ⇒ JUUID }
import org.joda.time.{DateTime, LocalDateTime}
import scalaz.@@
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std._
import riftwarp.std.kit._
import riftwarp.std.default._
import SerializationDefaults._

class JsonSerialization extends FunSuite with Matchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())
  
  val blobPackage = (WarpDescriptor("a") ~> Blob("theBlob", Vector(1,2,3,4,5,6,7,8,9,0).map(_.toByte))).forceResult
  
  test("A WarpString should dematerialize to the corresponding JSON String") {
    val res = WarpString("hallo").dematerialize[String @@ WarpTags.Json]
    res should equal(""""hallo"""")
  }

  test("A WarpBoolean should dematerialize to the corresponding JSON Boolean") {
    val res = WarpBoolean(true).dematerialize[String @@ WarpTags.Json]
    res should equal("""true""")
  }

  test("A WarpByte should dematerialize to the corresponding JSON Number") {
    val res = WarpByte(127.toByte).dematerialize[String @@ WarpTags.Json]
    res should equal("""127""")
  }

  test("A WarpInt should dematerialize to the corresponding JSON Number") {
    val res = WarpInt(12800000).dematerialize[String @@ WarpTags.Json]
    res should equal("""12800000""")
  }

  test("A WarpLong should dematerialize to the corresponding JSON Number") {
    val res = WarpLong(128000000000L).dematerialize[String @@ WarpTags.Json]
    res should equal("""128000000000""")
  }

  test("A WarpBigInt should dematerialize to the corresponding JSON String") {
    val res = WarpBigInt("1234567898765432123456789").dematerialize[String @@ WarpTags.Json]
    res should equal(""""1234567898765432123456789"""")
  }

  test("A WarpFloat should dematerialize to the corresponding JSON Number") {
    val res = WarpFloat(123.456.toFloat).dematerialize[String @@ WarpTags.Json]
    res should equal("""123.456""")
  }

  test("A WarpDouble should dematerialize to the corresponding JSON Number") {
    val res = WarpDouble(123.456).dematerialize[String @@ WarpTags.Json]
    res should equal("""123.456""")
  }

  test("A WarpBigDecimal should dematerialize to the corresponding JSON String") {
    val res = WarpBigDecimal("1233847837483.45623891237198732987").dematerialize[String @@ WarpTags.Json]
    res should equal(""""1233847837483.45623891237198732987"""")
  }

  test("A WarpUuid should dematerialize to the corresponding JSON String") {
    val uuid = JUUID.randomUUID()
    val res = WarpUuid(uuid).dematerialize[String @@ WarpTags.Json]
    res should equal(s""""${uuid.toString()}"""")
  }

  test("A WarpUri should dematerialize to the corresponding JSON String") {
    val uri = new java.net.URI("http://www.almhirt.org")
    val res = WarpUri(uri).dematerialize[String @@ WarpTags.Json]
    res should equal(s""""${uri.toString()}"""")
  }

  test("A WarpDateTime should dematerialize to the corresponding JSON String") {
    val dateTime = DateTime.now()
    val res = WarpDateTime(dateTime).dematerialize[String @@ WarpTags.Json]
    res should equal(s""""${dateTime.toString()}"""")
  }

  test("A WarpLocalDateTime should dematerialize to the corresponding JSON String") {
    val dateTime = LocalDateTime.now()
    val res = WarpLocalDateTime(dateTime).dematerialize[String @@ WarpTags.Json]
    res should equal(s""""${dateTime.toString()}"""")
  }
  
  test("A WarpBlob should dematerialize to the corresponding JSON String") {
    val blob = Vector(1,2,3,4,5,6,7,8,9,0).map(_.toByte)
    val expected = org.apache.commons.codec.binary.Base64.encodeBase64String(blob.toArray)
    val res = WarpBlob(blob).dematerialize[String @@ WarpTags.Json]
    res should equal("""{"warpdesc":"Base64Blob","data":"AQIDBAUGBwgJAA=="}""")
  }

 
  test("A WarpObject dematerialized should rematerialize to a WarpObject") {
    val obj = WarpObject(None, Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None) ))
    val dematerialized = obj.dematerialize[String @@ WarpTags.Json]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(obj)
  }

  test("The WarpObject with a blob should dematerialize and rematerialize to an equal instance") {
    val dematerialized = blobPackage.dematerialize[String @@ WarpTags.Json]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(blobPackage)
  }
  
  test("WarpObject(PrimitiveTypes) dematerialized should rematerialize to an equal instance") {
    val obj = TestObjectA.pete.primitiveTypes.packFlat.forceResult
    val dematerialized = obj.dematerialize[String @@ WarpTags.Json]
    val rematerialized = dematerialized.rematerialize.forceResult
    val result = rematerialized.unpackFlat[PrimitiveTypes].forceResult
    result should equal(TestObjectA.pete.primitiveTypes)
  }

    test("WarpObject(PrimitiveListMAs) dematerialized should rematerialize to an equal instance") {
    val objV = TestObjectA.pete.primitiveListMAs.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
    val rematerialized = dematerialized.rematerialize.forceResult
    val result = rematerialized.unpackFlat[PrimitiveListMAs].forceResult
    result should equal(TestObjectA.pete.primitiveListMAs)
  }

  test("WarpObject(PrimitiveMaps) dematerialized should rematerialize to an equal instance") {
    val objV = TestObjectA.pete.primitiveMaps.packFlat
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpackFlat[PrimitiveMaps]
    resultV.forceResult should equal(TestObjectA.pete.primitiveMaps)
  }

    test("WarpObject(TestObjectA) should be dematrielized succesfully") {
    val objV = TestObjectA.pete.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
  }

  test("WarpObject(TestObjectA) dematerialized should rematerialize without error") {
    val objV = TestObjectA.pete.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpack[TestObjectA]
    resultV.isSuccess should be(true)
  }
    
  ignore("WarpObject(TestObjectA) dematerialized should rematerialize to an equal instance") {
    val objV = TestObjectA.pete.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpack[TestObjectA]
    resultV.forceResult should equal(TestObjectA.pete)
  }
  
  test("RiftWarpFuns should dematerialize a DateTime") {
    val dt = DateTime.now
    val resV = prepareFlatDeparture[DateTime, String @@ WarpTags.Json](dt)
    resV.forceResult._1 should equal("\""+dt.toString()+"\"")
  }
  
  test("RiftWarpFuns should dematerialize the PrimitiveTypes without a failure") {
    val resV = prepareFlatDeparture[PrimitiveTypes, String @@ WarpTags.Json](TestObjectA.pete.primitiveTypes)
    resV.isSuccess should be(true)
  }

  test("RiftWarpFuns should dematerialize the PrimitiveTypes and rematerialize them") {
    val dematV = prepareFlatDeparture[PrimitiveTypes, String @@ WarpTags.Json](TestObjectA.pete.primitiveTypes)
    val resV = handleArrival[String @@ WarpTags.Json, PrimitiveTypes](dematV.forceResult._1)
    resV.forceResult should equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarpFuns should dematerialize the PrimitiveTypes and rematerialize them by lookup") {
    val dematV = prepareFreeDeparture[String @@ WarpTags.Json](TestObjectA.pete.primitiveTypes)
    val resV = handleTypedArrival[String @@ WarpTags.Json, PrimitiveTypes](dematV.forceResult._1)
    resV.forceResult should equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarp should dematerialize pete without error") {
    val riftwarp = RiftWarp(packers, unpackers)
    val dematV = riftwarp.departure("json", TestObjectA.pete)
    dematV.isSuccess should be(true)
  }
  
  test("RiftWarp should dematerialize the PrimitiveTypes and rematerialize them") {
    val riftwarp = RiftWarp(packers, unpackers)
    val dematV = riftwarp.departure("json", TestObjectA.pete.primitiveTypes).flatMap(x ⇒ x._1.castTo[String].map((_, x._2)))
    val resV = riftwarp.arrival("json", dematV.forceResult._1)
    resV.forceResult should equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarp should dematerialize a UUID") {
    val riftwarp = RiftWarp(packers, unpackers)
    val uuid = JUUID.randomUUID()
    val dematV = riftwarp.departure("json", uuid).flatMap(x ⇒ x._1.castTo[String].map((_, x._2)))
    dematV.forceResult should equal(("\""+uuid.toString()+"\"", WarpDescriptor("UUID")))
  }

//  test("SerializerOnStrings should serialize a UUID") {
//    val serializer = new WarpSerializerToString[JUUID](RiftWarp(packers, unpackers)).serialize("json")_
//    val uuid = JUUID.randomUUID()
//    val resV = serializer(uuid, Map.empty)
//    resV.forceResult should equal(("\""+uuid.toString()+"\"", Some(WarpDescriptor("UUID").toParsableString())))
//  }
//
//  test("SerializerOnStrings should serialize a Boolean") {
//    val serializer = new WarpSerializerToString[Boolean](RiftWarp(packers, unpackers)).serialize("json")_
//    val resV = serializer(true, Map.empty)
//    resV.forceResult should equal(("true", Some(WarpDescriptor("Boolean").toParsableString())))
//  }
//
//  test("SerializerOnStrings[String] should serialize and deserialze a String") {
//    val serializer = Serializers.createSpecificForStrings[String](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("json")("hallo")
//    val dematV = serializer.deserialize("json")(resV.forceResult._1)
//    dematV.forceResult should equal("hallo")
//  }
//
//  test("SerializerOnStrings[Any] should serialize and deserialze a String") {
//    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("json")("hallo")
//    val dematV = serializer.deserialize("json")(resV.forceResult._1)
//    dematV.forceResult should equal("hallo")
//  }
//
//  test("SerializerOnStrings[Any] should serialize and deserialze a Double") {
//    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("json")(1.234)
//    val dematV = serializer.deserialize("json")(resV.forceResult._1)
//    dematV.forceResult should equal(1.234)
//  }
//
//  test("SerializerOnStrings[Any] should serialize and deserialze the PrimitiveListMAs") {
//    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("json")(TestObjectA.pete.primitiveListMAs)
//    val dematV = serializer.deserialize("json")(resV.forceResult._1)
//    dematV.forceResult should equal(TestObjectA.pete.primitiveListMAs)
//  }
  
}