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
import riftwarp.std.kit._
import riftwarp.std.default._
import SerializationDefaults._
import riftwarp.util.WarpSerializerToString
import riftwarp.util.Serializers

class JsonSerialization extends FunSuite with MustMatchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())
  
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

    test("WarpObject(PrimitiveListMAs) dematerialized must rematerialize to an equal instance") {
    val objV = TestObjectA.pete.primitiveListMAs.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
    val rematerialized = dematerialized.rematerialize.forceResult
    val result = rematerialized.unpackFlat[PrimitiveListMAs].forceResult
    result must equal(TestObjectA.pete.primitiveListMAs)
  }

  test("WarpObject(PrimitiveMaps) dematerialized must rematerialize to an equal instance") {
    val objV = TestObjectA.pete.primitiveMaps.packFlat
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpackFlat[PrimitiveMaps]
    resultV.forceResult must equal(TestObjectA.pete.primitiveMaps)
  }

    test("WarpObject(TestObjectA) must be dematrielized succesfully") {
    val objV = TestObjectA.pete.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
  }

  test("WarpObject(TestObjectA) dematerialized must rematerialize without error") {
    val objV = TestObjectA.pete.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpack[TestObjectA]
    resultV.isSuccess must be(true)
  }
    
  ignore("WarpObject(TestObjectA) dematerialized must rematerialize to an equal instance") {
    val objV = TestObjectA.pete.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Json]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpack[TestObjectA]
    println(resultV)
    resultV.forceResult must equal(TestObjectA.pete)
  }
  
  test("RiftWarpFuns must dematerialize a DateTime") {
    val dt = DateTime.now
    val resV = prepareFlatDeparture[DateTime, String @@ WarpTags.Json](dt)
    resV.forceResult._1 must equal("\""+dt.toString()+"\"")
  }
  
  test("RiftWarpFuns must dematerialize the PrimitiveTypes without a failure") {
    val resV = prepareFlatDeparture[PrimitiveTypes, String @@ WarpTags.Json](TestObjectA.pete.primitiveTypes)
    resV.isSuccess must be(true)
  }

  test("RiftWarpFuns must dematerialize the PrimitiveTypes and rematerialize them") {
    val dematV = prepareFlatDeparture[PrimitiveTypes, String @@ WarpTags.Json](TestObjectA.pete.primitiveTypes)
    val resV = handleArrival[String @@ WarpTags.Json, PrimitiveTypes](dematV.forceResult._1)
    resV.forceResult must equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarpFuns must dematerialize the PrimitiveTypes and rematerialize them by lookup") {
    val dematV = prepareFreeDeparture[String @@ WarpTags.Json](TestObjectA.pete.primitiveTypes)
    val resV = handleTypedArrival[String @@ WarpTags.Json, PrimitiveTypes](dematV.forceResult._1)
    resV.forceResult must equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarp must dematerialize pete without error") {
    val riftwarp = RiftWarp(packers, unpackers)
    val dematV = riftwarp.departureTyped[String]("json", TestObjectA.pete)
    dematV.isSuccess must be(true)
  }
  
  test("RiftWarp must dematerialize the PrimitiveTypes and rematerialize them") {
    val riftwarp = RiftWarp(packers, unpackers)
    val dematV = riftwarp.departureTyped[String]("json", TestObjectA.pete.primitiveTypes)
    val resV = riftwarp.arrivalTyped[String, PrimitiveTypes]("json", dematV.forceResult._1)
    resV.forceResult must equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarp must dematerialize a UUID") {
    val riftwarp = RiftWarp(packers, unpackers)
    val uuid = JUUID.randomUUID()
    val dematV = riftwarp.departureTyped[String]("json", uuid)
    dematV.forceResult must equal(("\""+uuid.toString()+"\"", WarpDescriptor("UUID")))
  }

  test("SerializerOnStrings must serialize a UUID") {
    val serializer = new WarpSerializerToString[JUUID](RiftWarp(packers, unpackers)).serializingToChannel("json")
    val uuid = JUUID.randomUUID()
    val resV = serializer.serialize(uuid)
    resV.forceResult must equal(("\""+uuid.toString()+"\"", Some(WarpDescriptor("UUID").toParsableString())))
  }

  test("SerializerOnStrings must serialize a Boolean") {
    val serializer = new WarpSerializerToString[Boolean](RiftWarp(packers, unpackers)).serializingToChannel("json")
    val resV = serializer.serialize(true)
    resV.forceResult must equal(("true", Some(WarpDescriptor("Boolean").toParsableString())))
  }

  test("SerializerOnStrings[String] must serialize and deserialze a String") {
    implicit val execContext = HasExecutionContext.single
    val serializer = Serializers.createSpecificForStrings[String](RiftWarp(packers, unpackers)).serializingToChannel("json")
    val resV = serializer.serialize("hallo")
    val dematV = serializer.deserialize("json")(resV.forceResult._1)
    dematV.forceResult must equal("hallo")
  }

  test("SerializerOnStrings[Any] must serialize and deserialze a String") {
    implicit val execContext = HasExecutionContext.single
    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers)).serializingToChannel("json")
    val resV = serializer.serialize("hallo")
    val dematV = serializer.deserialize("json")(resV.forceResult._1)
    dematV.forceResult must equal("hallo")
  }

  test("SerializerOnStrings[Any] must serialize and deserialze a Double") {
    implicit val execContext = HasExecutionContext.single
    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers)).serializingToChannel("json")
    val resV = serializer.serialize(1.234)
    val dematV = serializer.deserialize("json")(resV.forceResult._1)
    dematV.forceResult must equal(1.234)
  }

  test("SerializerOnStrings[Any] must serialize and deserialze the PrimitiveListMAs") {
    implicit val execContext = HasExecutionContext.single
    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers)).serializingToChannel("json")
    val resV = serializer.serialize(TestObjectA.pete.primitiveListMAs)
    val dematV = serializer.deserialize("json")(resV.forceResult._1)
    dematV.forceResult must equal(TestObjectA.pete.primitiveListMAs)
  }
  
}