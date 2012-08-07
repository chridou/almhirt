package almhirt.validation

case class ValidationForcedException(val problem: Problem) extends Exception("A value has been forced from a failed validation")
