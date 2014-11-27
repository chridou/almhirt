package almhirt.i18n

import com.ibm.icu.util._

sealed trait UnitOfMeasurement {
  def icu: MeasureUnit
  private[almhirt] def toBase(value: Double): Double
  private[almhirt] def fromBase(value: Double): Double
}

sealed trait AccelerationMeasureUnit extends UnitOfMeasurement

sealed trait AngleMeasureUnit extends UnitOfMeasurement

sealed trait AreaMeasureUnit extends UnitOfMeasurement

sealed trait ConsumptionMeasureUnit extends UnitOfMeasurement

sealed trait DigitalMeasureUnit extends UnitOfMeasurement

sealed trait DurationMeasureUnit extends UnitOfMeasurement

sealed trait CurrentMeasureUnit extends UnitOfMeasurement

sealed trait VoltageMeasureUnit extends UnitOfMeasurement

sealed trait EnergyMeasureUnit extends UnitOfMeasurement

sealed trait FrequencyMeasureUnit extends UnitOfMeasurement

sealed trait LengthMeasureUnit extends UnitOfMeasurement

sealed trait LightMeasureUnit extends UnitOfMeasurement

sealed trait MassMeasureUnit extends UnitOfMeasurement

sealed trait PowerMeasureUnit extends UnitOfMeasurement

sealed trait PressureMeasureUnit extends UnitOfMeasurement

sealed trait ProportionMeasureUnit extends UnitOfMeasurement

sealed trait SpeedMeasureUnit extends UnitOfMeasurement

sealed trait TemperatureMeasureUnit extends UnitOfMeasurement

sealed trait VolumeMeasureUnit extends UnitOfMeasurement

