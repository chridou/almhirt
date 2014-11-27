package almhirt.i18n

import scala.language.postfixOps

import MeasuredImplicits._
import org.scalatest._

class MeasuredDoubleImplicitsTests extends FunSuite with Matchers {
  test("Meter") {
    val x = 1.0.meter
    x should equal(MeasuredLength(1.0, UnitsOfMeasurement.Meter))
  }

  test("xx") {
    val x = (1.0.meter) * 2.0
    x should equal(MeasuredLength(2.0, UnitsOfMeasurement.Meter))
  }
  
}