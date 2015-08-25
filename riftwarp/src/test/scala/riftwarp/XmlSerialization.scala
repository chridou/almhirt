package riftwarp

import org.scalatest._
import java.util.{ UUID ⇒ JUUID }
import _root_.java.time.{ ZonedDateTime, LocalDateTime }
import scalaz.@@
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std._
import riftwarp.std.kit._
import riftwarp.std.default._
import SerializationDefaults._
import almhirt.problem._

class XmlSerialization extends FunSuite with Matchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())

  val blobPackage = (WarpDescriptor("a") ~> Blob("theBlob", Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 0).map(_.toByte))).forceResult

  test("A WarpString should dematerialize to the corresponding XML String") {
    val res = WarpString("hallo").dematerialize[String @@ WarpTags.Xml]
    res should equal("""<Value type="String">hallo</Value>""")
  }

  test("A WarpBoolean should dematerialize to the corresponding XML Boolean") {
    val res = WarpBoolean(true).dematerialize[String @@ WarpTags.Xml]
    res should equal("""<Value type="Boolean">true</Value>""")
  }

  test("A WarpByte should dematerialize to the corresponding XML Number") {
    val res = WarpByte(127.toByte).dematerialize[String @@ WarpTags.Xml]
    res should equal("""<Value type="Byte">127</Value>""")
  }

  test("A WarpInt should dematerialize to the corresponding XML Number") {
    val res = WarpInt(12800000).dematerialize[String @@ WarpTags.Xml]
    res should equal("""<Value type="Int">12800000</Value>""")
  }

  test("A WarpLong should dematerialize to the corresponding XML Number") {
    val res = WarpLong(128000000000L).dematerialize[String @@ WarpTags.Xml]
    res should equal("""<Value type="Long">128000000000</Value>""")
  }

  test("A WarpBigInt should dematerialize to the corresponding XML String") {
    val res = WarpBigInt("1234567898765432123456789").dematerialize[String @@ WarpTags.Xml]
    res should equal("""<Value type="BigInt">1234567898765432123456789</Value>""")
  }

  test("A WarpFloat should dematerialize to the corresponding XML Number") {
    val res = WarpFloat(123.456.toFloat).dematerialize[String @@ WarpTags.Xml]
    res should equal("""<Value type="Float">123.456</Value>""")
  }

  test("A WarpDouble should dematerialize to the corresponding XML Number") {
    val res = WarpDouble(123.456).dematerialize[String @@ WarpTags.Xml]
    res should equal("""<Value type="Double">123.456</Value>""")
  }

  test("A WarpBigDecimal should dematerialize to the corresponding XML String") {
    val res = WarpBigDecimal("1233847837483.45623891237198732987").dematerialize[String @@ WarpTags.Xml]
    res should equal("""<Value type="BigDecimal">1233847837483.45623891237198732987</Value>""")
  }

  test("A WarpUuid should dematerialize to the corresponding XML String") {
    val uuid = JUUID.randomUUID()
    val res = WarpUuid(uuid).dematerialize[String @@ WarpTags.Xml]
    res should equal(s"""<Value type="Uuid">${uuid.toString()}</Value>""")
  }

  test("A WarpUri should dematerialize to the corresponding XML String") {
    val uri = new java.net.URI("http://www.almhirt.org")
    val res = WarpUri(uri).dematerialize[String @@ WarpTags.Xml]
    res should equal(s"""<Value type="Uri">${uri.toString()}</Value>""")
  }

  test("A WarpDateTime should dematerialize to the corresponding XML String") {
    val dateTime = ZonedDateTime.now()
    val res = WarpDateTime(dateTime).dematerialize[String @@ WarpTags.Xml]
    res should equal(s"""<Value type="DateTime">${dateTime.toString()}</Value>""")
  }

    test("A WarpLocalDateTime should dematerialize to the corresponding XML String") {
    val dateTime = LocalDateTime.now()
    val res = WarpLocalDateTime(dateTime).dematerialize[String @@ WarpTags.Xml]
    res should equal(s"""<Value type="LocalDateTime">${dateTime.toString()}</Value>""")
  }

  test("A WarpObject dematerialized should rematerialize to a WarpObject") {
    val obj = WarpObject(None, Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None)))
    val dematerialized = obj.dematerialize[String @@ WarpTags.Xml]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(obj)
  }

  test("WarpObject(PrimitiveTypes) dematerialized should rematerialize to an equal instance") {
    val obj = TestObjectA.pete.primitiveTypes.packFlat.forceResult
    val dematerialized = obj.dematerialize[String @@ WarpTags.Xml]
    val rematerialized = dematerialized.rematerialize.forceResult
    val result = rematerialized.unpackFlat[PrimitiveTypes].forceResult
    result should equal(TestObjectA.pete.primitiveTypes)
  }

  test("WarpObject(PrimitiveListMAs) dematerialized should rematerialize to an equal instance") {
    val objV = TestObjectA.pete.primitiveListMAs.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Xml]
    val rematerialized = dematerialized.rematerialize.forceResult
    val result = rematerialized.unpackFlat[PrimitiveListMAs].forceResult
    result should equal(TestObjectA.pete.primitiveListMAs)
  }

  test("The WarpObject with a blob should dematerialize and rematerialize to an equal instance") {
    val dematerialized = blobPackage.dematerialize[String @@ WarpTags.Xml]
    println(dematerialized)
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(blobPackage)
  }

  test("WarpObject(PrimitiveMaps) dematerialized should rematerialize to an equal instance") {
    val objV = TestObjectA.pete.primitiveMaps.packFlat
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Xml]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpackFlat[PrimitiveMaps]
    resultV.forceResult should equal(TestObjectA.pete.primitiveMaps)
  }
  
  test("WarpObject(TestObjectA) should be dematerialized succesfully") {
    val objV = TestObjectA.pete.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Xml]
  }

  test("WarpObject(TestObjectA) dematerialized should rematerialize without error") {
    val objV = TestObjectA.pete.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Xml]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpack[TestObjectA]
    resultV.isSuccess should be(true)
  }

  ignore("WarpObject(TestObjectA) dematerialized should rematerialize to an equal instance") {
    val objV = TestObjectA.pete.pack
    val dematerialized = objV.forceResult.dematerialize[String @@ WarpTags.Xml]
    val rematerializedV = dematerialized.rematerialize
    val resultV = rematerializedV.forceResult.unpack[TestObjectA]
    println(resultV)
    resultV.forceResult should equal(TestObjectA.pete)
  }

  test("RiftWarpFuns should dematerialize a DateTime") {
    val dt = ZonedDateTime.now
    val resV = prepareFlatDeparture[ZonedDateTime, String @@ WarpTags.Xml](dt)
    resV.forceResult._1 should equal(s"""<Value type="DateTime">${dt.toString()}</Value>""")
  }

  test("RiftWarpFuns should dematerialize the PrimitiveTypes without a failure") {
    val resV = prepareFlatDeparture[PrimitiveTypes, String @@ WarpTags.Xml](TestObjectA.pete.primitiveTypes)
    resV.isSuccess should be(true)
  }

 
  test("RiftWarpFuns should dematerialize the PrimitiveTypes and rematerialize them") {
    val dematV = prepareFlatDeparture[PrimitiveTypes, String @@ WarpTags.Xml](TestObjectA.pete.primitiveTypes)
    val resV = handleArrival[String @@ WarpTags.Xml, PrimitiveTypes](dematV.forceResult._1)
    resV.forceResult should equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarpFuns should dematerialize the PrimitiveTypes and rematerialize them by lookup") {
    val dematV = prepareFreeDeparture[String @@ WarpTags.Xml](TestObjectA.pete.primitiveTypes)
    val resV = handleTypedArrival[String @@ WarpTags.Xml, PrimitiveTypes](dematV.forceResult._1)
    resV.forceResult should equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarp should dematerialize pete without error") {
    val riftwarp = RiftWarp(packers, unpackers)
    val dematV = riftwarp.departure("xml", TestObjectA.pete).flatMap(x ⇒ x._1.castTo[String].map((_, x._2)))
    dematV.isSuccess should be(true)
  }

  test("RiftWarp should dematerialize the PrimitiveTypes and rematerialize them") {
    val riftwarp = RiftWarp(packers, unpackers)
    val dematV = riftwarp.departure("xml", TestObjectA.pete.primitiveTypes).flatMap(x ⇒ x._1.castTo[String].map((_, x._2)))
    val resV = riftwarp.arrival("xml", dematV.forceResult._1)
    resV.forceResult should equal(TestObjectA.pete.primitiveTypes)
  }

  test("RiftWarp should dematerialize a UUID") {
    val riftwarp = RiftWarp(packers, unpackers)
    val uuid = JUUID.randomUUID()
    val dematV = riftwarp.departure("xml", uuid).flatMap(x ⇒ x._1.castTo[String].map((_, x._2)))
    dematV.forceResult should equal((s"""<Value type="Uuid">${uuid.toString()}</Value>""", WarpDescriptor("UUID")))
  }

