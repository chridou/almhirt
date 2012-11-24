package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.almvalidation.flatmap

trait RiftWarp {
  def barracks: RiftWarpBarracks
  def toolShed: RiftWarpToolShed

  def prepareForWarp[TChannel <: RiftChannelDescriptor,To <: RiftTypedDimension[_]](what: AnyRef)(implicit m: Manifest[To], n: Manifest[TChannel]): AlmValidation[To] = {
    val typeDescriptor =
      what match {
        case htd: HasTypeDescriptor => htd.typeDescriptor
        case x => TypeDescriptor(x.getClass)
      }
    val decomposer = barracks.tryGetRawDecomposer(typeDescriptor)
    val dematerializer = toolShed.tryGetDematerializer[TChannel,To]
    (decomposer, dematerializer) match {
      case (Some(dec), Some(dem)) =>
        dec.decomposeRaw(what)(dem).bind(funnel =>
          almCast[RawDematerializer](funnel).bind(demat =>
            demat.dematerializeRaw.map(_.asInstanceOf[To])))
      case (None, Some(_)) => UnspecifiedProblem("No decomposer found for type '%s'".format(typeDescriptor)).failure
      case (Some(_), None) => UnspecifiedProblem("No dematerializer found for warping through '%s' into a '%s'".format(n.erasure.getName(), m.erasure.getName())).failure
      case (None, None) => UnspecifiedProblem("No decomposer found for type '%s' and no dematerializer found for warping through '%s' into a '%s'".format(typeDescriptor, n.erasure.getName(), m.erasure.getName())).failure
    }
  }

  def receiveFromWarp[From <: RiftTypedDimension[_], T <: AnyRef](warpType: RiftDescriptor)(warpStream: From)(implicit mtarget: Manifest[T], mfrom: Manifest[From]): AlmValidation[T] = {
	implicit val hasRecomposers = barracks
    toolShed.tryGetRematerializationArray(warpType, warpStream).bind {
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
        UnspecifiedProblem("No rematerialization array found for '%s' and source '%s'".format(warpType, mfrom.erasure.getName)).failure
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
    riftWarp.toolShed.addDematerializer(impl.dematerializers.ToJsonCordDematerializer()(riftWarp.barracks, riftWarp.toolShed))

    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromMapRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonMapRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonStringRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonCordRematerializationArray)
    
    import almhirt.riftwarp.impl.dematerializers.simplema._
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Iterable, String](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Boolean](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Byte](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Int](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Long](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, BigInt](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Float](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Double](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Iterable, BigDecimal](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Iterable, org.joda.time.DateTime](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Iterable, _root_.java.util.UUID](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Iterable, scala.xml.Node](){})
    
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[List, String](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Boolean](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Byte](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Int](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Long](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, BigInt](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Float](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Double](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[List, BigDecimal](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[List, org.joda.time.DateTime](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[List, _root_.java.util.UUID](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[List, scala.xml.Node](){})
    
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Vector, String](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Boolean](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Byte](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Int](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Long](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, BigInt](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Float](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Double](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Vector, BigDecimal](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Vector, org.joda.time.DateTime](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Vector, _root_.java.util.UUID](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Vector, scala.xml.Node](){})
    
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Set, String](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Boolean](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Byte](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Int](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Long](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, BigInt](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Float](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Double](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Set, BigDecimal](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Set, org.joda.time.DateTime](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Set, _root_.java.util.UUID](){})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Set, scala.xml.Node](){})
  }
}