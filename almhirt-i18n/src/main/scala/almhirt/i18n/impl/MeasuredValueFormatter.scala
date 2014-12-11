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

object MeasuredValueFormatter {
  final case class FormatDefinition(uom: UnitOfMeasurement, minFractionDigits: Option[Int], maxFractionDigits: Option[Int], useDigitsGrouping: Option[Boolean])
  final case class CtorParams(
    locale: ULocale,
    argname: String,
    formatWidth: Option[MeasureRenderWidth],
    default: FormatDefinition,
    specific: Map[UnitsOfMeasurementSystem, FormatDefinition])

  def apply(params: CtorParams): AlmValidation[BasicValueFormatter] = {
    construct(params)
  }

  private def construct(params: CtorParams): AlmValidation[BasicValueFormatter] = {
    val effectiveRenderWidth = params.formatWidth getOrElse MeasureRenderWidth.Short
    val uomDim = params.default.uom.dimension
    for {
      validatedSpecifics ← if (params.specific.forall(_._2.uom.dimension == uomDim))
        params.specific.success
      else
        ArgumentProblem("""Dimensions do not match""").failure
    } yield {
      val defaultUom = params.default.uom
      val defaultFormat = createMeasureFormat(
        params.locale,
        effectiveRenderWidth,
        params.default.minFractionDigits,
        params.default.maxFractionDigits,
        params.default.useDigitsGrouping)
      val specificFormats =
        params.specific.map({
          case (uomSys, FormatDefinition(uom, minFractionDigits, maxFractionDigits, useDigitsGrouping)) ⇒
            (uomSys, (uom, createMeasureFormat(params.locale, effectiveRenderWidth, minFractionDigits, maxFractionDigits, useDigitsGrouping)))
        }).toMap
      new MeasuredValueFormatterImpl(params.locale, params.argname, defaultFormat, defaultUom, specificFormats) {
        override def any2MeasuredValueArg(arg: Any): AlmValidation[MeasuredValueArg] =
          arg match {
            case d: Double ⇒
              MeasuredValueArg.SiArg(d, None).success
            case arg: MeasuredValueArg.SiArg ⇒
              arg.success
            case arg: MeasuredValueArg.FullArg ⇒
              if (arg.measured.uom.dimension == uomDim)
                arg.success
              else
                ArgumentProblem(s"Dimensions do not match: ${arg.measured.uom.dimension} <> $uomDim").failure
            case x ⇒
              ArgumentProblem(s""""$x" is not a valid argument for a measured value.""").failure
          }
      }
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

private[almhirt] abstract class MeasuredValueFormatterImpl(
  override val locale: ULocale,
  override val argname: String,
  defaultMeasureFormat: MeasureFormat,
  defaultUnitOfMeasure: UnitOfMeasurement,
  specific: Map[UnitsOfMeasurementSystem, (UnitOfMeasurement, MeasureFormat)]) extends BasicValueFormatter {

  def any2MeasuredValueArg(arg: Any): AlmValidation[MeasuredValueArg]

  override def renderIntoBuffer(arg: Any, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    for {
      measuredValueArg ← any2MeasuredValueArg(arg)
      rendered ← renderMeasuredValueArgIntoBuffer(measuredValueArg, appendTo, pos)
    } yield rendered

  def renderMeasuredValueArgIntoBuffer(arg: MeasuredValueArg, appendTo: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] = {
    val measured = arg match {
      case MeasuredValueArg.FullArg(measured, _) ⇒ measured
      case MeasuredValueArg.SiArg(value, _)      ⇒ defaultUnitOfMeasure.dimension.siMeasured(value)
    }
    for {
      rendered ← arg.targetSystem match {
        case None ⇒
          renderInto(measured, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos)
        case Some(ts) ⇒
          specific get ts match {
            case None ⇒
              renderInto(measured, defaultUnitOfMeasure, defaultMeasureFormat, appendTo, pos)
            case Some((uom, format)) ⇒
              renderInto(measured, uom, format, appendTo, pos)
          }
      }
    } yield rendered
  }

  def formatable: Formatable = new SingleArgFormatable(this)

  private def renderInto(arg: Measured, uom: UnitOfMeasurement, format: MeasureFormat, into: StringBuffer, pos: FieldPosition): AlmValidation[StringBuffer] =
    (for {
      icuMeasured ← uom.icu
      buffer ← inTryCatch { format.format(new Measure(arg.calcDirectTo(uom), icuMeasured), into, pos) }
    } yield buffer).leftMap { p ⇒ ArgumentProblem("Error formatting the value.", cause = Some(p)) }

}

