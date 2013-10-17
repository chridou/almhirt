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
  def readByteArray(size: Int): Array[Byte]
  final def readBytes(size: Int): Array[Byte] = readByteArray(size)
}

object BinaryReader {
  def apply(bytes: Array[Byte]): BinaryReader = {
    val byteBuffer = java.nio.ByteBuffer.wrap(bytes)
    new BinaryReader {
      def readByte: Byte = byteBuffer.get
      def readUnsignedByte: Int = byteBuffer.get & 0xff
      def readShort: Short = byteBuffer.getShort
      def readUnsignedShort: Int = byteBuffer.getShort & 0xffff
      def readInt: Int = byteBuffer.getInt
      def readLong: Long = byteBuffer.getLong
      def readFloat: Float = byteBuffer.getFloat
      def readDouble: Double = byteBuffer.getDouble
      def readByteArray(size: Int): Array[Byte] = {
        val array = Array.ofDim[Byte](size)
        byteBuffer.get(array)
        array}
    }
  }
}