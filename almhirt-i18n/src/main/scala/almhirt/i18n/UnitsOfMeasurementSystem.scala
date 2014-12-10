package almhirt.i18n

import scalaz.syntax.validation._
import almhirt.common._

sealed trait UnitsOfMeasurementSystem {
  def parseableString: String
}

object UnitsOfMeasurementSystem {
  case object SI extends UnitsOfMeasurementSystem {
    override val parseableString = "si"
  }

  case object AngloAmerican extends UnitsOfMeasurementSystem {
    override val parseableString = "anglo-american"
  }
  
  def parseString(toParse: String): AlmValidation[UnitsOfMeasurementSystem] =
    toParse match {
    case "si" => SI.success
    case "anglo-american" => AngloAmerican.success
    case x => ParsingProblem(s""""$x" is not a units of measurement system.""").failure
  }
}


