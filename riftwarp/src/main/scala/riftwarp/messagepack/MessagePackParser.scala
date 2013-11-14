package riftwarp.messagepack

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
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
    parseUnsafe(formatByte, reader)
  }

  def parseUnsafe(formatByte: Int, reader: BinaryReader): WarpPackage = {
    if (formatByte == MessagePackTypecodes.Null) {
      throw new Exception("Null is only allowed within a WarpElement")
    } else if (formatByte == MessagePackTypecodes.True) {
      WarpBoolean(true)
    } else if (formatByte == MessagePackTypecodes.False) {
      WarpBoolean(false)
    } else if (MessagePackTypecodes.isStr(formatByte)) {
      parseString(formatByte, reader)
    } else if ((formatByte & 0x80) == 0) { // 10000000
      WarpByte(formatByte.toByte)
    } else if ((formatByte & 0xE0) == 0xE0) { // 10000000
      WarpByte((-(formatByte & 0x1F)).toByte)
    } else if (formatByte == MessagePackTypecodes.Int8) {
      WarpByte(reader.readByte)
    } else if (formatByte == MessagePackTypecodes.Int16) {
      WarpShort(reader.readShort)
    } else if (formatByte == MessagePackTypecodes.Int32) {
      WarpInt(reader.readInt)
    } else if (formatByte == MessagePackTypecodes.Int64) {
      WarpLong(reader.readLong)
    } else if (formatByte == MessagePackTypecodes.Float) {
      WarpFloat(reader.readFloat)
    } else if (formatByte == MessagePackTypecodes.Double) {
      WarpDouble(reader.readDouble)
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

  def readInteger(formatByte: Int, reader: BinaryReader): WarpIntegralInteger = {
    if ((formatByte & 0x80) == 0) { // 10000000
      WarpByte(formatByte.toByte)
    } else if (formatByte == MessagePackTypecodes.Int8) {
      WarpByte(reader.readByte)
    } else if (formatByte == MessagePackTypecodes.Int16) {
      WarpShort(reader.readShort)
    } else if (formatByte == MessagePackTypecodes.Int32) {
      WarpInt(reader.readInt)
    } else if (formatByte == MessagePackTypecodes.Int64) {
      WarpLong(reader.readLong)
    } else {
      throw new Exception(s"$formatByte is not a format byte for any integer number")
    }
  }

  @inline
  def readString(formatByte: Int, reader: BinaryReader) = {
    val size = MessagePackTypecodes.parseStrHeader(formatByte, reader)
    val bytes = reader.readByteArray(size)
    new String(bytes, "utf-8")
  }

  def parseString(formatByte: Int, reader: BinaryReader): WarpString = {
    WarpString(readString(formatByte, reader))
  }

  def parseSpecialType(formatByte: Int, reader: BinaryReader): WarpPackage = {
    val (customType, size) = MessagePackTypecodes.parseExtHeader(formatByte, reader)
    customType match {
      case RiftwarpTypecodes.ObjectCode =>
        parseObject(size, reader)
      case RiftwarpTypecodes.BigIntCode =>
        parseBigInt(size, reader)
      case RiftwarpTypecodes.BigDecimalCode =>
        parseBigDecimal(size, reader)
      case RiftwarpTypecodes.UuidCode =>
        parseUuid(reader)
      case RiftwarpTypecodes.UriCode =>
        parseUri(size, reader)
      case RiftwarpTypecodes.DateTimeCode =>
        parseDateTime(size, reader)
      case RiftwarpTypecodes.LocalDateTimeCode =>
        parseLocalDateTime(size, reader)
      case RiftwarpTypecodes.Tuple2Code =>
        parseTuple2(size, reader)
      case RiftwarpTypecodes.Tuple3Code =>
        parseTuple3(size, reader)
      case RiftwarpTypecodes.DurationCode =>
        parseDuration(reader)
      case RiftwarpTypecodes.TreeCode =>
        parseTree(size, reader)
      case x =>
        throw new Exception(s"$x is not a valid custom type for a WarpPackage encoded in a MessagePack 'ext' type")
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

  def parseTree(size: Int, reader: BinaryReader): WarpTree = {
    val formatByte = reader.readUnsignedByte
    val nestedTreeCollection = parseCollection(formatByte, reader)
    WarpTree(parseTreeNode(nestedTreeCollection))
  }

  private def parseTreeNode(nestedTreeCollection: WarpPackage): Tree[WarpPackage] = {
    nestedTreeCollection match {
      case WarpCollection(Vector(label, WarpCollection(subforest))) =>
        label.node(subforest.map(parseTreeNode): _*)
      case WarpCollection(items @ Vector(a,b)) =>
        throw new Exception(
          s"""	|A tree node must be a collection of size 2 with the first element representing the label and the second representing the subforest. 
          		|Received a WarpCollection of size ${items.size}.
          		|The first element is a "$a",
          		|the second is a "$b".""".stripMargin)
      case x =>
        throw new Exception(
          s"""A tree node must be a collection of size 2 with the first element representing the label and the second representing the subforest. Received a $x.""")
    }
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
    try {
      MessagePackTypecodes.parseExtHeader(formatByte, reader)
      val identifier = readString(reader.readUnsignedByte, reader)
      val versionFormatByte = reader.readUnsignedByte
      val version: Option[Int] = versionFormatByte match {
        case MessagePackTypecodes.Null =>
          None
        case _ =>
          val v = readInteger(versionFormatByte, reader).as[Int].resultOrEscalate
          Some(v)
      }
      WarpDescriptor(identifier, version)
    } catch {
      case scala.util.control.NonFatal(exn) => throw new Exception("Could not parse WarpDescriptor", exn)
    }
  }

  def parseElement(reader: BinaryReader): WarpElement = {
    try {
      reader.advance(1)
      val label = readString(reader.readUnsignedByte, reader)
      val formatByte = reader.readUnsignedByte
      val value: Option[WarpPackage] = formatByte match {
        case MessagePackTypecodes.Null =>
          None
        case formatByte =>
          Some(parseUnsafe(formatByte, reader))
      }
      WarpElement(label, value)
    } catch {
      case scala.util.control.NonFatal(exn) => throw new Exception("Could not parse WarpElement", exn)
    }
  }

  def parseObject(size: Int, reader: BinaryReader): WarpObject = {
    try {
      val wdFormatByte = reader.readUnsignedByte
      val wd: Option[WarpDescriptor] = wdFormatByte match {
        case MessagePackTypecodes.Null =>
          None
        case formatByte =>
          Some(parseWarpDescriptor(formatByte, reader))
      }
      val numElems = MessagePackTypecodes.parseArrayHeader(reader.readUnsignedByte, reader)
      val elems = (for (n <- 0 until numElems) yield parseElement(reader))
      WarpObject(wd, elems.toVector)
    } catch {
      case scala.util.control.NonFatal(exn) => throw new Exception("Could not parse WarpObject", exn)
    }
  }

}