package almhirt

trait MightBeRegisteredSomewhere {
  def registration: Option[RegistrationHolder]
  def isRegisteredSome = registration.isDefined 
}