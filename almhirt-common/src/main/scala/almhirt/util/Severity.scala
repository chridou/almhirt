package almhirt.util

sealed trait Severity extends Ordered[Severity] {
  def and(other: Severity): Severity =
    (this, other) match {
      case (Critical, _) ⇒ Critical
      case (_, Critical) ⇒ Critical
      case (Major, _) ⇒ Major
      case (_, Major) ⇒ Major
      case _ ⇒ Minor
    }
  /** Used for comparison */
  def level: Int
  def compare(that: Severity) = this.level compare (that.level)
  def parseableString: String
}

final case object Critical extends Severity {
  val level = 3
  val parseableString = "critical"
}

final case object Major extends Severity {
  val level = 2
  val parseableString = "major"
}

final case object Minor extends Severity {
  val level = 1
  val parseableString = "minor"
}

object Severity {
  def fromString(str: String): almhirt.common.AlmValidation[Severity] =
    str.toLowerCase() match {
      case "minor" ⇒ scalaz.Success(Minor)
      case "major" ⇒ scalaz.Success(Major)
      case "critical" ⇒ scalaz.Success(Critical)
      case x ⇒ scalaz.Failure(almhirt.common.ParsingProblem(""""$str" is not a severity"""))
    }
}
