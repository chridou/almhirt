package almhirt

trait IsRegisteredSomewhere[T] {
  def registration(): Registration[T]
}