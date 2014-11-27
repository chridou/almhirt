package almhirt.i18n

import com.ibm.icu.util._

/**
 * Something that has been measured.
 * Represents a [[UnitOfMeasurement]] combined with a value.
 * Calculation are only to be used for localization purposes.
 */
sealed trait Measured {
  def value: Double
  def uom: UnitOfMeasurement
  final def icu: Measure = new Measure(value, uom.icu)

  def *(factor: Double): Measured

  protected def calcValue(newUom: UnitOfMeasurement): Double = {
    newUom.fromBase(uom.toBase(value))
  }
  
  final def render(implicit fmt: com.ibm.icu.text.MeasureFormat): String = {
    fmt.format(icu)
  }

}

final case class MeasuredAcceleration(value: Double, uom: AccelerationMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredAcceleration = MeasuredAcceleration(value * factor, uom)
  def to(newUom: AccelerationMeasureUnit): MeasuredAcceleration = MeasuredAcceleration(calcValue(newUom), newUom)
}

final case class MeasuredAngle(value: Double, uom: AngleMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredAngle = MeasuredAngle(value * factor, uom)
  def to(newUom: AngleMeasureUnit): MeasuredAngle = MeasuredAngle(calcValue(newUom), newUom)
}

final case class MeasuredArea(value: Double, uom: AreaMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredArea = MeasuredArea(value * factor, uom)
  def to(newUom: AreaMeasureUnit): MeasuredArea = MeasuredArea(calcValue(newUom), newUom)
}

final case class MeasuredConsumption(value: Double, uom: ConsumptionMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredConsumption = MeasuredConsumption(value * factor, uom)
  def to(newUom: ConsumptionMeasureUnit): MeasuredConsumption = MeasuredConsumption(calcValue(newUom), newUom)
}

final case class MeasuredDigital(value: Double, uom: DigitalMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredDigital = MeasuredDigital(value * factor, uom)
  def to(newUom: DigitalMeasureUnit): MeasuredDigital = MeasuredDigital(calcValue(newUom), newUom)
}

final case class MeasuredDuration(value: Double, uom: DurationMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredDuration = MeasuredDuration(value * factor, uom)
  def to(newUom: DurationMeasureUnit): MeasuredDuration = MeasuredDuration(calcValue(newUom), newUom)
}

final case class MeasuredCurrent(value: Double, uom: CurrentMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredCurrent = MeasuredCurrent(value * factor, uom)
  def to(newUom: CurrentMeasureUnit): MeasuredCurrent = MeasuredCurrent(calcValue(newUom), newUom)
}

final case class MeasuredVoltage(value: Double, uom: VoltageMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredVoltage = MeasuredVoltage(value * factor, uom)
  def to(newUom: VoltageMeasureUnit): MeasuredVoltage = MeasuredVoltage(calcValue(newUom), newUom)
}

final case class MeasuredEnergy(value: Double, uom: EnergyMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredEnergy = MeasuredEnergy(value * factor, uom)
  def to(newUom: EnergyMeasureUnit): MeasuredEnergy = MeasuredEnergy(calcValue(newUom), newUom)
}

final case class MeasuredFrequency(value: Double, uom: FrequencyMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredFrequency = MeasuredFrequency(value * factor, uom)
  def to(newUom: FrequencyMeasureUnit): MeasuredFrequency = MeasuredFrequency(calcValue(newUom), newUom)
}

final case class MeasuredLength(value: Double, uom: LengthMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredLength = MeasuredLength(value * factor, uom)
  def to(newUom: LengthMeasureUnit): MeasuredLength = MeasuredLength(calcValue(newUom), newUom)
}

final case class MeasuredLight(value: Double, uom: LightMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredLight = MeasuredLight(value * factor, uom)
  def to(newUom: LightMeasureUnit): MeasuredLight = MeasuredLight(calcValue(newUom), newUom)
}

final case class MeasuredLightFlux(value: Double, uom: LightFluxMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredLightFlux = MeasuredLightFlux(value * factor, uom)
  def to(newUom: LightFluxMeasureUnit): MeasuredLightFlux = MeasuredLightFlux(calcValue(newUom), newUom)
}

final case class MeasuredMass(value: Double, uom: MassMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredMass = MeasuredMass(value * factor, uom)
  def to(newUom: MassMeasureUnit): MeasuredMass = MeasuredMass(calcValue(newUom), newUom)
}

final case class MeasuredPower(value: Double, uom: PowerMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredPower = MeasuredPower(value * factor, uom)
  def to(newUom: PowerMeasureUnit): MeasuredPower = MeasuredPower(calcValue(newUom), newUom)
}

final case class MeasuredPressure(value: Double, uom: PressureMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredPressure = MeasuredPressure(value * factor, uom)
  def to(newUom: PressureMeasureUnit): MeasuredPressure = MeasuredPressure(calcValue(newUom), newUom)
}

final case class MeasuredProportion(value: Double, uom: ProportionMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredProportion = MeasuredProportion(value * factor, uom)
  def to(newUom: ProportionMeasureUnit): MeasuredProportion = MeasuredProportion(calcValue(newUom), newUom)
}

final case class MeasuredSpeed(value: Double, uom: SpeedMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredSpeed = MeasuredSpeed(value * factor, uom)
  def to(newUom: SpeedMeasureUnit): MeasuredSpeed = MeasuredSpeed(calcValue(newUom), newUom)
}

final case class MeasuredTemperature(value: Double, uom: TemperatureMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredTemperature = MeasuredTemperature(value * factor, uom)
  def to(newUom: TemperatureMeasureUnit): MeasuredTemperature = MeasuredTemperature(calcValue(newUom), newUom)
}

final case class MeasuredVolume(value: Double, uom: VolumeMeasureUnit) extends Measured {
  override def *(factor: Double): MeasuredVolume = MeasuredVolume(value * factor, uom)
  def to(newUom: VolumeMeasureUnit): MeasuredVolume = MeasuredVolume(calcValue(newUom), newUom)
}


