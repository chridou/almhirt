package amhirt.io.impl

import org.scalatest._
import almhirt.io.impl.GrowingBinaryWriter

class GrowingBinaryWriterTests extends FunSuite with Matchers {
  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 1, maxIncrement = 0) must store a Byte""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 1, maxIncrement = 0)
    writer.writeByte(1.toByte)
    writer.toArray should equal(Array(1.toByte))
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 1, maxIncrement = 1) must store a Byte""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 1, maxIncrement = 1)
    writer.writeByte(1.toByte)
    writer.toArray should equal(Array(1.toByte))
  }

  test("""A GrowingBinaryWriter(initialCapacity = 2, maxSize = 2, maxIncrement = 0) must store 2 Bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 2, maxSize = 2, maxIncrement = 0)
    writer.writeByte(1.toByte).writeByte(2.toByte)
    writer.toArray should equal(Array(1.toByte, 2.toByte))
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 2, maxIncrement = 1) must store 2 Bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 2, maxIncrement = 1)
    writer.writeByte(1.toByte).writeByte(2.toByte)
    writer.toArray should equal(Array(1.toByte, 2.toByte))
  }

  test("""A GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 0) must store 4 Bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 0)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    sample.foreach(writer.writeByte(_))
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 1) must store 4 Bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 1)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    sample.foreach(writer.writeByte(_))
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 1) must store 4 Bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 1)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    sample.foreach(writer.writeByte(_))
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 4) must store 4 Bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 4)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    sample.foreach(writer.writeByte(_))
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 1) must store 4 Bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 1)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    sample.foreach(writer.writeByte(_))
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 0) must store an Int""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 0)
    writer.writeInt(1)
    val expected = java.nio.ByteBuffer.allocate(4).putInt(1).array()
    writer.toArray should equal(expected)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 4) must store an Int""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 4)
    writer.writeInt(1)
    val expected = java.nio.ByteBuffer.allocate(4).putInt(1).array()
    writer.toArray should equal(expected)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 8, maxIncrement = 1) must store 2 Ints""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 8, maxIncrement = 4)
    writer.writeInt(1).writeInt(2)
    val expected = java.nio.ByteBuffer.allocate(8).putInt(1).putInt(2).array()
    writer.toArray should equal(expected)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 6, maxSize = 6, maxIncrement = 0) must store (Byte, Int, Byte)""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 6, maxSize = 6, maxIncrement = 0)
    writer.writeByte(1.toByte).writeInt(2).writeByte(3.toByte)
    val expected = java.nio.ByteBuffer.allocate(6).put(1.toByte).putInt(2).put(3.toByte).array()
    writer.toArray should equal(expected)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 5, maxSize = 6, maxIncrement = 1) must store (Byte, Int, Byte)""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 5, maxSize = 6, maxIncrement = 1)
    writer.writeByte(1.toByte).writeInt(2).writeByte(3.toByte)
    val expected = java.nio.ByteBuffer.allocate(6).put(1.toByte).putInt(2).put(3.toByte).array()
    writer.toArray should equal(expected)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 6, maxIncrement = 1) must fail on (Byte, Int, Byte) because a max increment of 4 is required when an Int gets stored and does not fit into the current buffer.""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 6, maxIncrement = 1)
    intercept[java.nio.BufferOverflowException] {
      writer.writeByte(1.toByte).writeInt(2).writeByte(3.toByte)
    }
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 6, maxIncrement = 4) must store (Byte, Int, Byte)""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 6, maxIncrement = 4)
    writer.writeByte(1.toByte).writeInt(2).writeByte(3.toByte)
    val expected = java.nio.ByteBuffer.allocate(6).put(1.toByte).putInt(2).put(3.toByte).array()
    writer.toArray should equal(expected)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 6, maxIncrement = 4) must store (Byte, Int, Byte)""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 6, maxIncrement = 4)
    writer.writeByte(1.toByte).writeInt(2).writeByte(3.toByte)
    val expected = java.nio.ByteBuffer.allocate(6).put(1.toByte).putInt(2).put(3.toByte).array()
    writer.toArray should equal(expected)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 0, maxIncrement = 0) must store an empty Array""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 0, maxIncrement = 0)
    val sample = Array.empty[Byte]
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 1, maxIncrement = 0) must store an empty Array""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 1, maxIncrement = 0)
    val sample = Array.empty[Byte]
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 1, maxIncrement = 0) must store an empty Array""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 1, maxIncrement = 0)
    val sample = Array.empty[Byte]
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }
  
  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 1, maxIncrement = 1) must store an empty Array""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 1, maxIncrement = 1)
    val sample = Array.empty[Byte]
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }
  
  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 1, maxIncrement = 0) must store an Array of 1 byte""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 1, maxIncrement = 0)
    val sample = Array(1.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 2, maxSize = 2, maxIncrement = 0) must store an Array of 2 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 2, maxSize = 2, maxIncrement = 0)
    val sample = Array(1.toByte, 2.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 2, maxIncrement = 1) must store an Array of 2 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 2, maxIncrement = 1)
    val sample = Array(1.toByte, 2.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 2, maxIncrement = 2) must store an Array of 2 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 2, maxIncrement = 2)
    val sample = Array(1.toByte, 2.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 4) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 4)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 3, maxSize = 4, maxIncrement = 4) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 3, maxSize = 4, maxIncrement = 4)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 4) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 4)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 4) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 4)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 4) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 4)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 3) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 3)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 3, maxSize = 4, maxIncrement = 3) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 3, maxSize = 4, maxIncrement = 3)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 3) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 3)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 3) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 3)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 3) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 3)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 2) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 2)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 3, maxSize = 4, maxIncrement = 2) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 3, maxSize = 4, maxIncrement = 2)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 2) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 2)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 2) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 2)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 2) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 2)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 1) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 4, maxSize = 4, maxIncrement = 1)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 3, maxSize = 4, maxIncrement = 1) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 3, maxSize = 4, maxIncrement = 1)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 1) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 2, maxSize = 4, maxIncrement = 1)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 1) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 4, maxIncrement = 1)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 1) must store an Array of 4 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 4, maxIncrement = 1)
    val sample = Array(1.toByte, 2.toByte, 3.toByte, 4.toByte)
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 2048, maxSize = 2048, maxIncrement = 0) must store an Array of 2048 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 2048, maxSize = 2048, maxIncrement = 0)
    val sample = (for (i ← 0 until 2048) yield ((i % Byte.MaxValue).toByte)).toArray
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 2048, maxIncrement = 1) must store an Array of 2048 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 0, maxSize = 2048, maxIncrement = 1)
    val sample = (for (i ← 0 until 2048) yield ((i % Byte.MaxValue).toByte)).toArray
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1, maxSize = 2048, maxIncrement = 1) must store an Array of 2048 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 2048, maxIncrement = 1)
    val sample = (for (i ← 0 until 2048) yield ((i % Byte.MaxValue).toByte)).toArray
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 16, maxSize = 2048, maxIncrement = 16) must store an Array of 2048 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 16, maxSize = 2048, maxIncrement = 16)
    val sample = (for (i ← 0 until 2048) yield ((i % Byte.MaxValue).toByte)).toArray
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1024, maxSize = 2048, maxIncrement = 1024) must store an Array of 2048 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1024, maxSize = 2048, maxIncrement = 1024)
    val sample = (for (i ← 0 until 2048) yield ((i % Byte.MaxValue).toByte)).toArray
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }
  
  test("""A GrowingBinaryWriter(initialCapacity = 2048, maxSize = 2047, maxIncrement = 2048) must store an Array of 2048 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 2048, maxSize = 2048, maxIncrement = 2048)
    val sample = (for (i ← 0 until 2048) yield ((i % Byte.MaxValue).toByte)).toArray
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }

  test("""A GrowingBinaryWriter(initialCapacity = 1024, maxSize = 2047, maxIncrement = 2048) must store an Array of 2048 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1024, maxSize = 2048, maxIncrement = 2048)
    val sample = (for (i ← 0 until 2048) yield ((i % Byte.MaxValue).toByte)).toArray
    writer.writeByteArray(sample)
    writer.toArray should equal(sample)
  }
  
  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 2047, maxIncrement = 2048) must fail to store an Array of 2048 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 2047, maxIncrement = 2048)
    val sample = (for (i ← 0 until 2048) yield ((i % Byte.MaxValue).toByte)).toArray
    intercept[Exception] {
      writer.writeByteArray(sample)
    }
  }

  test("""A GrowingBinaryWriter(initialCapacity = 0, maxSize = 2047, maxIncrement = 1) must fail to store an Array of 2048 bytes""") {
    val writer = new GrowingBinaryWriter(initialCapacity = 1, maxSize = 2047, maxIncrement = 1)
    val sample = (for (i ← 0 until 2048) yield ((i % Byte.MaxValue).toByte)).toArray
    intercept[Exception] {
      writer.writeByteArray(sample)
    }
  }
  
  
}