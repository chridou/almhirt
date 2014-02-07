package riftwarp.messagepack
import org.scalatest._
import java.util.{ UUID => JUUID }
import org.joda.time.{ DateTime, LocalDateTime }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std._
import riftwarp.std.kit._
import riftwarp.std.default._
import riftwarp.util.WarpSerializerToString
import riftwarp.util.Serializers
import scala.concurrent.duration.FiniteDuration
import almhirt.io.BinaryWriter

class MessagePackAssociativeCollectionSerialization extends FunSuite with Matchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())

  test("""A WarpAssociativeCollection(empty) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.empty)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, "))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, "))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(15 x (WarpString(Ø 250 mm, H 90 mm, ), WarpString(Ø 250 mm, H 90 mm, ))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(15)((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, "))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(16 x (WarpString(Ø 250 mm, H 90 mm, ), WarpString(Ø 250 mm, H 90 mm, ))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(16)((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, "))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(256*256-1 x (WarpString(Ø 250 mm, H 90 mm, ), WarpString(Ø 250 mm, H 90 mm, ))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256 * 256 - 1)((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, "))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(256*256 x (WarpString(Ø 250 mm, H 90 mm, ), WarpString(Ø 250 mm, H 90 mm, ))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256 * 256)((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, "))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(256*256*2 x (WarpString(1), WarpString(-1))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256 * 256 * 2)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""(In a WarpObject): A WarpAssociativeCollection(empty) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpObject(None, Vector(WarpElement("xxxxx", Some(WarpAssociativeCollection(Vector.empty)))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""(In a WarpObject): A WarpAssociativeCollection((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, "))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpObject(None, Vector(WarpElement("xxxxx", Some(WarpAssociativeCollection(Vector((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, " * 10000))))))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""(In a WarpObject): A WarpAssociativeCollection(15 x (WarpString(Ø 250 mm, H 90 mm, ), WarpString(Ø 250 mm, H 90 mm, ))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpObject(None, Vector(WarpElement("xxxxx", Some(WarpAssociativeCollection(Vector.fill(15)((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, " * 1000))))))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""(In a WarpObject): A WarpAssociativeCollection(16 x (WarpString(Ø 250 mm, H 90 mm, ), WarpString(Ø 250 mm, H 90 mm, ))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpObject(None, Vector(WarpElement("xxxxx", Some(WarpAssociativeCollection(Vector.fill(16)((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, " * 1000))))))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""(In a WarpObject): A WarpAssociativeCollection(256*256-1 x (WarpString(Ø 250 mm, H 90 mm, ), WarpString(Ø 250 mm, H 90 mm, ))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpObject(None, Vector(WarpElement("xxxxx", Some(WarpAssociativeCollection(Vector.fill(256 * 256 - 1)((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, " * 10))))))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""(In a WarpObject): A WarpAssociativeCollection(256*256 x (WarpString(Ø 250 mm, H 90 mm, ), WarpString(Ø 250 mm, H 90 mm, ))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpObject(None, Vector(WarpElement("xxxxx", Some(WarpAssociativeCollection(Vector.fill(256 * 256)((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, " * 10))))))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""(In a WarpObject): A WarpAssociativeCollection(256*256*2 x (WarpString(Ø 250 mm, H 90 mm, ), WarpString(Ø 250 mm, H 90 mm, ))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpObject(None, Vector(WarpElement("xxxxx", Some(WarpAssociativeCollection(Vector.fill(256 * 256 * 2)((WarpString("Ø 250 mm, H 90 mm, "), WarpString("Ø 250 mm, H 90 mm, "))))))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }
}
