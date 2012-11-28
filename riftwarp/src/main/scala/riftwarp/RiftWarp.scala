package riftwarp

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almvalidation.flatmap

trait RiftWarp {
  def barracks: RiftWarpBarracks
  def toolShed: RiftWarpToolShed

  def prepareForWarp[TDimension <: RiftDimension](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(what: AnyRef)(implicit m: Manifest[TDimension]): AlmValidation[TDimension] = {
    val typeDescriptor =
      what match {
        case htd: HasTypeDescriptor => htd.typeDescriptor
        case x => TypeDescriptor(x.getClass)
      }
    val decomposer = barracks.tryGetRawDecomposer(typeDescriptor)
    val dematerializerV = 
      toolShed.tryGetDematerializerFactory[TDimension](channel, toolGroup).map(factory =>
        factory.createDematerializer(barracks, toolShed)).validationOut
    dematerializerV.bind(dematerializer =>
    (decomposer, dematerializer) match {
      case (Some(dec), Some(dem)) =>
        dec.decomposeRaw(what)(dem).bind(funnel =>
          almCast[RawDematerializer](funnel).bind(demat =>
            demat.dematerializeRaw.map(_.asInstanceOf[TDimension])))
      case (None, Some(_)) => UnspecifiedProblem("No decomposer found for type '%s'".format(typeDescriptor)).failure
      case (Some(_), None) => UnspecifiedProblem("No dematerializer found for warping through '%s' into a '%s'".format(channel, m.erasure.getName())).failure
      case (None, None) => UnspecifiedProblem("No decomposer found for type '%s' and no dematerializer found for warping through '%s' into a '%s'".format(typeDescriptor, channel, m.erasure.getName())).failure
    })
  }

  def receiveFromWarp[TDimension <: RiftDimension, T <: AnyRef](channel: RiftChannel, toolGroup: Option[ToolGroup] = None)(warpStream: TDimension)(implicit mtarget: Manifest[T], mD: Manifest[TDimension]): AlmValidation[T] = {
    implicit val hasRecomposers = barracks
    implicit val hasRecomposersForKKTs = toolShed
    toolShed.tryGetRematerializationArray[TDimension](warpStream)(channel).bind {
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
        UnspecifiedProblem("No rematerialization array found for '%s' and from dimension '%s'".format(channel, mD.erasure.getName)).failure
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
    riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToMapDematerializer)
    riftWarp.toolShed.addDematerializerFactory(impl.dematerializers.ToJsonCordDematerializer)

    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromMapRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonMapRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonStringRematerializationArray)
    riftWarp.toolShed.addArrayFactory(impl.rematerializers.FromJsonCordRematerializationArray)

    import riftwarp.impl.dematerializers.simplema._
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

    import riftwarp.ma._
    riftWarp.toolShed.addMAFunctions(RegisterableToMADimensionFunctors.listFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableToMADimensionFunctors.vectorFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableToMADimensionFunctors.setFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableToMADimensionFunctors.iterableFunctionObject)
    riftWarp.toolShed.addMAFunctions(RegisterableToMADimensionFunctors.treeFunctionObject)
    
    riftWarp.toolShed.addChannelFolder(JsonCordFolder)
    
    import riftwarp.impl.rematerializers.simplema._
    import almhirt.almvalidation.funs._
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, String, DimensionListAny, String](RiftJson()) { def cToA(c: String) = c.success; def createMA(la: List[String]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, Boolean, DimensionListAny, Boolean](RiftJson()) { def cToA(c: Boolean) = c.success;  def createMA(la: List[Boolean]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, Byte, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toByte.success;  def createMA(la: List[Byte]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, Int, DimensionListAny, Double](RiftJson()){ def cToA(c: Double) = c.toInt.success;  def createMA(la: List[Int]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, Long, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toLong.success;  def createMA(la: List[Long]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, BigInt, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseBigIntAlm(c);  def createMA(la: List[BigInt]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, Float, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toFloat.success;  def createMA(la: List[Float]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, Double, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.success;  def createMA(la: List[Double]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, BigDecimal, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseDecimalAlm(c);  def createMA(la: List[BigDecimal]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, org.joda.time.DateTime, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseDateTimeAlm(c);  def createMA(la: List[org.joda.time.DateTime]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, _root_.java.util.UUID, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseUuidAlm(c);  def createMA(la: List[_root_.java.util.UUID]) = Iterable(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Iterable, scala.xml.Node, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseXmlAlm(c);  def createMA(la: List[scala.xml.Node]) = Iterable(la: _*) })

    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, String, DimensionListAny, String](RiftJson()) { def cToA(c: String) = c.success; def createMA(la: List[String]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, Boolean, DimensionListAny, Boolean](RiftJson()) { def cToA(c: Boolean) = c.success;  def createMA(la: List[Boolean]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, Byte, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toByte.success;  def createMA(la: List[Byte]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, Int, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toInt.success;  def createMA(la: List[Int]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, Long, DimensionListAny, Double](RiftJson()){ def cToA(c: Double) = c.toLong.success;  def createMA(la: List[Long]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, BigInt, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseBigIntAlm(c);  def createMA(la: List[BigInt]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, Float, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toFloat.success;  def createMA(la: List[Float]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, Double, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.success;  def createMA(la: List[Double]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, BigDecimal, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseDecimalAlm(c);  def createMA(la: List[BigDecimal]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, org.joda.time.DateTime, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseDateTimeAlm(c);  def createMA(la: List[org.joda.time.DateTime]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, _root_.java.util.UUID, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseUuidAlm(c);  def createMA(la: List[_root_.java.util.UUID]) = la })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[List, scala.xml.Node, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseXmlAlm(c);  def createMA(la: List[scala.xml.Node]) = la })

    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, String, DimensionListAny, String](RiftJson()) { def cToA(c: String) = c.success; def createMA(la: List[String]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, Boolean, DimensionListAny, Boolean](RiftJson()) { def cToA(c: Boolean) = c.success;  def createMA(la: List[Boolean]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, Byte, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toByte.success;  def createMA(la: List[Byte]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, Int, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toInt.success;  def createMA(la: List[Int]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, Long, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toLong.success;  def createMA(la: List[Long]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, BigInt, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseBigIntAlm(c);  def createMA(la: List[BigInt]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, Float, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toFloat.success;  def createMA(la: List[Float]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, Double, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.success;  def createMA(la: List[Double]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, BigDecimal, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseDecimalAlm(c);  def createMA(la: List[BigDecimal]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, org.joda.time.DateTime, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseDateTimeAlm(c);  def createMA(la: List[org.joda.time.DateTime]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, _root_.java.util.UUID, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseUuidAlm(c);  def createMA(la: List[_root_.java.util.UUID]) = Vector(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Vector, scala.xml.Node, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseXmlAlm(c);  def createMA(la: List[scala.xml.Node]) = Vector(la: _*) })

    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, String, DimensionListAny, String](RiftJson()) { def cToA(c: String) = c.success; def createMA(la: List[String]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, Boolean, DimensionListAny, Boolean](RiftJson()) { def cToA(c: Boolean) = c.success;  def createMA(la: List[Boolean]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, Byte, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toByte.success;  def createMA(la: List[Byte]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, Int, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toInt.success;  def createMA(la: List[Int]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, Long, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toLong.success;  def createMA(la: List[Long]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, BigInt, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseBigIntAlm(c);  def createMA(la: List[BigInt]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, Float, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.toFloat.success;  def createMA(la: List[Float]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, Double, DimensionListAny, Double](RiftJson()) { def cToA(c: Double) = c.success;  def createMA(la: List[Double]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, BigDecimal, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseDecimalAlm(c);  def createMA(la: List[BigDecimal]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, org.joda.time.DateTime, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseDateTimeAlm(c);  def createMA(la: List[org.joda.time.DateTime]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, _root_.java.util.UUID, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseUuidAlm(c);  def createMA(la: List[_root_.java.util.UUID]) = Set(la: _*) })
    riftWarp.toolShed.addCanRematerializePrimitiveMA(new CanRematerializeListAny[Set, scala.xml.Node, DimensionListAny, String](RiftJson()) { def cToA(c: String) = parseXmlAlm(c);  def createMA(la: List[scala.xml.Node]) = Set(la: _*) })
  }
}