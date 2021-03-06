package almhirt.i18n.impl

import java.text.FieldPosition
import java.util.HashMap
import scala.reflect.ClassTag
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.i18n._
import com.ibm.icu.util.{ ULocale, Measure }
import com.ibm.icu.text.{ MeasureFormat, NumberFormat }
import almhirt.i18n.util.DontCareFieldPosition

private[almhirt] object MeasuredFormatResourceValue {
  final case class FormatDefinition(
    uom: UnitOfMeasurement,
    minFractionDigits: Option[Int],
    maxFractionDigits: Option[Int],
    useDigitsGrouping: Option[Boolean],
    rangeSeparator: Option[String])
  final case class CtorParams(
    locale: ULocale,
    argname: String,
    formatWidth: Option[MeasureRenderWidth],
    default: FormatDefinition,
    specific: Map[UnitsOfMeasurementSystem, FormatDefinition])

  def apply(params: CtorParams): AlmValidation[BasicValueResourceValue] = {
    construct(params)
  }

  private def construct(params: CtorParams): AlmValidation[BasicValueResourceValue] = {
    val effectiveRenderWidth = params.formatWidth getOrElse MeasureRenderWidth.Short
    val uomDim = params.default.uom.dimension
    for {
      validatedSpecifics ← if (params.specific.forall(_._2.uom.dimension == uomDim))
        params.specific.success
      else
        ArgumentProblem("""Dimensions do not match""").failure
    } yield {
      val defaultUom = params.default.uom
      val defaultRangeSep = params.default.rangeSeparator getOrElse ("-")
      val defaultFormat = createMeasureFormat(
        params.locale,
        effectiveRenderWidth,
        params.default.minFractionDigits,
        params.default.maxFractionDigits,
        params.default.useDigitsGrouping)
      val specificFormats =
        params.specific.map({
          case (uomSys, FormatDefinition(uom, minFractionDigits, maxFractionDigits, useDigitsGrouping, rangeSeparator)) ⇒
            (uomSys, (uom, createMeasureFormat(params.locale, effectiveRenderWidth, minFractionDigits, maxFractionDigits, useDigitsGrouping), rangeSeparator getOrElse ("-")))
        }).toMap
      if (uomDim == UnitOfMeasureDimension.LightFluxDimension)
        new LightFluxMeasuredFormatResourceValue(params.locale, params.argname, defaultFormat, defaultUom, defaultRangeSep, specificFormats)
      else if (uomDim == UnitOfMeasureDimension.LuminousEfficacyDimension)
        new LuminousEfficacyMeasuredFormatResourceValue(params.locale, params.argname, defaultFormat, defaultUom, defaultRangeSep, specificFormats)
      else
        new MeasuredFormatResourceValue(params.locale, params.argname, defaultFormat, defaultUom, defaultRangeSep, specificFormats)
    }
  }

  private def createMeasureFormat(
    locale: ULocale,
    formatWidth: MeasureRenderWidth,
    minFractionDigits: Option[Int],
    maxFractionDigits: Option[Int],
    useDigitsGrouping: Option[Boolean]): MeasureFormat = {
    val numberFormat = NumberFormat.getInstance(locale)
    minFractionDigits.foreach { digits ⇒ numberFormat.setMinimumFractionDigits(digits) }
    maxFractionDigits.foreach { digits ⇒ numberFormat.setMaximumFractionDigits(digits) }
    useDigitsGrouping.foreach { useGrouping ⇒ numberFormat.setGroupingUsed(useGrouping) }
    MeasureFormat.getInstance(locale, formatWidth.icuFormatWidth, numberFormat)
  }

}

