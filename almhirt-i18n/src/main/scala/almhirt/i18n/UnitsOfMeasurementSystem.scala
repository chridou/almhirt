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

  case object UnitedStatesCustomaryUnits extends UnitsOfMeasurementSystem {
    override val parseableString = "us"
  }

  case object Imperial extends UnitsOfMeasurementSystem {
    override val parseableString = "imperial"
  }
  
  def parseString(toParse: String): AlmValidation[UnitsOfMeasurementSystem] =
    toParse match {
    case "si" => SI.success
    case "us" => UnitedStatesCustomaryUnits.success
    case "imperial" => Imperial.success
    case x => ParsingProblem(s""""$x" is not a units of measurement system.""").failure
  }
}


