package almhirt.converters

import java.util.{ UUID => JUUID }
import java.nio._

object BinaryConverter {
  import java.nio.ByteBuffer

  def shortToBytes(v: Short): Array[Byte] = {
    val bytes = new Array[Byte](2)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putShort(v)
    bytes
  }
  
  def intToBytes(v: Int): Array[Byte] = {
    val bytes = new Array[Byte](4)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putInt(v)
    bytes
  }
  
  def longToBytes(v: Long): Array[Byte] = {
    val bytes = new Array[Byte](8)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putLong(v)
    bytes
  }

  def floatToBytes(v: Float): Array[Byte] = {
    val bytes = new Array[Byte](4)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putFloat(v)
    bytes
  }

  def doubleToBytes(v: Double): Array[Byte] = {
    val bytes = new Array[Byte](8)
    val longBuffer = ByteBuffer.wrap(bytes)
    longBuffer.putDouble(v)
    bytes
  }
  
  def uuidToBytes(uuid: JUUID): Array[Byte] = {
    val bytes = new Array[Byte](16)
    val longBuffer = ByteBuffer.wrap(bytes).asLongBuffer
    longBuffer.put(uuid.getMostSignificantBits).put(uuid.getLeastSignificantBits)
    bytes
  }

  def uuidToBytesBigEndian(uuid: JUUID): Array[Byte] = {
    uuidToBytes(uuid)
  }
 
  def uuidToBytesLittleEndian(uuid: JUUID): Array[Byte] = {
    val bytes = new Array[Byte](16)
    val longBuffer = ByteBuffer.wrap(bytes).asLongBuffer
    longBuffer.put(uuid.getLeastSignificantBits).put(uuid.getMostSignificantBits)
    bytes
  }
  
  def bytesToShort(bytes: Array[Byte]): Short = ByteBuffer.wrap(bytes).getShort()
  def bytesToInt(bytes: Array[Byte]): Int = ByteBuffer.wrap(bytes).getInt()
  def bytesToLong(bytes: Array[Byte]): Long = ByteBuffer.wrap(bytes).getLong()
  def bytesToFloat(bytes: Array[Byte]): Float = ByteBuffer.wrap(bytes).getFloat()
  def bytesToDouble(bytes: Array[Byte]): Double = ByteBuffer.wrap(bytes).getDouble()
  
  
  def bytesToUuid(bytes: Array[Byte]): JUUID = {
    val longBuffer = ByteBuffer.wrap(bytes).asLongBuffer
    new JUUID(longBuffer.get, longBuffer.get)
  }
  
  def bytesBigEndianToUuid(bytes: Array[Byte]): JUUID = bytesToUuid(bytes)
  
  
}