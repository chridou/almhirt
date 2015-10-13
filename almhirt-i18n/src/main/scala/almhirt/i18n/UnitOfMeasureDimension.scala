package almhirt.i18n

sealed trait UnitOfMeasureDimension {
  def siUnit: UnitOfMeasurement
  def siMeasured(v: Double): Measured
}

object UnitOfMeasureDimension {
  object AccelerationDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.MeterPerSecondSquared
    override def siMeasured(v: Double): MeasuredAcceleration = MeasuredAcceleration(v, siUnit)
  }
  object AngleDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Radian
    override def siMeasured(v: Double): MeasuredAngle = MeasuredAngle(v, siUnit)
  }
  object AreaDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.SquareMeter
    override def siMeasured(v: Double): MeasuredArea = MeasuredArea(v, siUnit)
  }
  object ConsumptionDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.LiterPerKilometer
    override def siMeasured(v: Double): MeasuredConsumption = MeasuredConsumption(v, siUnit)
  }
  object DigitalDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Byte
    override def siMeasured(v: Double): MeasuredDigital = MeasuredDigital(v, siUnit)
  }
  object DurationDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Second
    override def siMeasured(v: Double): MeasuredDuration = MeasuredDuration(v, siUnit)
  }
  object CurrentDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Ampere
    override def siMeasured(v: Double): MeasuredCurrent = MeasuredCurrent(v, siUnit)
  }
  object VoltageDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Volt
    override def siMeasured(v: Double): MeasuredVoltage = MeasuredVoltage(v, siUnit)
  }
  object EnergyDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.KilowattHours
    override def siMeasured(v: Double): MeasuredEnergy = MeasuredEnergy(v, siUnit)
  }
  object FrequencyDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Hertz
    override def siMeasured(v: Double): MeasuredFrequency = MeasuredFrequency(v, siUnit)
  }
  object LengthDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Meter
    override def siMeasured(v: Double): MeasuredLength = MeasuredLength(v, siUnit)
  }
  object LightDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Lux
    override def siMeasured(v: Double): MeasuredLight = MeasuredLight(v, siUnit)
  }
  object LightFluxDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Lumen
    override def siMeasured(v: Double): MeasuredLightFlux = MeasuredLightFlux(v, siUnit)
  }
   object LuminousEfficacyDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.LumenPerWatt
    override def siMeasured(v: Double): MeasuredLuminousEfficacy = MeasuredLuminousEfficacy(v, siUnit)
  }
 object MassDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Kilogram
    override def siMeasured(v: Double): MeasuredMass = MeasuredMass(v, siUnit)
  }
  object PowerDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Watt
    override def siMeasured(v: Double): MeasuredPower = MeasuredPower(v, siUnit)
  }
  object PressureDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Pascal
    override def siMeasured(v: Double): MeasuredPressure = MeasuredPressure(v, siUnit)
  }
  object ProportionDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Karat
    override def siMeasured(v: Double): MeasuredProportion = MeasuredProportion(v, siUnit)
  }
  object SpeedDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.MeterPerSecond
    override def siMeasured(v: Double): MeasuredSpeed = MeasuredSpeed(v, siUnit)
  }
  object TemperatureDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.Kelvin
    override def siMeasured(v: Double): MeasuredTemperature = MeasuredTemperature(v, siUnit)
  }
  object VolumeDimension extends UnitOfMeasureDimension {
    override val siUnit = UnitsOfMeasurement.CubicMeter
    override def siMeasured(v: Double): MeasuredVolume = MeasuredVolume(v, siUnit)
  }
}