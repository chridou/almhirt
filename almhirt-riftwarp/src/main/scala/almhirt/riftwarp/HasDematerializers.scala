package almhirt.riftwarp

trait HasDematerializers {
  def addDematerializer[D <: Dematerializer[_], To <: RiftTypedDimension[_]](dematerializer: Dematerializer[To], asChannelDefault: Boolean = false)(implicit m: Manifest[To])
  def addDematerializerAsDefault[D <: Dematerializer[_], To <: RiftTypedDimension[_]](dematerializer: Dematerializer[To])(implicit m: Manifest[To]) = addDematerializer(dematerializer, true)
  def tryGetDematerializer[To <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[To]): Option[Dematerializer[To]]
}