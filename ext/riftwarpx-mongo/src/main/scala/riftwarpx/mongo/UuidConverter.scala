package riftwarpx.mongo

import java.util.{ UUID => JUUID }

object UuidConverter {
  //  import java.nio.ByteBuffer

  def uuidToBytes(uuid: JUUID): Array[Byte] = {
    //    val bytes = new Array[Byte](16)
    //    val longBuffer = ByteBuffer.wrap(bytes).asLongBuffer
    //    longBuffer.put(u.getMostSignificantBits).put(u.getLeastSignificantBits)
    //    bytes
    ???
  }

  def bytesToUuid(a: Array[Byte]): JUUID = {
    ???
    //    val longBuffer = ByteBuffer.wrap(a).asLongBuffer
    //    new UUID(longBuffer.get, longBuffer.get)
  }
}