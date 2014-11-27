package almhirt.i18n

import scala.language.postfixOps

import MeasuredShortImplicits._
import org.scalatest._

class MeasuredDoubleImplicitsTests extends FunSuite with Matchers {
  test("Meter") {
    val x = 1.0.m
    x should equal(MeasuredLength(1.0, UnitsOfMeasurement.Meter))
  }

  test("xx") {
    val x = (1.0.m) * 2.0
    x should equal(MeasuredLength(2.0, UnitsOfMeasurement.Meter))
  }
  
}