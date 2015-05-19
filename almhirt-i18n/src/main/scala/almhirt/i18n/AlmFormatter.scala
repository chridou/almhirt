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

  def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer]

  def formatIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): AlmValidation[StringBuffer] =
    formatArgsIntoAt(appendTo, pos, Map(args: _*))

  def formatInto(appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
    formatIntoAt(appendTo, util.DontCareFieldPosition, args: _*)

  def formatArgsInto(appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
    formatArgsIntoAt(appendTo, util.DontCareFieldPosition, args)

  def format(args: (String, Any)*): AlmValidation[String] =
    formatIntoAt(new StringBuffer(), util.DontCareFieldPosition, args: _*).map(_.toString)

  def formatArgs(args: Map[String, Any]): AlmValidation[String] =
    formatArgsIntoAt(new StringBuffer(), util.DontCareFieldPosition, args).map(_.toString)

  def formatValuesIntoAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): AlmValidation[StringBuffer] = {
    var i = 0
    val theMap = values.map { x ⇒ i = i + 1; (i.toString, x) }.toMap
    formatArgsIntoAt(appendTo, pos, theMap)
  }

  def formatValuesInto(appendTo: StringBuffer, values: Any*): AlmValidation[StringBuffer] =
    formatValuesIntoAt(appendTo, util.DontCareFieldPosition, values: _*)

  def formatValues(values: Any*): AlmValidation[String] =
    formatValuesIntoAt(new StringBuffer(), util.DontCareFieldPosition, values: _*).map(_.toString)
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

  def valueWithUnitOfMeasurement(v: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, String)]

  def formatMeasureRangeIntoAt(lower: Measured, upper: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer]
  def formatMeasureRangeInto(lower: Measured, upper: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
    formatMeasureRangeIntoAt(lower, upper, appendTo, util.DontCareFieldPosition, uomSys)
  def formatMeasureRange(lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[String] =
    formatMeasureRangeIntoAt(lower, upper, new StringBuffer(), util.DontCareFieldPosition, uomSys).map(_.toString())

  def rangeWithUnitOfMeasurement(lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, Double, String)]
}

final class IcuFormatter(msgFormat: MessageFormat) extends AlmFormatter {
  override val locale = msgFormat.getULocale

  override def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
    inTryCatch {
      val map = new java.util.HashMap[String, Any]
      args.foreach({ case (k, v) ⇒ map.put(k, v) })
      msgFormat.format(map, appendTo, pos)
    }

  val underlying = msgFormat
}

object AlmFormatter {
  def apply(theLocale: ULocale, text: String): AlmFormatter = new AlmFormatter {
    override val locale = theLocale

    override def formatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def formatIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def formatInto(appendTo: StringBuffer, args: (String, Any)*): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def formatArgsInto(appendTo: StringBuffer, args: Map[String, Any]): AlmValidation[StringBuffer] =
      scalaz.Success(appendTo.append(text))

    override def format(args: (String, Any)*): AlmValidation[String] =
      scalaz.Success(text)

    override def formatArgs(args: Map[String, Any]): AlmValidation[String] =
      scalaz.Success(text)
  }

  implicit class AlmFormatterOps(val self: AlmFormatter) extends AnyVal {
    def forceFormatArgsIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: Map[String, Any]): StringBuffer =
      self.formatArgsIntoAt(appendTo, pos, args) fold (
        fail ⇒ appendTo.append(fail.message),
        succ ⇒ succ)

    def forceFormatIntoAt(appendTo: StringBuffer, pos: FieldPosition, args: (String, Any)*): StringBuffer =
      self.formatArgsIntoAt(appendTo, pos, Map(args: _*)) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatInto(appendTo: StringBuffer, args: (String, Any)*): StringBuffer =
      self.formatIntoAt(appendTo, util.DontCareFieldPosition, args: _*) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatArgsIntoBuffer(appendTo: StringBuffer, args: Map[String, Any]): StringBuffer =
      self.formatArgsIntoAt(appendTo, util.DontCareFieldPosition, args) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormat(args: (String, Any)*): String =
      self.formatIntoAt(new StringBuffer(), util.DontCareFieldPosition, args: _*).map(_.toString) fold (
        fail ⇒ s"{${fail.message}}",
        succ ⇒ succ)

    def forceFormatArgs(args: Map[String, Any]): String =
      self.formatArgsIntoAt(new StringBuffer(), util.DontCareFieldPosition, args).map(_.toString) fold (
        fail ⇒ s"{${fail.message}}",
        succ ⇒ succ)

