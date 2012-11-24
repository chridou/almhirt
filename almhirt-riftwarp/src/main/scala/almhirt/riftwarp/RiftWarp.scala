package almhirt.riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.funs._
import almhirt.almvalidation.flatmap

trait RiftWarp {
  def barracks: RiftWarpBarracks
  def toolShed: RiftWarpToolShed

  def prepareForWarp[TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](what: AnyRef)(implicit m: Manifest[To], n: Manifest[TChannel]): AlmValidation[To] = {
    val typeDescriptor =
      what match {
        case htd: HasTypeDescriptor => htd.typeDescriptor
        case x => TypeDescriptor(x.getClass)
      }
    val decomposer = barracks.tryGetRawDecomposer(typeDescriptor)
    val dematerializer = toolShed.tryGetDematerializer[TChannel, To]
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

  def receiveFromWarp[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor, T <: AnyRef](warpStream: TDimension)(implicit mtarget: Manifest[T], mD: Manifest[TDimension], mC: Manifest[TChannel]): AlmValidation[T] = {
    implicit val hasRecomposers = barracks
    toolShed.tryGetRematerializationArray(warpStream).bind {
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
        UnspecifiedProblem("No rematerialization array found for '%s' and from dimension '%s'".format(mC.erasure.getName(), mD.erasure.getName)).failure
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
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Iterable, String]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Boolean]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Byte]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Int]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Long]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Iterable, BigInt]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Float]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Iterable, Double]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Iterable, BigDecimal]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Iterable, org.joda.time.DateTime]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Iterable, _root_.java.util.UUID]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Iterable, scala.xml.Node]() {})

    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[List, String]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Boolean]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Byte]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Int]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Long]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[List, BigInt]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Float]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[List, Double]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[List, BigDecimal]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[List, org.joda.time.DateTime]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[List, _root_.java.util.UUID]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[List, scala.xml.Node]() {})

    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Vector, String]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Boolean]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Byte]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Int]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Long]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Vector, BigInt]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Float]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Vector, Double]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Vector, BigDecimal]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Vector, org.joda.time.DateTime]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Vector, _root_.java.util.UUID]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Vector, scala.xml.Node]() {})

    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Set, String]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Boolean]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Byte]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Int]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Long]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Set, BigInt]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Float]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonValueByToString[Set, Double]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Set, BigDecimal]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Set, org.joda.time.DateTime]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonString[Set, _root_.java.util.UUID]() {})
    riftWarp.toolShed.addCanDematerializePrimitiveMA(new CanDematerializePrimitiveMAToJsonStringLaundered[Set, scala.xml.Node]() {})

    import almhirt.riftwarp.impl.rematerializers.simplema._
    import almhirt.almvalidation.funs._
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, String, String] { def cToA(c: String) = c.success; def createM(la: List[String]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, Boolean, Boolean] { def cToA(c: Boolean) = c.success;  def createM(la: List[Boolean]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, Byte, Double] { def cToA(c: Double) = c.toByte.success;  def createM(la: List[Byte]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, Int, Double] { def cToA(c: Double) = c.toInt.success;  def createM(la: List[Int]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, Long, Double] { def cToA(c: String) = c.toLong.success;  def createM(la: List[Long]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, BigInt, String] { def cToA(c: String) = parseBigIntAlm(c);  def createM(la: List[BigInt]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, Float, Double] { def cToA(c: Double) = c.toFloat.success;  def createM(la: List[Float]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, Double, Double] { def cToA(c: Double) = c.success;  def createM(la: List[Double]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, BigDecimal, String] { def cToA(c: String) = parseDecimalAlm(c);  def createM(la: List[BigDecimal]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, org.joda.time.DateTime, String] { def cToA(c: String) = parseDateTimeAlm(c);  def createM(la: List[org.joda.time.DateTime]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, _root_.java.util.UUID, String] { def cToA(c: String) = parseUuidAlm(c);  def createM(la: List[_root_.java.util.UUID]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Iterable, scala.xml.Node, String] { def cToA(c: String) = parseXmlAlm(c);  def createM(la: List[scala.xml.Node]) = Iterable(la: _*) })

    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, String, String] { def cToA(c: String) = c.success; def createM(la: List[String]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, Boolean, Boolean] { def cToA(c: Boolean) = c.success;  def createM(la: List[Boolean]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, Byte, Double] { def cToA(c: Double) = c.toByte.success;  def createM(la: List[Byte]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, Int, Double] { def cToA(c: Double) = c.toInt.success;  def createM(la: List[Int]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, Long, Double] { def cToA(c: String) = c.toLong.success;  def createM(la: List[Long]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, BigInt, String] { def cToA(c: String) = parseBigIntAlm(c);  def createM(la: List[BigInt]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, Float, Double] { def cToA(c: Double) = c.toFloat.success;  def createM(la: List[Float]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, Double, Double] { def cToA(c: Double) = c.success;  def createM(la: List[Double]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, BigDecimal, String] { def cToA(c: String) = parseDecimalAlm(c);  def createM(la: List[BigDecimal]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, org.joda.time.DateTime, String] { def cToA(c: String) = parseDateTimeAlm(c);  def createM(la: List[org.joda.time.DateTime]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, _root_.java.util.UUID, String] { def cToA(c: String) = parseUuidAlm(c);  def createM(la: List[_root_.java.util.UUID]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[List, scala.xml.Node, String] { def cToA(c: String) = parseXmlAlm(c);  def createM(la: List[scala.xml.Node]) = la })

    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, String, String] { def cToA(c: String) = c.success; def createM(la: List[String]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, Boolean, Boolean] { def cToA(c: Boolean) = c.success;  def createM(la: List[Boolean]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, Byte, Double] { def cToA(c: Double) = c.toByte.success;  def createM(la: List[Byte]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, Int, Double] { def cToA(c: Double) = c.toInt.success;  def createM(la: List[Int]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, Long, Double] { def cToA(c: String) = c.toLong.success;  def createM(la: List[Long]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, BigInt, String] { def cToA(c: String) = parseBigIntAlm(c);  def createM(la: List[BigInt]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, Float, Double] { def cToA(c: Double) = c.toFloat.success;  def createM(la: List[Float]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, Double, Double] { def cToA(c: Double) = c.success;  def createM(la: List[Double]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, BigDecimal, String] { def cToA(c: String) = parseDecimalAlm(c);  def createM(la: List[BigDecimal]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, org.joda.time.DateTime, String] { def cToA(c: String) = parseDateTimeAlm(c);  def createM(la: List[org.joda.time.DateTime]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, _root_.java.util.UUID, String] { def cToA(c: String) = parseUuidAlm(c);  def createM(la: List[_root_.java.util.UUID]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Vector, scala.xml.Node, String] { def cToA(c: String) = parseXmlAlm(c);  def createM(la: List[scala.xml.Node]) = Vector(la: _*) })

    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, String, String] { def cToA(c: String) = c.success; def createM(la: List[String]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, Boolean, Boolean] { def cToA(c: Boolean) = c.success;  def createM(la: List[Boolean]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, Byte, Double] { def cToA(c: Double) = c.toByte.success;  def createM(la: List[Byte]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, Int, Double] { def cToA(c: Double) = c.toInt.success;  def createM(la: List[Int]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, Long, Double] { def cToA(c: String) = c.toLong.success;  def createM(la: List[Long]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, BigInt, String] { def cToA(c: String) = parseBigIntAlm(c);  def createM(la: List[BigInt]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, Float, Double] { def cToA(c: Double) = c.toFloat.success;  def createM(la: List[Float]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, Double, Double] { def cToA(c: Double) = c.success;  def createM(la: List[Double]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, BigDecimal, String] { def cToA(c: String) = parseDecimalAlm(c);  def createM(la: List[BigDecimal]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, org.joda.time.DateTime, String] { def cToA(c: String) = parseDateTimeAlm(c);  def createM(la: List[org.joda.time.DateTime]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, _root_.java.util.UUID, String] { def cToA(c: String) = parseUuidAlm(c);  def createM(la: List[_root_.java.util.UUID]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeFromJsonStdList[Set, scala.xml.Node, String] { def cToA(c: String) = parseXmlAlm(c);  def createM(la: List[scala.xml.Node]) = Set(la: _*) })
  }
}