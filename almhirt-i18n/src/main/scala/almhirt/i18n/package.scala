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

  object MeasuredImplicits {
    implicit final class MeasuredImplicitsOps(private val d: Double) extends AnyVal {
      def millimeter = MeasuredLength(d, UnitsOfMeasurement.Millimeter)
      def centimeter = MeasuredLength(d, UnitsOfMeasurement.Centimeter)
      def meter = MeasuredLength(d, UnitsOfMeasurement.Meter)
      def kilometer = MeasuredLength(d, UnitsOfMeasurement.Kilometer)
      def inch = MeasuredLength(d, UnitsOfMeasurement.Inch)

      def gram = MeasuredMass(d, UnitsOfMeasurement.Gram)
      def kilogram = MeasuredMass(d, UnitsOfMeasurement.Kilogram)
      def ton = MeasuredMass(d, UnitsOfMeasurement.Ton)
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