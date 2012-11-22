package almhirt.riftwarp

trait HasDematerializers {
  def addDematerializer[D <: Dematerializer[_,_], To <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](dematerializer: Dematerializer[To,TChannel], asChannelDefault: Boolean = false)(implicit m: Manifest[To])
  def addDematerializerAsDefault[D <: Dematerializer[_,_], To <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](dematerializer: Dematerializer[To,TChannel])(implicit m: Manifest[To]) = addDematerializer(dematerializer, true)
  def tryGetDematerializerByDescriptor[To <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[To]): Option[Dematerializer[To,_]]
  def tryGetDematerializer[To <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor](implicit md: Manifest[To], mc: Manifest[TChannel]): Option[Dematerializer[To,TChannel]]
}