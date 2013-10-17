package almhirt.io

import scalaz._, Scalaz._

/**
 * Writes items as binary representations.
 * 
 * Warning! Implementations tend to be mutable!
 */
trait BinaryWriter {
  final def write(v: Byte) = writeByte(v)
  def writeByte(v: Byte): BinaryWriter
  def writeUnsignedByte(v: Int): BinaryWriter
  def writeShort(v: Short): BinaryWriter
  def writeUnsignedShort(v: Int): BinaryWriter
  def writeInt(v: Int): BinaryWriter
  def writeLong(v: Long): BinaryWriter
  def writeFloat(v: Float): BinaryWriter
  def writeDouble(v: Double): BinaryWriter
  def writeBigInt(v: BigInt): BinaryWriter
  def writeByteArray(v: Array[Byte]): BinaryWriter
  def writeBytes(v: Seq[Byte]): BinaryWriter

  def toArray: Array[Byte]

  def spawnNew(capacity: Option[Int] = None): BinaryWriter
}

object BinaryWriter {
  def apply(underlying: Array[Byte]): BinaryWriter = {
    val writer = java.nio.ByteBuffer.wrap(underlying)
    new BinaryWriter {
      def writeByte(v: Byte): BinaryWriter = { writer.put(v); this }
      def writeUnsignedByte(v: Int): BinaryWriter = { writer.put(v.toByte); this }
      def writeShort(v: Short): BinaryWriter = { writer.putShort(v); this }
      def writeUnsignedShort(v: Int): BinaryWriter = { writer.putShort(v.toShort); this }
      def writeInt(v: Int): BinaryWriter = { writer.putInt(v); this }
      def writeLong(v: Long): BinaryWriter = { writer.putLong(v); this }
      def writeFloat(v: Float): BinaryWriter = { writer.putFloat(v); this }
      def writeDouble(v: Double): BinaryWriter = { writer.putDouble(v); this }
      def writeBigInt(v: BigInt): BinaryWriter = { writer.put(v.toByteArray); this }
      def writeByteArray(v: Array[Byte]): BinaryWriter = { writer.put(v); this }
      def writeBytes(v: Seq[Byte]): BinaryWriter = { writer.put(v.toArray); this }

      def toArray: Array[Byte] = writer.array()

      def spawnNew(capacity: Option[Int] = None): BinaryWriter = 
        BinaryWriter(new Array[Byte](capacity | underlying.length))
    }
  }

  def apply(fixedCapacity: Int): BinaryWriter = {
    val writer = java.nio.ByteBuffer.allocate(fixedCapacity)
    new BinaryWriter {
      def writeByte(v: Byte): BinaryWriter = { writer.put(v); this }
      def writeUnsignedByte(v: Int): BinaryWriter = { writer.put(v.toByte); this }
      def writeShort(v: Short): BinaryWriter = { writer.putShort(v); this }
      def writeUnsignedShort(v: Int): BinaryWriter = { writer.putShort(v.toShort); this }
      def writeInt(v: Int): BinaryWriter = { writer.putInt(v); this }
      def writeLong(v: Long): BinaryWriter = { writer.putLong(v); this }
      def writeFloat(v: Float): BinaryWriter = { writer.putFloat(v); this }
      def writeDouble(v: Double): BinaryWriter = { writer.putDouble(v); this }
      def writeBigInt(v: BigInt): BinaryWriter = { writer.put(v.toByteArray); this }
      def writeByteArray(v: Array[Byte]): BinaryWriter = { writer.put(v); this }
      def writeBytes(v: Seq[Byte]): BinaryWriter = { writer.put(v.toArray); this }

      def toArray: Array[Byte] = writer.array()

      def spawnNew(capacity: Option[Int] = None): BinaryWriter = BinaryWriter(capacity | fixedCapacity)
    }
  }
  
  def apply(initialCapacity: Int, maxIncrement: Option[Int] = None, maxSize: Option[Int] = None): BinaryWriter = {
    val effMaxInc = maxIncrement | initialCapacity
    val effMaxSize = maxSize | Int.MaxValue 
    ???
  }
  
}