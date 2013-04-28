package riftwarp

import scala.reflect.ClassTag
import scala.collection.IterableLike
import scalaz.Tree
import almhirt.common._
import riftwarp.components.HasDecomposers

trait Dematerializer[TDimension <: RiftDimension] {
  def write(what: WarpPackage): TDimension
}