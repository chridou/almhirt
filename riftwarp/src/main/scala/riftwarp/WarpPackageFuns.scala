package riftwarp

import scalaz._, Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._

trait WarpPackageFuns {
  def addElement(element: WarpElement, into: WarpObject): WarpObject = into.copy(elements = into.elements :+ element)
  def addElementWithOptionalValue(label: String, value: Option[WarpPackage], into: WarpObject): WarpObject = addElement(WarpElement(label, value), into)
  def addElementWithValue(label: String, value: WarpPackage, into: WarpObject): WarpObject = addElement(WarpElement(label, Some(value)), into)

  def packWith[T](what: T, packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpPackage] =
    packer.pack(what)

  def packObjectWith[T <: AnyRef](what: T, packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpObject] =
    packer.pack(what).flatMap {
      case o: WarpObject => o.success
      case x => SerializationProblem(s"${x.getClass.getName} is not allowed. A WarpObject is required. Choose another WarpPacker.").failure
    }
  
  def packCollectionWith[T](what: Traversable[T], packer: WarpPacker[T])(implicit packers: WarpPackers): AlmValidation[WarpCollection] =
    what.toVector.map(packer.pack(_).toAgg).sequence.map(WarpCollection(_))
}