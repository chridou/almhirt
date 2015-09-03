package riftwarp.messagepack

import almhirt.converters.BinaryConverter
import almhirt.io.BinaryWriter

object RiftWarpPrimitiveAppenders {
  @inline
  def appendString(v: String, writer: BinaryWriter): BinaryWriter = {
    val bytes = v.getBytes("UTF-8")
    if (bytes.length < 32) {
      val h = 0xa0 | bytes.length
      writer.writeUnsignedByte(h).writeByteArray(bytes)
    } else if (bytes.length < 256) {
      writer.writeUnsignedByte(MessagePackTypecodes.Str8).writeUnsignedByte(bytes.length).writeByteArray(bytes)
    } else if (bytes.length < 256 * 256) {
      writer.writeUnsignedByte(MessagePackTypecodes.Str16).writeUnsignedShort(bytes.length).writeByteArray(bytes)
    } else {
      writer.writeUnsignedByte(MessagePackTypecodes.Str32).writeInt(bytes.length).writeByteArray(bytes)
    }
  }

  @inline
  def appendBoolean(v: Boolean, writer: BinaryWriter): BinaryWriter =
    if (v)
      writer.writeUnsignedByte(MessagePackTypecodes.True)
    else
      writer.writeUnsignedByte(MessagePackTypecodes.False)

  @inline
  def appendByte(v: Byte, writer: BinaryWriter): BinaryWriter =
    if ((v & 0x80) == 0) // 10000000
      writer.writeUnsignedByte(v)
    else if (v < 0 && v > -32) {
      writer.writeUnsignedByte((-v) | 0xE0)
    } else
      writer.writeUnsignedByte(MessagePackTypecodes.Int8).writeByte(v)

  @inline
  def appendShort(v: Short, writer: BinaryWriter): BinaryWriter =
    if (v >= Byte.MinValue && v <= Byte.MaxValue)
      appendByte(v.toByte, writer)
    else
      writer.writeUnsignedByte(MessagePackTypecodes.Int16).writeShort(v)

  @inline
  def appendInt(v: Int, writer: BinaryWriter): BinaryWriter =
    if (v >= Short.MinValue && v <= Short.MaxValue)
      appendShort(v.toShort, writer)
    else
      writer.writeUnsignedByte(MessagePackTypecodes.Int32).writeInt(v)

  @inline
  def appendLong(v: Long, writer: BinaryWriter): BinaryWriter =
    if (v >= Int.MinValue && v <= Int.MaxValue)
      appendInt(v.toInt, writer)
    else
      writer.writeUnsignedByte(MessagePackTypecodes.Int64).writeLong(v)

  @inline
  def appendBigInt(v: BigInt, writer: BinaryWriter): BinaryWriter = {
    if (v >= Long.MinValue && v <= Long.MaxValue)
      appendLong(v.toLong, writer)
    else {
      val bytes = v.toByteArray
      appendExt(bytes, RiftwarpTypecodes.BigIntCode, writer)
    }
  }

  @inline
  def appendFloat(v: Float, writer: BinaryWriter): BinaryWriter =
    writer.writeUnsignedByte(MessagePackTypecodes.Float).writeFloat(v)

  @inline
  def appendDouble(v: Double, writer: BinaryWriter): BinaryWriter =
    writer.writeUnsignedByte(MessagePackTypecodes.Double).writeDouble(v)

  @inline
  def appendBigDecimal(v: BigDecimal, writer: BinaryWriter): BinaryWriter = {
    val bytes = v.toString.getBytes()
    appendExt(bytes, RiftwarpTypecodes.BigDecimalCode, writer)
  }

  @inline
  def appendUuid(v: java.util.UUID, writer: BinaryWriter): BinaryWriter = {
    val bytes = BinaryConverter.uuidToBytesBigEndian(v)
    writer.writeUnsignedByte(MessagePackTypecodes.Fixext16).writeUnsignedByte(RiftwarpTypecodes.UuidCode).writeByteArray(bytes)
  }

  @inline
  def appendUri(v: java.net.URI, writer: BinaryWriter): BinaryWriter = {
    val bytes = v.toString.getBytes()
    appendExt(bytes, RiftwarpTypecodes.UriCode, writer)
  }

  @inline
  def appendDateTime(v: java.time.ZonedDateTime, writer: BinaryWriter): BinaryWriter = {
    val bytes = v.toString().getBytes()
    appendExt(bytes, RiftwarpTypecodes.DateTimeCode, writer)
  }

  @inline
  def appendLocalDateTime(v: java.time.LocalDateTime, writer: BinaryWriter): BinaryWriter = {
    val bytes = v.toString().getBytes()
    appendExt(bytes, RiftwarpTypecodes.LocalDateTimeCode, writer)
  }

  @inline
  def appendDuration(v: scala.concurrent.duration.FiniteDuration, writer: BinaryWriter): BinaryWriter = {
    val nanos = v.toNanos
    writer.writeUnsignedByte(MessagePackTypecodes.Fixext8).writeUnsignedByte(RiftwarpTypecodes.DurationCode).writeLong(nanos)
  }

  @inline
  def appendExt(data: Array[Byte], customType: Int, writer: BinaryWriter): BinaryWriter = {
    if (data.length < 256) {
      writer.writeUnsignedByte(MessagePackTypecodes.Ext8).writeUnsignedByte(data.length).writeUnsignedByte(customType).writeByteArray(data)
    } else if (data.length < 256 * 256) {
      writer.writeUnsignedByte(MessagePackTypecodes.Ext16).writeUnsignedShort(data.length).writeUnsignedByte(customType).writeByteArray(data)
    } else {
      writer.writeUnsignedByte(MessagePackTypecodes.Ext32).writeInt(data.length).writeUnsignedByte(customType).writeByteArray(data)
    }
  }
}