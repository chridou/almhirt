package almhirt

import scala.language.implicitConversions
import com.ibm.icu.util._
import almhirt.i18n.MeasuredLength

/**
 * Stuff for internationalizations.
 *
 * Calculation functins are only to be used for display purposes!
 */
package object i18n {
  implicit def uom2IcuMeasurement(self: UnitOfMeasurement): MeasureUnit = self.icu
  implicit def measured2IcuMeasure(self: Measured): Measure = self.icu

  implicit class ULocaleOps(val self: ULocale) extends AnyVal {
    def language =
      self.getLanguage match {
        case "" ⇒ None
        case x  ⇒ Some(x)
      }
    def script =
      self.getScript match {
        case "" ⇒ None
        case x  ⇒ Some(x)
      }
    def country =
      self.getCountry match {
        case "" ⇒ None
        case x  ⇒ Some(x)
      }
  }

  object MeasuredImplicits {
    implicit final class MeasuredImplicitsOps(private val d: Double) extends AnyVal {
      def squareMeter = MeasuredArea(d, UnitsOfMeasurement.SquareMeter)
      def squareCentimeter = MeasuredArea(d, UnitsOfMeasurement.SquareCentimeter)
      def squareFoot = MeasuredArea(d, UnitsOfMeasurement.SquareFoot)
      def squareInch = MeasuredArea(d, UnitsOfMeasurement.SquareInch)
      def squareYard = MeasuredArea(d, UnitsOfMeasurement.SquareYard)

      def volt = MeasuredVoltage(d, UnitsOfMeasurement.Volt)

      def ampere = MeasuredCurrent(d, UnitsOfMeasurement.Ampere)
      def milliampere = MeasuredCurrent(d, UnitsOfMeasurement.Milliampere)

      def millimeter = MeasuredLength(d, UnitsOfMeasurement.Millimeter)
      def centimeter = MeasuredLength(d, UnitsOfMeasurement.Centimeter)
      def meter = MeasuredLength(d, UnitsOfMeasurement.Meter)
      def kilometer = MeasuredLength(d, UnitsOfMeasurement.Kilometer)
      def inch = MeasuredLength(d, UnitsOfMeasurement.Inch)
      def yard = MeasuredLength(d, UnitsOfMeasurement.Yard)

      def gram = MeasuredMass(d, UnitsOfMeasurement.Gram)
      def kilogram = MeasuredMass(d, UnitsOfMeasurement.Kilogram)
      def ton = MeasuredMass(d, UnitsOfMeasurement.Ton)
      def pound = MeasuredMass(d, UnitsOfMeasurement.Pound)

      def watt = MeasuredPower(d, UnitsOfMeasurement.Watt)
      def milliwatt = MeasuredPower(d, UnitsOfMeasurement.Milliwatt)
      def kilowatt = MeasuredPower(d, UnitsOfMeasurement.Kilowatt)

      def fahrenheit = MeasuredTemperature(d, UnitsOfMeasurement.Fahrenheit)
      def celsius = MeasuredTemperature(d, UnitsOfMeasurement.Celsuis)
      def kelvin = MeasuredTemperature(d, UnitsOfMeasurement.Kelvin)

      def lux = MeasuredLight(d, UnitsOfMeasurement.Lux)
      def lumen = MeasuredLightFlux(d, UnitsOfMeasurement.Lumen)
    }

    implicit final class MeasuredLengtsOps(private val measured: MeasuredLength) extends AnyVal {
      def asMillimeter = measured.to(UnitsOfMeasurement.Millimeter)
      def asCentimeter = measured.to(UnitsOfMeasurement.Centimeter)
      def asMeter = measured.to(UnitsOfMeasurement.Meter)
      def asKilometer = measured.to(UnitsOfMeasurement.Kilometer)
      def asInch = measured.to(UnitsOfMeasurement.Inch)
    }
  }
}