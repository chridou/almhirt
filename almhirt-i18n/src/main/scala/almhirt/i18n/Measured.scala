package almhirt.i18n

import almhirt.common._
import com.ibm.icu.util._
import almhirt.i18n.UnitOfMeasureDimension._

/**
 * Something that has been measured.
 * Represents a [[UnitOfMeasurement]] combined with a value.
 * Calculation are only to be used for localization purposes.
 */
sealed trait Measured {
  def value: Double
  def uom: UnitOfMeasurement
  final def icu: AlmValidation[Measure] = uom.icu.map(new Measure(value, _))

  protected def calcValue(newUom: UnitOfMeasurement): Double = {
    if (newUom != this.uom)
      newUom.fromBase(uom.toBase(value))
    else
      value
  }

  final def format(implicit fmt: com.ibm.icu.text.MeasureFormat): String = {
    fmt.format(icu)
  }

  private[almhirt] def calcDirectTo(newUom: UnitOfMeasurement): Double = {
    calcValue(newUom)
  }

  def toSI: Measured
}

final case class MeasuredAcceleration(value: Double, uom: AccelerationMeasureUnit) extends Measured {
  def to(newUom: AccelerationMeasureUnit): MeasuredAcceleration = MeasuredAcceleration(calcValue(newUom), newUom)
  def toSI: MeasuredAcceleration = to(AccelerationDimension.siUnit)
}

final case class MeasuredAngle(value: Double, uom: AngleMeasureUnit) extends Measured {
  def to(newUom: AngleMeasureUnit): MeasuredAngle = MeasuredAngle(calcValue(newUom), newUom)
  def toSI: MeasuredAngle = to(AngleDimension.siUnit)
}

final case class MeasuredArea(value: Double, uom: AreaMeasureUnit) extends Measured {
  def to(newUom: AreaMeasureUnit): MeasuredArea = MeasuredArea(calcValue(newUom), newUom)
  def toSI: MeasuredArea = to(AreaDimension.siUnit)
}

final case class MeasuredConsumption(value: Double, uom: ConsumptionMeasureUnit) extends Measured {
  def to(newUom: ConsumptionMeasureUnit): MeasuredConsumption = MeasuredConsumption(calcValue(newUom), newUom)
  def toSI: MeasuredConsumption = to(ConsumptionDimension.siUnit)
}

final case class MeasuredDigital(value: Double, uom: DigitalMeasureUnit) extends Measured {
  def to(newUom: DigitalMeasureUnit): MeasuredDigital = MeasuredDigital(calcValue(newUom), newUom)
  def toSI: MeasuredDigital = to(DigitalDimension.siUnit)
}

final case class MeasuredDuration(value: Double, uom: DurationMeasureUnit) extends Measured {
  def to(newUom: DurationMeasureUnit): MeasuredDuration = MeasuredDuration(calcValue(newUom), newUom)
  def toSI: MeasuredDuration = to(DurationDimension.siUnit)
}

final case class MeasuredCurrent(value: Double, uom: CurrentMeasureUnit) extends Measured {
  def to(newUom: CurrentMeasureUnit): MeasuredCurrent = MeasuredCurrent(calcValue(newUom), newUom)
  def toSI: MeasuredCurrent = to(CurrentDimension.siUnit)
}

final case class MeasuredVoltage(value: Double, uom: VoltageMeasureUnit) extends Measured {
  def to(newUom: VoltageMeasureUnit): MeasuredVoltage = MeasuredVoltage(calcValue(newUom), newUom)
  def toSI: MeasuredVoltage = to(VoltageDimension.siUnit)
}

final case class MeasuredEnergy(value: Double, uom: EnergyMeasureUnit) extends Measured {
  def to(newUom: EnergyMeasureUnit): MeasuredEnergy = MeasuredEnergy(calcValue(newUom), newUom)
  def toSI: MeasuredEnergy = to(EnergyDimension.siUnit)
}

final case class MeasuredFrequency(value: Double, uom: FrequencyMeasureUnit) extends Measured {
  def to(newUom: FrequencyMeasureUnit): MeasuredFrequency = MeasuredFrequency(calcValue(newUom), newUom)
  def toSI: MeasuredFrequency = to(FrequencyDimension.siUnit)
}

final case class MeasuredLength(value: Double, uom: LengthMeasureUnit) extends Measured {
  def to(newUom: LengthMeasureUnit): MeasuredLength = MeasuredLength(calcValue(newUom), newUom)
  def toSI: MeasuredLength = to(LengthDimension.siUnit)
}

final case class MeasuredLight(value: Double, uom: LightMeasureUnit) extends Measured {
  def to(newUom: LightMeasureUnit): MeasuredLight = MeasuredLight(calcValue(newUom), newUom)
  def toSI: MeasuredLight = to(LightDimension.siUnit)
}

final case class MeasuredLightFlux(value: Double, uom: LightFluxMeasureUnit) extends Measured {
  def to(newUom: LightFluxMeasureUnit): MeasuredLightFlux = MeasuredLightFlux(calcValue(newUom), newUom)
  def toSI: MeasuredLightFlux = to(LightFluxDimension.siUnit)
}

final case class MeasuredLuminousEfficacy(value: Double, uom: LuminousEfficacyMeasureUnit) extends Measured {
  def to(newUom: LuminousEfficacyMeasureUnit): MeasuredLuminousEfficacy = MeasuredLuminousEfficacy(calcValue(newUom), newUom)
  def toSI: MeasuredLuminousEfficacy = to(LuminousEfficacyDimension.siUnit)
}

final case class MeasuredMass(value: Double, uom: MassMeasureUnit) extends Measured {
  def to(newUom: MassMeasureUnit): MeasuredMass = MeasuredMass(calcValue(newUom), newUom)
  def toSI: MeasuredMass = to(MassDimension.siUnit)
}

final case class MeasuredPower(value: Double, uom: PowerMeasureUnit) extends Measured {
  def to(newUom: PowerMeasureUnit): MeasuredPower = MeasuredPower(calcValue(newUom), newUom)
  def toSI: MeasuredPower = to(PowerDimension.siUnit)
}

final case class MeasuredPressure(value: Double, uom: PressureMeasureUnit) extends Measured {
  def to(newUom: PressureMeasureUnit): MeasuredPressure = MeasuredPressure(calcValue(newUom), newUom)
  def toSI: MeasuredPressure = to(PressureDimension.siUnit)
}

final case class MeasuredProportion(value: Double, uom: ProportionMeasureUnit) extends Measured {
  def to(newUom: ProportionMeasureUnit): MeasuredProportion = MeasuredProportion(calcValue(newUom), newUom)
  def toSI: MeasuredProportion = to(ProportionDimension.siUnit)
}

final case class MeasuredSpeed(value: Double, uom: SpeedMeasureUnit) extends Measured {
  def to(newUom: SpeedMeasureUnit): MeasuredSpeed = MeasuredSpeed(calcValue(newUom), newUom)
  def toSI: MeasuredSpeed = to(SpeedDimension.siUnit)
}

final case class MeasuredTemperature(value: Double, uom: TemperatureMeasureUnit) extends Measured {
  def to(newUom: TemperatureMeasureUnit): MeasuredTemperature = MeasuredTemperature(calcValue(newUom), newUom)
  def toSI: MeasuredTemperature = to(TemperatureDimension.siUnit)
}

final case class MeasuredVolume(value: Double, uom: VolumeMeasureUnit) extends Measured {
  def to(newUom: VolumeMeasureUnit): MeasuredVolume = MeasuredVolume(calcValue(newUom), newUom)
  def toSI: MeasuredVolume = to(VolumeDimension.siUnit)
}