package riftwarp.std
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait DematerializerTemplate[T] extends Dematerializer[T] {
  type ValueRepr

  def transform(what: WarpPackage): ValueRepr =
    what match {
      case prim: WarpPrimitive =>
        getPrimitiveRepr(prim)
      case obj: WarpObject =>
        getObjectRepr(obj)
      case WarpCollection(items) =>
        foldReprs(items.map(transform))
      case WarpAssociativeCollection(items) =>
        foldReprs(items.map(item => foldTupleReprs((transform(item._1), transform(item._2)))))
      case WarpTree(tree) =>
        foldTreeRepr(tree.map(transform))
      case WarpBytes(bytes) =>
        foldByteArrayRepr(bytes)
      case WarpBlob(bytes) =>
        foldBlobRepr(bytes)
    }

  override def dematerialize(what: WarpPackage): T =
    valueReprToDim(transform(what))
    
  protected def valueReprToDim(repr: ValueRepr): T
  protected def getPrimitiveRepr(prim: WarpPrimitive): ValueRepr
  protected def getObjectRepr(warpObject: WarpObject): ValueRepr
  protected def foldReprs(elems: Traversable[ValueRepr]): ValueRepr
  protected def foldTupleReprs(tuple: (ValueRepr, ValueRepr)): ValueRepr
  protected def foldTreeRepr(tree: scalaz.Tree[ValueRepr]): ValueRepr
  protected def foldByteArrayRepr(bytes: Array[Byte]): ValueRepr
  protected def foldBlobRepr(bytes: Array[Byte]): ValueRepr
}