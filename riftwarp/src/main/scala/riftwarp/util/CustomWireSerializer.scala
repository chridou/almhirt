package riftwarp.util

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.http.{ HttpSerializer, HttpDeserializer }
import riftwarp._

trait CustomHttpSerializer[T] extends CustomHttpSerializerTemplate[T] {
  
  def packer: AlmValidation[WarpPacker[TT]]
  def unpacker: AlmValidation[WarpUnpacker[TT]]

  def packers: WarpPackers
  def unpackers: WarpUnpackers

  def dematerializers: Dematerializers
  def rematerializers: Rematerializers

  override protected def getDematerializer(channel: WarpChannel): AlmValidation[Dematerializer[Any]] = dematerializers.get(channel.channelDescriptor)
  override protected def getStringRematerializer(channel: String): AlmValidation[Rematerializer[String]] = rematerializers.getTyped[String](channel)
  override protected def getBinaryRematerializer(channel: String): AlmValidation[Rematerializer[Array[Byte]]] = rematerializers.getTyped[Array[Byte]](channel)
  
  override protected def packInner(what: TT): AlmValidation[WarpPackage] =
    packer.flatMap(_(what)(packers))

  override protected def unpackInner(what: WarpPackage): AlmValidation[TT] =
    unpacker.flatMap(_(what)(unpackers))
}

trait FlatHttpSerializer[T] { self : CustomHttpSerializer[T] ⇒
  def packers: WarpPackers = WarpPackers.empty
  def unpackers: WarpUnpackers = WarpUnpackers.empty
}

trait RiftWarpHttpSerializer[T] { self : CustomHttpSerializer[T] ⇒
  def riftwarp: RiftWarp

  override lazy val packers: WarpPackers = riftwarp.packers
  override lazy val unpackers: WarpUnpackers = riftwarp.unpackers

  override lazy val dematerializers: Dematerializers = riftwarp.dematerializers
  override lazy val rematerializers: Rematerializers = riftwarp.rematerializers
  
}
