package almhirt.converters

import java.util.{ UUID ⇒ JUUID }
import java.nio.ByteBuffer

object BinaryConverter {
  @inline
  def toUnsignedByte(v: Int): Byte =
    v.toByte

  @inline
  def fromUnsignedByte(v: Byte): Int =
    ???
    
  @inline
  def toUnsignedShort(v: Int): Short =
    v.toShort

  @inline
  def shortToBytes(v: Short): Array[Byte] = {
    val bytes = new Array[Byte](2)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putShort(v)
    bytes
  }

  @inline
  def intToBytes(v: Int): Array[Byte] = {
    val bytes = new Array[Byte](4)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putInt(v)
    bytes
  }

  @inline
  def longToBytes(v: Long): Array[Byte] = {
    val bytes = new Array[Byte](8)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putLong(v)
    bytes
  }

  @inline
  def floatToBytes(v: Float): Array[Byte] = {
    val bytes = new Array[Byte](4)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putFloat(v)
    bytes
  }

  @inline
  def doubleToBytes(v: Double): Array[Byte] = {
    val bytes = new Array[Byte](8)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putDouble(v)
    bytes
  }

  @inline
  def uuidToBytes(uuid: JUUID): Array[Byte] = {
    val bytes = new Array[Byte](16)
    val longBuffer = ByteBuffer.wrap(bytes).asLongBuffer
    longBuffer.put(uuid.getMostSignificantBits).put(uuid.getLeastSignificantBits)
    bytes
  }

  @inline
  def uuidToBytesBigEndian(uuid: JUUID): Array[Byte] = {
    uuidToBytes(uuid)
  }

  @inline
  def uuidToBytesLittleEndian(uuid: JUUID): Array[Byte] = {
    val bytes = new Array[Byte](16)
    val longBuffer = ByteBuffer.wrap(bytes).asLongBuffer
    longBuffer.put(uuid.getLeastSignificantBits).put(uuid.getMostSignificantBits)
    bytes
  }

  @inline
  def bytesToShort(bytes: Array[Byte]): Short = ByteBuffer.wrap(bytes).getShort()
  @inline
  def bytesToInt(bytes: Array[Byte]): Int = ByteBuffer.wrap(bytes).getInt()
  @inline
  def bytesToLong(bytes: Array[Byte]): Long = ByteBuffer.wrap(bytes).getLong()
  @inline
  def bytesToFloat(bytes: Array[Byte]): Float = ByteBuffer.wrap(bytes).getFloat()
  @inline
  def bytesToDouble(bytes: Array[Byte]): Double = ByteBuffer.wrap(bytes).getDouble()

  @inline
  def bytesToUuid(bytes: Array[Byte]): JUUID = {
    val longBuffer = ByteBuffer.wrap(bytes).asLongBuffer
    new JUUID(longBuffer.get, longBuffer.get)
  }

  @inline
  def bytesBigEndianToUuid(bytes: Array[Byte]): JUUID = bytesToUuid(bytes)

}