package almhirt.riftwarp

trait HasDematerializers {
  def addDematerializer[D <: Dematerializer[_,_], TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](dematerializer: Dematerializer[TChannel,To], asChannelDefault: Boolean = false)(implicit m: Manifest[To])
  def addDematerializerAsDefault[D <: Dematerializer[_,_], TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](dematerializer: Dematerializer[TChannel,To])(implicit m: Manifest[To]) = addDematerializer(dematerializer, true)

  def tryGetDematerializerByDescriptor[To <: RiftTypedDimension[_]](warpType: RiftDescriptor)(implicit m: Manifest[To]): Option[Dematerializer[_, To]]
  def tryGetDematerializer[TChannel <: RiftChannelDescriptor, To <: RiftTypedDimension[_]](implicit md: Manifest[To], mc: Manifest[TChannel]): Option[Dematerializer[TChannel,To]]
}