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
  
  val maxSize = 12 * 1024 * 1024

  implicit val MessagePackDematerializer = new messagepack.ToMessagePackDematerializer { def createBinaryWriter(): BinaryWriter = BinaryWriter(maxSize) }
  
  val blobPackage = (WarpDescriptor("a") ~> Blob("theBlob", Vector(1, 2, 3, 4, 5, 6, 7, 8, 9, 0).map(_.toByte))).forceResult

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
    val sample = WarpString((for(i <- 1 to 31) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
  
  test("""A WarpString(32 chars) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for(i <- 1 to 32) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
  
  test("""A WarpString(1000 chars) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for(i <- 1 to 1000) yield 'x').mkString)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("""A WarpString(100000 chars) must dematerialize and rematerialize to an equal instance""") {
    val sample = WarpString((for(i <- 1 to 100000) yield 'x').mkString)
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

  test("A WarpByte(Byte.min) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(Byte.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
  
  test("A WarpByte(Byte.max) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpByte(Byte.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
  
  test("A WarpInt(0) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpInt(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpInt(Int.min) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpInt(Int.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
  
  test("A WarpInt(Int.max) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpInt(Int.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpLong(0) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpLong(0)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpLong(Long.min) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpLong(Long.MinValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
  
  test("A WarpLong(Long.max) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpLong(Long.MaxValue)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
  
  test("A WarpBigInt(0) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBigInt(0)
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

  test("A WarpCollection(WarpInt(1)) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpCollection(Vector(WarpInt(1)))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpCollection(15 x WarpInt(1)) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpCollection(Vector.fill(15)(WarpInt(1)))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpCollection(16 x WarpInt(1)) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpCollection(Vector.fill(16)(WarpInt(1)))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpCollection(256*256-1 x WarpInt(1)) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpCollection(Vector.fill(256*256-1)(WarpInt(1)))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpCollection(256*256 x WarpInt(1)) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpCollection(Vector.fill(256*256)(WarpInt(1)))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpCollection(256*256*2 x WarpInt(1)) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpCollection(Vector.fill(256*256*2)(WarpInt(1)))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
 
  
  
  test("A WarpAssociativeCollection(empty) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpAssociativeCollection(Vector.empty)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpAssociativeCollection((WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpAssociativeCollection(Vector((WarpInt(1), WarpInt(-1))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpAssociativeCollection(15 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpAssociativeCollection(Vector.fill(15)((WarpInt(1), WarpInt(-1))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpAssociativeCollection(16 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpAssociativeCollection(Vector.fill(16)((WarpInt(1), WarpInt(-1))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpAssociativeCollection(256*256-1 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpAssociativeCollection(Vector.fill(256*256-1)((WarpInt(1), WarpInt(-1))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpAssociativeCollection(256*256 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpAssociativeCollection(Vector.fill(256*256)((WarpInt(1), WarpInt(-1))))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpAssociativeCollection(256*256*2 x (WarpInt(1), WarpInt(-1))) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpAssociativeCollection(Vector.fill(256*256*2)((WarpInt(1), WarpInt(-1))))
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

  test("A WarpBytes(empty) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Vector.empty)
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBytes(1 byte) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(1)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBytes(255 bytes) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(255)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBytes(256 bytes) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(256)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBytes(256*256-1 bytes) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(256*256-1)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBytes(256*256 bytes) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(256*256)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBytes(256*256*2 bytes) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(256*256*2)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test(s"A WarpBytes(${256*256*32} bytes) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(256*256*32)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }
  
  test(s"WarpBytes(${maxSize-10} bytes) must dematerialize") {
    val sample = WarpBytes(Array.fill(maxSize-10)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
  }

  test(s"WarpBytes(${maxSize-10} bytes) must dematerialize and rematerialize") {
    val sample = WarpBytes(Array.fill(maxSize-10)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
  }
  

  
//  test("A WarpObject  must dematerialize and rematerialize to an equal instance") {
//    val obj = WarpObject(None, Vector(WarpElement("propA", Some(WarpDouble(123.456))), WarpElement("propB", None)))
//    val dematerialized = obj.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
//    val rematerialized = dematerialized.rematerialize.forceResult
//    rematerialized must equal(obj)
//  }
//
  
}