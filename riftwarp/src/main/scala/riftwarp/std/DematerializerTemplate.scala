package riftwarp.std
import scalaz._
import scalaz.Scalaz._
import almhirt.common._
import almhirt.almvalidation.kit._
import riftwarp._

trait DematerializerTemplate[T] extends Dematerializer[T] {
  type ValueRepr
  type ObjRepr <: ValueRepr

  def transform(what: WarpPackage): ValueRepr =
    what match {
      case prim: WarpPrimitive ⇒
        getPrimitiveRepr(prim)
      case obj: WarpObject ⇒
        getObjectRepr(obj)
      case WarpCollection(items) ⇒
        foldReprs(items.map(transform))
      case WarpAssociativeCollection(items) ⇒
        foldAssocRepr(items.map(item ⇒ (transform(item._1), transform(item._2))))
      case WarpTree(tree) ⇒
        foldTreeRepr(tree.map(transform))
      case WarpTuple2(a, b) ⇒
        foldTuple2Reprs((transform(a), transform(b)))
      case WarpTuple3(a, b, c) ⇒
        foldTuple3Reprs((transform(a), transform(b), transform(c)))
      case WarpBytes(bytes) ⇒
        foldByteArrayRepr(bytes)
      case WarpBlob(bytes) ⇒
        foldBlobRepr(bytes)
    }

  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): T =
    valueReprToDim(transform(what))
    
  protected def valueReprToDim(repr: ValueRepr): T
  protected def getPrimitiveRepr(prim: WarpPrimitive): ValueRepr
  protected def getObjectRepr(warpObject: WarpObject): ObjRepr
  protected def foldReprs(elems: Traversable[ValueRepr]): ValueRepr
  protected def foldAssocRepr(assoc: Traversable[(ValueRepr, ValueRepr)]): ValueRepr
  protected def foldTuple2Reprs(tuple: (ValueRepr, ValueRepr)): ValueRepr
  protected def foldTuple3Reprs(tuple: (ValueRepr, ValueRepr, ValueRepr)): ValueRepr
  protected def foldTreeRepr(tree: scalaz.Tree[ValueRepr]): ValueRepr
  protected def foldByteArrayRepr(bytes: IndexedSeq[Byte]): ValueRepr
  protected def foldBlobRepr(bytes: IndexedSeq[Byte]): ValueRepr
}