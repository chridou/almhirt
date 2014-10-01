package riftwarp.std

import scala.annotation.tailrec
import scalaz._
import scalaz.Cord
import scalaz.Cord._
import scalaz.std._
import almhirt.almvalidation.kit._
import almhirt.common._
import riftwarp._
import riftwarp.WarpTags._

object ToExplodedDematerializer extends Dematerializer[Any @@ Exploded] {

  override val channels = Set(WarpChannels.`rift-exploded`)
  val dimension = classOf[Any].getName()

  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): Any @@ Exploded = ExplodedAny(transform(what))

  private def transform(what: WarpPackage): Any =
    what match {
      case prim: WarpPrimitive ⇒
        prim.value
      case obj: WarpObject ⇒
        getObjectRepr(obj)
      case WarpCollection(items) ⇒
        items.map(transform)
      case WarpAssociativeCollection(items) ⇒
        items.map(x ⇒ (transform(x._1), transform(x._2)))
      case WarpTree(tree) ⇒
        tree.map(transform)
      case WarpTuple2(a, b) ⇒
        (transform(a), transform(b))
      case WarpTuple3(a, b, c) ⇒
        (transform(a), transform(b), transform(c))
      case WarpBytes(bytes) ⇒
        bytes
      case WarpBlob(bytes) ⇒
        bytes
    }

  private def getObjectRepr(warpObject: WarpObject): Map[String, Any] =
    (warpObject.warpDescriptor.map(w ⇒ (WarpDescriptor.defaultKey, w.toParsableString())).toVector ++ warpObject.elements.map(we ⇒
      we.value.map(v ⇒
        (we.label, transform(v)))).flatten).toMap

}