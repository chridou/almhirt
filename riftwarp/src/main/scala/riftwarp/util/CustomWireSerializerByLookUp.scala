package riftwarp.util

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp._
import riftwarp.std.RiftWarpFuns
import scala.reflect.ClassTag

trait CustomWireSerializerByLookUp[T] extends CustomWireSerializerTemplate[T] with WireSerializer[T] with RiftWarpFuns { self: HasRiftWarp =>
  def tag: ClassTag[TT]

  override protected def getDematerializer(channel: WarpChannel): AlmValidation[Dematerializer[Any]] = myRiftWarp.dematerializers.get(channel.channelDescriptor)
  override protected def getStringRematerializer(channel: String): AlmValidation[Rematerializer[String]] = myRiftWarp.rematerializers.getTyped[String](channel)
  override protected def getBinaryRematerializer(channel: String): AlmValidation[Rematerializer[Array[Byte]]] = myRiftWarp.rematerializers.getTyped[Array[Byte]](channel)
  
  
  protected def packInner(what: TT): AlmValidation[WarpPackage] =
    for {
      packer <- myRiftWarp.packers.getFor(what, None, None)
      packed <- packer.packBlind(what)(myRiftWarp.packers)
    } yield packed

  protected def unpackInner(what: WarpPackage): AlmValidation[TT] =
    for {
      unpacked <- unpack(what, None, None)(myRiftWarp.unpackers)
      casted <- unpacked.castTo[TT](tag)
    } yield casted
}
