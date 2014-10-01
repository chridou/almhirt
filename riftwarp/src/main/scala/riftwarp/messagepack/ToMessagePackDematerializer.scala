package riftwarp.messagepack

import scalaz._, Scalaz._
import riftwarp._
import almhirt.io.BinaryWriter

trait ToMessagePackDematerializer extends Dematerializer[Array[Byte] @@ WarpTags.MessagePack] {
  override val channels = Set(WarpChannels.`rift-msgpack`, WarpChannels.`rift-x-msgpack`)
  
  def createBinaryWriter(): BinaryWriter

  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): Array[Byte] @@ WarpTags.MessagePack = {
    val writer = createBinaryWriter()
    WarpPackageAppenders.appendWarpPackage(what, writer)
    WarpTags.MessagePack(writer.toArray)
  }

}