private[almhirt] class MeasuredFormatResourceValue(
  override val locale: ULocale,
  override val argname: String,
  defaultMeasureFormat: MeasureFormat,
  override val defaultUnitOfMeasure: UnitOfMeasurement,
  defaultRangeSeparator: String,
  specific: Map[UnitsOfMeasurementSystem, (UnitOfMeasurement, MeasureFormat, String)]) extends MeasuredValueResourceValue with MeasuredArgFormattingModule {

  override def formatIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
    for {
      argValidated ← if (arg.uom.dimension != defaultUnitOfMeasure.dimension)
        ArgumentProblem(s"Dimensions do not match: !${arg.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
      else
        arg.success
      rendered ← uomSys match {
        case None ⇒
          renderMeasureInto(argValidated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos)
        case Some(ts) ⇒
          specific get ts match {
            case None ⇒
              renderMeasureInto(argValidated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos)
            case Some((uom, format, _)) ⇒
              renderMeasureInto(argValidated, uom, format, appendTo, pos)
          }
      }
    } yield rendered

  def formatRangeIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg1: Measured, arg2: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] = {
    for {
      arg1Validated ← if (arg1.uom.dimension != defaultUnitOfMeasure.dimension)
        ArgumentProblem(s"Dimensions do not match: !${arg1.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
      else
        arg1.success
      arg2Validated ← if (arg2.uom.dimension != defaultUnitOfMeasure.dimension)
        ArgumentProblem(s"Dimensions do not match: !${arg2.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
      else
        arg2.success
      rendered ← uomSys match {
        case None ⇒
          renderRangeInto(arg1Validated, arg2Validated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos, defaultRangeSeparator)
        case Some(ts) ⇒
          specific get ts match {
            case None ⇒
              renderRangeInto(arg1Validated, arg2Validated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos, defaultRangeSeparator)
            case Some((uom, format, sep)) ⇒
              renderRangeInto(arg1Validated, arg2Validated, uom, format, appendTo, pos, sep)
          }
      }
    } yield rendered
  }

  override def formatable: AlmMeasureFormatter = new MeasuredArgFormatter(this)

  private def renderMeasureInto(arg: Measured, uom: UnitOfMeasurement, format: MeasureFormat, into: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    (for {
      icuMeasured ← uom.icu
      buffer ← inTryCatch { format.format(new Measure(arg.calcDirectTo(uom), icuMeasured), into, pos) }
    } yield buffer).leftMap { p ⇒ ArgumentProblem("Error formatting the value.", cause = Some(p)) }

  private def renderRangeInto(arg1: Measured, arg2: Measured, uom: UnitOfMeasurement, format: MeasureFormat, into: StringBuffer, pos: FieldPosition, separator: String): AlmValidation[StringBuffer] =
    (for {
      argRendered ← inTryCatch { format.getNumberFormat.format(arg1.calcDirectTo(uom), into, pos).append(separator) }
      buffer ← renderMeasureInto(arg2, uom, format, argRendered, pos)
    } yield buffer).leftMap { p ⇒ ArgumentProblem("Error formatting the value.", cause = Some(p)) }

  def valueWithUnitOfMeasurement(arg: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, String)] = {
    if (arg.uom.dimension != defaultUnitOfMeasure.dimension)
      ArgumentProblem(s"Dimensions do not match: !${arg.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
    else {
      val builder = new StringBuffer()
      for {
        res ← uomSys match {
          case None ⇒
            renderMeasureInto(arg, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg.calcDirectTo(defaultUnitOfMeasure), _))
          case Some(ts) ⇒
            specific get ts match {
              case None ⇒
                renderMeasureInto(arg, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg.calcDirectTo(defaultUnitOfMeasure), _))
              case Some((uom, format, _)) ⇒
                renderMeasureInto(arg, uom, format, builder, DontCareFieldPosition).map((arg.calcDirectTo(uom), _))
            }

        }
      } yield (res._1, res._2.toString().dropWhile(ch ⇒ ch.isDigit || ch.isWhitespace || ch == '.' || ch == ','))
    }
  }

  def rangeWithUnitOfMeasurement(arg1: Measured, arg2: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, Double, String)] = {
    if (arg1.uom.dimension != defaultUnitOfMeasure.dimension)
      ArgumentProblem(s"Dimensions do not match: !${arg1.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
    else if (arg2.uom.dimension != defaultUnitOfMeasure.dimension)
      ArgumentProblem(s"Dimensions do not match: !${arg2.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
    else {
      val builder = new StringBuffer()
      for {
        res ← uomSys match {
          case None ⇒
            renderMeasureInto(arg1, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg1.calcDirectTo(defaultUnitOfMeasure), arg2.calcDirectTo(defaultUnitOfMeasure), _))
          case Some(ts) ⇒
            specific get ts match {
              case None ⇒
                renderMeasureInto(arg1, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg1.calcDirectTo(defaultUnitOfMeasure), arg2.calcDirectTo(defaultUnitOfMeasure), _))
              case Some((uom, format, _)) ⇒
                renderMeasureInto(arg1, uom, format, builder, DontCareFieldPosition).map((arg1.calcDirectTo(uom), arg2.calcDirectTo(uom), _))
            }

        }
      } yield (res._1, res._2, res._3.toString().dropWhile(ch ⇒ ch.isDigit || ch.isWhitespace || ch == '.' || ch == ','))
    }
  }
}

