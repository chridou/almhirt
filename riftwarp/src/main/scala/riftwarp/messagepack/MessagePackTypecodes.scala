package riftwarp.messagepack

import almhirt.io.BinaryReader

object MessagePackTypecodes {
  val Null = 0xc0

  val True = 0xc3
  val False = 0xc2

  val Str8 = 0xd9
  val Str16 = 0xda
  val Str32 = 0xdb

  val Int8 = 0xd0
  val Int16 = 0xd1
  val Int32 = 0xd2
  val Int64 = 0xd3

  val UInt8 = 0xcc
  val UInt16 = 0xcd
  val UInt32 = 0xce
  
  val Float = 0xca
  val Double = 0xcb

  val Ext8 = 0xc7
  val Ext16 = 0xc8
  val Ext32 = 0xc9

  val Fixext1 = 0xd4
  val Fixext2 = 0xd5
  val Fixext4 = 0xd6
  val Fixext8 = 0xd7
  val Fixext16 = 0xd8

  val Array16 = 0xdc
  val Array32 = 0xdd

  val Map16 = 0xde
  val Map32 = 0xdf

  val Bin8 = 0xc4
  val Bin16 = 0xc5
  val Bin32 = 0xc6

  def extFormatBytes = Set(Ext8, Ext16, Ext32, Fixext1, Fixext2, Fixext4, Fixext8, Fixext16)

  def isExt(formatByte: Int): Boolean = extFormatBytes(formatByte)

  def parseExtHeader(formatByte: Int, reader: BinaryReader): (Int, Int) = {
    val size =
      formatByte match {
        case Fixext1 ⇒ 1
        case Fixext2 ⇒ 2
        case Fixext4 ⇒ 4
        case Fixext8 ⇒ 8
        case Fixext16 ⇒ 16
        case Ext8 ⇒ reader.readUnsignedByte
        case Ext16 ⇒ reader.readUnsignedShort
        case Ext32 ⇒ reader.readInt
      }
    val customType = reader.readByte
    (customType, size)
  }

  def isStr(formatByte: Int): Boolean = {
    formatByte == Str8 ||
      formatByte == Str16 ||
      formatByte == Str32 ||
      (formatByte & 0xe0) == 0xa0
  }

  def parseStrHeader(formatByte: Int, reader: BinaryReader): Int = {
    if (formatByte == Str8)
      reader.readUnsignedByte
    else if (formatByte == Str16)
      reader.readUnsignedShort
    else if (formatByte == Str32)
      reader.readInt
    else
      formatByte & 0x1f
  }

  def isArray(formatByte: Int): Boolean = {
    formatByte == Array16 ||
      formatByte == Array32 ||
      (formatByte & 0xf0) == 0x90
  }

  def parseArrayHeader(formatByte: Int, reader: BinaryReader): Int = {
    if (formatByte == Array16)
      reader.readUnsignedShort
    else if (formatByte == Array32)
      reader.readInt
    else
      formatByte & 0x0f
  }

  def isMap(formatByte: Int): Boolean = {
    formatByte == Map16 ||
      formatByte == Map32 ||
      (formatByte & 0xf0) == 0x80
  }

  def parseMapHeader(formatByte: Int, reader: BinaryReader): Int = {
    if (formatByte == Map16)
      reader.readUnsignedShort
    else if (formatByte == Map32)
      reader.readInt
    else
      formatByte & 0x0f
  }

  def isBin(formatByte: Int): Boolean = {
    formatByte == Bin8 ||
      formatByte == Bin16 ||
      formatByte == Bin32
  }

  def parseBinHeader(formatByte: Int, reader: BinaryReader): Int = {
    formatByte match {
      case Bin8 ⇒ reader.readUnsignedByte
      case Bin16 ⇒ reader.readUnsignedShort
      case Bin32 ⇒ reader.readInt
    }
  }

}