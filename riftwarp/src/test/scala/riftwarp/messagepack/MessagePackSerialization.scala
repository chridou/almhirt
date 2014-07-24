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
import scala.concurrent.duration.FiniteDuration
import almhirt.io.BinaryWriter

class MessagePackSerialization extends FunSuite with Matchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())

  test("A WarpBoolean(false) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpBoolean(false)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpBoolean(true) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpBoolean(true)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpString("") should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString("")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpString("a") should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString("a")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpString(20 chars) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString("abcdefghijklmnopqrstu")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpString(31 chars) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 31) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpString(32 chars) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 32) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpString(255 chars) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 255) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }
  
  test("""A WarpString(256 chars) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 256) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }
  
  test("""A WarpString(1000 chars) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 1000) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpString(100000 chars) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for (i <- 1 to 100000) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpByte(0) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpByte(1) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(1)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpByte(-1) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(-1)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpByte(-31) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(-31)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpByte(-32) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(-32)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpByte(-33) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(-33)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpByte(Byte.MaxValue) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpByte(Byte.MinValue) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpShort(0) should dematerialize and rematerialize to a WarpByte(0)") {
    val sample = WarpShort(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(0))
  }

  test("A WarpShort(Byte.MinValue) should dematerialize and rematerialize to a WarpByte(Byte.MinValue)") {
    val sample = WarpShort(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(Byte.MinValue))
  }

  test("A WarpShort(Byte.MaxValue) should dematerialize and rematerialize to a WarpByte(Byte.MaxValue)") {
    val sample = WarpShort(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(Byte.MaxValue))
  }

  test("A WarpShort(Short.MinValue) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpShort(Short.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpShort(Short.MaxValue) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpShort(Short.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpInt(0) should dematerialize and rematerialize to a WarpByte(0)") {
    val sample = WarpInt(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(0))
  }

  test("A WarpInt(Byte.MinValue) should dematerialize and rematerialize to a WarpByte(Byte.MinValue)") {
    val sample = WarpInt(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(Byte.MinValue))
  }

  test("A WarpInt(Byte.MaxValue) should dematerialize and rematerialize to a WarpByte(Byte.MaxValue)") {
    val sample = WarpInt(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(Byte.MaxValue))
  }

  test("A WarpInt(Short.MinValue) should dematerialize and rematerialize to a WarpShort(Short.MinValue)") {
    val sample = WarpInt(Short.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpShort(Short.MinValue))
  }

  test("A WarpInt(Short.MaxValue) should dematerialize and rematerialize to a WarpShort(Short.MaxValue)") {
    val sample = WarpInt(Short.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpShort(Short.MaxValue))
  }

  test("A WarpInt(Int.MinValue) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpInt(Int.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpInt(Int.MaxValue) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpInt(Int.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpLong(0) should dematerialize and rematerialize to a WarpByte(0)") {
    val sample = WarpLong(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(0))
  }

  test("A WarpLong(Byte.MinValue) should dematerialize and rematerialize to a WarpByte(Byte.MinValue)") {
    val sample = WarpLong(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(Byte.MinValue))
  }

  test("A WarpLong(Byte.MaxValue) should dematerialize and rematerialize to a WarpByte(Byte.MaxValue)") {
    val sample = WarpLong(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(Byte.MaxValue))
  }

  test("A WarpLong(Short.MinValue) should dematerialize and rematerialize to a WarpShort(Short.MinValue)") {
    val sample = WarpLong(Short.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpShort(Short.MinValue))
  }

  test("A WarpLong(Short.MaxValue) should dematerialize and rematerialize to a WarpShort(Short.MaxValue)") {
    val sample = WarpLong(Short.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpShort(Short.MaxValue))
  }

  test("A WarpLong(Int.MinValue) should dematerialize and rematerialize to a WarpInt(Int.MinValue)") {
    val sample = WarpLong(Int.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpInt(Int.MinValue))
  }

  test("A WarpLong(Int.MaxValue) should dematerialize and rematerialize to a WarpInt(Int.MaxValue)") {
    val sample = WarpLong(Int.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpInt(Int.MaxValue))
  }

  test("A WarpLong(Long.MinValue) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpLong(Long.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpLong(Long.MaxValue) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpLong(Long.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpBigInt(0) should dematerialize and rematerialize to a WarpByte(0)") {
    val sample = WarpBigInt(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(0))
  }

  test("A WarpBigInt(Byte.MinValue) should dematerialize and rematerialize to a WarpByte(Byte.MinValue)") {
    val sample = WarpBigInt(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(Byte.MinValue))
  }

  test("A WarpBigInt(Byte.MaxValue) should dematerialize and rematerialize to a WarpByte(Byte.MaxValue)") {
    val sample = WarpBigInt(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpByte(Byte.MaxValue))
  }

  test("A WarpBigInt(Short.MinValue) should dematerialize and rematerialize to a WarpShort(Short.MinValue)") {
    val sample = WarpBigInt(Short.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpShort(Short.MinValue))
  }

  test("A WarpBigInt(Short.MaxValue) should dematerialize and rematerialize to a WarpShort(Short.MaxValue)") {
    val sample = WarpBigInt(Short.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpShort(Short.MaxValue))
  }

  test("A WarpBigInt(Int.MinValue) should dematerialize and rematerialize to a WarpInt(Int.MinValue)") {
    val sample = WarpBigInt(Int.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpInt(Int.MinValue))
  }

  test("A WarpBigInt(Int.MaxValue) should dematerialize and rematerialize to a WarpInt(Int.MaxValue)") {
    val sample = WarpBigInt(Int.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpInt(Int.MaxValue))
  }

  test("A WarpBigInt(Long.MinValue) should dematerialize and rematerialize to a WarpLong(Int.MaxValue)") {
    val sample = WarpBigInt(Long.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpLong(Long.MinValue))
  }

  test("A WarpBigInt(Long.MaxValue) should dematerialize and rematerialize to a WarpLong(Int.MaxValue)") {
    val sample = WarpBigInt(Long.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(WarpLong(Long.MaxValue))
  }

  test("A WarpBigInt(Long.MinValue - 1) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigInt(BigInt(Long.MinValue) - 1)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpBigInt(Long.MaxValue + 1) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigInt(BigInt(Long.MaxValue) + 1)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpBigInt(1793498249712481927498172497129479127497294712974974124) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigInt("1793498249712481927498172497129479127497294712974974124")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpBigInt(-1793498249712481927498172497129479127497294712974974124) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigInt("-1793498249712481927498172497129479127497294712974974124")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpFloat(0) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpFloat(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpFloat(Float.min) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpFloat(Float.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpFloat(Float.max) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpFloat(Float.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpDouble(0) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpDouble(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpDouble(Double.min) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpDouble(Double.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpDouble(Double.max) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpDouble(Double.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpBigDecimal(0) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigDecimal(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpBigDecimal(347214929384738471924.34871298347) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigDecimal("347214929384738471924.34871298347")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpBigDecimal(-347214929384738471924.34871298347) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigDecimal("-347214929384738471924.34871298347")
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpUri should dematerialize and rematerialize to an equal instance") {
    val sample = WarpUri(new java.net.URI("http://www.almhirt.org"))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpUuid should dematerialize and rematerialize to an equal instance") {
    val sample = WarpUuid(JUUID.randomUUID())
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpDateTime should dematerialize and rematerialize to an equal instance") {
    val sample = WarpDateTime(DateTime.now())
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized.toString() should equal(sample.toString())
  }

  test("A WarpLocalDateTime should dematerialize and rematerialize to an equal instance") {
    val sample = WarpLocalDateTime(LocalDateTime.now())
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpDuration should dematerialize and rematerialize to an equal instance") {
    val sample = WarpDuration(FiniteDuration(3, "d"))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpCollection(empty) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpCollection(Vector.empty)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpCollection(WarpString("a")) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpCollection(15 x WarpString("a")) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(15)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpCollection(16 x WarpString("a")) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(16)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpCollection(255 x WarpString("a")) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(255)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }
  
  test("""A WarpCollection(256 x WarpString("a")) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(256)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }
  
  
  test("""A WarpCollection(256*256-1 x WarpString("a")) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(256 * 256 - 1)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpCollection(256*256 x WarpString("a")) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(256 * 256)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpCollection(256*256*2 x WarpString("a")) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpCollection(Vector.fill(256 * 256 * 2)(WarpString("a")))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(empty) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.empty)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection((WarpString("a"), WarpString("b"))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(15 x (WarpInt(1), WarpInt(-1))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(15)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(16 x (WarpInt(1), WarpInt(-1))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(16)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(255 x (WarpInt(1), WarpInt(-1))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(255)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(256 x (WarpInt(1), WarpInt(-1))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(256*256-1 x (WarpInt(1), WarpInt(-1))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256 * 256 - 1)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(256*256 x (WarpInt(1), WarpInt(-1))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256 * 256)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("""A WarpAssociativeCollection(256*256*2 x (WarpInt(1), WarpInt(-1))) should dematerialize and rematerialize to an equal instance""") {
    val sample = WarpAssociativeCollection(Vector.fill(256 * 256 * 2)((WarpString("a"), WarpString("b"))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpTuple2(WarpByte(1), WarpByte(2)) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpTuple2(WarpByte(1), WarpByte(2))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }

  test("A WarpTuple3(WarpByte(11), WarpByte(22), WarpByte(33)) should dematerialize and rematerialize to an equal instance") {
    val sample = WarpTuple3(WarpByte(11), WarpByte(22), WarpByte(33))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized should equal(sample)
  }
}