package riftwarp.messagepack

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.io.BinaryReader
import almhirt.converters.BinaryConverter
import riftwarp._

object MessagePackParser {
  def parse(reader: BinaryReader): AlmValidation[WarpPackage] = {
    try {
      parseUnsafe(reader).success
    } catch {
      case scala.util.control.NonFatal(exn) => ParsingProblem(s"Could not parse MessagePack: ${exn.getMessage()}", cause = Some(exn)).failure
    }
  }

  def parseUnsafe(reader: BinaryReader): WarpPackage = {
    val formatByte = reader.readUnsignedByte
    if (formatByte == MessagePackTypecodes.Null) {
      throw new Exception("Null is only allowed inside a WarpElement")
    } else if (formatByte == MessagePackTypecodes.True) {
      WarpBoolean(true)
    } else if (formatByte == MessagePackTypecodes.False) {
      WarpBoolean(false)
    } else if (formatByte == MessagePackTypecodes.Int8) {
      WarpByte(reader.readByte)
    } else if (formatByte == MessagePackTypecodes.Int16) {
      WarpInt(reader.readShort.toInt)
    } else if (formatByte == MessagePackTypecodes.Int32) {
      WarpInt(reader.readInt)
    } else if (formatByte == MessagePackTypecodes.Int64) {
      WarpLong(reader.readLong)
    } else if (MessagePackTypecodes.isExt(formatByte)) {
      parseSpecialType(formatByte, reader)
    } else if (MessagePackTypecodes.isArray(formatByte)) {
      parseCollection(formatByte, reader)
    } else if (MessagePackTypecodes.isMap(formatByte)) {
      parseAssociativeCollection(formatByte, reader)
    } else if (MessagePackTypecodes.isBin(formatByte)) {
      parseBytes(formatByte, reader)
    } else {
      throw new Exception(s"Invalid format byte: $formatByte")
    }
  }

  def parseSpecialType(formatByte: Int, reader: BinaryReader): WarpPackage = {
    val (customType, size) = MessagePackTypecodes.parseExtHeader(formatByte, reader)
    customType match {
      case RiftwarpTypecodes.BigIntCode => parseBigInt(size, reader)
      case RiftwarpTypecodes.BigDecimalCode => parseBigDecimal(size, reader)
      case RiftwarpTypecodes.UuidCode => parseUuid(reader)
      case RiftwarpTypecodes.UriCode => parseUri(size, reader)
      case RiftwarpTypecodes.DateTimeCode => parseDateTime(size, reader)
      case RiftwarpTypecodes.LocalDateTimeCode => parseLocalDateTime(size, reader)
      case RiftwarpTypecodes.Tuple2Code => parseTuple2(size, reader)
      case RiftwarpTypecodes.Tuple3Code => parseTuple3(size, reader)
      case RiftwarpTypecodes.DurationCode => parseDuration(reader)
    }
  }

  def parseBigInt(size: Int, reader: BinaryReader): WarpBigInt = {
    val bytes = reader.readByteArray(size)
    WarpBigInt(BigInt(bytes))
  }

  def parseBigDecimal(size: Int, reader: BinaryReader): WarpBigDecimal = {
    val bytes = reader.readByteArray(size)
    WarpBigDecimal(BigDecimal(new String(bytes, "utf-8")))
  }

  def parseUuid(reader: BinaryReader): WarpUuid = {
    val bytes = reader.readBytes(16)
    WarpUuid(BinaryConverter.bytesBigEndianToUuid(bytes))
  }

  def parseUri(size: Int, reader: BinaryReader): WarpUri = {
    val bytes = reader.readByteArray(size)
    WarpUri(new java.net.URI(new String(bytes, "utf-8")))
  }

  def parseDateTime(size: Int, reader: BinaryReader): WarpDateTime = {
    val bytes = reader.readByteArray(size)
    WarpDateTime(org.joda.time.DateTime.parse(new String(bytes, "utf-8")))
  }

  def parseLocalDateTime(size: Int, reader: BinaryReader): WarpLocalDateTime = {
    val bytes = reader.readByteArray(size)
    WarpLocalDateTime(org.joda.time.LocalDateTime.parse(new String(bytes, "utf-8")))
  }

  def parseDuration(reader: BinaryReader): WarpDuration = {
    val nanos = reader.readLong
    WarpDuration(scala.concurrent.duration.FiniteDuration.apply(nanos, "ns"))
  }

  def parseCollection(formatByte: Int, reader: BinaryReader): WarpCollection = {
    val numElems = MessagePackTypecodes.parseArrayHeader(formatByte, reader)
    WarpCollection((for (i <- 0 until numElems) yield parseUnsafe(reader)).toVector)
  }

  def parseAssociativeCollection(formatByte: Int, reader: BinaryReader): WarpAssociativeCollection = {
    val numElems = MessagePackTypecodes.parseMapHeader(formatByte, reader)
    WarpAssociativeCollection((for (i <- 0 until numElems) yield (parseUnsafe(reader), parseUnsafe(reader))).toVector)
  }

  def parseTree(formatByte: Int, reader: BinaryReader): WarpTree = {
    ???
  }

  def parseTuple2(size: Int, reader: BinaryReader): WarpTuple2 = {
    val a = parseUnsafe(reader)
    val b = parseUnsafe(reader)
    WarpTuple2(a, b)
  }

  def parseTuple3(size: Int, reader: BinaryReader): WarpTuple3 = {
    val a = parseUnsafe(reader)
    val b = parseUnsafe(reader)
    val c = parseUnsafe(reader)
    WarpTuple3(a, b, c)
  }

  def parseBytes(formatByte: Int, reader: BinaryReader): WarpBytes = {
    val size = MessagePackTypecodes.parseBinHeader(formatByte, reader)
    WarpBytes(reader.readBytes(size))
  }

  def parseWarpDescriptor(formatByte: Int, reader: BinaryReader): WarpDescriptor = {
    ???
  }

  def parseElement(formatByte: Int, reader: BinaryReader): WarpElement = {
    ???
  }

  def parseObject(formatByte: Int, reader: BinaryReader): WarpObject = {
    ???
  }

}