package almhirt.i18n

import almhirt.common._
import scalaz.syntax.validation._
import com.ibm.icu.text.MeasureFormat.FormatWidth

sealed trait MeasureRenderWidth

object MeasureRenderWidth {
  /**
   * Spell out everything.
   */
  case object Wide extends MeasureRenderWidth

  /**
   * Abbreviate when possible.
   */
  case object Short extends MeasureRenderWidth

  /**
   * Brief. Use only a symbol for the unit when possible.
   */
  case object Narrow extends MeasureRenderWidth

  /**
   * Identical to NARROW except when formatMeasures is called with
   * an hour and minute; minute and second; or hour, minute, and second Measures.
   * In these cases formatMeasures formats as 5:37:23 instead of 5h, 37m, 23s.
   */
  case object Numeric extends MeasureRenderWidth

  implicit class MeasureRenderWidthOps(val self: MeasureRenderWidth) {
    def icuFormatWidth: FormatWidth =
      self match {
        case Wide    ⇒ FormatWidth.WIDE
        case Short   ⇒ FormatWidth.SHORT
        case Narrow  ⇒ FormatWidth.NARROW
        case Numeric ⇒ FormatWidth.NUMERIC
      }

    def parsableString: String =
      self match {
        case Wide    ⇒ "wide"
        case Short   ⇒ "short"
        case Narrow  ⇒ "narrow"
        case Numeric ⇒ "numeric"
      }
  }
  
  def parseString(toParse: String): AlmValidation[MeasureRenderWidth] = 
    toParse match {
    case "wide" => Wide.success
    case "short" => Short.success
    case "narrow" => Narrow.success
    case "numeric" => Numeric.success
    case x => ParsingProblem(s""""$x" is not a MeasureRenderWidth.""").failure
  }
}