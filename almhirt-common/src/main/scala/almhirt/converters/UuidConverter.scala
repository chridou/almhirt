package almhirt.converters

import java.util.{ UUID => JUUID }
import java.nio.ByteBuffer

object UuidConverter {
  import java.nio.ByteBuffer

  def uuidToBytes(uuid: JUUID): Array[Byte] = {
    val bytes = new Array[Byte](16)
    val longBuffer = ByteBuffer.wrap(bytes).asLongBuffer
    longBuffer.put(uuid.getMostSignificantBits).put(uuid.getLeastSignificantBits)
    bytes
  }

  def bytesToUuid(a: Array[Byte]): JUUID = {
    val longBuffer = ByteBuffer.wrap(a).asLongBuffer
    new JUUID(longBuffer.get, longBuffer.get)
  }
}