package riftwarp.messagepack

import org.scalatest._
import org.scalatest.matchers.MustMatchers
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

class MessagePackWarpObjectSerialization extends FunSuite with MustMatchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())

  test("""A WarpObject with elements and without a WarpDecriptor must dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(None, Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None)))
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(obj)
  }
  
  test("""A WarpObject with elements and with a WarpDecriptor("a", None) must dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(Some(WarpDescriptor("a", None)), Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None)))
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(obj)
  }
  
  test("""A WarpObject with elements and with a WarpDecriptor("a", Some(1)) must dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(Some(WarpDescriptor("a", Some(1))), Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None)))
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(obj)
  }
  
  test("""A WarpObject without elements and without a WarpDecriptor must dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(None, Vector.empty)
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(obj)
  }
  
  test("""A WarpObject without elements and with a WarpDecriptor("a", None) must dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(Some(WarpDescriptor("a", None)), Vector.empty)
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(obj)
  }
  
  test("""A WarpObject without elements and with a WarpDecriptor("a", Some(1)) must dematerialize and rematerialize to an equal instance""") {
    val obj = WarpObject(Some(WarpDescriptor("a", Some(1))), Vector.empty)
    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(obj)
  }

}