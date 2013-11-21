package riftwarp

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.serialization._
import riftwarp.std.RiftWarpFuns

trait RiftWarp {
  private object myFuns extends RiftWarpFuns

  def packers: WarpPackers
  def unpackers: WarpUnpackers
  def dematerializers: Dematerializers
  def rematerializers: Rematerializers

  def departure(channel: String, what: Any, options: Map[String, Any] = Map.empty): AlmValidation[(Any, WarpDescriptor)] =
    for {
      packer <- packers.getFor(what, None, None)
      packed <- packer.packBlind(what)(packers)
      dematerialize <- dematerializers.get(channel)
    } yield (dematerialize(packed, options), packer.warpDescriptor)

  def arrival(channel: String, from: Any, options: Map[String, Any] = Map.empty): AlmValidation[Any] =
    for {
      rematerializer <- rematerializers.getTyped[Any](channel)
      arrived <- myFuns.handleFreeArrivalWith(from, rematerializer, None, None, options)(unpackers)
    } yield arrived
}

object RiftWarp {
  def apply(): RiftWarp = apply(WarpPackers(), WarpUnpackers(), Dematerializers(), Rematerializers())
  def apply(thePackers: WarpPackers, theUnpackers: WarpUnpackers): RiftWarp = apply(thePackers, theUnpackers, Dematerializers(), Rematerializers())
  def apply(thePackers: WarpPackers, theUnpackers: WarpUnpackers, theDematerializers: Dematerializers, theRematerializers: Rematerializers): RiftWarp = new RiftWarp {
    val packers = thePackers
    val unpackers = theUnpackers
    val dematerializers = theDematerializers
    val rematerializers = theRematerializers
  }

  def empty: RiftWarp = new RiftWarp {
    val packers = WarpPackers.empty
    val unpackers = WarpUnpackers.empty
    val dematerializers = Dematerializers.empty
    val rematerializers = Rematerializers.empty
  }
}