package almhirt

import scalaz.Validation
import almhirt.validation.Problem._

package object validation {
//  type AlmValidation[+α] = ({type λ[α] = Validation[Problem, α]})#λ[α]
//  type AlmValidationSingleBadData[+α] = ({type λ[α] = Validation[SingleBadDataProblem, α]})#λ[α]
//  type AlmValidationMultipleBadData[+α] = ({type λ[α] = Validation[MultipleBadDataProblem, α]})#λ[α]
  type AlmValidation[+α] = Validation[Problem, α]
  type AlmValidationSingleBadData[+α] = Validation[SingleBadDataProblem, α]
  type AlmValidationMultipleBadData[+α] = Validation[MultipleBadDataProblem, α]

object AlmValidation extends AlmValidationOps with AlmValidationParseOps with AlmValidationImplicits

}