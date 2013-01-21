package riftwarp

import almhirt.common._

package object components {
  implicit class HasDecomposerOps(private val hasDecomposers: HasDecomposers) {
    def getRawDecomposerFor(what: AnyRef, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[RawDecomposer] = {
      val rd = what match {
        case htd: HasRiftDescriptor => htd.riftDescriptor
        case x => backupRiftDescriptor.getOrElse(RiftDescriptor(what.getClass()))
      }
      hasDecomposers.getRawDecomposer(rd)
    }

    def getRawDecomposerFor(what: AnyRef): AlmValidation[RawDecomposer] =
      getRawDecomposerFor(what, None)
      
   def getDecomposer[T <: AnyRef](riftDescriptor: RiftDescriptor): AlmValidation[Decomposer[T]] =
      hasDecomposers.getRawDecomposer(riftDescriptor).map(_.asInstanceOf[Decomposer[T]])

   def getDecomposerFor[T <: AnyRef](what: T, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Decomposer[T]] =
      hasDecomposers.getRawDecomposerFor(what, backupRiftDescriptor).map(_.asInstanceOf[Decomposer[T]])

   def getDecomposerFor[T <: AnyRef](what: T): AlmValidation[Decomposer[T]] =
      hasDecomposers.getRawDecomposerFor(what, None).map(_.asInstanceOf[Decomposer[T]])

  }
  //  def tryGetDecomposer[T <: AnyRef](implicit m: Manifest[T]): Option[Decomposer[T]] = tryGetDecomposer(m.runtimeClass)
  //  def tryGetDecomposerFor[T <: HasRiftDescriptor](what: T): Option[Decomposer[T]] = tryGetDecomposer[T](what.riftDescriptor)
  //  def tryGetDecomposerForAny[T <: AnyRef](what: T): Option[Decomposer[T]] =
  //    what match {
  //      case htd: HasRiftDescriptor => tryGetDecomposer(htd.riftDescriptor)
  //      case x => tryGetDecomposer(RiftDescriptor(x.getClass()))
  //    }
  //
  //  def getDecomposer[T <: AnyRef](implicit m: Manifest[T]): AlmValidation[Decomposer[T]] =
  //    option.cata(tryGetDecomposer[T](m))(
  //      decomposer => decomposer.success,
  //      UnspecifiedProblem("No decomposer found for type '%s')".format(m.runtimeClass.getName())).failure)
  //
  //  def getDecomposerForAny[T <: AnyRef](what: T): AlmValidation[Decomposer[T]] =
  //    option.cata(tryGetDecomposerForAny(what))(
  //      decomposer => decomposer.asInstanceOf[Decomposer[T]].success,
  //      UnspecifiedProblem("No decomposer found for type '%s')".format(what.getClass().getName())).failure)
  //
  //  def getRawDecomposerForAny(what: AnyRef): AlmValidation[RawDecomposer] =
  //    option.cata(tryGetRawDecomposerForAny(what))(
  //      decomposer => decomposer.success,
  //      UnspecifiedProblem("No decomposer found for type '%s')".format(what.getClass().getName())).failure)
  //  }
}