package almhirt.io

trait BinaryReader {
  final def read = readByte
  def readByte: Byte
  def readUnsignedByte: Int
  def readShort: Short
  def readUnsignedShort: Int
  def readInt: Int
  def readLong: Long
  def readFloat: Float
  def readDouble: Double
  def readBigInt: BigInt
  def readByteArray(size: Int): Array[Byte]
  final def readBytes(size: Int): Array[Byte] = readByteArray(size)
}

object BinaryReader {
  def apply(bytes: Array[Byte]): BinaryReader = {
    ???
  }
}