//Temp hack... because there are no translations for lumen....
private[almhirt] class LightFluxMeasuredFormatResourceValue(
  override val locale: ULocale,
  override val argname: String,
  defaultMeasureFormat: MeasureFormat,
  override val defaultUnitOfMeasure: UnitOfMeasurement,
  defaultRangeSeparator: String,
  specific: Map[UnitsOfMeasurementSystem, (UnitOfMeasurement, MeasureFormat, String)]) extends MeasuredValueResourceValue with MeasuredArgFormattingModule {

  override def formatIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
    for {
      argValidated ← if (arg.uom.dimension != defaultUnitOfMeasure.dimension)
        ArgumentProblem(s"Dimensions do not match: !${arg.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
      else
        arg.success
      rendered ← uomSys match {
        case None ⇒
          renderMeasureInto(argValidated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos)
        case Some(ts) ⇒
          specific get ts match {
            case None ⇒
              renderMeasureInto(argValidated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos)
            case Some((uom, format, _)) ⇒
              renderMeasureInto(argValidated, uom, format, appendTo, pos)
          }
      }
    } yield rendered

  def formatRangeIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg1: Measured, arg2: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] = {
    for {
      arg1Validated ← if (arg1.uom.dimension != defaultUnitOfMeasure.dimension)
        ArgumentProblem(s"Dimensions do not match: !${arg1.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
      else
        arg1.success
      arg2Validated ← if (arg2.uom.dimension != defaultUnitOfMeasure.dimension)
        ArgumentProblem(s"Dimensions do not match: !${arg2.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
      else
        arg2.success
      rendered ← uomSys match {
        case None ⇒
          renderRangeInto(arg1Validated, arg2Validated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos, defaultRangeSeparator)
        case Some(ts) ⇒
          specific get ts match {
            case None ⇒
              renderRangeInto(arg1Validated, arg2Validated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos, defaultRangeSeparator)
            case Some((uom, format, sep)) ⇒
              renderRangeInto(arg1Validated, arg2Validated, uom, format, appendTo, pos, sep)
          }
      }
    } yield rendered
  }

  override def formatable: AlmMeasureFormatter = new MeasuredArgFormatter(this)

  private def renderMeasureInto(arg: Measured, uom: UnitOfMeasurement, format: MeasureFormat, into: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] = {
    val targetValue = arg.calcDirectTo(uom)
    (if (uom == UnitsOfMeasurement.Lumen)
      inTryCatch {
      format.getNumberFormat.format(targetValue, into, pos)
      into.append(" lm")
    }
    else
      for {
        icuMeasured ← uom.icu
        buffer ← inTryCatch { format.format(new Measure(targetValue, icuMeasured), into, pos) }
      } yield buffer).leftMap { p ⇒ ArgumentProblem("Error formatting the value.", cause = Some(p)) }
  }

  private def renderRangeInto(arg1: Measured, arg2: Measured, uom: UnitOfMeasurement, format: MeasureFormat, into: StringBuffer, pos: FieldPosition, separator: String): AlmValidation[StringBuffer] =
    (for {
      argRendered ← inTryCatch { format.getNumberFormat.format(arg1.calcDirectTo(uom), into, pos).append(separator) }
      buffer ← renderMeasureInto(arg2, uom, format, argRendered, pos)
    } yield buffer).leftMap { p ⇒ ArgumentProblem("Error formatting the value.", cause = Some(p)) }

  def valueWithUnitOfMeasurement(arg: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, String)] = {
    if (arg.uom.dimension != defaultUnitOfMeasure.dimension)
      ArgumentProblem(s"Dimensions do not match: !${arg.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
    else if (arg.uom == UnitsOfMeasurement.Lumen) {
      (arg.value, "lm").success
    } else {
      val builder = new StringBuffer()
      for {
        res ← uomSys match {
          case None ⇒
            renderMeasureInto(arg, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg.calcDirectTo(defaultUnitOfMeasure), _))
          case Some(ts) ⇒
            specific get ts match {
              case None ⇒
                renderMeasureInto(arg, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg.calcDirectTo(defaultUnitOfMeasure), _))
              case Some((uom, format, _)) ⇒
                renderMeasureInto(arg, uom, format, builder, DontCareFieldPosition).map((arg.calcDirectTo(uom), _))
            }
        }
      } yield (res._1, res._2.toString().dropWhile(ch ⇒ ch.isDigit || ch.isWhitespace || ch == '.' || ch == ','))

    }
  }

  def rangeWithUnitOfMeasurement(arg1: Measured, arg2: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, Double, String)] = {
    if (arg1.uom.dimension != defaultUnitOfMeasure.dimension)
      ArgumentProblem(s"Dimensions do not match: !${arg1.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
    else if (arg2.uom.dimension != defaultUnitOfMeasure.dimension)
      ArgumentProblem(s"Dimensions do not match: !${arg2.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
    else if (arg1.uom == UnitsOfMeasurement.Lumen && arg2.uom == UnitsOfMeasurement.Lumen) {
      (arg1.value, arg2.value, "lm").success
    } else if (arg1.uom == UnitsOfMeasurement.Lumen || arg2.uom == UnitsOfMeasurement.Lumen) {
      ArgumentProblem(s"When lumen is the target unit of measure, both have to be lumen.").failure
    } else {
      val builder = new StringBuffer()
      for {
        res ← uomSys match {
          case None ⇒
            renderMeasureInto(arg1, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg1.calcDirectTo(defaultUnitOfMeasure), arg2.calcDirectTo(defaultUnitOfMeasure), _))
          case Some(ts) ⇒
            specific get ts match {
              case None ⇒
                renderMeasureInto(arg1, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg1.calcDirectTo(defaultUnitOfMeasure), arg2.calcDirectTo(defaultUnitOfMeasure), _))
              case Some((uom, format, _)) ⇒
                renderMeasureInto(arg1, uom, format, builder, DontCareFieldPosition).map((arg1.calcDirectTo(uom), arg2.calcDirectTo(uom), _))
            }

        }
      } yield (res._1, res._2, res._3.toString().dropWhile(ch ⇒ ch.isDigit || ch.isWhitespace || ch == '.' || ch == ','))
    }
  }
}

