package almhirt

/** The implementor might be registered with some other service */
trait MightBeRegisteredSomewhere {
  def registration: Option[RegistrationHolder]
  def isRegistered = registration.isDefined 
}