package almhirt

trait MightBeRegisteredSomewhere[T] {
  def registration(): Option[Registration[T]]
}