//  test("SerializerOnStrings should serialize a UUID") {
//    val serializer = new WarpSerializerToString[JUUID](RiftWarp(packers, unpackers))
//    val uuid = JUUID.randomUUID()
//    val resV = serializer.serialize("xml")(uuid)
//    resV.forceResult should equal((s"""<Value type="Uuid">${uuid.toString()}</Value>""", Some(WarpDescriptor("UUID").toParsableString())))
//  }
//
//  test("SerializerOnStrings should serialize a Boolean") {
//    val serializer = new WarpSerializerToString[Boolean](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("xml")(true)
//    resV.forceResult should equal((s"""<Value type="Boolean">true</Value>""", Some(WarpDescriptor("Boolean").toParsableString())))
//  }
//
//  test("SerializerOnStrings[String] should serialize and deserialze a String") {
//    val serializer = Serializers.createSpecificForStrings[String](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("xml")("hallo")
//    val dematV = serializer.deserialize("xml")(resV.forceResult._1)
//    dematV.forceResult should equal("hallo")
//  }
//
//  test("SerializerOnStrings[Any] should serialize and deserialze a String") {
//    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("xml")("hallo")
//    val dematV = serializer.deserialize("xml")(resV.forceResult._1)
//    dematV.forceResult should equal("hallo")
//  }
//
//  test("SerializerOnStrings[Any] should serialize and deserialze a Double") {
//    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("xml")(1.234)
//    val dematV = serializer.deserialize("xml")(resV.forceResult._1)
//    dematV.forceResult should equal(1.234)
//  }
//
//  test("SerializerOnStrings[Any] should serialize and deserialze a SingleProblem") {
//    val prob = UnspecifiedProblem("Error", cause = Some(MultipleProblems(Vector(NoSuchElementProblem("Huhu!")))))
//    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("xml")(prob)
//    val demat = serializer.deserialize("xml")(resV.forceResult._1)
//    demat should equal(Success(prob))
//  }
//  
//  test("SerializerOnStrings[Any] should serialize and deserialze the PrimitiveListMAs") {
//    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("xml")(TestObjectA.pete.primitiveListMAs)
//    val dematV = serializer.deserialize("xml")(resV.forceResult._1)
//    dematV.forceResult should equal(TestObjectA.pete.primitiveListMAs)
//  }
//
//  ignore("SerializerOnStrings[Any] should serialize and deserialze the pete") {
//    val serializer = Serializers.createSpecificForStrings[Any](RiftWarp(packers, unpackers))
//    val resV = serializer.serialize("xml")(TestObjectA.pete)
//    val dematV = serializer.deserialize("xml")(resV.forceResult._1)
//    dematV.forceResult should equal(TestObjectA.pete)
//  }

}