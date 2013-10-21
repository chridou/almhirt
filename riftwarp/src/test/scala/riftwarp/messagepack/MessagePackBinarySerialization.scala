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

class MessagePackBinarySerialization extends FunSuite with MustMatchers {
  implicit val packers = Serialization.addPackers(WarpPackers())
  implicit val unpackers = Serialization.addUnpackers(WarpUnpackers())

  val maxSize = 12 * 1024 * 1024

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
    val sample = WarpBytes(Array.fill(256 * 256 - 1)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBytes(256*256 bytes) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(256 * 256)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test("A WarpBytes(256*256*2 bytes) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(256 * 256 * 2)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  test(s"A WarpBytes(${256 * 256 * 32} bytes) must dematerialize and rematerialize to an equal instance") {
    val sample = WarpBytes(Array.fill(256 * 256 * 32)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
    rematerialized must equal(sample)
  }

  
  test(s"WarpBytes(${maxSize - 10} bytes) must dematerialize") {
    val sample = WarpBytes(Array.fill(maxSize - 10)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
  }

  test(s"WarpBytes(${maxSize - 10} bytes) must dematerialize and rematerialize") {
    val sample = WarpBytes(Array.fill(maxSize - 10)(1.toByte))
    val dematerialized = sample.dematerialize[Array[Byte] @@ WarpTags.MessagePack]
    val rematerialized = dematerialized.rematerialize.forceResult
  }
}