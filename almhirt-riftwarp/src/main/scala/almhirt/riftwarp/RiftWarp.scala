package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.almvalidation.flatmap

trait RiftWarp {
  def barracks: RiftWarpBarracks
  def toolShed: RiftWarpToolShed

  def prepareForWarp[To <: AnyRef](channel: RiftChannel)(what: AnyRef)(implicit m: Manifest[To]): AlmValidation[To] = {
    val typeDescriptor =
      what match {
        case htd: HasTypeDescriptor => htd.typeDescriptor
        case x => TypeDescriptor(x.getClass)
      }
    val decomposer = barracks.tryGetRawDecomposer(typeDescriptor)
    val dematerializer = toolShed.tryGetDematerializer[To](channel)
    (decomposer, dematerializer) match {
      case (Some(dec), Some(dem)) =>
        dec.decomposeRaw(what)(dem).bind(funnel =>
          almCast[RawDematerializer](funnel).bind(demat =>
            demat.dematerializeRaw.map(_.asInstanceOf[To])))
      case (None, Some(_)) => UnspecifiedProblem("No decomposer found for '%s'".format(typeDescriptor)).failure
      case (Some(_), None) => UnspecifiedProblem("No dematerializer found channel '%s'".format(channel)).failure
      case (None, None) => UnspecifiedProblem("No decomposer found for '%s' and no dematerializer found channel '%s'".format(typeDescriptor, channel)).failure
    }
  }

  def receiveFromWarp[From <: AnyRef, T <: AnyRef](channel: RiftChannel)(warpStream: From)(implicit mtarget: Manifest[T], mfrom: Manifest[From]): AlmValidation[T] = {
	implicit val hasRecomposers = barracks
    toolShed.tryGetRematerializationArray(channel, warpStream) match {
      case Some(array) =>
        array.tryGetTypeDescriptor.bind { descFromArray =>
          val typeDescriptor = descFromArray.getOrElse(TypeDescriptor(mtarget.erasure))
          barracks.tryGetRawRecomposer(typeDescriptor) match {
            case Some(recomposer) =>
              recomposer.recomposeRaw(array).bind(almCast[T](_))
            case None =>
              UnspecifiedProblem("No recomposer found for '%s'".format(typeDescriptor)).failure
          }
        }
      case None =>
        UnspecifiedProblem("No rematerialization array found for channel '%s' and source '%s'".format(channel, mfrom.erasure.getName)).failure
    }
  }
}

object RiftWarp {
  def apply(theBarracks: RiftWarpBarracks, theToolShed: RiftWarpToolShed): RiftWarp =
    new RiftWarp {
      val barracks = theBarracks
      val toolShed = theToolShed
    }

  def unsafe(): RiftWarp = apply(RiftWarpBarracks.unsafe, RiftWarpToolShed.unsafe)
  def unsafeWithDefaults(): RiftWarp = {
    val riftWarp = apply(RiftWarpBarracks.unsafe, RiftWarpToolShed.unsafe)
    initializeWithDefaults(riftWarp)
    riftWarp
  }

  private def initializeWithDefaults(riftWarp: RiftWarp) {
    riftWarp.toolShed.addDematerializer(impl.dematerializers.ToMapDematerializer(Map.empty)(riftWarp.barracks))

    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromMapRematerializationArray)
  }
}