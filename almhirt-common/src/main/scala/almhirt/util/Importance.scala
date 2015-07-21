package almhirt.util

sealed trait Importance extends Ordered[Importance] {
  def and(other: Importance): Importance =
    (this, other) match {
      case (Importance.VeryImportant, _)     ⇒ Importance.VeryImportant
      case (_, Importance.VeryImportant)     ⇒ Importance.VeryImportant
      case (Importance.Important, _)         ⇒ Importance.Important
      case (_, Importance.Important)         ⇒ Importance.Important
      case (Importance.SlightlyImportant, _) ⇒ Importance.SlightlyImportant
      case (_, Importance.SlightlyImportant) ⇒ Importance.SlightlyImportant
      case _                                 ⇒ Importance.NotImportant
    }
  /** Used for comparison */
  def level: Int
  def compare(that: Importance) = this.level compare (that.level)
  def parseableString: String
}

object Importance {
  final case object NotImportant extends Importance {
    val level = 1
    val parseableString = "not-important"
  }

  final case object SlightlyImportant extends Importance {
    val level = 2
    val parseableString = "slightly-important"
  }

  final case object Important extends Importance {
    val level = 3
    val parseableString = "important"
  }

  final case object VeryImportant extends Importance {
    val level = 4
    val parseableString = "very-important"
  }

  def fromString(str: String): almhirt.common.AlmValidation[Importance] =
    str.toLowerCase() match {
      case "very-important"     ⇒ scalaz.Success(VeryImportant)
      case "important"          ⇒ scalaz.Success(Important)
      case "slightly-important" ⇒ scalaz.Success(SlightlyImportant)
      case "not-important"      ⇒ scalaz.Success(NotImportant)
      case x                    ⇒ scalaz.Failure(almhirt.common.ParsingProblem(""""$str" is not an importance"""))
    }
}
