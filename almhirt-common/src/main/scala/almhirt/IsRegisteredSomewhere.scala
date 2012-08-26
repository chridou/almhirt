package almhirt

/** The implementor is registered with some other service */
trait IsRegisteredSomewhere {
  def registration(): RegistrationHolder
}