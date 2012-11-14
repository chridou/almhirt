package almhirt.riftwarp

trait HasDematerializers {
  def addDematerializer[D <: Dematerializer[_], To <: AnyRef](dematerializer: Dematerializer[To])(implicit m: Manifest[To])
  def tryGetDematerializer[To <: AnyRef](forChannel: RiftChannel)(implicit m: Manifest[To]): Option[Dematerializer[To]]
}