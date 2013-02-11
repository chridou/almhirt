package riftwarp

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import java.util.{UUID => JUUID}
import org.joda.time.DateTime
import scalaz._, Scalaz._
import almhirt.syntax.almvalidation._
import riftwarp._
import riftwarp.impl.dematerializers.ToJsonCordDematerializer
import riftwarp.components.HasRiftDescriptor

class ToJsonDematerializerSpecs extends WordSpec with ShouldMatchers {
  val riftWarp = RiftWarp.concurrentWithDefaults()
  implicit val hasRecomposers = riftWarp.barracks
  implicit val toolShed = riftWarp.toolShed

  private sealed trait ComplexType
  private case class ComplexTypeA(x: String) extends ComplexType
  private case class ComplexTypeB(y: Int) extends ComplexType with HasRiftDescriptor { def riftDescriptor = RiftDescriptor("ComplexTypeB")}
  
  "ToJsonCordDematerializer" when {
    "adding primitive types" should {
      "add a string" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addString("v", "Hallo").forceResult.dematerialize.manifestation.toString should equal("""{"v":"Hallo"}""")
      }
      "not add an empty string" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addString("v", "").isFailure should be(true)
      }
      "add a boolean value true" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addBoolean("v", true).forceResult.dematerialize.manifestation.toString should equal("""{"v":true}""")
      }
      "add a boolean value false" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addBoolean("v", false).forceResult.dematerialize.manifestation.toString should equal("""{"v":false}""")
      }
      "add a byte" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addByte("v", 127.toByte).forceResult.dematerialize.manifestation.toString should equal("""{"v":127}""")
      }
      "add an Int Min-Value" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addInt("v", Integer.MIN_VALUE).forceResult.dematerialize.manifestation.toString should equal("""{"v":-2147483648}""")
      }
      "add an Int Max-Value" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addInt("v", Integer.MAX_VALUE).forceResult.dematerialize.manifestation.toString should equal("""{"v":2147483647}""")
      }
      "add a Long Min-Value" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addLong("v", Long.MinValue).forceResult.dematerialize.manifestation.toString should equal("""{"v":-9223372036854775808}""")
      }
      "add a Long Max-Value" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addLong("v", Long.MaxValue).forceResult.dematerialize.manifestation.toString should equal("""{"v":9223372036854775807}""")
      }
      "add a BigInt" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addBigInt("v", BigInt("34247284829424788979879184717471479279447237624")).forceResult.dematerialize.manifestation.toString should equal("""{"v":"34247284829424788979879184717471479279447237624"}""")
      }
      "add a Float" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addFloat("v", 1.25f).forceResult.dematerialize.manifestation.toString should equal("""{"v":1.25}""")
      }
      "add a Double" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addDouble("v", 1.25f).forceResult.dematerialize.manifestation.toString should equal("""{"v":1.25}""")
      }
      "add a BigDecimal" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addBigDecimal("v", BigDecimal("123454536645646466.2378747834873497")).forceResult.dematerialize.manifestation.toString should equal("""{"v":"123454536645646466.2378747834873497"}""")
      }
      "add a DateTime" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addDateTime("v", new DateTime("2013-01-23T06:23:14.421+01:00")).forceResult.dematerialize.manifestation.toString should equal("""{"v":"2013-01-23T06:23:14.421+01:00"}""")
      }
      "add an UUID" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addUuid("v", JUUID.fromString("cdcb6d64-c8a6-4d5c-ac26-4dadcfbd6c7f")).forceResult.dematerialize.manifestation.toString should equal("""{"v":"cdcb6d64-c8a6-4d5c-ac26-4dadcfbd6c7f"}""")
      }
      "add an URI" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addUri("v", java.net.URI.create("file://home/cd/bin")).forceResult.dematerialize.manifestation.toString should equal("""{"v":"file://home/cd/bin"}""")
      }
    }
    "working with byte arrays" should {
      "add a ByteArray[1,2,3]" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addByteArray("v", Array[Byte](1, 2, 3)).forceResult.dematerialize.manifestation.toString should equal("""{"v":[1,2,3]}""")
      }
      "add a ByteArray[1,2,3] as base64" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addBase64EncodedByteArray("v", Array[Byte](1, 2, 3)).forceResult.dematerialize.manifestation.toString should equal("""{"v":"AQID"}""")
      }
      "add a ByteArray[1,2,3] blob encoded" in {
        ToJsonCordDematerializer(NoDivertBlobDivert).addByteArrayBlobEncoded("v", Array[Byte](1, 2, 3)).forceResult.dematerialize.manifestation.toString should equal("""{"v":"AQID"}""")
      }
    }
//    "working with complex types" should {
//      "add a ByteArray[1,2,3]" in {
//        ToJsonCordDematerializer(NoDivertBlobDivert).addByteArray("v", Array[Byte](1, 2, 3)).forceResult.dematerialize.manifestation.toString should equal("""{"v":[1,2,3]}""")
//      }
//      "add a ByteArray[1,2,3] as base64" in {
//        ToJsonCordDematerializer(NoDivertBlobDivert).addBase64EncodedByteArray("v", Array[Byte](1, 2, 3)).forceResult.dematerialize.manifestation.toString should equal("""{"v":"AQID"}""")
//      }
//      "add a ByteArray[1,2,3] blob encoded" in {
//        ToJsonCordDematerializer(NoDivertBlobDivert).addByteArrayBlobEncoded("v", Array[Byte](1, 2, 3)).forceResult.dematerialize.manifestation.toString should equal("""{"v":"AQID"}""")
//      }
//    }
  }
}
