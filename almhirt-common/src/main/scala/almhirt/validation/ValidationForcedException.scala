package almhirt.validation

/** Wow, let's hope you'll never trigger that one.... */
case class ValidationForcedException(val problem: Problem) extends Exception("A value has been forced from a failed validation.")
