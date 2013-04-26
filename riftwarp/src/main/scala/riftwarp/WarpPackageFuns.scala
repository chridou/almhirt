package riftwarp

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._

trait WarpPackageFuns {
  def addElement(element: WarpElement, into: WarpObject): WarpObject = into.copy(elements = into.elements :+ element)
  def addElementWithOptionalValue(label: String, value: Option[WarpPackage], into: WarpObject): WarpObject = addElement(WarpElement(label, value), into)
  def addElementWithValue(label: String, value: WarpPackage, into: WarpObject): WarpObject = addElement(WarpElement(label, Some(value)), into)

  def packWith[T](what: T, packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    packer(what)

  def packObjectWith[T <: AnyRef](what: T, packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpObject] =
    packer(what).flatMap {
      case o: WarpObject => o.success
      case x => SerializationProblem(s"${x.getClass.getName} is not allowed. A WarpObject is required. Choose the correct WarpPacker.").failure
    }

  def packCollectionWith[T](what: Traversable[T], packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpCollection] =
    what.toVector.map(packer(_).toAgg)
      .sequence
      .map(WarpCollection(_))

  def packTreeWith[T](what: scalaz.Tree[T], packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpTree] =
    what.map(packer(_).toAgg)
      .sequence
      .map(WarpTree(_))

  def packAssociativeCollectionWith[A, B](what: Traversable[(A, B)], packerA: WarpPacker[A], packerB: WarpPacker[B])(implicit packers: WarpPackers): AlmValidation[WarpAssociativeCollection] =
    what.map { case (a, b) => packerA(a).flatMap(pa => packerB(b).map(pb => (pa, pb))).toAgg }
      .toVector
      .sequence
      .map(WarpAssociativeCollection(_))

  def packByDescriptor(what: Any, descriptor: RiftDescriptor)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    packers(descriptor).flatMap(_.packBlind(what))

  def packObjectByDescriptor(what: AnyRef, descriptor: RiftDescriptor)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    packers(descriptor).flatMap(_.packBlind(what)).flatMap {
      case o: WarpObject => o.success
      case x => SerializationProblem(s"${x.getClass.getName} is not allowed. A WarpObject is required. Choose the correct RiftDescriptor.").failure
    }

  def packCollectionByDescriptor(what: Traversable[Any], descriptor: RiftDescriptor)(implicit packers: WarpPackers): AlmValidation[WarpCollection] =
    packers(descriptor).flatMap(packer =>
      what.toVector.map(packer.packBlind(_).toAgg)
        .sequence
        .map(WarpCollection(_)))

  def packAssociativeCollectionByDescriptors[A, B](what: Traversable[(A, B)], descriptorA: RiftDescriptor, descriptorB: RiftDescriptor)(implicit packers: WarpPackers): AlmValidation[WarpAssociativeCollection] =
    for {
      packerA <- packers(descriptorA)
      packerB <- packers(descriptorB)
      res <- what.map {
        case (a, b) =>
          packerA.packBlind(a).flatMap(pa =>
            packerB.packBlind(b).map(pb =>
              (pa, pb))).toAgg
      }.toVector.sequence.map(WarpAssociativeCollection(_))
    } yield res

  def pack(what: Any)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    packByDescriptor(what, what.getClass)

  def packObject(what: AnyRef)(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    packObjectByDescriptor(what, what.getClass)

  def packCollection(what: Traversable[Any])(implicit packers: WarpPackers): AlmValidation[WarpCollection] =
    what.toVector.map(item => packers(what.getClass).flatMap(_.packBlind(item)).toAgg)
      .sequence
      .map(WarpCollection(_))

  def packAssociativeCollection(what: Traversable[(Any, Any)])(implicit packers: WarpPackers): AlmValidation[WarpAssociativeCollection] =
    what.map {
      case (a, b) =>
        (for {
          packerA <- packers(a.getClass())
          packerB <- packers(b.getClass())
          packedA <- packerA.packBlind(a)
          packedB <- packerB.packBlind(b)
        } yield (packedA, packedB)).toAgg
    }.toVector.sequence.map(WarpAssociativeCollection(_))

}