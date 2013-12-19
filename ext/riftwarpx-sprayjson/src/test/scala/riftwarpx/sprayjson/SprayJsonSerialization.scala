package riftwarpx.sprayjson

import org.scalatest._
import java.util.{ UUID => JUUID }
import org.joda.time.{DateTime, LocalDateTime}
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


class SprayJsonSerialization extends FunSuite with Matchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())
  
  implicit val StdLibJsonStringRematerializer = riftwarpx.sprayjson.FromJsonStringRematerializer

  val riftWarp = {
    val rw = RiftWarp(packers, unpackers)
    SprayJson.addToRiftWarp(rw)
  }
  
  val blobPackage = (WarpDescriptor("a") ~> Blob("theBlob", Vector(1,2,3,4,5,6,7,8,9,0).map(_.toByte))).forceResult
  
  test("A WarpObject dematerialized should rematerialize to a WarpObject") {
    val obj = WarpObject(None, Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None) ))
    val dematerialized = obj.dematerialize[String @@ WarpTags.Json]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpObject(None, Vector(WarpElement("propA", Some(WarpBigDecimal(123.456))), WarpElement("propB", None) )))
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
    val dematV = riftWarp.departure("json", TestObjectA.pete).flatMap(x => x._1.castTo[String].map((_, x._2)))
    dematV.isSuccess should be(true)
  }
  
  test("RiftWarp should dematerialize the PrimitiveTypes and rematerialize them") {
    val dematV = riftWarp.departure("json", TestObjectA.pete.primitiveTypes).flatMap(x => x._1.castTo[String].map((_, x._2)))
    val resV = riftWarp.arrival("json", dematV.forceResult._1)
    resV.forceResult should equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarp should dematerialize a UUID") {
    val uuid = JUUID.randomUUID()
    val dematV = riftWarp.departure("json", uuid).flatMap(x => x._1.castTo[String].map((_, x._2)))
    dematV.forceResult should equal(("\""+uuid.toString()+"\"", WarpDescriptor("UUID")))
  }

  test("SerializerOnStrings should serialize a UUID") {
    val serializer = new WarpSerializerToString[JUUID](riftWarp).serialize("json")_
    val uuid = JUUID.randomUUID()
    val resV = serializer(uuid, Map.empty)
    resV.forceResult should equal(("\""+uuid.toString()+"\"", Some(WarpDescriptor("UUID").toParsableString())))
  }

  test("SerializerOnStrings should serialize a Boolean") {
    val serializer = new WarpSerializerToString[Boolean](riftWarp).serialize("json")_
    val resV = serializer(true, Map.empty)
    resV.forceResult should equal(("true", Some(WarpDescriptor("Boolean").toParsableString())))
  }

  test("SerializerOnStrings[String] should serialize and deserialze a String") {
    val serializer = Serializers.createSpecificForStrings[String](riftWarp)
    val resV = serializer.serialize("json")("hallo")
    val dematV = serializer.deserialize("json")(resV.forceResult._1)
    dematV.forceResult should equal("hallo")
  }

  test("SerializerOnStrings[Any] should serialize and deserialze a String") {
    val serializer = Serializers.createSpecificForStrings[Any](riftWarp)
    val resV = serializer.serialize("json")("hallo")
    val dematV = serializer.deserialize("json")(resV.forceResult._1)
    dematV.forceResult should equal("hallo")
  }

  test("SerializerOnStrings[Any] should serialize and deserialze a Double") {
    val serializer = Serializers.createSpecificForStrings[Any](riftWarp)
    val resV = serializer.serialize("json")(1.234)
    val dematV = serializer.deserialize("json")(resV.forceResult._1)
    dematV.forceResult should equal(1.234)
  }

  test("SerializerOnStrings[Any] should serialize and deserialze the PrimitiveListMAs") {
    val serializer = Serializers.createSpecificForStrings[Any](riftWarp)
    val resV = serializer.serialize("json")(TestObjectA.pete.primitiveListMAs)
    val dematV = serializer.deserialize("json")(resV.forceResult._1)
    dematV.forceResult should equal(TestObjectA.pete.primitiveListMAs)
  }
  
}