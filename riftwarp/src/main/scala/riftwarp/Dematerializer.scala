package riftwarp

import scala.reflect.ClassTag
import scala.collection.IterableLike
import scalaz.Tree
import almhirt.common._
import riftwarp.components.HasDecomposers

trait Dematerializer[T] extends Function1[WarpPackage, T]{
  final def apply(what: WarpPackage): T = dematerialize(what)
  def dematerialize(what: WarpPackage): T
}