package almhirt.i18n

import almhirt.common._

sealed trait NumberFormatStyle { def parsableString: String }

object NumberFormatStyle {
  case object Percentage extends NumberFormatStyle { val parsableString = "percentage" }
  case object Scientific extends NumberFormatStyle { val parsableString = "scientific" }
  case object Integer extends NumberFormatStyle { val parsableString = "integer" }
  case object NoStyle extends NumberFormatStyle { val parsableString = "nostyle" }

  def parse(toParse: String): AlmValidation[NumberFormatStyle] =
    toParse match {
      case "percentage" ⇒ scalaz.Success(Percentage)
      case "scientific" ⇒ scalaz.Success(Scientific)
      case "integer"    ⇒ scalaz.Success(Integer)
      case "nostyle"    ⇒ scalaz.Success(NoStyle)
      case x            ⇒ scalaz.Failure(ParsingProblem(s""""$x"" is not a NumberFormatStyle."""))

    }
}