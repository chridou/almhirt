package almhirt.riftwarp.impl

import scalaz.syntax.validation._
import almhirt.common._
import almhirt.riftwarp.HasDecomposers

class UnsafeDecomposerRegistry extends HasDecomposers {
  private var decomposers = Map.empty[String, AnyRef]
  def tryGetDecomposerByName(typeDescriptor: String): Option[AnyRef] = {
    decomposers.get(typeDescriptor)
  }

  def addDecomposerByName(typeDescriptor: String, decomposer: AnyRef) {
    decomposers = decomposers + (typeDescriptor -> decomposer)
  }
}