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

class MessagePackSerialization extends FunSuite with MustMatchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())

  test("A WarpBoolean(false) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBoolean(false)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBoolean(true) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBoolean(true)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpString("") must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString("")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpString("a") must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString("a")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpString(20 chars) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString("abcdefghijklmnopqrstu")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpString(31 chars) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 31) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpString(32 chars) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 32) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpString(1000 chars) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 1000) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpString(100000 chars) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 100000) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpByte(0) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpByte(1) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(1)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpByte(-1) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(-1)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpByte(Byte.MaxValue) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpByte(Byte.MinValue) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpShort(0) must dematerialize and rematerialize to a WarpByte(0)") {
    val sample = WarpShort(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(0))
  }

  test("A WarpShort(Byte.MinValue) must dematerialize and rematerialize to a WarpByte(Byte.MinValue)") {
    val sample = WarpShort(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(Byte.MinValue))
  }

  test("A WarpShort(Byte.MaxValue) must dematerialize and rematerialize to a WarpByte(Byte.MaxValue)") {
    val sample = WarpShort(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(Byte.MaxValue))
  }

  test("A WarpShort(Short.MinValue) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpShort(Short.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpShort(Short.MaxValue) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpShort(Short.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpInt(0) must dematerialize and rematerialize to a WarpByte(0)") {
    val sample = WarpInt(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(0))
  }

  test("A WarpInt(Byte.MinValue) must dematerialize and rematerialize to a WarpByte(Byte.MinValue)") {
    val sample = WarpInt(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(Byte.MinValue))
  }

  test("A WarpInt(Byte.MaxValue) must dematerialize and rematerialize to a WarpByte(Byte.MaxValue)") {
    val sample = WarpInt(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(Byte.MaxValue))
  }

  test("A WarpInt(Short.MinValue) must dematerialize and rematerialize to a WarpShort(Short.MinValue)") {
    val sample = WarpInt(Short.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpShort(Short.MinValue))
  }

  test("A WarpInt(Short.MaxValue) must dematerialize and rematerialize to a WarpShort(Short.MaxValue)") {
    val sample = WarpInt(Short.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpShort(Short.MaxValue))
  }

  test("A WarpInt(Int.MinValue) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpInt(Int.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpInt(Int.MaxValue) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpInt(Int.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpLong(0) must dematerialize and rematerialize to a WarpByte(0)") {
    val sample = WarpLong(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(0))
  }

  test("A WarpLong(Byte.MinValue) must dematerialize and rematerialize to a WarpByte(Byte.MinValue)") {
    val sample = WarpLong(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(Byte.MinValue))
  }

  test("A WarpLong(Byte.MaxValue) must dematerialize and rematerialize to a WarpByte(Byte.MaxValue)") {
    val sample = WarpLong(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(Byte.MaxValue))
  }

  test("A WarpLong(Short.MinValue) must dematerialize and rematerialize to a WarpShort(Short.MinValue)") {
    val sample = WarpLong(Short.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpShort(Short.MinValue))
  }

  test("A WarpLong(Short.MaxValue) must dematerialize and rematerialize to a WarpShort(Short.MaxValue)") {
    val sample = WarpLong(Short.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpShort(Short.MaxValue))
  }

  test("A WarpLong(Int.MinValue) must dematerialize and rematerialize to a WarpInt(Int.MinValue)") {
    val sample = WarpLong(Int.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpInt(Int.MinValue))
  }

  test("A WarpLong(Int.MaxValue) must dematerialize and rematerialize to a WarpInt(Int.MaxValue)") {
    val sample = WarpLong(Int.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpInt(Int.MaxValue))
  }

  test("A WarpLong(Long.MinValue) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpLong(Long.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpLong(Long.MaxValue) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpLong(Long.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBigInt(0) must dematerialize and rematerialize to a WarpByte(0)") {
    val sample = WarpBigInt(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(0))
  }

  test("A WarpBigInt(Byte.MinValue) must dematerialize and rematerialize to a WarpByte(Byte.MinValue)") {
    val sample = WarpBigInt(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(Byte.MinValue))
  }

  test("A WarpBigInt(Byte.MaxValue) must dematerialize and rematerialize to a WarpByte(Byte.MaxValue)") {
    val sample = WarpBigInt(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpByte(Byte.MaxValue))
  }

  test("A WarpBigInt(Short.MinValue) must dematerialize and rematerialize to a WarpShort(Short.MinValue)") {
    val sample = WarpBigInt(Short.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpShort(Short.MinValue))
  }

  test("A WarpBigInt(Short.MaxValue) must dematerialize and rematerialize to a WarpShort(Short.MaxValue)") {
    val sample = WarpBigInt(Short.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpShort(Short.MaxValue))
  }

  test("A WarpBigInt(Int.MinValue) must dematerialize and rematerialize to a WarpInt(Int.MinValue)") {
    val sample = WarpBigInt(Int.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpInt(Int.MinValue))
  }

  test("A WarpBigInt(Int.MaxValue) must dematerialize and rematerialize to a WarpInt(Int.MaxValue)") {
    val sample = WarpBigInt(Int.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpInt(Int.MaxValue))
  }

  test("A WarpBigInt(Long.MinValue) must dematerialize and rematerialize to a WarpLong(Int.MaxValue)") {
    val sample = WarpBigInt(Long.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpLong(Long.MinValue))
  }

  test("A WarpBigInt(Long.MaxValue) must dematerialize and rematerialize to a WarpLong(Int.MaxValue)") {
    val sample = WarpBigInt(Long.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(WarpLong(Long.MaxValue))
  }

  test("A WarpBigInt(Long.MinValue - 1) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigInt(BigInt(Long.MinValue) - 1)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBigInt(Long.MaxValue + 1) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigInt(BigInt(Long.MaxValue) + 1)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBigInt(1793498249712481927498172497129479127497294712974974124) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigInt("1793498249712481927498172497129479127497294712974974124")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBigInt(-1793498249712481927498172497129479127497294712974974124) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigInt("-1793498249712481927498172497129479127497294712974974124")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpFloat(0) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpFloat(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpFloat(Float.min) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpFloat(Float.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpFloat(Float.max) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpFloat(Float.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpDouble(0) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpDouble(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpDouble(Double.min) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpDouble(Double.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpDouble(Double.max) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpDouble(Double.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBigDecimal(0) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigDecimal(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBigDecimal(347214929384738471924.34871298347) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigDecimal("347214929384738471924.34871298347")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBigDecimal(-347214929384738471924.34871298347) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigDecimal("-347214929384738471924.34871298347")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpUri must dematerialize and rematerialize to an equal instance") {
    val sample = WarpUri(new java.net.URI("http://www.almhirt.org"))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpUuid must dematerialize and rematerialize to an equal instance") {
    val sample = WarpUuid(JUUID.randomUUID())
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpDateTime must dematerialize and rematerialize to an equal instance") {
    val sample = WarpDateTime(DateTime.now())
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized.toString() must equal(sample.toString())
  }

  test("A WarpLocalDateTime must dematerialize and rematerialize to an equal instance") {
    val sample = WarpLocalDateTime(LocalDateTime.now())
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpDuration must dematerialize and rematerialize to an equal instance") {
    val sample = WarpDuration(FiniteDuration(3, "d"))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpCollection(empty) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpCollection(Vector.empty)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpCollection(WarpString("a")) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpCollection(15 x WarpString("a")) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(15)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpCollection(16 x WarpString("a")) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(16)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpCollection(256*256-1 x WarpString("a")) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(256 * 256 - 1)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpCollection(256*256 x WarpString("a")) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(256 * 256)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpCollection(256*256*2 x WarpString("a")) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(256 * 256 * 2)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpAssociativeCollection(empty) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.empty)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpAssociativeCollection((WarpString("a"), WarpString("b"))) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpAssociativeCollection(15 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(15)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpAssociativeCollection(16 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(16)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpAssociativeCollection(256*256-1 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256 * 256 - 1)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpAssociativeCollection(256*256 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256 * 256)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpAssociativeCollection(256*256*2 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256 * 256 * 2)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpTuple2(WarpByte(1), WarpByte(2)) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpTuple2(WarpByte(1), WarpByte(2))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpTuple3(WarpByte(11), WarpByte(22), WarpByte(33)) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpTuple3(WarpByte(11), WarpByte(22), WarpByte(33))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
}