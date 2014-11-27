package almhirt.i18n

import MeasuredImplicits._
import com.ibm.icu.text._
import com.ibm.icu.util.ULocale
import com.ibm.icu.text.MeasureFormat.FormatWidth
import org.scalatest._

class MeasureFormatTests extends FunSuite with Matchers {
  test("French(1001.137)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.FRENCH, FormatWidth.SHORT)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("US(1001.137)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.US, FormatWidth.SHORT)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("PRC(1001.137)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.PRC, FormatWidth.NARROW)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("GERMANY(1000.0)(SHORT)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.GERMANY, FormatWidth.SHORT)
    val x = (1000.0.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("GERMANY(1m+30cm)(SHORT)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.GERMANY, FormatWidth.SHORT)
    val x1 = 1.meter
    val x2 = 30.centimeter
    info(s"""${fmtFr.formatMeasures(x1, x2)}""")
  }
  test("GERMANY(1m+30cm)(WIDE)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.GERMANY, FormatWidth.WIDE)
    val x1 = 1.meter
    val x2 = 30.centimeter
    info(s"""${fmtFr.formatMeasures(x1, x2)}""")
  }
  test("GERMANY(100000.0)(SHORT)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.GERMANY, FormatWidth.SHORT)
    val x = (100000.0.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("GERMANY(1001.1)(SHORT)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.GERMANY, FormatWidth.SHORT)
    val x = (1001.1.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("GERMANY(1001.137)(SHORT)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.GERMANY, FormatWidth.SHORT)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("GERMANY(1001.137)(NARROW)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.GERMANY, FormatWidth.NARROW)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("GERMANY(1001.137)(NUMERIC)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.GERMANY, FormatWidth.NUMERIC)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("GERMANY(1001.137)(WIDE)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.GERMANY, FormatWidth.WIDE)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("JAPANESE(1001.137)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.JAPANESE, FormatWidth.SHORT)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("TRADITIONAL_CHINESE(1001.137 Fahrenheit)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.TRADITIONAL_CHINESE, FormatWidth.SHORT)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("TRADITIONAL_CHINESE(1001.137m)(SHORT)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.TRADITIONAL_CHINESE, FormatWidth.SHORT)
    val x = 1001.137.meter
    info(s"""$x -> "${x.render}"""")
  }
  test("TRADITIONAL_CHINESE(1001.137m)(WIDE)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.TRADITIONAL_CHINESE, FormatWidth.WIDE)
    val x = 1001.137.meter
    info(s"""$x -> "${x.render}"""")
  }
  test("TRADITIONAL_CHINESE(1m+30cm)(SHORT)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.TRADITIONAL_CHINESE, FormatWidth.SHORT)
    val x1 = 1.meter
    val x2 = 30.centimeter
    info(s"""${fmtFr.formatMeasures(x1, x2)}""")
  }
  test("TRADITIONAL_CHINESE(1m+30cm)(WIDE)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      ULocale.TRADITIONAL_CHINESE, FormatWidth.WIDE)
    val x1 = 1.meter
    val x2 = 30.centimeter
    info(s"""${fmtFr.formatMeasures(x1, x2)}""")
  }
  test("THAI(1001.137)") {
    implicit val fmtFr = MeasureFormat.getInstance(
      new ULocale("thai"), FormatWidth.SHORT)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("IRAN(1001.137)(WIDE)") {
    val loc = new ULocale("fa_IR")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.WIDE)
    val x = (1001.137.fahrenheit)
    info(s"""[${loc.getDisplayCountry()}]:$x -> "${x.render}"""")
  }
  test("IRAN(1001.137)(NUMERIC)") {
    val loc = new ULocale("fa_IR")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.NUMERIC)
    val x = (1001.137.fahrenheit)
    info(s"""[${loc.getDisplayCountry()}]:$x -> "${x.render}"""")
  }
  test("IRAN(1001.137)(SHORT)") {
    val loc = new ULocale("fa_IR")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.SHORT)
    val x = (1001.137.fahrenheit)
    info(s"""[${loc.getDisplayCountry()}]:$x -> "${x.render}"""")
  }

  test("Russia(1001.137 Fahrenheit)") {
    val loc = new ULocale("ru_RU")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.SHORT)
    val x = (1001.137.fahrenheit)
    info(s"""$x -> "${x.render}"""")
  }
  test("Russia(1001.137m)(SHORT)") {
    val loc = new ULocale("ru_RU")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.SHORT)
    val x = 1001.137.meter
    info(s"""$x -> "${x.render}"""")
  }
  test("Russia(1001.137m)(WIDE)") {
    val loc = new ULocale("ru_RU")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.SHORT)
    val x = 1001.137.meter
    info(s"""$x -> "${x.render}"""")
  }
  test("Russia(1m+30cm)(SHORT)") {
    val loc = new ULocale("ru_RU")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.SHORT)
    val x1 = 1.meter
    val x2 = 30.centimeter
    info(s"""${fmtFr.formatMeasures(x1, x2)}""")
  }
  test("Russia(1m+30cm)(WIDE)") {
    val loc = new ULocale("ru_RU")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.SHORT)
    val x1 = 1.meter
    val x2 = 30.centimeter
    info(s"""${fmtFr.formatMeasures(x1, x2)}""")
  }
  test("Russia(1-20pound)(NARROW)") {
    val loc = new ULocale("ru_RU")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.NARROW)
    for (i <- 0 to 20) {
      val x = i.pound
      info(s"""${x.render}""")
    }
  }
  test("Russia(1-20pound)(SHORT)") {
    val loc = new ULocale("ru_RU")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.SHORT)
    for (i <- 0 to 20) {
      val x = i.pound
      info(s"""${x.render}""")
    }
  }
  test("Russia(1-20pound)(NUMERIC)") {
    val loc = new ULocale("ru_RU")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.NUMERIC)
    for (i <- 0 to 20) {
      val x = i.pound
      info(s"""${x.render}""")
    }
  }
  test("Russia(1-20pound)(WIDE)") {
    val loc = new ULocale("ru_RU")
    implicit val fmtFr = MeasureFormat.getInstance(
      loc, FormatWidth.SHORT)
    for (i <- 0 to 20) {
      val x = i.pound
      info(s"""${x.render}""")
    }
  }

}