package riftwarp

import scala.reflect.ClassTag
import scala.collection.IterableLike
import scalaz.Tree
import almhirt.common._

trait Dematerializer[+T] extends Function2[WarpPackage, Map[String, Any], T]{
  def channels: Set[WarpChannel]
  final def apply(what: WarpPackage, options: Map[String, Any] = Map.empty): T = dematerialize(what, options)
  def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): T
}