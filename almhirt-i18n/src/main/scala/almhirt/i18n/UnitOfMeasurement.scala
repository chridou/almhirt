package almhirt.i18n

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import com.ibm.icu.util._
import almhirt.problem.problemtypes.ArgumentProblem

sealed trait UnitOfMeasurement {
  def icu: AlmValidation[MeasureUnit]
  private[almhirt] def toBase(value: Double): Double
  private[almhirt] def fromBase(value: Double): Double
  def dimension: UnitOfMeasureDimension
  def name: String
  def measured(v: Double): Measured
}

sealed trait UomCompanion[T <: UnitOfMeasurement] {
  def dimension: UnitOfMeasureDimension
  final def lookup(name: String): AlmValidation[T] =
    UnitsOfMeasurement.byName(name).flatMap(foundUom =>
      if (foundUom.dimension == dimension)
        foundUom.asInstanceOf[T].success
      else
        ArgumentProblem(s"""Unit of measurement with name "" is not a ${dimension}. It is a "${foundUom.dimension}".""").failure)

  def siUnit: T = dimension.siUnit.asInstanceOf[T]
}

sealed trait AccelerationMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.AccelerationDimension
  override def measured(v: Double): MeasuredAcceleration = MeasuredAcceleration(v, this)

}
object AccelerationMeasureUnit extends UomCompanion[AccelerationMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.AccelerationDimension
}
sealed trait AngleMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.AngleDimension
  override def measured(v: Double): MeasuredAngle = MeasuredAngle(v, this)
}
object AngleMeasureUnit extends UomCompanion[AngleMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.AngleDimension
}
sealed trait AreaMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.AreaDimension
  override def measured(v: Double): MeasuredArea = MeasuredArea(v, this)
}
object AreaMeasureUnit extends UomCompanion[AreaMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.AreaDimension
}
sealed trait ConsumptionMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.ConsumptionDimension
  override def measured(v: Double): MeasuredConsumption = MeasuredConsumption(v, this)
}
object ConsumptionMeasureUnit extends UomCompanion[ConsumptionMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.ConsumptionDimension
}
sealed trait DigitalMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.DigitalDimension
  override def measured(v: Double): MeasuredDigital = MeasuredDigital(v, this)
}
object DigitalMeasureUnit extends UomCompanion[DigitalMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.DigitalDimension
}
sealed trait DurationMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.DurationDimension
  override def measured(v: Double): MeasuredDuration = MeasuredDuration(v, this)
}
object DurationMeasureUnit extends UomCompanion[DurationMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.DurationDimension
}
sealed trait CurrentMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.CurrentDimension
  override def measured(v: Double): MeasuredCurrent = MeasuredCurrent(v, this)
}
object CurrentMeasureUnit extends UomCompanion[CurrentMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.CurrentDimension
}
sealed trait VoltageMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.VoltageDimension
  override def measured(v: Double): MeasuredVoltage = MeasuredVoltage(v, this)
}
object VoltageMeasureUnit extends UomCompanion[VoltageMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.VoltageDimension
}
sealed trait EnergyMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.EnergyDimension
  override def measured(v: Double): MeasuredEnergy = MeasuredEnergy(v, this)
}
object EnergyMeasureUnit extends UomCompanion[EnergyMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.EnergyDimension
}
sealed trait FrequencyMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.FrequencyDimension
  override def measured(v: Double): MeasuredFrequency = MeasuredFrequency(v, this)
}
object FrequencyMeasureUnit extends UomCompanion[FrequencyMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.FrequencyDimension
}
sealed trait LengthMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.LengthDimension
  override def measured(v: Double): MeasuredLength = MeasuredLength(v, this)
}
object LengthMeasureUnit extends UomCompanion[LengthMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.LengthDimension
}
sealed trait LightMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.LightDimension
  override def measured(v: Double): MeasuredLight = MeasuredLight(v, this)
}
object LightMeasureUnit extends UomCompanion[AccelerationMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.LightDimension
}
sealed trait LightFluxMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.LightFluxDimension
  override def measured(v: Double): MeasuredLightFlux = MeasuredLightFlux(v, this)
}
object LightFluxMeasureUnit extends UomCompanion[LightFluxMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.LightFluxDimension
}
sealed trait MassMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.MassDimension
  override def measured(v: Double): MeasuredMass = MeasuredMass(v, this)
}
object MassMeasureUnit extends UomCompanion[MassMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.MassDimension
}
sealed trait PowerMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.PowerDimension
  override def measured(v: Double): MeasuredPower = MeasuredPower(v, this)
}
object PowerMeasureUnit extends UomCompanion[PowerMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.PowerDimension
}
sealed trait PressureMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.PressureDimension
  override def measured(v: Double): MeasuredPressure = MeasuredPressure(v, this)
}
object PressureMeasureUnit extends UomCompanion[PressureMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.PressureDimension
}
sealed trait ProportionMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.ProportionDimension
  override def measured(v: Double): MeasuredProportion = MeasuredProportion(v, this)
}
object ProportionMeasureUnit extends UomCompanion[ProportionMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.ProportionDimension
}
sealed trait SpeedMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.SpeedDimension
  override def measured(v: Double): MeasuredSpeed = MeasuredSpeed(v, this)
}
object SpeedMeasureUnit extends UomCompanion[SpeedMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.SpeedDimension
}
sealed trait TemperatureMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.TemperatureDimension
  override def measured(v: Double): MeasuredTemperature = MeasuredTemperature(v, this)
}
object TemperatureMeasureUnit extends UomCompanion[TemperatureMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.TemperatureDimension
}
sealed trait VolumeMeasureUnit extends UnitOfMeasurement {
  override val dimension = UnitOfMeasureDimension.VolumeDimension
  override def measured(v: Double): MeasuredVolume = MeasuredVolume(v, this)
}
object VolumeMeasureUnit extends UomCompanion[VolumeMeasureUnit] {
  override val dimension = UnitOfMeasureDimension.VolumeDimension
}