    def forceFormatValuesIntoAt(appendTo: StringBuffer, pos: FieldPosition, values: Any*): StringBuffer =
      self.formatValuesIntoAt(appendTo, pos, values: _*) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatValuesInto(appendTo: StringBuffer, values: Any*): StringBuffer =
      self.formatValuesIntoAt(appendTo, util.DontCareFieldPosition, values: _*) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatValues(values: Any*): String =
      self.formatValuesIntoAt(new StringBuffer(), util.DontCareFieldPosition, values: _*).map(_.toString) fold (
        fail ⇒ s"{${fail.message}}",
        succ ⇒ succ)
  }
}

object AlmNumericFormatter {
  implicit class AlmNumericFormatterOps(val self: AlmNumericFormatter) extends AnyVal {
    def forceFormatNumericIntoAt[T: Numeric](num: T, appendTo: StringBuffer, pos: FieldPosition): StringBuffer =
      self.formatNumericIntoAt(num, appendTo, pos) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatNumericInto[T: Numeric](num: T, appendTo: StringBuffer): StringBuffer =
      self.formatNumericIntoAt(num, appendTo, util.DontCareFieldPosition) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatNumeric[T: Numeric](num: T): String =
      self.formatNumericIntoAt(num, new StringBuffer(), util.DontCareFieldPosition).map(_.toString()) fold (
        fail ⇒ s"{${fail.message}}",
        succ ⇒ succ)

    def forceFormatNumericRangeIntoAt[T: Numeric](lower: T, upper: T, appendTo: StringBuffer, pos: FieldPosition): StringBuffer =
      self.formatNumericRangeIntoAt(lower, upper, appendTo, pos) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatNumericRangeInto[T: Numeric](lower: T, upper: T, appendTo: StringBuffer): StringBuffer =
      self.formatNumericRangeIntoAt(lower, upper, appendTo, util.DontCareFieldPosition) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatNumericRange[T: Numeric](lower: T, upper: T): String =
      self.formatNumericRangeIntoAt(lower, upper, new StringBuffer(), util.DontCareFieldPosition).map(_.toString()) fold (
        fail ⇒ s"{${fail.message}}",
        succ ⇒ succ)
  }
}

object AlmMeasureFormatter {
  implicit class AlmMeasureFormatterOps(val self: AlmMeasureFormatter) extends AnyVal {
    def forceFormatMeasureIntoAt(v: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): StringBuffer =
      self.formatMeasureIntoAt(v, appendTo, pos, uomSys) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatMeasureInto(v: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): StringBuffer =
      self.formatMeasureIntoAt(v, appendTo, util.DontCareFieldPosition, uomSys) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatMeasure(v: Measured, uomSys: Option[UnitsOfMeasurementSystem]): String =
      self.formatMeasureIntoAt(v, new StringBuffer(), util.DontCareFieldPosition, uomSys).map(_.toString()) fold (
        fail ⇒ s"{${fail.message}}",
        succ ⇒ succ)

    def forceFormatMeasureRangeIntoAt(lower: Measured, upper: Measured, appendTo: StringBuffer, pos: FieldPosition, uomSys: Option[UnitsOfMeasurementSystem]): StringBuffer =
      self.formatMeasureRangeIntoAt(lower, upper, appendTo, pos, uomSys) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatMeasureRangeInto(lower: Measured, upper: Measured, appendTo: StringBuffer, uomSys: Option[UnitsOfMeasurementSystem]): StringBuffer =
      self.formatMeasureRangeIntoAt(lower, upper, appendTo, util.DontCareFieldPosition, uomSys) fold (
        fail ⇒ appendTo.append(s"{${fail.message}}"),
        succ ⇒ succ)

    def forceFormatMeasureRange(lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): String =
      self.formatMeasureRangeIntoAt(lower, upper, new StringBuffer(), util.DontCareFieldPosition, uomSys).map(_.toString()) fold (
        fail ⇒ s"{${fail.message}}",
        succ ⇒ succ)

    def forceValueWithUnitOfMeasurement(v: Measured, uomSys: Option[UnitsOfMeasurementSystem]): (Double, String) =
      self.valueWithUnitOfMeasurement(v, uomSys) fold (
        fail ⇒ (v.value, s"{${fail.message}}"),
        succ ⇒ succ)

    def forceRangeWithUnitOfMeasurement(lower: Measured, upper: Measured, uomSys: Option[UnitsOfMeasurementSystem]): (Double, Double, String) =
      self.rangeWithUnitOfMeasurement(lower, upper, uomSys) fold (
        fail ⇒ (lower.value, upper.value, s"{${fail.message}}"),
        succ ⇒ succ)
  }
}



