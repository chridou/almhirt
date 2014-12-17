package almhirt.i18n

import almhirt.common._
import almhirt.almvalidation.kit._
import com.ibm.icu.text._
import java.text.FieldPosition
import com.ibm.icu.util.ULocale

/**
 * A [[Formatable]] contains something that can be formatted given the required arguments to fill the gaps.
 *
 * Implementations of this trait must be considered non thread safe.
 * With* methods usually do not return a new instance.
 */
trait AlmFormatter {
  def locale: ULocale

  def formatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer]

  def formatIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): AlmValidation[StringBuffer] =
    formatArgsIntoBufferAt(appendTo, pos, Map(args: _*))

  def formatIntoBuffer(appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
    formatIntoBufferAt(appendTo, util.DontCareFieldPosition, args: _*)

  def formatArgsIntoBuffer(appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
    formatArgsIntoBufferAt(appendTo, util.DontCareFieldPosition, args)

  def format(args: (String, Any)*): AlmValidation[String] =
    formatIntoBufferAt(new StringBuffer(), util.DontCareFieldPosition, args: _*).map(_.toString)

  def formatArgs(args: Map[String, Any]): AlmValidation[String] =
    formatArgsIntoBufferAt(new StringBuffer(), util.DontCareFieldPosition, args).map(_.toString)

  def formatValuesIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): AlmValidation[StringBuffer] = {
    var i = 0
    val theMap = values.map { x ⇒ i = i + 1; (i.toString, x) }.toMap
    formatArgsIntoBufferAt(appendTo, pos, theMap)
  }

  def formatValuesIntoBuffer(appendTo: StringBuffer, values: Any*): AlmValidation[StringBuffer] =
    formatValuesIntoBufferAt(appendTo, util.DontCareFieldPosition, values: _*)

  def formatValues(values: Any*): AlmValidation[String] =
    formatValuesIntoBufferAt(new StringBuffer(), util.DontCareFieldPosition, values: _*).map(_.toString)
}

trait AlmNumericFormatter extends AlmFormatter {
  def formatNumericIntoAt[T: Numeric](num: T, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer]
  def formatNumericInto[T: Numeric](num: T, appendTo: StringBuffer): AlmValidation[StringBuffer] =
    formatNumericIntoAt(num, appendTo, util.DontCareFieldPosition)
  def formatNumeric[T: Numeric](num: T): AlmValidation[String] =
    formatNumericIntoAt(num, new StringBuffer(), util.DontCareFieldPosition).map(_.toString())

  def formatNumericRangeIntoAt[T: Numeric](lower: T, upper: T, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer]
  def formatNumericRangeInto[T: Numeric](lower: T, upper: T, appendTo: StringBuffer): AlmValidation[StringBuffer] =
    formatNumericRangeIntoAt(lower, upper, appendTo, util.DontCareFieldPosition)
  def formatNumericRange[T: Numeric](lower: T, upper: T): AlmValidation[String] =
    formatNumericRangeIntoAt(lower, upper, new StringBuffer(), util.DontCareFieldPosition).map(_.toString())
}

trait AlmMeasureFormatter extends AlmNumericFormatter {
  def formatMeasureIntoAt(v: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer]
  def formatMeasureInto(v: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
    formatMeasureIntoAt(v, appendTo, util.DontCareFieldPosition, uomSys)
  def formatMeasure(v: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[String] =
    formatMeasureIntoAt(v, new StringBuffer(), util.DontCareFieldPosition, uomSys).map(_.toString())

  def formatMeasureRangeIntoAt(lower: Measured, upper: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer]
  def formatMeasureRangeInto(lower: Measured, upper: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
    formatMeasureRangeIntoAt(lower, upper, appendTo, util.DontCareFieldPosition, uomSys)
  def formatMeasureRange(lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[String] =
    formatMeasureRangeIntoAt(lower, upper, new StringBuffer(), util.DontCareFieldPosition, uomSys).map(_.toString())
}

object AlmFormatter {
  def apply(theLocale: ULocale, text: String): AlmFormatter = new AlmFormatter {
    override val locale = theLocale

    override def formatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def formatIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def formatIntoBuffer(appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def formatArgsIntoBuffer(appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def format(args: (String, Any)*): AlmValidation[String] =
      scalaz.Success(text)

    override def formatArgs(args: Map[String, Any]): AlmValidation[String] =
      scalaz.Success(text)
  }

  implicit class AlmFormatterOps(val self: AlmFormatter) extends AnyVal {
    def forceFormatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): StringBuffer =
      self.formatArgsIntoBufferAt(appendTo, pos, args).resultOrEscalate
    def forceFormatIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): StringBuffer =
      self.formatIntoBufferAt(appendTo, pos, args: _*).resultOrEscalate
    def forceFormatIntoBuffer(appendTo: StringBuffer, args: (String, Any)*): StringBuffer =
      self.formatIntoBuffer(appendTo, args: _*).resultOrEscalate
    def forceFormatArgsIntoBuffer(appendTo: StringBuffer, args: Map[String, Any]): StringBuffer =
      self.formatArgsIntoBuffer(appendTo, args).resultOrEscalate
    def forceFormat(args: (String, Any)*): String =
      self.format(args: _*).resultOrEscalate
    def forceFormatArgs(args: Map[String, Any]): String =
      self.formatArgs(args).resultOrEscalate

    def forceFormatValuesIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): StringBuffer = {
      self.formatValuesIntoBufferAt(appendTo, pos, values: _*).resultOrEscalate
    }

    def forceFormatValuesIntoBuffer(appendTo: StringBuffer, values: Any*): StringBuffer =
      self.formatValuesIntoBuffer(appendTo, values: _*).resultOrEscalate

    def forceFormatValues(values: Any*): String =
      self.formatValues(values: _*).resultOrEscalate

  }
}

final class IcuFormatter(msgFormat: MessageFormat) extends AlmFormatter {
  override val locale = msgFormat.getULocale

  override def formatArgsIntoBufferAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    inTryCatch {
      val map = new java.util.HashMap[String, Any]
      args.foreach({ case (k, v) ⇒ map.put(k, v) })
      msgFormat.format(map, appendTo, pos)
    }

  val underlying = msgFormat
}


