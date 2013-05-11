//package riftwarp
//
//import org.scalatest._
//import org.scalatest.matchers.ShouldMatchers
//import java.util.{UUID => JUUID}
//import org.joda.time.DateTime
//import scalaz._, Scalaz._
//import almhirt.syntax.almvalidation._
//import riftwarp._
//import riftwarp.impl.dematerializers.ToJsonCordWarpSequencer
//import riftwarp.components.HasWarpDescriptor
//
//class ToJsonWarpSequencerSpecs extends WordSpec with ShouldMatchers {
//  val riftWarp = RiftWarp.concurrentWithDefaults()
//  implicit val hasWarpUnpackers = riftWarp.barracks
//  implicit val toolShed = riftWarp.toolShed
//
//  private sealed trait ComplexType
//  private case class ComplexTypeA(x: String) extends ComplexType
//  private case class ComplexTypeB(y: Int) extends ComplexType with HasWarpDescriptor { def warpDescriptor = WarpDescriptor("ComplexTypeB")}
//  
//  "ToJsonCordWarpSequencer" when {
//    "adding primitive types" should {
//      "add a string" in {
//        ToJsonCordWarpSequencer().addString("v", "Hallo").dematerialize.manifestation.toString should equal("""{"v":"Hallo"}""")
//      }
//      "add a boolean value true" in {
//        ToJsonCordWarpSequencer().addBoolean("v", true).dematerialize.manifestation.toString should equal("""{"v":true}""")
//      }
//      "add a boolean value false" in {
//        ToJsonCordWarpSequencer().addBoolean("v", false).dematerialize.manifestation.toString should equal("""{"v":false}""")
//      }
//      "add a byte" in {
//        ToJsonCordWarpSequencer().addByte("v", 127.toByte).dematerialize.manifestation.toString should equal("""{"v":127}""")
//      }
//      "add an Int Min-Value" in {
//        ToJsonCordWarpSequencer().addInt("v", Integer.MIN_VALUE).dematerialize.manifestation.toString should equal("""{"v":-2147483648}""")
//      }
//      "add an Int Max-Value" in {
//        ToJsonCordWarpSequencer().addInt("v", Integer.MAX_VALUE).dematerialize.manifestation.toString should equal("""{"v":2147483647}""")
//      }
//      "add a Long Min-Value" in {
//        ToJsonCordWarpSequencer().addLong("v", Long.MinValue).dematerialize.manifestation.toString should equal("""{"v":-9223372036854775808}""")
//      }
//      "add a Long Max-Value" in {
//        ToJsonCordWarpSequencer().addLong("v", Long.MaxValue).dematerialize.manifestation.toString should equal("""{"v":9223372036854775807}""")
//      }
//      "add a BigInt" in {
//        ToJsonCordWarpSequencer().addBigInt("v", BigInt("34247284829424788979879184717471479279447237624")).dematerialize.manifestation.toString should equal("""{"v":"34247284829424788979879184717471479279447237624"}""")
//      }
//      "add a Float" in {
//        ToJsonCordWarpSequencer().addFloat("v", 1.25f).dematerialize.manifestation.toString should equal("""{"v":1.25}""")
//      }
//      "add a Double" in {
//        ToJsonCordWarpSequencer().addDouble("v", 1.25f).dematerialize.manifestation.toString should equal("""{"v":1.25}""")
//      }
//      "add a BigDecimal" in {
//        ToJsonCordWarpSequencer().addBigDecimal("v", BigDecimal("123454536645646466.2378747834873497")).dematerialize.manifestation.toString should equal("""{"v":"123454536645646466.2378747834873497"}""")
//      }
//      "add a DateTime" in {
//        ToJsonCordWarpSequencer().addDateTime("v", new DateTime("2013-01-23T06:23:14.421+01:00")).dematerialize.manifestation.toString should equal("""{"v":"2013-01-23T06:23:14.421+01:00"}""")
//      }
//      "add an UUID" in {
//        ToJsonCordWarpSequencer().addUuid("v", JUUID.fromString("cdcb6d64-c8a6-4d5c-ac26-4dadcfbd6c7f")).dematerialize.manifestation.toString should equal("""{"v":"cdcb6d64-c8a6-4d5c-ac26-4dadcfbd6c7f"}""")
//      }
//      "add an URI" in {
//        ToJsonCordWarpSequencer().addUri("v", java.net.URI.create("file://home/cd/bin")).dematerialize.manifestation.toString should equal("""{"v":"file://home/cd/bin"}""")
//      }
//    }
//    "working with byte arrays" should {
//      "add a ByteArray[1,2,3]" in {
//        ToJsonCordWarpSequencer().addByteArray("v", Array[Byte](1, 2, 3)).dematerialize.manifestation.toString should equal("""{"v":[1,2,3]}""")
//      }
//      "add a ByteArray[1,2,3] as base64" in {
//        ToJsonCordWarpSequencer().addBase64EncodedByteArray("v", Array[Byte](1, 2, 3)).dematerialize.manifestation.toString should equal("""{"v":"AQID"}""")
//      }
//      "add a ByteArray[1,2,3] blob encoded" in {
//        ToJsonCordWarpSequencer().addByteArrayBlobEncoded("v", Array[Byte](1, 2, 3)).dematerialize.manifestation.toString should equal("""{"v":"AQID"}""")
//      }
//    }
////    "working with complex types" should {
////      "add a ByteArray[1,2,3]" in {
////        ToJsonCordWarpSequencer(NoDivertBlobDivert).addByteArray("v", Array[Byte](1, 2, 3)).forceResult.dematerialize.manifestation.toString should equal("""{"v":[1,2,3]}""")
////      }
////      "add a ByteArray[1,2,3] as base64" in {
////        ToJsonCordWarpSequencer(NoDivertBlobDivert).addBase64EncodedByteArray("v", Array[Byte](1, 2, 3)).forceResult.dematerialize.manifestation.toString should equal("""{"v":"AQID"}""")
////      }
////      "add a ByteArray[1,2,3] blob encoded" in {
////        ToJsonCordWarpSequencer(NoDivertBlobDivert).addByteArrayBlobEncoded("v", Array[Byte](1, 2, 3)).forceResult.dematerialize.manifestation.toString should equal("""{"v":"AQID"}""")
////      }
////    }
//  }
//}
