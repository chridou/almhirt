package almhirt

import scalaz.Validation
import almhirt.validation.Problem._

package object validation {
//  type AlmValidation[+α] = ({type λ[α] = Validation[Problem, α]})#λ[α]
//  type AlmValidationSBD[+α] = ({type λ[α] = Validation[SingleBadDataProblem, α]})#λ[α]
//  type AlmValidationMBD[+α] = ({type λ[α] = Validation[MultipleBadDataProblem, α]})#λ[α]
  type AlmValidation[+α] = Validation[Problem, α]
  type AlmValidationSBD[+α] = Validation[SingleBadDataProblem, α]
  type AlmValidationMBD[+α] = Validation[MultipleBadDataProblem, α]
  type AlmValidationSM[+α] = Validation[SingleMappingProblem, α]
  type AlmValidationMM[+α] = Validation[MultipleMappingProblem, α]
  type AlmValidationBRV[+α] = Validation[BusinessRuleViolatedProblem, α]
  type AlmValidationMBRV[+α] = Validation[ManyBusinessRulesViolatedProblem, α]

  case class ValidationException(val problem: Problem) extends Exception("A value has been forced from a failed validation")
  case class NotAFailureException() extends Exception("The problem has been forced from a success")
  
  object AlmValidation extends AlmValidationOps with AlmValidationParseOps with AlmValidationImplicits

}