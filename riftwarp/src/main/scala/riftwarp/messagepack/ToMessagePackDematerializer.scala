package riftwarp.messagepack

import scalaz._, Scalaz._
import riftwarp._
import almhirt.io.BinaryWriter

trait ToMessagePackDematerializer extends Dematerializer[Array[Byte] @@ WarpTags.MessagePack] {
  override val channel = "messagepack"
  override val dimension = classOf[Array[Byte]].getName()

  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): Array[Byte] @@ WarpTags.MessagePack = {
    val writer = BinaryWriter(1024*32)
    WarpPackageAppenders.appendWarpPackage(what, writer)
    WarpTags.MessagePack(writer.toArray)
  }

}