object UnitsOfMeasurement {
  case object GForce extends AccelerationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.G_FORCE.success
    val name = "g-force"
  }
  case object MeterPerSecondSquared extends AccelerationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.METER_PER_SECOND_SQUARED.success
    val name = "meter-per-second"
  }

  case object ArcMinute extends AngleMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.ARC_MINUTE.success
    val name = "arc-minute"
  }
  case object ArcSecond extends AngleMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.ARC_SECOND.success
    val name = "arc-second"
  }
  case object Degree extends AngleMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.DEGREE.success
    val name = "degree"
  }
  case object Radian extends AngleMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.RADIAN.success
    val name = "radian"
  }

  case object Acre extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 4046.8564224
    private[almhirt] override def fromBase(value: Double): Double = value / 4046.8564224
    val icu = MeasureUnit.ACRE.success
    val name = "acre"
  }
  case object Hectare extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 10000.0
    private[almhirt] override def fromBase(value: Double): Double = value / 10000.0
    val icu = MeasureUnit.HECTARE.success
    val name = "hectare"
  }
  case object SquareCentimeter extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value / (100.0 * 100.0)
    private[almhirt] override def fromBase(value: Double): Double = value * (100.0 * 100.0)
    val icu = MeasureUnit.SQUARE_CENTIMETER.success
    val name = "square-centimeter"
  }
  case object SquareFoot extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 0.09290304
    private[almhirt] override def fromBase(value: Double): Double = value / 0.09290304
    val icu = MeasureUnit.SQUARE_FOOT.success
    val name = "square-foot"
  }
  case object SquareInch extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 0.00064516
    private[almhirt] override def fromBase(value: Double): Double = value / 0.00064516
    val icu = MeasureUnit.SQUARE_INCH.success
    val name = "square-inch"
  }
  case object SquareKilometer extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * (1000.0 * 1000.0)
    private[almhirt] override def fromBase(value: Double): Double = value / (1000.0 * 1000.0)
    val icu = MeasureUnit.SQUARE_KILOMETER.success
    val name = "square-kilomemeter"
  }
  case object SquareMeter extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SQUARE_METER.success
    val name = "square-meter"
  }
  case object SquareMile extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 2589988.11033600
    private[almhirt] override def fromBase(value: Double): Double = value / 2589988.11033600
    val icu = MeasureUnit.SQUARE_MILE.success
    val name = "square-mile"
  }
  case object SquareYard extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 0.83612736
    private[almhirt] override def fromBase(value: Double): Double = value / 0.83612736
    val icu = MeasureUnit.SQUARE_YARD.success
    val name = "square-yard"
  }

  case object LiterPerKilometer extends ConsumptionMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.LITER_PER_KILOMETER.success
    val name = "liter-per-kilometer"
  }
  case object MilePerGallon extends AreaMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILE_PER_GALLON.success
    val name = "mile-per-gallon"
  }

  case object Bit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.BIT.success
    val name = "bit"
  }
  case object Byte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.BYTE.success
    val name = "byte"
  }
  case object Gigabit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.GIGABIT.success
    val name = "gigabit"
  }
  case object Gigabyte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.GIGABYTE.success
    val name = "gigabyte"
  }
  case object Kilobit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOBIT.success
    val name = "kilobit"
  }
  case object Kilobyte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOBYTE.success
    val name = "kilobyte"
  }
  case object Megabit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MEGABIT.success
    val name = "megabit"
  }
  case object Megabyte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MEGABYTE.success
    val name = "megabyte"
  }
  case object Terabit extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.TERABIT.success
    val name = "terabit"
  }
  case object Terabyte extends DigitalMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.TERABYTE.success
    val name = "terabyte"
  }

  case object Day extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * (60.0 * 60.0 * 24.0)
    private[almhirt] override def fromBase(value: Double): Double = value / (60.0 * 60.0 * 24.0)
    val icu = MeasureUnit.DAY.success
    val name = "day"
  }
  case object Hour extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * (60.0 * 60.0)
    private[almhirt] override def fromBase(value: Double): Double = value / (60.0 * 60.0)
    val icu = MeasureUnit.HOUR.success
    val name = "hour"
  }
  case object Microsecond extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-6
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E6
    val icu = MeasureUnit.MICROSECOND.success
    val name = "microsecond"
  }
  case object Millisecond extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-3
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E3
    val icu = MeasureUnit.MILLISECOND.success
    val name = "millisecond"
  }
  case object Minute extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 60.0
    private[almhirt] override def fromBase(value: Double): Double = value / 60.0
    val icu = MeasureUnit.MINUTE.success
    val name = "minute"
  }
  case object Month extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * (60.0 * 60.0 * 24.0 * 30.0)
    private[almhirt] override def fromBase(value: Double): Double = value * (60.0 * 60.0 * 24.0 * 30.0)
    val icu = MeasureUnit.MONTH.success
    val name = "month"
  }
  case object Nanosecond extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-9
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E9
    val icu = MeasureUnit.NANOSECOND.success
    val name = "nanosecond"
  }
  case object Second extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.SECOND.success
    val name = "second"
  }
  case object Week extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * (60.0 * 60.0 * 24.0 * 7.0)
    private[almhirt] override def fromBase(value: Double): Double = value / (60.0 * 60.0 * 24.0 * 7.0)
    val icu = MeasureUnit.WEEK.success
    val name = "week"
  }
  case object Year extends DurationMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * (60.0 * 60.0 * 24.0 * 365.0)
    private[almhirt] override def fromBase(value: Double): Double = value / (60.0 * 60.0 * 24.0 * 365.0)
    val icu = MeasureUnit.YEAR.success
    val name = "year"
  }

  case object Ampere extends CurrentMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.AMPERE.success
    val name = "ampere"
  }
  case object Milliampere extends CurrentMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value / 1000.0
    private[almhirt] override def fromBase(value: Double): Double = value * 1000.0
    val icu = MeasureUnit.MILLIAMPERE.success
    val name = "milliampere"
  }

  case object Volt extends VoltageMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.VOLT.success
    val name = "volt"
  }

  case object Calorie extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CALORIE.success
    val name = "calorie"
  }
  case object Foodcalorie extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.FOODCALORIE.success
    val name = "foodcalorie"
  }
  case object Joule extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.JOULE.success
    val name = "joule"
  }
  case object Kilocalorie extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOCALORIE.success
    val name = "kilocalorie"
  }
  case object Kilojoule extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOJOULE.success
    val name = "kilojoule"
  }
  case object KilowattHours extends EnergyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOWATT_HOUR.success
    val name = "kilowatt-hour"
  }

  case object Hertz extends FrequencyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HERTZ.success
    val name = "hertz"
  }
  case object Kilohertz extends FrequencyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1000.0
    private[almhirt] override def fromBase(value: Double): Double = value / 1000.0
    val icu = MeasureUnit.KILOHERTZ.success
    val name = "kilohertz"
  }
  case object Megahertz extends FrequencyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E6
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E-6
    val icu = MeasureUnit.MEGAHERTZ.success
    val name = "megahertz"
  }
  case object Gigahertz extends FrequencyMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E9
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E-9
    val icu = MeasureUnit.GIGAHERTZ.success
    val name = "gigahertz"
  }

  case object Picometer extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-12
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E12
    val icu = MeasureUnit.PICOMETER.success
    val name = "picometer"
  }
  case object Nanometer extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-9
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E9
    val icu = MeasureUnit.NANOMETER.success
    val name = "nanometer"
  }
  case object Micrometer extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-6
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E6
    val icu = MeasureUnit.MICROMETER.success
    val name = "micrometer"
  }
  case object Millimeter extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-3
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E3
    val icu = MeasureUnit.MILLIMETER.success
    val name = "millimeter"
  }
  case object Centimeter extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-2
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E2
    val icu = MeasureUnit.CENTIMETER.success
    val name = "centimeter"
  }
  case object Decimeter extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value / 10.0
    private[almhirt] override def fromBase(value: Double): Double = value * 10.0
    val icu = MeasureUnit.DECIMETER.success
    val name = "decimeter"
  }
  case object Meter extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.METER.success
    val name = "meter"
  }
  case object Kilometer extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1000.0
    private[almhirt] override def fromBase(value: Double): Double = value / 1000.0
    val icu = MeasureUnit.KILOMETER.success
    val name = "kilometer"
  }
  case object Fathom extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.8288
    private[almhirt] override def fromBase(value: Double): Double = value / 1.8288
    val icu = MeasureUnit.FATHOM.success
    val name = "fathom"
  }
  case object Furlong extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 201.1684
    private[almhirt] override def fromBase(value: Double): Double = value / 201.1684
    val icu = MeasureUnit.FURLONG.success
    val name = "furlong"
  }
  case object Inch extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 0.0254
    private[almhirt] override def fromBase(value: Double): Double = value / 0.0254
    val icu = MeasureUnit.INCH.success
    val name = "inch"
  }
  case object Foot extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 0.3048
    private[almhirt] override def fromBase(value: Double): Double = value / 0.3048
    val icu = MeasureUnit.FOOT.success
    val name = "foot"
  }
  case object Lightyear extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 9.4605284E15
    private[almhirt] override def fromBase(value: Double): Double = value * 9.4605284E-15
    val icu = MeasureUnit.LIGHT_YEAR.success
    val name = "light-year"
  }
  case object Mile extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1609.3440
    private[almhirt] override def fromBase(value: Double): Double = value / 1609.3440
    val icu = MeasureUnit.MILE.success
    val name = "mile"
  }
  case object NauticalMile extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1852
    private[almhirt] override def fromBase(value: Double): Double = value / 1852
    val icu = MeasureUnit.NAUTICAL_MILE.success
    val name = "nautical-mile"
  }
  case object Parsec extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 3.08567758E16
    private[almhirt] override def fromBase(value: Double): Double = value * 3.08567758E-16
    val icu = MeasureUnit.PARSEC.success
    val name = "parsec"
  }
  case object Yard extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 0.3048
    private[almhirt] override def fromBase(value: Double): Double = value / 0.3048
    val icu = MeasureUnit.YARD.success
    val name = "yard"
  }
  case object AstronomicalUnit extends LengthMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 149597871.0
    private[almhirt] override def fromBase(value: Double): Double = value / 149597871.0
    val icu = MeasureUnit.ASTRONOMICAL_UNIT.success
    val name = "astronomical-unit"
  }

  case object Lux extends LightMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.LUX.success
    val name = "lux"
  }

  case object Lumen extends LightFluxMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = IllegalOperationProblem("""Lumen is not supported by ICU.""").failure
    val name = "lumen"
  }

  case object Carat extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 2.0E-4
    private[almhirt] override def fromBase(value: Double): Double = value * 2.0E4
    val icu = MeasureUnit.CARAT.success
    val name = "carat"
  }
  case object Gram extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1000.0
    private[almhirt] override def fromBase(value: Double): Double = value / 1000.0
    val icu = MeasureUnit.GRAM.success
    val name = "gram"
  }
  case object Kilogram extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOGRAM.success
    val name = "kilogram"
  }
  case object MetricTon extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E3
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E-3
    val icu = MeasureUnit.METRIC_TON.success
    val name = "metric-ton"
  }
  case object Microgram extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-9
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E9
    val icu = MeasureUnit.MICROGRAM.success
    val name = ""
  }
  case object Milligram extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-6
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E6
    val icu = MeasureUnit.MILLIGRAM.success
    val name = "milligram"
  }
  case object Ounce extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 28.349523125E-3
    private[almhirt] override def fromBase(value: Double): Double = value / 28.349523125E3
    val icu = MeasureUnit.OUNCE.success
    val name = "ounce"
  }
  case object OunceTroy extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.OUNCE_TROY.success
    val name = "ounce-troy"
  }
  case object Pound extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 453.59237E-3
    private[almhirt] override def fromBase(value: Double): Double = value / 453.59237E3
    val icu = MeasureUnit.POUND.success
    val name = "pound"
  }
  case object Stone extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 6350.29318E-3
    private[almhirt] override def fromBase(value: Double): Double = value / 6350.29318E3
    val icu = MeasureUnit.STONE.success
    val name = "stone"
  }
  case object Ton extends MassMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 907.18474
    private[almhirt] override def fromBase(value: Double): Double = value / 907.18474
    val icu = MeasureUnit.TON.success
    val name = "ton"
  }

  case object Gigawatt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E9
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E-9
    val icu = MeasureUnit.GIGAWATT.success
    val name = "gigawatt"
  }
  case object Megawatt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E6
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E-6
    val icu = MeasureUnit.MEGAWATT.success
    val name = "megawatt"
  }
  case object Kilowatt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E3
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E-3
    val icu = MeasureUnit.KILOWATT.success
    val name = "kilowatt"
  }
  case object Watt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.WATT.success
    val name = "watt"
  }
  case object Milliwatt extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value * 1.0E-3
    private[almhirt] override def fromBase(value: Double): Double = value * 1.0E3
    val icu = MeasureUnit.MILLIWATT.success
    val name = "milliwatt"
  }
  case object Horsepower extends PowerMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HORSEPOWER.success
    val name = "horsepower"
  }

  case object Pascal extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = IllegalOperationProblem("""Pascal is not supported by ICU.""").failure
    val name = "pascal"
  }
  case object Hectopascal extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HECTOPASCAL.success
    val name = "hectopascal"
  }
  case object InchHg extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.INCH_HG.success
    val name = "inch-hg"
  }
  case object Millibar extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLIBAR.success
    val name = "millibar"
  }
  case object MillimeterOfMercury extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLIMETER_OF_MERCURY.success
    val name = "millimeter-of-mercury"
  }
  case object PundPerSquareInch extends PressureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.POUND_PER_SQUARE_INCH.success
    val name = "pound-per-square-inch"
  }

  case object Karat extends ProportionMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KARAT.success
    val name = "karat"
  }

  case object KilometerPerHour extends SpeedMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KILOMETER_PER_HOUR.success
    val name = "kilometer-per-hour"
  }
  case object MeterPerSecond extends SpeedMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.METER_PER_SECOND.success
    val name = "meter-per-second"
  }
  case object MilePerHour extends SpeedMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILE_PER_HOUR.success
    val name = "mile-per-hour"
  }

  case object Celsius extends TemperatureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value + 273.15
    private[almhirt] override def fromBase(value: Double): Double = value - 273.15
    val icu = MeasureUnit.CELSIUS.success
    val name = "celsius"
  }
  case object Fahrenheit extends TemperatureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = (value + 459.67) * (5.0 / 9.0)
    private[almhirt] override def fromBase(value: Double): Double = (value * 1.8) - 459.67
    val icu = MeasureUnit.FAHRENHEIT.success
    val name = "fahrenheit"
  }
  case object Kelvin extends TemperatureMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.KELVIN.success
    val name = "kelvin"
  }

  case object AcreFoot extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.ACRE_FOOT.success
    val name = "acre-foot"
  }
  case object Bushel extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.BUSHEL.success
    val name = "bushel"
  }
  case object Centiliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CENTILITER.success
    val name = "centiliter"
  }
  case object CubicCentimeter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_CENTIMETER.success
    val name = "cubic-centimeter"
  }
  case object CubicFoot extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_FOOT.success
    val name = "cubic-foot"
  }
  case object CubicInch extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_INCH.success
    val name = "cubic-inch"
  }
  case object CubicKilometer extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_KILOMETER.success
    val name = "cubic-kilometer"
  }
  case object CubicMeter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_METER.success
    val name = "cubic-meter"
  }
  case object CubicMile extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_MILE.success
    val name = "cubic-mile"
  }
  case object CubicYard extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUBIC_YARD.success
    val name = "cubic-yard"
  }
  case object Cup extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.CUP.success
    val name = "cup"
  }
  case object Deciliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.DECILITER.success
    val name = "deciliter"
  }
  case object FluidOunce extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.FLUID_OUNCE.success
    val name = "fluid-ounce"
  }
  case object Gallon extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.GALLON.success
    val name = "gallon"
  }
  case object Hectoliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.HECTOLITER.success
    val name = "hectoliter"
  }
  case object Liter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.LITER.success
    val name = "liter"
  }
  case object Megaliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MEGALITER.success
    val name = "megaliter"
  }
  case object Milliliter extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.MILLILITER.success
    val name = "milliliter"
  }
  case object Pint extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.PINT.success
    val name = "pint"
  }
  case object Quart extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.QUART.success
    val name = "quart"
  }
  case object Tablespoon extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.TABLESPOON.success
    val name = "tablespoon"
  }
  case object Teaspoon extends VolumeMeasureUnit {
    private[almhirt] override def toBase(value: Double): Double = value
    private[almhirt] override def fromBase(value: Double): Double = value
    val icu = MeasureUnit.TEASPOON.success
    val name = "teaspoon"
  }

  val allUnits: Seq[UnitOfMeasurement] =
    Vector(
      GForce,
      MeterPerSecondSquared,
      ArcMinute,
      ArcSecond,
      Degree,
      Radian,
      Acre,
      Hectare,
      SquareCentimeter,
      SquareFoot,
      SquareInch,
      SquareKilometer,
      SquareMeter,
      SquareMile,
      SquareYard,
      LiterPerKilometer,
      MilePerGallon,
      Bit,
      Byte,
      Gigabit,
      Gigabyte,
      Kilobit,
      Kilobyte,
      Megabit,
      Megabyte,
      Terabit,
      Terabyte,
      Day,
      Hour,
      Microsecond,
      Millisecond,
      Minute,
      Month,
      Nanosecond,
      Second,
      Week,
      Year,
      Ampere,
      Milliampere,
      Volt,
      Calorie,
      Foodcalorie,
      Joule,
      Kilocalorie,
      Kilojoule,
      KilowattHours,
      Hertz,
      Kilohertz,
      Megahertz,
      Gigahertz,
      Picometer,
      Nanometer,
      Micrometer,
      Millimeter,
      Centimeter,
      Decimeter,
      Meter,
      Kilometer,
      Fathom,
      Furlong,
      Inch,
      Foot,
      Lightyear,
      Mile,
      NauticalMile,
      Parsec,
      Yard,
      AstronomicalUnit,
      Lux,
      Lumen,
      Carat,
      Gram,
      Kilogram,
      MetricTon,
      Microgram,
      Milligram,
      Ounce,
      OunceTroy,
      Pound,
      Stone,
      Ton,
      Gigawatt,
      Megawatt,
      Kilowatt,
      Watt,
      Milliwatt,
      Horsepower,
      Pascal,
      Hectopascal,
      InchHg,
      Millibar,
      MillimeterOfMercury,
      PundPerSquareInch,
      Karat,
      KilometerPerHour,
      MeterPerSecond,
      MilePerHour,
      Celsius,
      Fahrenheit,
      Kelvin,
      AcreFoot,
      Bushel,
      Centiliter,
      CubicCentimeter,
      CubicFoot,
      CubicInch,
      CubicKilometer,
      CubicMeter,
      CubicMile,
      CubicYard,
      Cup,
      Deciliter,
      FluidOunce,
      Gallon,
      Hectoliter,
      Liter,
      Megaliter,
      Milliliter,
      Pint,
      Quart,
      Tablespoon,
      Teaspoon)

  def unitsByName: Map[String, UnitOfMeasurement] = allUnits.map { unit ⇒ (unit.name, unit) }.toMap
  def unitsByDimension: Map[UnitOfMeasureDimension, Seq[UnitOfMeasurement]] = allUnits.groupBy { _.dimension }

  def byName(name: String): AlmValidation[UnitOfMeasurement] =
    unitsByName get name match {
      case Some(uom) ⇒ scalaz.Success(uom)
      case None      ⇒ scalaz.Failure(NoSuchElementProblem(s"""No unit of measurement with name "$name" found."""))
    }
}