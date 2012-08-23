package almhirt

trait MightBeRegisteredSomewhere {
  def registration: Option[RegistrationHolder]
  def isRegistered = registration.isDefined 
}