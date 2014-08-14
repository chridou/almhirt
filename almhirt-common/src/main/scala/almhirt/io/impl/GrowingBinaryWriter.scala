package almhirt.io.impl

import almhirt.io.BinaryWriter
import scala.annotation.tailrec

class GrowingBinaryWriter(initialCapacity: Int, maxSize: Int, maxIncrement: Int) extends BinaryWriter {
  var count = 0
  var currentBuffer: java.nio.ByteBuffer = java.nio.ByteBuffer.wrap(Array.ofDim[Byte](initialCapacity))

  var previousData = Vector.empty[(Array[Byte], Int)]

  def preAdd(numBytes: Int) {
    val newCount = count + numBytes
    if (newCount > maxSize) throw new Exception(s"""MaxSize is $maxSize. But the new required size is $newCount.""")
    if (currentBuffer.position() + numBytes > currentBuffer.capacity())
      createNewBuffer(numBytes)
    count = newCount
  }

  private def addArray(array: Array[Byte], offset: Int, numBytes: Int) {
    val newCount = count + numBytes
    if (newCount > maxSize) throw new Exception(s"""MaxSize is $maxSize. But the new required size is $newCount.""")
    addArrayAndMaybeGrow(array, offset, numBytes)
  }

  @tailrec
  private def addArrayAndMaybeGrow(array: Array[Byte], offset: Int, numBytes: Int) {
    if (numBytes > 0) {
      val bytesLeftInCurrentBuffer = currentBuffer.capacity() - currentBuffer.position()
      if (bytesLeftInCurrentBuffer >= numBytes) {
        preAdd(numBytes)
        currentBuffer.put(array, offset, numBytes)
      } else if (bytesLeftInCurrentBuffer == 0) {
        createNewBuffer(1)
        addArrayAndMaybeGrow(array, offset, numBytes)
      } else {
        preAdd(bytesLeftInCurrentBuffer)
        currentBuffer.put(array, offset, bytesLeftInCurrentBuffer)
        val stillToAdd = numBytes - bytesLeftInCurrentBuffer
        if (stillToAdd > 0)
          addArrayAndMaybeGrow(array, offset + bytesLeftInCurrentBuffer, stillToAdd)
      }
    }
  }

  def createNewBuffer(min: Int) {
    previousData = addBufferArray(currentBuffer, previousData)
    val increment = Math.min(Math.max(currentBuffer.capacity(), min) * 2, maxIncrement)
    currentBuffer = java.nio.ByteBuffer.wrap(Array.ofDim[Byte](increment))
  }

  @inline
  def addBufferArray(buffer: java.nio.ByteBuffer, previousData: Vector[(Array[Byte], Int)]): Vector[(Array[Byte], Int)] = {
    previousData :+ (buffer.array(), buffer.position())
  }

  override def writeByte(v: Byte): BinaryWriter = {
    preAdd(1)
    currentBuffer.put(v)
    this
  }
  override def writeUnsignedByte(v: Int): BinaryWriter = {
    preAdd(1)
    currentBuffer.put(v.toByte)
    this
  }
  override def writeShort(v: Short): BinaryWriter = {
    preAdd(2)
    currentBuffer.putShort(v)
    this
  }
  override def writeUnsignedShort(v: Int): BinaryWriter = {
    preAdd(2)
    currentBuffer.putShort(v.toShort)
    this
  }
  override def writeInt(v: Int): BinaryWriter = {
    preAdd(4)
    currentBuffer.putInt(v)
    this
  }
  override def writeLong(v: Long): BinaryWriter = {
    preAdd(8)
    currentBuffer.putLong(v)
    this
  }
  override def writeFloat(v: Float): BinaryWriter = {
    preAdd(4)
    currentBuffer.putFloat(v)
    this
  }
  override def writeDouble(v: Double): BinaryWriter = {
    preAdd(8)
    currentBuffer.putDouble(v)
    this
  }
  override def writeByteArray(v: Array[Byte]): BinaryWriter = {
    addArray(v, 0, v.length)
    this
  }

  override def writeBytes(v: Seq[Byte]): BinaryWriter = writeByteArray(v.toArray)

  override def toArray: Array[Byte] = {
    createCompleteArray(addBufferArray(currentBuffer, previousData))
  }

  override def spawnNew(capacity: Option[Int] = None): BinaryWriter = new GrowingBinaryWriter(initialCapacity, maxSize, maxIncrement)

  private def createCompleteArray(from: Vector[(Array[Byte], Int)]): Array[Byte] = {
    val size = from.foldLeft(0) { case (acc, next) ⇒ acc + next._2 }
    val target = Array.ofDim[Byte](size)
    var offset = 0
    from foreach {
      case (arr, count) ⇒
        System.arraycopy(arr, 0, target, offset, count)
        offset += count
    }
    target
  }

}