package almhirt.common

sealed trait Importance extends Ordered[Importance] {
  def and(other: Importance): Importance =
    (this, other) match {
      case (Importance.VeryImportant, _) ⇒ Importance.VeryImportant
      case (_, Importance.VeryImportant) ⇒ Importance.VeryImportant
      case (Importance.Important, _) ⇒ Importance.Important
      case (_, Importance.Important) ⇒ Importance.Important
      case (Importance.Mentionable, _) ⇒ Importance.Mentionable
      case (_, Importance.Mentionable) ⇒ Importance.Mentionable
      case _ ⇒ Importance.NotWorthMentioning
    }
  /** Used for comparison */
  def level: Int
  def compare(that: Importance) = this.level compare (that.level)
  def parseableString: String
}

object Importance {
  final case object VeryImportant extends Importance {
    val level = 4
    val parseableString = "very-important"
  }

  final case object Important extends Importance {
    val level = 3
    val parseableString = "important"
  }

  final case object Mentionable extends Importance {
    val level = 2
    val parseableString = "mentionable"
  }

  final case object NotWorthMentioning extends Importance {
    val level = 1
    val parseableString = "not-worth-mentioning"
  }

  def fromString(str: String): almhirt.common.AlmValidation[Importance] =
    str.toLowerCase() match {
      case "very-important" ⇒ scalaz.Success(VeryImportant)
      case "important" ⇒ scalaz.Success(Important)
      case "mentionable" ⇒ scalaz.Success(Mentionable)
      case "not-worth-mentioning" ⇒ scalaz.Success(NotWorthMentioning)
      case x ⇒ scalaz.Failure(ParsingProblem(""""$str" is not an importance"""))
    }
}