//Temp hack... because there are no translations for lumen per watt....
private[almhirt] class LuminousEfficacyMeasuredFormatResourceValue(
  override val locale: ULocale,
  override val argname: String,
  defaultMeasureFormat: MeasureFormat,
  override val defaultUnitOfMeasure: UnitOfMeasurement,
  defaultRangeSeparator: String,
  specific: Map[UnitsOfMeasurementSystem, (UnitOfMeasurement, MeasureFormat, String)]) extends MeasuredValueResourceValue with MeasuredArgFormattingModule {

  override def formatIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] =
    for {
      argValidated ← if (arg.uom.dimension != defaultUnitOfMeasure.dimension)
        ArgumentProblem(s"Dimensions do not match: !${arg.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
      else
        arg.success
      rendered ← uomSys match {
        case None ⇒
          renderMeasureInto(argValidated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos)
        case Some(ts) ⇒
          specific get ts match {
            case None ⇒
              renderMeasureInto(argValidated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos)
            case Some((uom, format, _)) ⇒
              renderMeasureInto(argValidated, uom, format, appendTo, pos)
          }
      }
    } yield rendered

  def formatRangeIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg1: Measured, arg2: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer] = {
    for {
      arg1Validated ← if (arg1.uom.dimension != defaultUnitOfMeasure.dimension)
        ArgumentProblem(s"Dimensions do not match: !${arg1.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
      else
        arg1.success
      arg2Validated ← if (arg2.uom.dimension != defaultUnitOfMeasure.dimension)
        ArgumentProblem(s"Dimensions do not match: !${arg2.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
      else
        arg2.success
      rendered ← uomSys match {
        case None ⇒
          renderRangeInto(arg1Validated, arg2Validated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos, defaultRangeSeparator)
        case Some(ts) ⇒
          specific get ts match {
            case None ⇒
              renderRangeInto(arg1Validated, arg2Validated, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos, defaultRangeSeparator)
            case Some((uom, format, sep)) ⇒
              renderRangeInto(arg1Validated, arg2Validated, uom, format, appendTo, pos, sep)
          }
      }
    } yield rendered
  }

  override def formatable: AlmMeasureFormatter = new MeasuredArgFormatter(this)

  private def renderMeasureInto(arg: Measured, uom: UnitOfMeasurement, format: MeasureFormat, into: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] = {
    val targetValue = arg.calcDirectTo(uom)
    (if (uom == UnitsOfMeasurement.LumenPerWatt)
      inTryCatch {
      format.getNumberFormat.format(targetValue, into, pos)
      into.append(" lm/W")
    }
    else
      for {
        icuMeasured ← uom.icu
        buffer ← inTryCatch { format.format(new Measure(targetValue, icuMeasured), into, pos) }
      } yield buffer).leftMap { p ⇒ ArgumentProblem("Error formatting the value.", cause = Some(p)) }
  }

  private def renderRangeInto(arg1: Measured, arg2: Measured, uom: UnitOfMeasurement, format: MeasureFormat, into: StringBuffer, pos: FieldPosition, separator: String): AlmValidation[StringBuffer] =
    (for {
      argRendered ← inTryCatch { format.getNumberFormat.format(arg1.calcDirectTo(uom), into, pos).append(separator) }
      buffer ← renderMeasureInto(arg2, uom, format, argRendered, pos)
    } yield buffer).leftMap { p ⇒ ArgumentProblem("Error formatting the value.", cause = Some(p)) }

  def valueWithUnitOfMeasurement(arg: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, String)] = {
    if (arg.uom.dimension != defaultUnitOfMeasure.dimension)
      ArgumentProblem(s"Dimensions do not match: !${arg.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
    else if (arg.uom == UnitsOfMeasurement.LumenPerWatt) {
      (arg.value, "lm/W").success
    } else {
      val builder = new StringBuffer()
      for {
        res ← uomSys match {
          case None ⇒
            renderMeasureInto(arg, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg.calcDirectTo(defaultUnitOfMeasure), _))
          case Some(ts) ⇒
            specific get ts match {
              case None ⇒
                renderMeasureInto(arg, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg.calcDirectTo(defaultUnitOfMeasure), _))
              case Some((uom, format, _)) ⇒
                renderMeasureInto(arg, uom, format, builder, DontCareFieldPosition).map((arg.calcDirectTo(uom), _))
            }
        }
      } yield (res._1, res._2.toString().dropWhile(ch ⇒ ch.isDigit || ch.isWhitespace || ch == '.' || ch == ','))

    }
  }

  def rangeWithUnitOfMeasurement(arg1: Measured, arg2: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[(Double, Double, String)] = {
    if (arg1.uom.dimension != defaultUnitOfMeasure.dimension)
      ArgumentProblem(s"Dimensions do not match: !${arg1.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
    else if (arg2.uom.dimension != defaultUnitOfMeasure.dimension)
      ArgumentProblem(s"Dimensions do not match: !${arg2.uom.dimension}! <> ${defaultUnitOfMeasure.dimension}.").failure
    else if (arg1.uom == UnitsOfMeasurement.LumenPerWatt && arg2.uom == UnitsOfMeasurement.LumenPerWatt) {
      (arg1.value, arg2.value, "lm/W").success
    } else if (arg1.uom == UnitsOfMeasurement.LumenPerWatt || arg2.uom == UnitsOfMeasurement.LumenPerWatt) {
      ArgumentProblem(s"When LumenPerWatt is the target unit of measure, both have to be LumenPerWatt.").failure
    } else {
      val builder = new StringBuffer()
      for {
        res ← uomSys match {
          case None ⇒
            renderMeasureInto(arg1, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg1.calcDirectTo(defaultUnitOfMeasure), arg2.calcDirectTo(defaultUnitOfMeasure), _))
          case Some(ts) ⇒
            specific get ts match {
              case None ⇒
                renderMeasureInto(arg1, defaultUnitOfMeasure, defaultMeasureFormat, builder, DontCareFieldPosition).map((arg1.calcDirectTo(defaultUnitOfMeasure), arg2.calcDirectTo(defaultUnitOfMeasure), _))
              case Some((uom, format, _)) ⇒
                renderMeasureInto(arg1, uom, format, builder, DontCareFieldPosition).map((arg1.calcDirectTo(uom), arg2.calcDirectTo(uom), _))
            }

        }
      } yield (res._1, res._2, res._3.toString().dropWhile(ch ⇒ ch.isDigit || ch.isWhitespace || ch == '.' || ch == ','))
    }
  }
}

