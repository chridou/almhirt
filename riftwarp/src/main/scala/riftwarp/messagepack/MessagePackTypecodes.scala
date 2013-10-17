package riftwarp.messagepack

object MessagePackTypecodes {
  val Null = 0xc0
  
  val True = 0xc3
  val False = 0xc2

  val Str8 = 0xd9
  val Str16 = 0xda
  val Str32 = 0xdb
  
  val Int8 = 0xcc
  val Int16 = 0xcd
  val Int32 = 0xce
  val Int64 = 0xcf

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
  val Bin32= 0xc6
}