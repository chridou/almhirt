package almhirt.io

import scala.annotation.tailrec
import scalaz._, Scalaz._
import almhirt.io.impl.GrowingBinaryWriter

/**
 * Writes items as binary representations.
 *
 * Warning! Consider implementations to be mutable!
 */
trait BinaryWriter { self =>
  final def write[T: CanWriteIntoBinaryWriter](v: T) = implicitly[CanWriteIntoBinaryWriter[T]].writeInto(v, self)
  def writeByte(v: Byte): BinaryWriter
  def writeUnsignedByte(v: Int): BinaryWriter
  def writeShort(v: Short): BinaryWriter
  def writeUnsignedShort(v: Int): BinaryWriter
  def writeInt(v: Int): BinaryWriter
  def writeLong(v: Long): BinaryWriter
  def writeFloat(v: Float): BinaryWriter
  def writeDouble(v: Double): BinaryWriter
  def writeByteArray(v: Array[Byte]): BinaryWriter
  def writeBytes(v: Seq[Byte]): BinaryWriter

  def toArray: Array[Byte]

  def spawnNew(capacity: Option[Int] = None): BinaryWriter
}

object BinaryWriter {
  def apply(initialCapacity: Int = 2048, maxSize: Option[Int] = None, maxIncrement: Int = 4 * 1024 * 1024): BinaryWriter = {
    val effMaxSize = maxSize | Int.MaxValue
    new GrowingBinaryWriter(initialCapacity, effMaxSize, maxIncrement)
  }

  def backed(underlying: Array[Byte]): BinaryWriter = {
    val writer = java.nio.ByteBuffer.wrap(underlying)
    var count = 0
    new BinaryWriter {
      def writeByte(v: Byte): BinaryWriter = { writer.put(v); count += 1; this }
      def writeUnsignedByte(v: Int): BinaryWriter = { writer.put(v.toByte); count += 1; this }
      def writeShort(v: Short): BinaryWriter = { writer.putShort(v); count += 2; this }
      def writeUnsignedShort(v: Int): BinaryWriter = { writer.putShort(v.toShort); count += 2; this }
      def writeInt(v: Int): BinaryWriter = { writer.putInt(v); count += 4; this }
      def writeLong(v: Long): BinaryWriter = { writer.putLong(v); count += 8; this }
      def writeFloat(v: Float): BinaryWriter = { writer.putFloat(v); count += 4; this }
      def writeDouble(v: Double): BinaryWriter = { writer.putDouble(v); count += 8; this }
      def writeByteArray(v: Array[Byte]): BinaryWriter = { writer.put(v); count += v.length; this }
      def writeBytes(v: Seq[Byte]): BinaryWriter = {
        val arr = v.toArray
        writer.put(arr.toArray);
        count += arr.length;
        this
      }

      def toArray: Array[Byte] = {
        val target = Array.ofDim[Byte](count)
        System.arraycopy(writer.array(), 0, target, 0, count)
        target
      }

      def spawnNew(capacity: Option[Int] = None): BinaryWriter =
        backed(new Array[Byte](capacity | underlying.length))
    }
  }

  def fixed(fixedCapacity: Int): BinaryWriter = {
    val writer = java.nio.ByteBuffer.allocate(fixedCapacity)
    var count = 0
    new BinaryWriter {
      def writeByte(v: Byte): BinaryWriter = { writer.put(v); count += 1; this }
      def writeUnsignedByte(v: Int): BinaryWriter = { writer.put(v.toByte); count += 1; this }
      def writeShort(v: Short): BinaryWriter = { writer.putShort(v); count += 2; this }
      def writeUnsignedShort(v: Int): BinaryWriter = { writer.putShort(v.toShort); count += 2; this }
      def writeInt(v: Int): BinaryWriter = { writer.putInt(v); count += 4; this }
      def writeLong(v: Long): BinaryWriter = { writer.putLong(v); count += 8; this }
      def writeFloat(v: Float): BinaryWriter = { writer.putFloat(v); count += 4; this }
      def writeDouble(v: Double): BinaryWriter = { writer.putDouble(v); count += 8; this }
      def writeByteArray(v: Array[Byte]): BinaryWriter = { writer.put(v); count += v.length; this }
      def writeBytes(v: Seq[Byte]): BinaryWriter = {
        val arr = v.toArray
        writer.put(arr.toArray);
        count += arr.length;
        this
      }

      def toArray: Array[Byte] = {
        val target = Array.ofDim[Byte](count)
        System.arraycopy(writer.array(), 0, target, 0, count)
        target
      }

      def spawnNew(capacity: Option[Int] = None): BinaryWriter = fixed(capacity | fixedCapacity)
    }
  }

}
