package almhirt.io

trait BinaryReader {
  final def read = readByte
  final def readBytes(size: Int): Array[Byte] = readByteArray(size)
  def readByte: Byte
  def readUnsignedByte: Int
  def readShort: Short
  def readUnsignedShort: Int
  def readInt: Int
  def readLong: Long
  def readFloat: Float
  def readDouble: Double
  def readByteArray(size: Int): Array[Byte]
  def slice(size: Int): BinaryReader
  def advance(distance: Int)
}

object BinaryReader {
  def apply(bytes: Array[Byte]): BinaryReader = {
    val byteBuffer = java.nio.ByteBuffer.wrap(bytes)
    new SimpleReaderImpl(byteBuffer)
  }
  
  private class SimpleReaderImpl(byteBuffer: java.nio.ByteBuffer) extends BinaryReader {
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
        array
      }
      def advance(distance: Int) {
        byteBuffer.position(byteBuffer.position() + distance)
      }
      def slice(size: Int): BinaryReader = {
        val sliceBuffer = byteBuffer.slice()
        sliceBuffer.position(byteBuffer.position())
        sliceBuffer.limit(byteBuffer.position()+size)
        new SimpleReaderImpl(sliceBuffer)
      }
  }
  
  
}