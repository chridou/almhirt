package riftwarp.messagepack

import org.scalatest._
import java.util.{ UUID ⇒ JUUID }
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._
import riftwarp.std._
import riftwarp.std.kit._
import riftwarp.std.default._
import scala.concurrent.duration.FiniteDuration
import almhirt.io.BinaryWriter

class MessagePackWarpObjectSerialization extends FunSuite with Matchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())

  test("""A WarpObject with elements should dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(None, Vector(
      WarpElement("propA", Some(WarpDouble(123.456))),
      WarpElement("propB", None),
      WarpElement("propC", Some(WarpCollection(Vector(
        WarpString("Hallo"),
        WarpInt(1112384),
        WarpObject(None, Vector(
          WarpElement("propX", Some(WarpDouble(123.456))),
          WarpElement("propY", None))),
        WarpCollection(Vector(WarpDouble(1.23), WarpCollection(), WarpBoolean(true)))))))))
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(obj)
  }

  test("""A WarpObject with elements and without a WarpDecriptor should dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(None, Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None)))
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(obj)
  }

  test("""A WarpObject with elements and with a WarpDecriptor("a", None) should dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(Some(WarpDescriptor("a", None)), Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None)))
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(obj)
  }

  test("""A WarpObject with elements and with a WarpDecriptor("a", Some(1)) should dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(Some(WarpDescriptor("a", Some(1))), Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None)))
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(obj)
  }

  test("""A WarpObject without elements and without a WarpDecriptor should dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(None, Vector.empty)
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(obj)
  }

  test("""A WarpObject without elements and with a WarpDecriptor("a", None) should dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(Some(WarpDescriptor("a", None)), Vector.empty)
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(obj)
  }

  test("""A WarpObject without elements and with a WarpDecriptor("a", Some(1)) should dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(Some(WarpDescriptor("a", Some(1))), Vector.empty)
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(obj)
  }

}