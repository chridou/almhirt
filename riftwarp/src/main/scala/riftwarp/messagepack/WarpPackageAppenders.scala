package riftwarp.messagepack

import scalaz._, Scalaz._
import riftwarp._
import almhirt.io.BinaryWriter
import almhirt.converters.BinaryConverter

object WarpPackageAppenders {
  def appendWarpPackage(p: WarpPackage, writer: BinaryWriter): BinaryWriter = {
    p match {
      case pack: WarpPrimitive => appendWarpPrimitive(pack, writer)
      case pack: WarpObject => appendWarpObject(pack, writer)
      case pack: WarpCollection => appendWarpCollection(pack, writer)
      case pack: WarpAssociativeCollection => appendWarpAssociativeCollection(pack, writer)
      case pack: WarpTree => appendWarpTree(pack, writer)
      case pack: WarpTuple2 => appendWarpTuple2(pack, writer)
      case pack: WarpTuple3 => appendWarpTuple3(pack, writer)
      case pack: BinaryWarpPackage => appendBinaryWarpPackage(pack, writer)
    }
  }

  @inline
  def appendWarpPrimitive(p: WarpPrimitive, writer: BinaryWriter): BinaryWriter = {
    p match {
      case prim: WarpBoolean => appendWarpBoolean(prim, writer)
      case prim: WarpString => appendWarpString(prim, writer)
      case prim: WarpByte => appendWarpByte(prim, writer)
      case prim: WarpShort => appendWarpShort(prim, writer)
      case prim: WarpInt => appendWarpInt(prim, writer)
      case prim: WarpLong => appendWarpLong(prim, writer)
      case prim: WarpBigInt => appendWarpBigInt(prim, writer)
      case prim: WarpFloat => appendWarpFloat(prim, writer)
      case prim: WarpDouble => appendWarpDouble(prim, writer)
      case prim: WarpBigDecimal => appendWarpBigDecimal(prim, writer)
      case prim: WarpUuid => appendWarpUuid(prim, writer)
      case prim: WarpUri => appendWarpUri(prim, writer)
      case prim: WarpDateTime => appendWarpDateTime(prim, writer)
      case prim: WarpLocalDateTime => appendWarpLocalDateTime(prim, writer)
      case prim: WarpDuration => appendWarpDuration(prim, writer)
    }
  }

  def appendWarpCollection(v: WarpCollection, writer: BinaryWriter): BinaryWriter = {
    val size = v.items.size
    if (size < 16) {
      val c = 0x90 | size // 10010000
      writer.writeUnsignedByte(c)
    } else if (size < 256 * 256) {
      writer.writeUnsignedByte(MessagePackTypecodes.Array16)
      writer.writeUnsignedShort(size)
    } else {
      writer.writeUnsignedByte(MessagePackTypecodes.Array32)
      writer.writeInt(size)
    }
    v.items.foreach(x => appendWarpPackage(x, writer))
    writer
  }

  def appendWarpAssociativeCollection(v: WarpAssociativeCollection, writer: BinaryWriter): BinaryWriter = {
    val size = v.items.size
    if (size < 16) {
      val c = 0x80 | size // 10000000
      writer.writeUnsignedByte(c)
    } else if (size < 256 * 256) {
      writer.writeUnsignedByte(MessagePackTypecodes.Map16)
      writer.writeUnsignedShort(size)
    } else {
      writer.writeUnsignedByte(MessagePackTypecodes.Map32)
      writer.writeInt(size)
    }
    v.items.foreach { x =>
      appendWarpPackage(x._1, writer)
      appendWarpPackage(x._2, writer)
    }
    writer
  }

  def appendWarpTree(v: WarpTree, writer: BinaryWriter): BinaryWriter = {
    val treeWriter = writer.spawnNew(None)
    appendWarpTreeNode(v.tree, treeWriter)
    RiftWarpPrimitiveAppenders.appendExt(treeWriter.toArray, RiftwarpTypecodes.TreeCode, writer)
  }

  private def appendWarpTreeNode(tree: scalaz.Tree[WarpPackage], writer: BinaryWriter) {
    writer.writeUnsignedByte(0x90 | 2)
    appendWarpPackage(tree.rootLabel, writer)
    val children = tree.subForest.toVector
    val size = children.size
    if (size < 16) {
      val c = 0x90 | size 
      writer.writeUnsignedByte(c)
    } else if (size < 256 * 256) {
      writer.writeUnsignedByte(MessagePackTypecodes.Array16)
      writer.writeUnsignedShort(size)
    } else {
      writer.writeUnsignedByte(MessagePackTypecodes.Array32)
      writer.writeInt(size)
    }
    tree.subForest.foreach(st => appendWarpTreeNode(st, writer))
  }

  def appendWarpTuple2(v: WarpTuple2, writer: BinaryWriter): BinaryWriter = {
    val tupleWriter = writer.spawnNew()
    appendWarpPackage(v.a, tupleWriter)
    appendWarpPackage(v.b, tupleWriter)
    val x = tupleWriter.toArray
    RiftWarpPrimitiveAppenders.appendExt(x, RiftwarpTypecodes.Tuple2Code, writer)
  }

