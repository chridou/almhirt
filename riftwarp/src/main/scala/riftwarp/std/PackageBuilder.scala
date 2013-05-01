package riftwarp.std

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait PackageBuilderFuns {

  def toWarpPrimitive[A](what: A)(implicit conv: WarpPrimitiveConverter[A]): WarpPrimitive = conv.convertBack(what)

  def toWarpPrimitivesCollection[A](what: Traversable[A])(implicit conv: WarpPrimitiveConverter[A]): WarpCollection =
    WarpCollection(what.map(conv.convertBack(_)).toVector)

  def toWarpCollectionWith[A](what: Traversable[A], packer: WarpPacker[A])(implicit packers: WarpPackers): AlmValidation[WarpCollection] =
    what.map(item => packer(item).toAgg).toVector.sequence.map(WarpCollection.apply)

  def toWarpCollectionLookUp(what: Traversable[Any])(implicit packers: WarpPackers): AlmValidation[WarpCollection] =
    what.map(item =>
      packers(RiftDescriptor(item.getClass)).flatMap(_.packBlind(item)).toAgg)
      .toVector
      .sequence
      .map(WarpCollection.apply)

  def E(label: String, what: WarpPackage): AlmValidation[WarpElement] =
    WarpElement(label, Some(what)).success

  def P[A: WarpPrimitiveConverter](label: String, what: A): AlmValidation[WarpElement] =
    WarpElement(label, Some(toWarpPrimitive(what))).success

  def POpt[A: WarpPrimitiveConverter](label: String, what: Option[A]): AlmValidation[WarpElement] =
    what match {
      case Some(v) => P(label, v)
      case None => WarpElement(label).success
    }

  def CP[A: WarpPrimitiveConverter](label: String, what: Traversable[A]): AlmValidation[WarpElement] =
   WarpElement(label, Some(toWarpPrimitivesCollection(what))).success

  def CPOpt[A: WarpPrimitiveConverter](label: String, what: Option[Traversable[A]]): AlmValidation[WarpElement] =
    what match {
      case Some(v) => WarpElement(label, Some(toWarpPrimitivesCollection(v))).success
      case None => WarpElement(label).success
    }

  def CWith[A](label: String, what: Traversable[A], packer: WarpPacker[A])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    toWarpCollectionWith(what, packer).map(x => WarpElement(label, Some(x)))

  def CWithOpt[A](label: String, what: Option[Traversable[A]], packer: WarpPacker[A])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) => toWarpCollectionWith(v, packer).map(x => WarpElement(label, Some(x)))
      case None => WarpElement(label).success
    }
 
  def CLookUp[A](label: String, what: Traversable[A])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    toWarpCollectionLookUp(what).map(x => WarpElement(label, Some(x)))

  def CLookUpOpt[A](label: String, what: Option[Traversable[A]])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) => toWarpCollectionLookUp(v).map(x => WarpElement(label, Some(x)))
      case None => WarpElement(label).success
    }
    
  def With[T](label: String, what: T, packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    packer(what).map(x => WarpElement(label, Some(x)))

  def WithOpt[T](label: String, what: Option[T], packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) => packer(v).map(x => WarpElement(label, Some(x)))
      case None => WarpElement(label).success
    }

  def LookUp(label: String, what: Any)(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    funs.pack(what).map(x => WarpElement(label, Some(x)))

  def LookUpOpt[T](label: String, what: Option[Any])(implicit packers: WarpPackers): AlmValidation[WarpElement] =
    what match {
      case Some(v) => funs.pack(v).map(x => WarpElement(label, Some(x)))
      case None => WarpElement(label).success
    }

  def Bytes(label: String, bytes: Array[Byte]): AlmValidation[WarpElement] = BytesOpt(label, Some(bytes))
  def BytesOpt(label: String, bytes: Option[Array[Byte]]): AlmValidation[WarpElement] = WarpElement(label, bytes.map(WarpBytes(_))).success

  def Base64(label: String, bytes: Array[Byte]): AlmValidation[WarpElement] = Base64Opt(label, Some(bytes))
  def Base64Opt(label: String, bytes: Option[Array[Byte]]): AlmValidation[WarpElement] = WarpElement(label, bytes.map(WarpBytes(_))).success

  def Blob(label: String, bytes: Array[Byte]): AlmValidation[WarpElement] = BlobOpt(label, Some(bytes))
  def BlobOpt(label: String, bytes: Option[Array[Byte]]): AlmValidation[WarpElement] = WarpElement(label, bytes.map(WarpBytes(_))).success
}

trait PackageBuilderOps {
  implicit class WarpElementOps(self: WarpElement) {
    def ~>(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      next.fold(
        fail => fail.failure,
        succ => WarpObject(None, Vector(self, succ)).success)

    def ⟿(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] = ~>(next)
  }

  implicit class WarpObjectOps(self: WarpObject) {
    def ~>(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      next.fold(
        fail => fail.failure,
        succ => WarpObject(self.riftDescriptor, self.elements :+ succ).success)

    def ⟿(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] = ~>(next)
  }

  implicit class RiftDescriptorOps(self: RiftDescriptor) {
    def ~>(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      next.fold(
        fail => fail.failure,
        succ => WarpObject(Some(self), Vector(succ)).success)

    def ⟿(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] = ~>(next)
  }

  implicit class WarpObjectVOps(self: AlmValidation[WarpObject]) {
    def ~>(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] =
      self.fold(
        fail => fail.failure,
        succObj =>
          next.fold(
            fail => fail.failure,
            succ => WarpObject(succObj.riftDescriptor, succObj.elements :+ succ).success))

    def ⟿(next: => AlmValidation[WarpElement]): AlmValidation[WarpObject] = ~>(next)

  }

}