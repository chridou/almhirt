package almhirt

import scala.language.implicitConversions
import com.ibm.icu.util._

package object i18n {
  implicit def uom2IcuMeasurement(self: UnitOfMeasurement): MeasureUnit = self.icu
  implicit def measured2IcuMeasure(self: Measured): Measure = self.icu

  object MeasuredShortImplicits {
    implicit final class MeasuredShortImplicitsOps(private val d: Double) extends AnyVal {
      def mm = MeasuredLength(d, UnitsOfMeasurement.Millimeter)
      def cm = MeasuredLength(d, UnitsOfMeasurement.Centimeter)
      def m = MeasuredLength(d, UnitsOfMeasurement.Meter)
      def km = MeasuredLength(d, UnitsOfMeasurement.Kilometer)

      def g = MeasuredMass(d, UnitsOfMeasurement.Gram)
      def kg = MeasuredMass(d, UnitsOfMeasurement.Kilogram)
      def t = MeasuredMass(d, UnitsOfMeasurement.Ton)
    }
  }
}