object UnitsOfMeasurement {
  case object GForce extends AccelerationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.G_FORCE
  }
  case object MeterPerSecondSquared extends AccelerationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.METER_PER_SECOND_SQUARED
  }

  case object ArcMinute extends AngleMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.ARC_MINUTE
  }
  case object ArcSecond extends AngleMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.ARC_SECOND
  }
  case object Degree extends AngleMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.DEGREE
  }
  case object Radian extends AngleMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.RADIAN
  }

  case object Acre extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.ACRE
  }
  case object Hectare extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HECTARE
  }
  case object SquareCentimeter extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SQUARE_CENTIMETER
  }
  case object SquareFoot extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SQUARE_FOOT
  }
  case object SquareInch extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SQUARE_INCH
  }
  case object SquareKilometer extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SQUARE_KILOMETER
  }
  case object SquareMeter extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SQUARE_METER
  }
  case object SquareMile extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SQUARE_MILE
  }
  case object SquareYard extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SQUARE_YARD
  }

  case object LiterPerKilometer extends ConsumptionMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.LITER_PER_KILOMETER
  }
  case object MilePerGallon extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILE_PER_GALLON
  }

  case object Bit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.BIT
  }
  case object Byte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.BYTE
  }
  case object Gigabit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.GIGABIT
  }
  case object Gigabyte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.GIGABYTE
  }
  case object Kilobit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOBIT
  }
  case object Kilobyte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOBYTE
  }
  case object Megabit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MEGABIT
  }
  case object Megabyte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MEGABYTE
  }
  case object Terabit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.TERABIT
  }
  case object Terabyte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.TERABYTE
  }

  case object Day extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.DAY
  }
  case object Hour extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HOUR
  }
  case object Microsecond extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MICROSECOND
  }
  case object Millisecond extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLISECOND
  }
  case object Minute extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MINUTE
  }
  case object Month extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MONTH
  }
  case object Nanosecond extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.NANOSECOND
  }
  case object Second extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SECOND
  }
  case object Week extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.WEEK
  }
  case object Year extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.YEAR
  }

  case object Ampere extends CurrentMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.AMPERE
  }
  case object Milliampere extends CurrentMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLIAMPERE
  }

  case object Calorie extends VoltageMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CALORIE
  }
  case object Foodcalorie extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.FOODCALORIE
  }
  case object Joule extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.JOULE
  }
  case object Kilocalorie extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOCALORIE
  }
  case object Kilojoule extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOJOULE
  }
  case object KilowattHours extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOWATT_HOUR
  }

  case object Hertz extends FrequencyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HERTZ
  }
  case object Kilohertz extends FrequencyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOHERTZ
  }
  case object Megahertz extends FrequencyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MEGAHERTZ
  }
  case object Gigahertz extends FrequencyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.GIGAHERTZ
  }

  case object Picometer extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.PICOMETER
  }
  case object Nanometer extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.NANOMETER
  }
  case object Micrometer extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MICROMETER
  }
  case object Millimeter extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLIMETER
  }
  case object Centimeter extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CENTIMETER
  }
  case object Decimeter extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLIMETER
  }
  case object Meter extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.METER
  }
  case object Kilometer extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOMETER
  }
  case object Fathom extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.FATHOM
  }
  case object Furlong extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.FURLONG
  }
  case object Inch extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.INCH
  }
  case object Lightyear extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.LIGHT_YEAR
  }
  case object Mile extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILE
  }
  case object NauticalMile extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.NAUTICAL_MILE
  }
  case object Parsec extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.PARSEC
  }
  case object Yard extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.YARD
  }
  case object AstronomicalUnit extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.ASTRONOMICAL_UNIT
  }

  case object Lux extends LightMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.LUX
  }

  case object Carat extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.LUX
  }
  case object Gram extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.GRAM
  }
  case object Kilogram extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOGRAM
  }
  case object MetricTon extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.METRIC_TON
  }
  case object Microgram extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MICROGRAM
  }
  case object Milligram extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLIGRAM
  }
  case object Ounce extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.OUNCE
  }
  object OunceTroy extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.OUNCE_TROY
  }
  case object Pound extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.POUND
  }
  case object Stone extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.STONE
  }
  case object Ton extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.TON
  }

  case object Gigawatt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.GIGAWATT
  }
  case object Megawatt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MEGAWATT
  }
  case object Kilowatt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOWATT
  }
  case object Watt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.WATT
  }
  case object Milliwatt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLIWATT
  }
  case object Horsepower extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HORSEPOWER
  }

  case object Hectopascal extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HECTOPASCAL
  }
  case object InchHg extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.INCH_HG
  }
  case object Millibar extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLIBAR
  }
  case object MillimeterOfMercury extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLIMETER_OF_MERCURY
  }
  case object PundPerSquareInch extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.POUND_PER_SQUARE_INCH
  }

  case object Karat extends ProportionMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KARAT
  }

  case object KilometerPerHour extends SpeedMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOMETER_PER_HOUR
  }
  case object MeterPerSecond extends SpeedMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.METER_PER_SECOND
  }
  case object MilePerHour extends SpeedMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILE_PER_HOUR
  }

  case object Celsuis extends TemperatureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CELSIUS
  }
  case object Fahrenheit extends TemperatureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.FAHRENHEIT
  }
  case object Kelvin extends TemperatureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KELVIN
  }

  case object AcreFoot extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.ACRE_FOOT
  }
  case object Bushel extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.BUSHEL
  }
  case object Centiliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CENTILITER
  }
  case object CubicCentimeter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_CENTIMETER
  }
  case object CubicFoot extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_FOOT
  }
  case object CubicInch extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_INCH
  }
  case object CubicKilometer extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_KILOMETER
  }
  case object CubicMeter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_METER
  }
  case object CubicMile extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_MILE
  }
  case object CubicYard extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_YARD
  }
  case object Cup extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUP
  }
  case object Deciliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.DECILITER
  }
  case object FluidOunce extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.FLUID_OUNCE
  }
  case object GAllon extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.GALLON
  }
  case object Hectoliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HECTOLITER
  }
  case object Liter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.LITER
  }
  case object Megaliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MEGALITER
  }
  case object Milliliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLILITER
  }
  case object Pint extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.PINT
  }
  case object Quart extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.QUART
  }
  case object Tablespoon extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.TABLESPOON
  }
  case object Teaspoon extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.TEASPOON
  }
  
}