package almhirt.riftwarp.impl

import almhirt.common._
import almhirt.riftwarp.HasRecomposers

class UnsafeRecomposerRegistry extends HasRecomposers {
  private var recomposers = Map.empty[String, AnyRef]
  def tryGetRecomposerByName(typeDescriptor: String): Option[AnyRef] = {
    recomposers.get(typeDescriptor)
  }

  def addRecomposerByName(typeDescriptor: String, decomposer: AnyRef) {
    recomposers = recomposers + (typeDescriptor -> decomposer)
  }
}