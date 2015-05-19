package almhirt.i18n

import scalaz.syntax.validation._
import almhirt.common._
import com.ibm.icu.util.LocaleData.MeasurementSystem

sealed trait UnitsOfMeasurementSystem {
  def parseableString: String
  def icu: MeasurementSystem
}

object UnitsOfMeasurementSystem {
  case object SI extends UnitsOfMeasurementSystem {
    override val parseableString = "si"
    override val icu = MeasurementSystem.SI
  }

  case object US extends UnitsOfMeasurementSystem {
    override val parseableString = "us"
    override val icu = MeasurementSystem.US
  }

  def parseString(toParse: String): AlmValidation[UnitsOfMeasurementSystem] =
    toParse match {
      case "si"             ⇒ SI.success
      case "anglo-american" ⇒ US.success
      case "us"             ⇒ US.success
      case x                ⇒ ParsingProblem(s""""$x" is not a units of measurement system.""").failure
    }
}


