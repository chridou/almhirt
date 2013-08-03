package riftwarp

import scalaz._, Scalaz._
import scalaz.Tree._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.std.WarpPrimitiveConverter

sealed trait WarpPackage {
  def asWarpObject: AlmValidation[WarpObject] =
    this match {
      case wo: WarpObject => wo.success
      case _ => UnspecifiedProblem("No WarpObject").failure
    }
}

final case class WarpElement(label: String, value: Option[WarpPackage])

sealed trait WarpPrimitive extends WarpPackage {
  def as[T](implicit conv: WarpPrimitiveConverter[T]): AlmValidation[T] = conv.convert(this)
  def value: Any
}
final case class WarpBoolean(override val value: Boolean) extends WarpPrimitive
final case class WarpString(override val value: String) extends WarpPrimitive
final case class WarpByte(override val value: Byte) extends WarpPrimitive
final case class WarpInt(override val value: Int) extends WarpPrimitive
final case class WarpLong(override val value: Long) extends WarpPrimitive
final case class WarpBigInt(override val value: BigInt) extends WarpPrimitive
final case class WarpFloat(override val value: Float) extends WarpPrimitive
final case class WarpDouble(override val value: Double) extends WarpPrimitive
final case class WarpBigDecimal(override val value: BigDecimal) extends WarpPrimitive
final case class WarpUuid(override val value: java.util.UUID) extends WarpPrimitive
final case class WarpUri(override val value: java.net.URI) extends WarpPrimitive
final case class WarpDateTime(override val value: org.joda.time.DateTime) extends WarpPrimitive
final case class WarpLocalDateTime(override val value: org.joda.time.LocalDateTime) extends WarpPrimitive
final case class WarpDuration(override val value: scala.concurrent.duration.FiniteDuration) extends WarpPrimitive

final case class WarpObject(warpDescriptor: Option[WarpDescriptor], elements: Vector[WarpElement]) extends WarpPackage {
  def getWarpDescriptor: AlmValidation[WarpDescriptor] =
    warpDescriptor match {
      case Some(rd) => rd.success
      case None => NoSuchElementProblem("Object has no WarpDescriptor").failure
    }
}

final case class WarpCollection(items: Vector[WarpPackage]) extends WarpPackage
final case class WarpAssociativeCollection(items: Vector[(WarpPackage, WarpPackage)]) extends WarpPackage
final case class WarpTree(tree: scalaz.Tree[WarpPackage]) extends WarpPackage
final case class WarpTuple2(a: WarpPackage, b:WarpPackage) extends WarpPackage
final case class WarpTuple3(a: WarpPackage, b:WarpPackage, c: WarpPackage) extends WarpPackage

sealed trait BinaryWarpPackage extends WarpPackage { def bytes: IndexedSeq[Byte] }
final case class WarpBytes(override val bytes: IndexedSeq[Byte]) extends BinaryWarpPackage
final case class WarpBlob(override val bytes: IndexedSeq[Byte]) extends BinaryWarpPackage

object WarpBigInt {
  def apply(strRepr: String): WarpBigInt = WarpBigInt(BigInt(strRepr))
}

object WarpBigDecimal {
  def apply(strRepr: String): WarpBigDecimal = WarpBigDecimal(BigDecimal(strRepr))
}

object WarpElement {
  def apply(label: String): WarpElement = WarpElement(label, None)
}

object WarpObject {
  def apply(): WarpObject = WarpObject(None, Vector.empty)
  def apply(elements: Vector[WarpElement]): WarpObject = WarpObject(None, elements)
  def apply(warpDescriptor: WarpDescriptor): WarpObject = WarpObject(Some(warpDescriptor), Vector.empty)
  def apply(warpDescriptor: WarpDescriptor, elements: Vector[WarpElement]): WarpObject = WarpObject(Some(warpDescriptor), elements)
}

object WarpCollection {
  def apply(): WarpCollection = WarpCollection(Vector.empty)

  def tree(wc: WarpCollection): AlmValidation[Tree[WarpPackage]] =
    wc match {
      case WarpCollection(Vector(l, WarpCollection(subForest))) =>
        val sfV = subForest.map {
          case aTree: WarpCollection => tree(aTree).toAgg
          case _ => ???
        }
        sfV.sequence.map(sf => l.node(sf: _*))
      case x => UnspecifiedProblem(s""""${x.toString()}" can not be converted to a tree element""").failure
    }

  implicit class WarpCollectionOps(self: WarpCollection) {
    def associative: AlmValidation[WarpAssociativeCollection] =
      self.items.map(item =>
        (item match {
          case WarpCollection(innerItems) =>
            if (innerItems.length == 2)
              (innerItems(0), innerItems(1)).success
            else
              UnspecifiedProblem("An inner item must have a length of to, when tranforming a WarpCollection to a WarpAssociativeCollection").failure
          case _ => UnspecifiedProblem("An inner item must be a WarpCollection of WarpCollections in order to transform a WarpCollection to a WarpAssociativeCollection").failure
        }).toAgg).sequence.map(WarpAssociativeCollection(_))

    def warpTree: AlmValidation[WarpTree] =
      WarpCollection.tree(self).map(x => WarpTree(x))
  }
}

object WarpAssociativeCollection {
  def apply(): WarpAssociativeCollection = WarpAssociativeCollection(Vector.empty)
}

object WarpPackage {
  implicit class WarpPackageOps(self: WarpPackage) {
    def dematerialize[To](implicit dematerializer: Dematerializer[To]): To =
      dematerializer.dematerialize(self)
    def unpack[T](implicit unpacker: WarpUnpacker[T], unpackers: WarpUnpackers): AlmValidation[T] = unpacker.unpack(self)
    def unpackFlat[T](implicit unpacker: WarpUnpacker[T]): AlmValidation[T] = unpacker.unpack(self)(WarpUnpackers.NoWarpUnpackers)
  }

}



