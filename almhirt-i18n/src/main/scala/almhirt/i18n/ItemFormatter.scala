package almhirt.i18n

import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util.ULocale

trait ItemFormatter[T] {
  def appendToBuffer(what: T, locale: ULocale, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(implicit lookUp: ResourceLookup): AlmValidation[StringBuffer]

  final def appendToBuffer(what: T, locale: ULocale, appendTo: StringBuffer)(implicit lookUp: ResourceLookup): AlmValidation[StringBuffer] =
    appendToBuffer(what, locale, None, appendTo)
}

trait ItemFormatterStrategy[T] {
  def appendToBuffer(key: ResourceKey)(what: T, locale: ULocale, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(lookup: ResourceLookup): AlmValidation[StringBuffer]
}

object ItemFormatter {
  def create[T: ItemFormatterStrategy](key: ResourceKey): ItemFormatter[T] = {
    val theMethod = implicitly[ItemFormatterStrategy[T]].appendToBuffer _
    new ItemFormatter[T] {
      override def appendToBuffer(what: T, locale: ULocale, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(implicit lookup: ResourceLookup): AlmValidation[StringBuffer] = {
        theMethod(key)(what, locale, uomSys, appendTo)(lookup)
      }
    }
  }

  def createExplicitly[T](key: ResourceKey, strategy: ItemFormatterStrategy[T]): ItemFormatter[T] = {
    create(key)(strategy)
  }

}

object DefaultItemFormatterStrategies {
  implicit val IntStrategy = new ItemFormatterStrategy[Int] {
    override def appendToBuffer(key: ResourceKey)(what: Int, locale: ULocale, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(lookup: ResourceLookup): AlmValidation[StringBuffer] = {
      for {
        formatter ← lookup.getNumericFormatter(key, locale)
        formatted ← formatter.formatNumericInto(what, appendTo)
      } yield formatted
    }
  }

  implicit val LongStrategy = new ItemFormatterStrategy[Long] {
    override def appendToBuffer(key: ResourceKey)(what: Long, locale: ULocale, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(lookup: ResourceLookup): AlmValidation[StringBuffer] = {
      for {
        formatter ← lookup.getNumericFormatter(key, locale)
        formatted ← formatter.formatNumericInto(what, appendTo)
      } yield formatted
    }
  }

  implicit val FloatStrategy = new ItemFormatterStrategy[Float] {
    override def appendToBuffer(key: ResourceKey)(what: Float, locale: ULocale, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(lookup: ResourceLookup): AlmValidation[StringBuffer] = {
      for {
        formatter ← lookup.getNumericFormatter(key, locale)
        formatted ← formatter.formatNumericInto(what, appendTo)
      } yield formatted
    }
  }

  implicit val DoubleStrategy = new ItemFormatterStrategy[Double] {
    override def appendToBuffer(key: ResourceKey)(what: Double, locale: ULocale, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(lookup: ResourceLookup): AlmValidation[StringBuffer] = {
      for {
        formatter ← lookup.getNumericFormatter(key, locale)
        formatted ← formatter.formatNumericInto(what, appendTo)
      } yield formatted
    }
  }

  implicit val MeasuredStrategy = new ItemFormatterStrategy[Measured] {
    override def appendToBuffer(key: ResourceKey)(what: Measured, locale: ULocale, uomSys: Option[UnitsOfMeasurementSystem], appendTo: StringBuffer)(lookup: ResourceLookup): AlmValidation[StringBuffer] = {
      for {
        formatter ← lookup.getMeasureFormatter(key, locale)
        formatted ← formatter.formatMeasureInto(what, appendTo, uomSys)
      } yield formatted
    }
  }

} 