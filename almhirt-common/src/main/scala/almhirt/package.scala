
import scalaz.Validation

/** Classes and traits needed at other places*/
package object almhirt {
  /** A registration using a UUID as a token */
  type RegistrationUUID = Registration[java.util.UUID]
  
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
  type AlmValidationAP[+α] = Validation[AggregateProblem, α]    

  object almvalidationinstances extends almvalidation.ProblemInstances with almvalidation.ProblemCategoryInstances 
    with almvalidation.SeverityInstances with almvalidation.AlmValidationInstances

  object almvalidationfunctions extends almvalidation.ProblemFunctions with almvalidation.AlmValidationFunctions with almvalidation.AlmValidationParseFunctions

  object almvalidationimports 
    extends almvalidation.ProblemFunctions with almvalidation.AlmValidationFunctions with almvalidation.AlmValidationParseFunctions
    with almvalidation.ProblemInstances with almvalidation.ProblemCategoryInstances  with almvalidation.SeverityInstances with almvalidation.AlmValidationInstances
    with almvalidation.ToProblemOps with almvalidation.ToAlmValidationOps
    
  object probleminstances extends almvalidation.ProblemInstances with almvalidation.ProblemCategoryInstances with almvalidation.SeverityInstances
  
}