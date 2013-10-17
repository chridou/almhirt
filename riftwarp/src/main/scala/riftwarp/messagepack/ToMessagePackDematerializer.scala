//package riftwarp.messagepack
//
//trait ToMessagePackDematerializer extends Dematerializer[Array[Byte] @@ WarpTags.MessagePack] {
//  override val channel = "messagepack"
//  override val dimension = classOf[Array[Byte]].getName()
//
//  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): Array[Byte] @@ WarpTags.MessagePack = {
//    val messagePack = new MessagePack()
//    val out = new ByteArrayOutputStream()
//    val packer = messagePack.createPacker(out)
//    transform(what, packer)
//    WarpTags.MessagePack(out.toByteArray())
//  }
//
//  def transform(what: WarpPackage, packer: Packer) {
//    what match {
//      case prim: WarpPrimitive =>
//        addPrimitiveRepr(prim, packer)
//      case obj: WarpObject =>
//        addObjectRepr(obj, packer: Packer)
//      //      case WarpCollection(items) =>
//      //        foldReprs(items.map(transform))
//      //      case WarpAssociativeCollection(items) =>
//      //        foldAssocRepr(items.map(item => (transform(item._1), transform(item._2))))
//      //      case WarpTree(tree) =>
//      //        foldTreeRepr(tree.map(transform))
//      //      case WarpTuple2(a, b) =>
//      //        foldTuple2Reprs((transform(a), transform(b)))
//      //      case WarpTuple3(a, b, c) =>
//      //        foldTuple3Reprs((transform(a), transform(b), transform(c)))
//      //      case WarpBytes(bytes) =>
//      //        foldByteArrayRepr(bytes)
//      //      case WarpBlob(bytes) =>
//      //        foldBlobRepr(bytes)
//    }
//  }
//
//  private def addPrimitiveRepr(prim: WarpPrimitive, packer: Packer) {
//    prim match {
//      case WarpBoolean(value) => packer.write(value)
//      case WarpString(value) => packer.write(value)
//      case WarpByte(value) => packer.write(value)
//      case WarpInt(value) => packer.write(value)
//      case WarpLong(value) => packer.write(value)
//      case WarpBigInt(value) => packer.write(value)
//      case WarpFloat(value) => packer.write(value)
//      case WarpDouble(value) => packer.write(value)
//      case WarpBigDecimal(value) => packer.write(value.toString())
//      case WarpUuid(value) => packer.write(BinaryConverter.uuidToBytes(value))
//      case WarpUri(value) => packer.write(value.toString())
//      case WarpDateTime(value) => packer.write(value.toString())
//      case WarpLocalDateTime(value) => packer.write(value.toString())
//      case WarpDuration(value) => packer.write(value.toString())
//    }
//  }
//
//  private def addObjectRepr(warpObject: WarpObject, packer: Packer) {
//    val theMap = new java.util.HashMap[String, Any]
//     warpObject.warpDescriptor.foreach(wd => theMap.put(WarpDescriptor.defaultKey, wd.toParsableString(";")))
////    warpObject.elements.foreach{ case WarpElement(label, value) =>
////    	value.foreach(v => theMap.put(label, transform(v, )))
//    }
//
//}