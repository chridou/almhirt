package riftwarp.messagepack

import scalaz.syntax.validation._
import scalaz._
import almhirt.common._
import riftwarp._
import almhirt.io.BinaryReader

trait FromMessagePackByteArrayRematerializer extends Rematerializer[Array[Byte] @@ WarpTags.MessagePack] {
  override val channels = Set(WarpChannels.`rift-msgpack`, WarpChannels.`rift-x-msgpack`)
  def rematerialize(what: Array[Byte] @@ WarpTags.MessagePack, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] = {
    val reader = BinaryReader(Tag.unwrap(what))
    MessagePackParser.parse(reader)
  }
}