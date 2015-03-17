package almhirt.i18n.impl

import java.text.FieldPosition
import almhirt.common._
import almhirt.i18n._
import com.ibm.icu.util.ULocale

private[almhirt] trait SingleArgFormattingModule {
  def locale: ULocale
  def argname: String
  def formatIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg: Any): AlmValidation[StringBuffer]
}

private[almhirt] trait NumericArgFormattingModule {
  def locale: ULocale
  def argname: String
  def formatIntoBuffer[T: Numeric](appendTo: StringBuffer, pos: FieldPosition, arg: T): AlmValidation[StringBuffer]
  def formatRangeIntoBuffer[T: Numeric](appendTo: StringBuffer, pos: FieldPosition, arg1: T, arg2: T): AlmValidation[StringBuffer]
}

private[almhirt] trait MeasuredArgFormattingModule {
  def locale: ULocale
  def argname: String
  def defaultUnitOfMeasure: UnitOfMeasurement
  def formatIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer]
  def formatRangeIntoBuffer(appendTo: StringBuffer, pos: FieldPosition, arg1: Measured, arg2: Measured, uomSys: Option[UnitsOfMeasurementSystem]): AlmValidation[StringBuffer]
}