  def appendWarpTuple3(v: WarpTuple3, writer: BinaryWriter): BinaryWriter = {
    val tupleWriter = writer.spawnNew()
    appendWarpPackage(v.a, tupleWriter)
    appendWarpPackage(v.b, tupleWriter)
    appendWarpPackage(v.c, tupleWriter)
    RiftWarpPrimitiveAppenders.appendExt(tupleWriter.toArray, RiftwarpTypecodes.Tuple3Code, writer)
  }

  def appendBinaryWarpPackage(v: BinaryWarpPackage, writer: BinaryWriter): BinaryWriter = {
    val size = v.bytes.length
    if (size < 256) {
      writer.writeUnsignedByte(MessagePackTypecodes.Bin8)
      writer.writeUnsignedByte(size)
    } else if (size < 256 * 256) {
      writer.writeUnsignedByte(MessagePackTypecodes.Bin16)
      writer.writeUnsignedShort(size)
    } else {
      writer.writeUnsignedByte(MessagePackTypecodes.Bin32)
      writer.writeInt(size)
    }
    writer.writeBytes(v.bytes)
  }
  
  @inline
  def appendWarpDescriptor(wd: WarpDescriptor, writer: BinaryWriter): BinaryWriter = {
    val wdWriter = writer.spawnNew()
    RiftWarpPrimitiveAppenders.appendString(wd.identifier, wdWriter)
    wd.version match {
      case Some(v) => 
        RiftWarpPrimitiveAppenders.appendInt(v, wdWriter)
      case None => 
        wdWriter.writeUnsignedByte(MessagePackTypecodes.Null)
    }
    RiftWarpPrimitiveAppenders.appendExt(wdWriter.toArray, RiftwarpTypecodes.WarpDescriptorCode, writer)
  }

  def appendWarpObject(v: WarpObject, writer: BinaryWriter): BinaryWriter = {
    val objWriter = writer.spawnNew(None)
    v.warpDescriptor match {
      case Some(wd) => appendWarpDescriptor(wd, objWriter)
      case None => objWriter.writeUnsignedByte(MessagePackTypecodes.Null)
    }
    val size = v.elements.size
    if (size < 16) {
      val c = 0x80 | size // 10000000, fixmap
      objWriter.writeUnsignedByte(c)
    } else if (size < 256 * 256) {
      objWriter.writeUnsignedByte(MessagePackTypecodes.Map16)
      objWriter.writeUnsignedShort(size)
    } else {
      objWriter.writeUnsignedByte(MessagePackTypecodes.Map32)
      objWriter.writeInt(size)
    }
    v.elements.foreach(e => appendWarpElement(e, objWriter))
    RiftWarpPrimitiveAppenders.appendExt(objWriter.toArray, RiftwarpTypecodes.ObjectCode, writer)
  }

  private def appendWarpElement(v: WarpElement, writer: BinaryWriter): BinaryWriter = {
    //writer.writeUnsignedByte(0x82) // 10000010
    RiftWarpPrimitiveAppenders.appendString(v.label, writer)
    v.value match {
      case Some(v) => appendWarpPackage(v, writer)
      case None => writer.writeUnsignedByte(MessagePackTypecodes.Null)
    }
  }

  @inline
  def appendWarpBoolean(v: WarpBoolean, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendBoolean(v.value, writer)

  @inline
  def appendWarpString(v: WarpString, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendString(v.value, writer)

  @inline
  def appendWarpByte(v: WarpByte, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendByte(v.value, writer)
  
  @inline
  def appendWarpShort(v: WarpShort, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendShort(v.value, writer)

  @inline
  def appendWarpInt(v: WarpInt, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendInt(v.value, writer)

  @inline
  def appendWarpLong(v: WarpLong, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendLong(v.value, writer)

  @inline
  def appendWarpBigInt(v: WarpBigInt, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendBigInt(v.value, writer)

  @inline
  def appendWarpFloat(v: WarpFloat, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendFloat(v.value, writer)

  @inline
  def appendWarpDouble(v: WarpDouble, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendDouble(v.value, writer)

  @inline
  def appendWarpBigDecimal(v: WarpBigDecimal, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendBigDecimal(v.value, writer)

  @inline
  def appendWarpUuid(v: WarpUuid, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendUuid(v.value, writer)

  @inline
  def appendWarpUri(v: WarpUri, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendUri(v.value, writer)

  @inline
  def appendWarpDateTime(v: WarpDateTime, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendDateTime(v.value, writer)

  @inline
  def appendWarpLocalDateTime(v: WarpLocalDateTime, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendLocalDateTime(v.value, writer)

  @inline
  def appendWarpDuration(v: WarpDuration, writer: BinaryWriter): BinaryWriter =
    RiftWarpPrimitiveAppenders.appendDuration(v.value, writer)

}