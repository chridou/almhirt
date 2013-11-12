package riftwarp.messagepack

import scalaz._, Scalaz._
import almhirt.common._
import riftwarp._
import almhirt.io.BinaryReader

trait FromMessagePackByteArrayRematerializer extends Rematerializer[Array[Byte] @@ WarpTags.MessagePack] {
  override val channel = WarpChannels.`rift-msgpack`
  def rematerialize(what: Array[Byte] @@ WarpTags.MessagePack, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] = {
    val reader = BinaryReader(what)
    MessagePackParser.parse(reader)
  }
}