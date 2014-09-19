package riftwarp

import scalaz._, Scalaz._
import scalaz.Tree._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp.std.{ WarpPrimitiveConverter, WarpPackageConverter }

sealed trait WarpPackage {
  def toWarpObject: AlmValidation[WarpObject] =
    this match {
      case wo: WarpObject ⇒ wo.success
      case _ ⇒ MappingProblem("Not a WarpObject").failure
    }

  def to[T <: WarpPackage: WarpPackageConverter]: AlmValidation[T] =
    implicitly[WarpPackageConverter[T]].convert(this)
}

final case class WarpElement(label: String, value: Option[WarpPackage])

sealed trait WarpPrimitive extends WarpPackage { self ⇒
  def as[T: WarpPrimitiveConverter]: AlmValidation[T] = implicitly[WarpPrimitiveConverter[T]].convert(self)
  def value: Any
}

object WarpPrimitive {
  def unapply(what: WarpPackage): Option[Any] =
    what match {
      case w: WarpPrimitive ⇒ Some(w.value)
      case _ ⇒ None
    }
}

sealed trait WarpIntegralInteger extends WarpPrimitive

final case class WarpBoolean(override val value: Boolean) extends WarpPrimitive
final case class WarpString(override val value: String) extends WarpPrimitive
final case class WarpByte(override val value: Byte) extends WarpIntegralInteger
final case class WarpInt(override val value: Int) extends WarpIntegralInteger
final case class WarpShort(override val value: Short) extends WarpIntegralInteger
final case class WarpLong(override val value: Long) extends WarpIntegralInteger
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
      case Some(rd) ⇒ rd.success
      case None ⇒ NoSuchElementProblem("Object has no WarpDescriptor").failure
    }
}

final case class WarpCollection(items: Vector[WarpPackage]) extends WarpPackage
final case class WarpAssociativeCollection(items: Vector[(WarpPackage, WarpPackage)]) extends WarpPackage
final case class WarpTree(tree: scalaz.Tree[WarpPackage]) extends WarpPackage
final case class WarpTuple2(a: WarpPackage, b: WarpPackage) extends WarpPackage
final case class WarpTuple3(a: WarpPackage, b: WarpPackage, c: WarpPackage) extends WarpPackage

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
      case WarpCollection(Vector(l, WarpCollection(subForest))) ⇒
        val sfV = subForest.map {
          case aTree: WarpCollection ⇒ tree(aTree).toAgg
          case _ ⇒ ???
        }
        sfV.sequence.map(sf ⇒ l.node(sf: _*))
      case x ⇒ UnspecifiedProblem(s""""${x.toString()}" can not be converted to a tree element""").failure
    }

  implicit class WarpCollectionOps(self: WarpCollection) {

    def typedItemPackages[T <: WarpPackage: WarpPackageConverter]: AlmValidation[IndexedSeq[T]] = {
      val conv = implicitly[WarpPackageConverter[T]]

      val typedV = self.items.map(conv.convert(_).toAgg)
      typedV.sequence.map(_.toIndexedSeq)
    }

    def unpackItems[T: WarpUnpacker](implicit unpackers: WarpUnpackers = WarpUnpackers.empty): AlmValidation[IndexedSeq[T]] = {
      val up = implicitly[WarpUnpacker[T]]
      val typedV = self.items.map(up(_).toAgg)
      typedV.sequence.map(_.toIndexedSeq)
    }

    def associative: AlmValidation[WarpAssociativeCollection] =
      self.items.map(item ⇒
        (item match {
          case WarpCollection(innerItems) ⇒
            if (innerItems.length == 2)
              (innerItems(0), innerItems(1)).success
            else
              UnspecifiedProblem("An inner item must have a length of 2, when tranforming a WarpCollection to a WarpAssociativeCollection").failure
          case _ ⇒ UnspecifiedProblem("An inner item must be a WarpCollection of WarpCollections in order to transform a WarpCollection to a WarpAssociativeCollection").failure
        }).toAgg).sequence.map(WarpAssociativeCollection(_))

    def warpTree: AlmValidation[WarpTree] =
      WarpCollection.tree(self).map(x ⇒ WarpTree(x))
  }
}

object WarpAssociativeCollection {
  def apply(): WarpAssociativeCollection = WarpAssociativeCollection(Vector.empty)

  implicit class WarpAssociativeCollectionOps(self: WarpAssociativeCollection) {
    def typedItemPackages[T <: WarpPackage: WarpPackageConverter, U <: WarpPackage: WarpPackageConverter]: AlmValidation[IndexedSeq[(T, U)]] = {
      val ca = implicitly[WarpPackageConverter[T]]
      val cb = implicitly[WarpPackageConverter[U]]

      val typedV = self.items.map {
        case (a, b) ⇒
          ca.convert(a).flatMap(a1 ⇒
            cb.convert(b).map(b1 ⇒
              (a1, b1))).toAgg
      }
      typedV.sequence.map(_.toIndexedSeq)
    }

    def unpackItems[T: WarpUnpacker, U: WarpUnpacker](implicit unpackers: WarpUnpackers = WarpUnpackers.empty): AlmValidation[IndexedSeq[(T, U)]] = {
      val upA = implicitly[WarpUnpacker[T]]
      val upB = implicitly[WarpUnpacker[U]]
      val typedV = self.items.map {
        case (a, b) ⇒
          upA.unpack(a).flatMap(a1 ⇒
            upB.unpack(b).map(b1 ⇒
              (a1, b1))).toAgg
      }
      typedV.sequence.map(_.toIndexedSeq)
    }

    def unpackTupleItems[T: WarpUnpacker](implicit unpackers: WarpUnpackers = WarpUnpackers.empty): AlmValidation[IndexedSeq[(T, WarpPackage)]] = {
      val upA = implicitly[WarpUnpacker[T]]
      val typedV = self.items.map {
        case (a, b) ⇒
          upA.unpack(a).map(a1 ⇒ (a1, b)).toAgg
      }
      typedV.sequence.map(_.toIndexedSeq)
    }

    def unpackedAsTypedBs[T: WarpUnpacker, U <: WarpPackage: WarpPackageConverter](implicit unpackers: WarpUnpackers = WarpUnpackers.empty): AlmValidation[IndexedSeq[(T, U)]] = {
      val upA = implicitly[WarpUnpacker[T]]
      val cb = implicitly[WarpPackageConverter[U]]
      val typedV = self.items.map {
        case (a, b) ⇒
          upA.unpack(a).flatMap(a1 ⇒
            cb.convert(b).map((a1, _))).toAgg
      }
      typedV.sequence.map(_.toIndexedSeq)
    }
  }
}

object WarpPackage {
  implicit class WarpPackageOps(self: WarpPackage) {
    def dematerialize[To](implicit dematerializer: Dematerializer[To]): To =
      dematerializer.dematerialize(self)
    def unpack[T](implicit unpacker: WarpUnpacker[T], unpackers: WarpUnpackers): AlmValidation[T] = unpacker.unpack(self)
    def unpackFlat[T](implicit unpacker: WarpUnpacker[T]): AlmValidation[T] = unpacker.unpack(self)(WarpUnpackers.NoWarpUnpackers)
  }

}



